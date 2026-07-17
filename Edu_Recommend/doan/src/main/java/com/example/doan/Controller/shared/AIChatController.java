package com.example.doan.Controller.shared;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.HashMap;
import java.util.List;


@RestController
@RequestMapping("/api/ai")
public class AIChatController {

    @Value("${gemini.api.key:}")
    private String apiKey;

    // 1. Nhận tin nhắn hỏi đáp từ widget client và trả về phản hồi thông minh từ Gemini API
    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "").toLowerCase().trim();
        
        String keyToUse = System.getenv("GEMINI_API_KEY");
        if (keyToUse == null || keyToUse.trim().isEmpty()) {
            keyToUse = apiKey;
        }

        // Gọi Gemini API
        try {
            if (keyToUse != null && !keyToUse.trim().isEmpty() && !keyToUse.contains("YOUR_GEMINI_API_KEY")) {
                String url = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + keyToUse;
                
                RestTemplate restTemplate = new RestTemplate();
                
                // Build Request Body
                Map<String, Object> requestBody = new HashMap<>();
                
                // Trộn System Prompt trực tiếp vào câu hỏi người dùng để đảm bảo tương thích 100% mọi phiên bản API
                String fullPrompt = "Bạn là Trợ lý AI thông minh của website học trực tuyến EduRecommend. Hãy hỗ trợ người dùng nhiệt tình bằng tiếng Việt.\n" +
                        "Dưới đây là thông tin chi tiết về hệ thống của chúng tôi để bạn trả lời chính xác:\n" +
                        "1. Gợi ý Lộ trình AI: Học sinh nhập chỉ số cá nhân (giờ tự học, chuyên cần, điểm số, giấc ngủ...) để Flask API (Machine Learning) dự phóng kết quả và đề xuất các khóa học phù hợp nhất.\n" +
                        "2. Chính sách Hoàn tiền (Refund): Hoàn tiền nếu mua dưới 7 ngày và tiến độ học tập dưới 20%. Admin duyệt hoàn tiền hệ thống sẽ tự động thu hồi quyền học và khấu trừ hoa hồng tương ứng của giảng viên để tránh lỗi doanh thu âm.\n" +
                        "3. Chức năng Giảng viên (Teacher): Đăng khóa học (upload video không giới hạn size), xem báo cáo doanh thu thực nhận sau khi chia hoa hồng, xem tiến độ học của từng học sinh, quản lý đánh giá của học viên.\n" +
                        "4. Đánh giá: Học viên đã mua khóa học được phép chấm điểm 1-5 sao kèm nhận xét. Giảng viên và Admin không được phép tự đánh giá.\n\n" +
                        "Hãy trả lời một cách lịch sự, ngắn gọn và có cấu trúc rõ ràng. Bạn có thể sử dụng markdown để làm nổi bật các ý chính.\n\n" +
                        "Câu hỏi của người dùng: " + message;

                Map<String, Object> part = new HashMap<>();
                part.put("text", fullPrompt);
                Map<String, Object> contentObj = new HashMap<>();
                contentObj.put("parts", List.of(part));
                requestBody.put("contents", List.of(contentObj));
                
                // Gửi Request POST tới Gemini
                Map<String, Object> responseEntity = restTemplate.postForObject(url, requestBody, Map.class);
                
                // Phân tích kết quả trả về từ Gemini
                if (responseEntity != null && responseEntity.containsKey("candidates")) {
                    List<?> candidates = (List<?>) responseEntity.get("candidates");
                    if (!candidates.isEmpty()) {
                        Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
                        Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");
                        List<?> parts = (List<?>) content.get("parts");
                        if (!parts.isEmpty()) {
                            Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);
                            String geminiText = (String) firstPart.get("text");
                            
                            Map<String, String> response = new HashMap<>();
                            response.put("reply", geminiText);
                            return response;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi gọi Gemini API (Sử dụng phản hồi Fallback): " + e.getMessage());
        }
        
        // Phản hồi offline nếu không cấu hình key hoặc gọi API lỗi
        Map<String, String> fallbackResponse = new HashMap<>();
        fallbackResponse.put("reply", getAIResponseOffline(message));
        return fallbackResponse;
    }

    private String getAIResponseOffline(String msg) {
        if (msg.contains("hoàn tiền") || msg.contains("refund") || msg.contains("trả tiền") || msg.contains("hủy")) {
            return "🎯 **Chính sách hoàn tiền của EduRecommend:**\n\n" +
                   "Bạn có thể yêu cầu hoàn tiền cho bất kỳ khóa học nào đã mua nếu đáp ứng đủ **3 điều kiện** sau:\n" +
                   "1. Khóa học được mua **dưới 7 ngày**.\n" +
                   "2. Tiến độ học tập của khóa học đó phải **dưới 20%**.\n" +
                   "3. Trạng thái giao dịch đơn hàng phải là **Đã thanh toán (Paid)**.\n\n" +
                   "👉 *Cách thực hiện: Học viên vào mục 'Lịch sử đơn hàng' trên Avatar để gửi yêu cầu hoàn tiền. Admin sẽ xét duyệt ở trang quản trị, hệ thống tự động thu hồi quyền học và khấu trừ hoa hồng của giảng viên.*";
        }
        
        if (msg.contains("lộ trình") || msg.contains("recommend") || msg.contains("gợi ý") || msg.contains("ai")) {
            return "🤖 **Hệ thống Gợi ý Lộ trình AI:**\n\n" +
                   "Hệ thống sử dụng mô hình học máy (Machine Learning) được deploy trên Flask API để phân tích các chỉ số cá nhân của bạn:\n" +
                   "- Số giờ tự học mỗi tuần.\n" +
                   "- Tỉ lệ đi học lớp lý thuyết.\n" +
                   "- Điểm số trung bình kỳ trước.\n" +
                   "- Thời gian ngủ và các hoạt động ngoại khóa.\n\n" +
                   "Từ đó, AI sẽ dự phóng điểm số đầu ra của bạn và đề xuất các khóa học công nghệ thông tin phù hợp nhất để cải thiện kết quả học tập!";
        }

        if (msg.contains("giảng viên") || msg.contains("teacher") || msg.contains("giáo viên") || msg.contains("doanh thu") || msg.contains("báo cáo")) {
            return "👨‍🏫 **Tính năng dành cho Giảng viên:**\n\n" +
                   "- **Quản lý khóa học**: Tạo bài giảng, tải lên bài học video (không giới hạn kích thước upload).\n" +
                   "- **Báo cáo & Doanh thu**: Xem tổng số học sinh thực tế đăng ký học, tiến độ học của từng học sinh, và doanh thu thực nhận sau khi được chia sẻ từ Admin (theo tỉ lệ commission).\n" +
                   "- **Đánh giá**: Quản lý và đọc tất cả nhận xét, số sao đánh giá từ học viên gửi về các khóa học của mình.";
        }

        if (msg.contains("đánh giá") || msg.contains("sao") || msg.contains("review") || msg.contains("nhận xét")) {
            return "⭐ **Hệ thống đánh giá khóa học:**\n\n" +
                   "- Học viên đã mua khóa học và chưa từng đánh giá sẽ được quyền chấm điểm từ 1 - 5 sao kèm nhận xét.\n" +
                   "- Admin và Giảng viên dạy khóa học đó **không được phép** tự đánh giá sao để đảm bảo tính khách quan.\n" +
                   "- Giảng viên có thể xem danh sách đánh giá của học viên trong mục 'Đánh giá học viên'.";
        }

        if (msg.contains("chào") || msg.contains("hello") || msg.contains("hi") || msg.contains("bắt đầu")) {
            return "👋 Xin chào! Tôi là **Trợ lý AI thông minh** của hệ sinh thái **EduRecommend**.\n\n" +
                   "Tôi có thể hỗ trợ bạn tìm hiểu về:\n" +
                   "- 🎯 Chính sách hoàn tiền của hệ thống.\n" +
                   "- 🤖 Thuật toán gợi ý lộ trình học tập AI.\n" +
                   "- 👨‍🏫 Tính năng dành cho Giảng viên & Admin.\n" +
                   "- ⭐ Quy chế đánh giá sao khóa học.\n\n" +
                   "Bạn muốn tôi giải đáp thắc mắc nào ở trên?";
        }

        return "🤖 Trợ lý AI đang chạy ở chế độ Offline (do chưa điền Gemini API Key trong `application.properties`):\n\n" +
               "- Để nhận lộ trình học tập tối ưu, hãy bấm vào mục **Gợi ý lộ trình AI** ở avatar của bạn.\n" +
               "- Để xem các khóa học hiện có, chọn mục **Khóa học**.\n" +
               "Nếu bạn có câu hỏi cụ thể về hoàn tiền, chức năng giảng viên hay thuật toán gợi ý, hãy nhập từ khóa để tôi giải thích nhé!";
    }
}
