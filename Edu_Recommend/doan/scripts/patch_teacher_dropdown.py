import os

html_files = [
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\student\recommend.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\student\orders.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\student\my-courses.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\student\courses\list.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\student\courses\detail.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\student\cart.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\home.html"
]

for file in html_files:
    if os.path.exists(file):
        with open(file, 'r', encoding='utf-8') as f:
            content = f.read()

        if 'Trang Giảng viên' not in content:
            content = content.replace(
                '<a href="/admin" sec:authorize="hasRole(\'admin\')" class="dropdown-item" style="color: var(--accent);">⚙️ Trang quản trị</a>',
                '<a href="/admin" sec:authorize="hasRole(\'admin\')" class="dropdown-item" style="color: var(--accent);">⚙️ Trang quản trị</a>\n                <a href="/teacher/dashboard" sec:authorize="hasRole(\'teacher\')" class="dropdown-item" style="color: var(--accent);">👨‍🏫 Trang Giảng viên</a>'
            )

        with open(file, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Patched {os.path.basename(file)}")
