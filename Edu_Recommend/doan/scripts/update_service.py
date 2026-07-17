import os

path = r"d:\NHOM3\DoAnKPDL\doan\src\main\java\com\example\doan\Service\Course_Service.java"
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Add imports
imports = """
import com.example.doan.Model.*;
import com.example.doan.Repository.*;
import org.springframework.transaction.annotation.Transactional;
"""
if "import org.springframework.transaction.annotation.Transactional;" not in content:
    content = content.replace("import java.util.List;", "import java.util.List;\n" + imports)

# Add dependencies to constructor
new_constructor = """
    private final CourseRepository courseRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final OrderItemRepository orderItemRepo;
    private final OrderRepository orderRepo;
    private final RefundRequestRepository refundRepo;
    private final InstructorEarningRepository earningRepo;

    public Course_Service(CourseRepository courseRepo,
                          EnrollmentRepository enrollmentRepo,
                          OrderItemRepository orderItemRepo,
                          OrderRepository orderRepo,
                          RefundRequestRepository refundRepo,
                          InstructorEarningRepository earningRepo) {
        this.courseRepo = courseRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.orderItemRepo = orderItemRepo;
        this.orderRepo = orderRepo;
        this.refundRepo = refundRepo;
        this.earningRepo = earningRepo;
    }
"""
import re
content = re.sub(
    r'    private final CourseRepository courseRepo;.*?    \}',
    new_constructor.strip(),
    content,
    flags=re.DOTALL
)

# Add Delete_With_Refund method
delete_refund_method = """
    @Transactional
    public void Delete_With_Refund(Long courseId, String reason) {
        Course course = Get_ById(courseId);
        if (course == null) return;

        // 1. Get all enrollments for this course
        List<Enrollment> enrollments = enrollmentRepo.findAll().stream()
                .filter(e -> e.getCourse() != null && e.getCourse().getId().equals(courseId))
                .toList();

        // 2. For each enrollment, process refund
        for (Enrollment e : enrollments) {
            User student = e.getUser();
            
            // Find order item for this student and course
            List<Order> studentOrders = orderRepo.findByUserId(student.getId());
            for (Order order : studentOrders) {
                if (!"completed".equals(order.getTrangThai())) continue;
                
                List<OrderItem> items = orderItemRepo.findByOrderId(order.getId());
                for (OrderItem item : items) {
                    if (item.getCourse().getId().equals(courseId)) {
                        // Create approved refund request
                        RefundRequest refund = new RefundRequest(order, "Tự động hoàn tiền do khóa học bị xóa. Lý do: " + reason);
                        refund.setTrangThai("approved");
                        refund.setXuLyAt(java.time.LocalDateTime.now());
                        refundRepo.save(refund);
                        
                        // Deduct earning from teacher if exists
                        List<InstructorEarning> earnings = earningRepo.findAll().stream()
                                .filter(earn -> earn.getOrderItem().getId().equals(item.getId()))
                                .toList();
                        for (InstructorEarning earning : earnings) {
                            earningRepo.delete(earning); // Or create negative earning
                        }
                    }
                }
            }
            // 3. Delete enrollment
            enrollmentRepo.delete(e);
        }

        // 4. Soft delete course
        course.setTrangThai("deleted");
        courseRepo.save(course);
    }
"""
content = content.replace("public void Delete(Long id) {", delete_refund_method + "\n    public void Delete(Long id) {")

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated Course_Service")
