package com.cms.controller;

import com.cms.model.User;
import com.cms.service.EmailService;
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
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    // ── Root redirect ─────────────────────────────────────────
    @GetMapping("/")
    public String root(HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    // ── Login ─────────────────────────────────────────────────
    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        User user = userService.login(username, password);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Invalid username or password");
            return "redirect:/login";
        }
        session.setAttribute("loggedInUser", user);
        return "redirect:/dashboard";
    }

    // ── Register ──────────────────────────────────────────────
    @GetMapping("/register")
    public String registerPage(HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/dashboard";
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                           @RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String role,
                           RedirectAttributes redirectAttributes) {
        User user = new User();
        user.setFullName(fullName);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(User.Role.valueOf(role));

        String result = userService.registerUser(user);
        if (!"success".equals(result)) {
            redirectAttributes.addFlashAttribute("error", result);
            return "redirect:/register";
        }
        redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
        return "redirect:/login";
    }

    // ── Forgot Password ───────────────────────────────────────
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String sendOtp(@RequestParam String email,
                          RedirectAttributes redirectAttributes) {
        String otp = userService.generateOtp(email);
        if ("Email not found".equals(otp)) {
            redirectAttributes.addFlashAttribute("error", "No account found with that email");
            redirectAttributes.addFlashAttribute("step", "email");
            return "redirect:/forgot-password";
        }
        emailService.sendOtpEmail(email, otp);
        redirectAttributes.addFlashAttribute("step", "otp");
        redirectAttributes.addFlashAttribute("email", email);
        redirectAttributes.addFlashAttribute("success", "OTP has been sent to your email");
        return "redirect:/forgot-password";
    }

    // ── Resend OTP ────────────────────────────────────────────
    @PostMapping("/resend-otp")
    public String resendOtp(@RequestParam String email,
                            RedirectAttributes redirectAttributes) {
        String otp = userService.generateOtp(email);
        if ("Email not found".equals(otp)) {
            redirectAttributes.addFlashAttribute("error", "No account found with that email");
            redirectAttributes.addFlashAttribute("step", "email");
            return "redirect:/forgot-password";
        }
        emailService.sendOtpEmail(email, otp);
        redirectAttributes.addFlashAttribute("step", "otp");
        redirectAttributes.addFlashAttribute("email", email);
        redirectAttributes.addFlashAttribute("success", "New OTP has been sent to your email");
        return "redirect:/forgot-password";
    }

    // ── Verify OTP ────────────────────────────────────────────
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email,
                            @RequestParam String otp,
                            RedirectAttributes redirectAttributes) {
        String result = userService.verifyOtp(email, otp);
        if (!"success".equals(result)) {
            redirectAttributes.addFlashAttribute("error", result);
            redirectAttributes.addFlashAttribute("step", "otp");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/forgot-password";
        }
        redirectAttributes.addFlashAttribute("step", "reset");
        redirectAttributes.addFlashAttribute("email", email);
        return "redirect:/forgot-password";
    }

    // ── Reset Password ────────────────────────────────────────
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String password,
                                @RequestParam String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match");
            redirectAttributes.addFlashAttribute("step", "reset");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/forgot-password";
        }
        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters");
            redirectAttributes.addFlashAttribute("step", "reset");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/forgot-password";
        }

        String result = userService.resetPassword(email, password);
        if (!"success".equals(result)) {
            redirectAttributes.addFlashAttribute("error", result);
            redirectAttributes.addFlashAttribute("step", "email");
            return "redirect:/forgot-password";
        }
        redirectAttributes.addFlashAttribute("success", "Password reset successful! Please login.");
        return "redirect:/login";
    }

    // ── Logout ────────────────────────────────────────────────
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
