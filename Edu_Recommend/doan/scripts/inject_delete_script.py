import re

files = [
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\admin\courses\list.html",
    r"d:\NHOM3\DoAnKPDL\doan\src\main\resources\templates\teacher\course-list.html"
]

script_block = """
<script>
async function handleDeleteCourse(event, form, courseId) {
    event.preventDefault();
    
    // Check enrollment count
    try {
        const response = await fetch(`/api/courses/${courseId}/enrollment-count`);
        const data = await response.json();
        
        if (data.count > 0) {
            Swal.fire({
                title: 'Khóa học đang có người học!',
                text: `Hiện tại đang có ${data.count} học viên theo học khóa này. Bạn có chắc chắn muốn xóa không? Nếu có, hệ thống sẽ tự động hoàn tiền và trừ doanh thu tương ứng. Vui lòng nhập lý do xóa:`,
                icon: 'warning',
                input: 'textarea',
                inputPlaceholder: 'Nhập lý do xóa (Bắt buộc)...',
                showCancelButton: true,
                confirmButtonColor: '#dc2626',
                cancelButtonColor: '#7a8c80',
                confirmButtonText: 'Đồng ý xóa & Hoàn tiền',
                cancelButtonText: 'Hủy',
                background: '#faf7f2',
                borderRadius: '16px',
                inputValidator: (value) => {
                    if (!value) {
                        return 'Bạn phải nhập lý do xóa!'
                    }
                }
            }).then((result) => {
                if (result.isConfirmed) {
                    const reasonInput = document.createElement('input');
                    reasonInput.type = 'hidden';
                    reasonInput.name = 'reason';
                    reasonInput.value = result.value;
                    form.appendChild(reasonInput);
                    form.submit();
                }
            });
        } else {
            Swal.fire({
                title: 'Xác nhận',
                text: 'Bạn có chắc chắn muốn xóa khóa học này cùng toàn bộ các bài học của nó?',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#dc2626',
                cancelButtonColor: '#7a8c80',
                confirmButtonText: 'Đồng ý',
                cancelButtonText: 'Hủy',
                background: '#faf7f2',
                borderRadius: '16px'
            }).then((result) => {
                if (result.isConfirmed) {
                    form.submit();
                }
            });
        }
    } catch(err) {
        console.error(err);
        Swal.fire('Lỗi', 'Không thể kiểm tra dữ liệu khóa học', 'error');
    }
}
</script>
</body>
"""

for filepath in files:
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Replace the old onsubmit inline code with the function call
    content = re.sub(
        r'onsubmit="event\.preventDefault\(\); Swal\.fire\(\{.*?\}\)\.then\(\(result\) => \{ if \(result\.isConfirmed\) \{ this\.submit\(\); \} \}\);" *>',
        r'onsubmit="handleDeleteCourse(event, this, [[${c.id}]])">',
        content,
        flags=re.DOTALL
    )
    
    if "handleDeleteCourse" not in content:
        content = content.replace("</body>", script_block)
        
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
