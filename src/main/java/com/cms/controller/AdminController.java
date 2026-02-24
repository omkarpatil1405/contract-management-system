package com.cms.controller;

import com.cms.model.User;
import com.cms.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    // ── Admin: List All Users ────────────────────────────────
    @GetMapping("/users")
    public String listUsers(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null) return "redirect:/login";

        if (admin.getRole() != User.Role.ADMIN) {
            return "redirect:/dashboard";
        }

        List<User> allUsers = userService.getAllUsers();
        model.addAttribute("users", allUsers);
        model.addAttribute("currentUser", admin);
        return "admin-users";
    }

    // ── Admin: Permanently Delete User ───────────────────────
    @PostMapping("/users/delete/{id}")
    public String permanentlyDeleteUser(@PathVariable Long id,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null) return "redirect:/login";

        if (admin.getRole() != User.Role.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/dashboard";
        }

        if (admin.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "You cannot permanently delete your own account");
            return "redirect:/admin/users";
        }

        String result = userService.permanentlyDeleteUser(id);
        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "User permanently deleted");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }
        return "redirect:/admin/users";
    }

    // ── Admin: Restore User ──────────────────────────────────
    @PostMapping("/users/{id}/restore")
    public String restoreUser(@PathVariable Long id,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null) return "redirect:/login";

        if (admin.getRole() != User.Role.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/dashboard";
        }

        String result = userService.restoreUser(id);
        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "User restored successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }
        return "redirect:/admin/users";
    }
}
