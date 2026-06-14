import json

with open(r"C:\Users\r0xef\.gemini\antigravity-ide\brain\ca548d10-20f2-481a-8329-637ff131a655\.system_generated\logs\transcript.jsonl", "r", encoding="utf-8") as f:
    for line in f:
        data = json.loads(line)
        if data.get("step_index") == 5:
            content = data.get("content", "")
            # Print first 200 lines of content
            print("\n".join(content.splitlines()[:200]))
            break
