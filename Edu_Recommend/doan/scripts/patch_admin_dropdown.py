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

        content = content.replace(
            '<a href="/student/my-courses" class="dropdown-item">📖 Khóa học của tôi</a>',
            '<a href="/student/my-courses" sec:authorize="!hasRole(\'admin\')" class="dropdown-item">📖 Khóa học của tôi</a>'
        )
        content = content.replace(
            '<a href="/student/orders" class="dropdown-item">🧾 Lịch sử đơn hàng</a>',
            '<a href="/student/orders" sec:authorize="!hasRole(\'admin\')" class="dropdown-item">🧾 Lịch sử đơn hàng</a>'
        )
        content = content.replace(
            '<a href="/student/recommend" class="dropdown-item">🤖 Gợi ý lộ trình AI</a>',
            '<a href="/student/recommend" sec:authorize="!hasRole(\'admin\')" class="dropdown-item">🤖 Gợi ý lộ trình AI</a>'
        )

        with open(file, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Patched {os.path.basename(file)}")
