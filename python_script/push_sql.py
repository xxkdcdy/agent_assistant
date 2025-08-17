# -*- coding: utf-8 -*-
import os
import re
import uuid
import psycopg2
from psycopg2.extras import Json
import requests
from dotenv import load_dotenv

load_dotenv()

DB_URL = os.getenv("DB_URL", "postgresql:xxxxx")
EMBEDDING_KEY = os.getenv("OPENAI_API_KEY", "sk-xxxxxxx")
EMBEDDING_MODEL = "BAAI/bge-large-zh-v1.5"
README_FOLDER = "trending_readmes"

# 匹配完整头部信息，允许 LLM 关键词和论文链接为空
HEADER_REGEX = re.compile(
    r"^#\s*(.+)\s*$\n"                        # 仓库名
    r"^\*\*趋势日期：\*\*\s*(.+)\s*$\n"
    r"^\*\*项目简介：\*\*\s*(.+)\s*$\n"
    r"^\*\*关键词：\*\*\s*(.*)\s*$\n"        # 原始关键词允许为空
    r"^\*\*Star 数量：\*\*\s*(\d+)\s*$\n"
    r"^\*\*仓库地址：\*\*\s*(.+)\s*$\n"
    r"^\*\*关键词\(LLM\)：\*\*\s*(.*)\s*$\n" # LLM关键词允许为空
    r"^\*\*论文链接：\*\*\s*(.*)\s*$",        # 论文链接允许为空
    re.MULTILINE
)

def get_embedding(text):
    """调用 BAAI embedding API 获取向量"""
    url = "https://api.siliconflow.cn/v1/embeddings"
    headers = {
        "Authorization": f"Bearer {EMBEDDING_KEY}",
        "Content-Type": "application/json"
    }
    payload = {
        "model": EMBEDDING_MODEL,
        "input": text
    }
    # print(text)
    resp = requests.post(url, headers=headers, json=payload, timeout=30)
    resp.raise_for_status()
    data = resp.json()
    # 返回向量列表
    return data["data"][0]["embedding"]

def connect_db():
    return psycopg2.connect(DB_URL)

def ensure_table_exists(conn, table_name="github_vector_vector_store"):
    """确保向量表存在"""
    with conn.cursor() as cur:
        cur.execute(f"""
        CREATE TABLE IF NOT EXISTS {table_name} (
            id UUID PRIMARY KEY,
            content TEXT NOT NULL,
            metadata JSONB,
            embedding VECTOR(1024)
        );
        """)
    conn.commit()

def content_exists(conn, content):
    """检查内容是否已经存在"""
    with conn.cursor() as cur:
        cur.execute("SELECT COUNT(*) FROM github_vector_vector_store WHERE content = %s", (content,))
        return cur.fetchone()[0] > 0

def insert_document(conn, content, metadata, embedding):
    """插入向量库"""
    with conn.cursor() as cur:
        cur.execute(
            "INSERT INTO github_vector_vector_store (id, content, metadata, embedding) VALUES (%s, %s, %s, %s)",
            (str(uuid.uuid4()), content, Json(metadata), embedding)
        )
    conn.commit()

def process_readme_file(conn, filepath):
    """处理单个 README 文件"""
    with open(filepath, "r", encoding="utf-8") as f:
        text = f.read()

    match = HEADER_REGEX.search(text)
    if not match:
        print(f"❌ 未匹配头部信息: {filepath}")
        return

    repo_name, trend_date, description, keywords, stars, repo_url, llm_keywords, citation = match.groups()

    # 只保留头部信息作为 content
    content = (
        f"# {repo_name}\n"
        f"**趋势日期：** {trend_date}\n"
        f"**项目简介：** {description}\n"
        f"**关键词：** {keywords}\n"
        f"**Star 数量：** {stars}\n"
        f"**仓库地址：** {repo_url}\n"
        f"**关键词(LLM)：** {llm_keywords}\n"
        f"**论文链接：** {citation}"
    )

    if content_exists(conn, content):
        print(f"已存在，跳过: {repo_name}")
        return

    metadata = {
        "repoName": repo_name,
        "repoUrl": repo_url,
        "stars": int(stars),
        "keywords": keywords,
        "llmKeywords": llm_keywords,
        "trendDate": trend_date,
        "citation": citation,
    }

    try:
        embedding = get_embedding(content)
    except requests.exceptions.HTTPError as e:
        print(f"❌ embedding生成失败（{e}），跳过: {repo_name}")
        return

    insert_document(conn, content, metadata, embedding)
    print(f"✅ 已添加: {repo_name}")

def main():
    conn = connect_db()
    ensure_table_exists(conn)
    for filename in os.listdir(README_FOLDER):
        if filename.endswith(".md"):
            process_readme_file(conn, os.path.join(README_FOLDER, filename))
    conn.close()

if __name__ == "__main__":
    main()
