package com.cms.controller;

import com.cms.model.User;
import com.cms.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public String usersList(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }
        if (user.getRole() != User.Role.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Access denied. Admin only.");
            return "redirect:/dashboard";
        }

        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("currentUser", user);
        return "users";
    }
}
