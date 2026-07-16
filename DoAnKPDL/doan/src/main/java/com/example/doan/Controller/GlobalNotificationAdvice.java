package com.example.doan.Controller;

import com.example.doan.Model.User;
import com.example.doan.Service.Notification_Service;
import com.example.doan.Service.User_Service;
import com.example.doan.Service.Cart_Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.List;

/**
 * ControllerAdvice phục vụ cung cấp các biến toàn cục (Thông báo, Giỏ hàng) cho Thymeleaf.
 * File: Controller/GlobalNotificationAdvice.java
 */
@ControllerAdvice
public class GlobalNotificationAdvice {

    private final Notification_Service notificationService;
    private final User_Service userService;
    private final Cart_Service cartService;

    public GlobalNotificationAdvice(Notification_Service notificationService,
                                   User_Service userService,
                                   Cart_Service cartService) {
        this.notificationService = notificationService;
        this.userService = userService;
        this.cartService = cartService;
    }

    @ModelAttribute
    public void addGlobalsToModel(jakarta.servlet.http.HttpSession session, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            currentUser = userService.FindUserByEmail(auth.getName());
            if (currentUser != null) {
                long unreadCount = notificationService.Get_Unread_Count(currentUser.getId());
                var list = notificationService.Get_Notifications_By_User(currentUser.getId());

                model.addAttribute("globalUnreadCount", unreadCount);
                model.addAttribute("globalNotifications", list);
            }
        }

        // Đếm số lượng sản phẩm trong giỏ hàng và đồng bộ hóa từ DB khi đăng nhập
        @SuppressWarnings("unchecked")
        List<Long> cart = (List<Long>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }

        if (currentUser != null) {
            Boolean isCartSynced = (Boolean) session.getAttribute("isCartSynced");
            if (isCartSynced == null || !isCartSynced) {
                // Lấy từ DB
                List<Long> dbItems = cartService.Get_Db_Cart_Items(currentUser.getId());
                // Gộp vào Session Cart
                for (Long cid : dbItems) {
                    if (!cart.contains(cid)) {
                        cart.add(cid);
                    }
                }
                // Đồng bộ ngược lại DB (nếu lúc ẩn danh có add thêm)
                for (Long cid : cart) {
                    cartService.Save_Db_Cart_Item(currentUser.getId(), cid);
                }
                session.setAttribute("cart", cart);
                session.setAttribute("isCartSynced", true);
            }
        }

        model.addAttribute("globalCartCount", cart.size());
    }
}
