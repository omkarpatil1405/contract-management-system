package com.cms.controller;

import com.cms.model.User;
import com.cms.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SettingsController {

    @Autowired
    private UserService userService;

    @GetMapping("/settings")
    public String settings(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        model.addAttribute("currentUser", user);
        return "settings";
    }

    @PostMapping("/settings/profile")
    public String updateProfile(@RequestParam String fullName,
                                @RequestParam String email,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        String result = userService.updateProfile(user, fullName, email);
        if ("success".equals(result)) {
            session.setAttribute("loggedInUser", userService.findById(user.getId()).orElse(user));
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }
        return "redirect:/settings";
    }

    @PostMapping("/settings/password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match");
            return "redirect:/settings";
        }

        String result = userService.changePassword(user, currentPassword, newPassword);
        if ("success".equals(result)) {
            session.setAttribute("loggedInUser", userService.findById(user.getId()).orElse(user));
            redirectAttributes.addFlashAttribute("success", "Password changed successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }
        return "redirect:/settings";
    }

    // ── Deactivate Own Account (Soft Delete) ─────────────────
    @PostMapping("/settings/delete-account")
    public String deactivateOwnAccount(HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        userService.deactivateUser(user);
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Your account has been deactivated.");
        return "redirect:/login";
    }
}
