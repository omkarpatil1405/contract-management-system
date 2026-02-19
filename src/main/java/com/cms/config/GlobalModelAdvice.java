package com.cms.config;

import com.cms.model.User;
import com.cms.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    @Autowired
    private NotificationService notificationService;

    @ModelAttribute("notifCount")
    public long notifCount(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null) {
            return notificationService.getUnreadCount(user);
        }
        return 0;
    }
}
