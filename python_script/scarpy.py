import re
import time
import requests
from bs4 import BeautifulSoup
from datetime import datetime
import os

BASE_URL = "https://www.github-zh.com"
TRENDING_URL = f"{BASE_URL}/trending"
OUT_DIR = "trending_readmes"

# 保留代码，但不使用环境变量
# GITHUB_TOKEN = os.getenv("GITHUB_TOKEN")
# HEADERS = {"Authorization": f"token {GITHUB_TOKEN}"} if GITHUB_TOKEN else {}
HEADERS = {}  # 默认不带 token

os.makedirs(OUT_DIR, exist_ok=True)

def get_trending_repos():
    resp = requests.get(TRENDING_URL, timeout=15)
    resp.encoding = "utf-8"
    soup = BeautifulSoup(resp.text, "html.parser")
    repos = []

    for a in soup.find_all("a", href=re.compile(r"^https://github\.com/[^/]+/[^/]+$")):
        href = a.get("href")
        match = re.match(r"https://github\.com/([^/]+)/([^/]+)", href)
        if match:
            owner, repo = match.group(1), match.group(2)
            if (owner, repo) not in repos:
                block = a.find_parent("div")
                summary = "无简介"
                keywords = []

                if block:
                    desc_tag = block.find("p")
                    if desc_tag:
                        summary = desc_tag.get_text(strip=True)
                    keyword_tags = block.find_all("a", class_=lambda c: c and "topic-tag" in c)
                    keywords = [k.get_text(strip=True) for k in keyword_tags]

                repos.append({
                    "owner": owner,
                    "repo": repo,
                    "summary": summary,
                    "keywords": keywords
                })
    return repos

def download_repo_info(repo):
    owner, name = repo["owner"], repo["repo"]
    today_str = datetime.now().strftime("%Y-%m-%d")

    # 仓库信息
    repo_api = f"https://api.github.com/repos/{owner}/{name}"
    r = requests.get(repo_api, headers=HEADERS)
    if r.status_code != 200:
        print(f"❌ 无法获取仓库信息: {owner}/{name} ({r.status_code})")
        return None
    repo_info = r.json()
    description = repo_info.get("description", repo["summary"])
    topics = repo_info.get("topics", repo["keywords"])
    stargazers = repo_info.get("stargazers_count", 0)
    html_url = repo_info.get("html_url", f"https://github.com/{owner}/{name}")

    # README
    readme_api = f"https://api.github.com/repos/{owner}/{name}/readme"
    r2 = requests.get(readme_api, headers={**HEADERS, "Accept": "application/vnd.github.v3.raw"})
    if r2.status_code != 200:
        print(f"❌ 无 README.md: {owner}/{name} ({r2.status_code})")
        return None
    readme_text = r2.text

    header = f"""# {owner}/{name}

**趋势日期：** {today_str}  
**项目简介：** {description}  
**关键词：** {', '.join(topics) if topics else ''}  
**Star 数量：** {stargazers}  
**仓库地址：** {html_url}

---
"""
    return header + readme_text

def run_once():
    trending_repos = get_trending_repos()
    print(f"共发现 {len(trending_repos)} 个仓库")

    for repo in trending_repos:
        filename = f"{repo['owner']}-{repo['repo']}-README.md"
        path = os.path.join(OUT_DIR, filename)
        if os.path.exists(path):
            print(f"已存在: {filename}, 跳过")
            continue

        content = download_repo_info(repo)
        if content:
            with open(path, "w", encoding="utf-8") as f:
                f.write(content)
            print(f"✅ 已保存: {filename}")
        time.sleep(1)  # 每个仓库间隔 1 秒，避免被限流

def main():
    print("挂起模式，脚本会每天晚上 23:00 执行抓取任务。")
    while True:
        now = datetime.now()
        if now.hour == 23 and now.minute == 0:
            print(f"\n开始抓取：{now.strftime('%Y-%m-%d %H:%M:%S')}")
            run_once()
            time.sleep(60)  # 避免一分钟内重复抓取
        else:
            time.sleep(30)

if __name__ == "__main__":
    main()
