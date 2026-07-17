import os
import shutil

admin_list = r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\admin\courses\list.html"
admin_form = r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\admin\courses\form.html"
teacher_dir = r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\teacher"
teacher_list = os.path.join(teacher_dir, "course-list.html")
teacher_form = os.path.join(teacher_dir, "course-form.html")

os.makedirs(teacher_dir, exist_ok=True)
shutil.copy(admin_list, teacher_list)
shutil.copy(admin_form, teacher_form)

# Patch teacher_list
with open(teacher_list, 'r', encoding='utf-8') as f:
    content = f.read()
content = content.replace('Bảng Điều Khiển Admin', 'Bảng Điều Khiển Giảng Viên')
content = content.replace('/admin/courses', '/teacher/courses')
content = content.replace('Quản lý Khóa học', 'Khóa học của tôi')
content = content.replace('href="/admin"', 'href="/teacher/dashboard"')
content = content.replace('Thêm Khóa Học Mới', 'Đăng Khóa Học Mới')
# Remove other admin sidebar links
content = content.replace('href="/admin/categories"', 'href="#" style="display:none;"')
content = content.replace('href="/admin/coupons"', 'href="#" style="display:none;"')
content = content.replace('href="/admin/reviews"', 'href="#" style="display:none;"')
content = content.replace('href="/admin/refunds"', 'href="#" style="display:none;"')
with open(teacher_list, 'w', encoding='utf-8') as f:
    f.write(content)

# Patch teacher_form
with open(teacher_form, 'r', encoding='utf-8') as f:
    content = f.read()
content = content.replace('Bảng Điều Khiển Admin', 'Bảng Điều Khiển Giảng Viên')
content = content.replace('/admin/courses', '/teacher/courses')
content = content.replace('href="/admin"', 'href="/teacher/dashboard"')
content = content.replace('Quản lý Khóa học', 'Khóa học của tôi')
content = content.replace('href="/admin/categories"', 'href="#" style="display:none;"')
content = content.replace('href="/admin/coupons"', 'href="#" style="display:none;"')
content = content.replace('href="/admin/reviews"', 'href="#" style="display:none;"')
content = content.replace('href="/admin/refunds"', 'href="#" style="display:none;"')
with open(teacher_form, 'w', encoding='utf-8') as f:
    f.write(content)

print("Teacher UI created!")
