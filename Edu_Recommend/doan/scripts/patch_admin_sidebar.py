import os
import glob

admin_files = [
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\admin\dashboard.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\admin\categories\form.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\admin\categories\list.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\admin\coupons\form.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\admin\coupons\list.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\admin\courses\form.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\admin\courses\list.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\admin\lessons\form.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\admin\lessons\list.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\admin\refunds\list.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\admin\reviews\list.html"
]

insert_text = """
        <a href="/admin/users" class="nav-link">
            <span>👥</span> Quản lý Người dùng
        </a>"""

for file in admin_files:
    if os.path.exists(file):
        with open(file, 'r', encoding='utf-8') as f:
            content = f.read()

        if 'Quản lý Người dùng' not in content:
            # We look for "Tổng quan" string and the closing </a>
            if '<span>📊</span> Tổng quan' in content:
                content = content.replace(
                    '<span>📊</span> Tổng quan\n        </a>',
                    f'<span>📊</span> Tổng quan\n        </a>{insert_text}'
                )
            
        with open(file, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Patched {os.path.basename(file)}")
