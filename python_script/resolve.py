# -*- coding: utf-8 -*-
import os
import re
from datetime import datetime
from openai import OpenAI

TR_FOLDER = "trending_readmes"
MAX_README_CHARS = 12000
MODELSCOPE_BASE_URL = "https://api-inference.modelscope.cn/v1"
# MODELSCOPE_API_KEY = os.getenv("MODELSCOPE_API_KEY", "YOUR_MODELSCOPE_TOKEN")
MODELSCOPE_API_KEY = 'ms-xxxxxxxxx'
MODEL_ID = "Qwen/Qwen3-235B-A22B-Instruct-2507"


def extract_original_keywords(readme_text: str) -> str | None:
    m = re.search(r"\*\*关键词：\*\*\s*(.+)", readme_text)
    if not m:
        return None
    line = m.group(1).strip()
    line = re.sub(r"\s+\**$", "", line).strip()
    return line if line else None


def build_prompt(readme_text: str, original_keywords: str | None) -> str:
    ok = original_keywords if (original_keywords and original_keywords.strip()) else "无关键词"
    truncated = readme_text[:MAX_README_CHARS]

    prompt = f"""你将收到一个 GitHub 仓库 README 的正文，以及其中（可能存在的）“关键词：”一行。
请严格按下列规则，输出**仅包含**中文关键词，不要添加任何解释或前后缀：

规则：
1）如果“关键词：”一行已经是中文，则直接原样输出该行中的关键词。
2）如果“关键词：”是英文，请把每个关键词准确翻译成中文，按原顺序输出。
3）如果“关键词：”为空、缺失，或等于“无关键词”，请基于全文内容自动生成 5 个最能概括主题的中文关键词。

格式要求：
- 仅输出关键词本身，使用中文逗号“，”分隔；
- 不要输出序号、括号、引号或任何其它文字；
- 单个关键词尽量 2–6 个字，或使用常见技术词（如“操作系统”“正则引擎”“区块链”等）。

原始关键词行：{ok}

全文（仅供参考）：
{truncated}
"""
    return prompt


def generate_llm_keywords(readme_text: str, original_keywords: str | None) -> str:
    client = OpenAI(base_url=MODELSCOPE_BASE_URL, api_key=MODELSCOPE_API_KEY)

    prompt = build_prompt(readme_text, original_keywords)
    response = client.chat.completions.create(
        model=MODEL_ID,
        messages=[
            {"role": "system", "content": "You are a helpful assistant."},
            {"role": "user", "content": prompt},
        ],
        stream=False,
    )
    # 返回完整内容
    return response.choices[0].message.content.strip()


def extract_citation_info(readme_text: str) -> str:
    """
    提取 README 中 BibTeX 风格的 Citation 信息（通常在底部）。
    返回拼接后的字符串，如果未找到返回空字符串。
    """
    citations = []

    # 匹配 @InProceedings, @Article, @Book 等
    bib_matches = re.findall(r"@(?:InProceedings|Article|Book)\{[^}]+\}", readme_text, re.IGNORECASE | re.DOTALL)
    for m in bib_matches:
        # 可提取 title 或直接拼接
        title_match = re.search(r"title\s*=\s*\{(.+?)\}", m, re.IGNORECASE | re.DOTALL)
        if title_match:
            title = title_match.group(1).replace("\n", " ").strip()
            citations.append(title)

    return ", ".join(citations)


def process_all_readmes():
    for fname in os.listdir(TR_FOLDER):
        if not fname.endswith(".md"):
            continue
        path = os.path.join(TR_FOLDER, fname)
        with open(path, "r", encoding="utf-8") as f:
            content = f.read()

        # 跳过已有 LLM 关键词
        if "**关键词(LLM)：**" in content:
            print(f"已存在 LLM 关键词，跳过 {fname}")
            continue

        original_keywords = extract_original_keywords(content)
        llm_keywords = generate_llm_keywords(content, original_keywords)

        citation_info = extract_citation_info(content)
        citation_line = f"**论文链接：** {citation_info}" if citation_info else "**论文链接：**"

        # 插入到头部信息最后一行后
        new_content = re.sub(
            r"(\*\*仓库地址：.*\n)",
            r"\1**关键词(LLM)：** " + llm_keywords + "\n" + citation_line + "\n",
            content,
            count=1
        )

        with open(path, "w", encoding="utf-8") as f:
            f.write(new_content)
        print(f"✅ 已更新 {fname}")


if __name__ == "__main__":
    process_all_readmes()
