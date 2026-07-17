import os
import re

target_files = [
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\admin\dashboard.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\admin\categories\list.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\admin\coupons\list.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\admin\courses\list.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\admin\lessons\list.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\admin\reviews\list.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\admin\users\list.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\teacher\course-list.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\teacher\lessons\list.html"
]

swal_script = '<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>'

def replace_confirm(match):
    attr = match.group(1) # 'onsubmit' or 'onclick'
    msg = match.group(2) # the message
    
    action = "this.submit();" if attr == "onsubmit" else "this.closest('form').submit();"
    
    replacement = f"""{attr}="event.preventDefault(); Swal.fire({{ title: 'Xác nhận', text: '{msg}', icon: 'warning', showCancelButton: true, confirmButtonColor: '#dc2626', cancelButtonColor: '#7a8c80', confirmButtonText: 'Đồng ý', cancelButtonText: 'Hủy', background: '#faf7f2', borderRadius: '16px' }}).then((result) => {{ if (result.isConfirmed) {{ {action} }} }});" """
    return replacement

pattern = re.compile(r'(onsubmit|onclick)="return confirm\(\'(.*?)\'\);" *')

for filepath in target_files:
    if not os.path.exists(filepath):
        continue
        
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
        
    if "sweetalert2" not in content:
        content = content.replace("</head>", f"    {swal_script}\n</head>")
        
    new_content = pattern.sub(replace_confirm, content)
    
    if content != new_content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated: {os.path.basename(filepath)}")

print("Done.")
