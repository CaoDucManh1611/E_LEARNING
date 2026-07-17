import os
import glob
import re

templates_dir = r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates"

# Find all html files
html_files = []
for root, dirs, files in os.walk(templates_dir):
    for file in files:
        if file.endswith(".html"):
            html_files.append(os.path.join(root, file))

for filepath in html_files:
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # We want to replace th:src="${c.hinhAnh}" or th:src="${course.hinhAnh}" with fallback
    # Example: th:src="${c.hinhAnh != null and !c.hinhAnh.isEmpty() ? c.hinhAnh : '/images/default-course.jpg'}"
    
    # Matches patterns like th:src="${c.hinhAnh}"
    pattern = r'th:src="\$\{([a-zA-Z0-9_]+)\.hinhAnh\}"'
    
    def replacer(match):
        var_name = match.group(1)
        return f'th:src="${{{var_name}.hinhAnh != null ? {var_name}.hinhAnh : \'/images/default-course.jpg\'}}"'
    
    new_content = re.sub(pattern, replacer, content)
    
    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Fixed image fallback in: {filepath}")

