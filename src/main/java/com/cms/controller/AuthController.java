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

    // ── Register (Step 1: Show form) ──────────────────────────
    @GetMapping("/register")
    public String registerPage(HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/dashboard";
        }
        return "register";
    }

    // ── Register (Step 2: Validate & send OTP) ───────────────
    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                           @RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String role,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        // Validate uniqueness before sending OTP
        if (userService.existsByUsername(username)) {
            redirectAttributes.addFlashAttribute("error", "Username already exists");
            return "redirect:/register";
        }
        if (userService.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "Email already exists");
            return "redirect:/register";
        }

        // Store registration data in session
        User pendingUser = new User();
        pendingUser.setFullName(fullName);
        pendingUser.setUsername(username);
        pendingUser.setEmail(email);
        pendingUser.setPassword(password);
        pendingUser.setRole(User.Role.valueOf(role));
        session.setAttribute("regUser", pendingUser);

        // Generate and store OTP in session
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        session.setAttribute("regOtp", otp);
        session.setAttribute("regOtpExpiry", java.time.LocalDateTime.now().plusMinutes(5));

        // Send OTP email
        emailService.sendRegistrationOtpEmail(email, otp);

        redirectAttributes.addFlashAttribute("step", "otp");
        redirectAttributes.addFlashAttribute("email", email);
        redirectAttributes.addFlashAttribute("success", "OTP has been sent to " + email);
        return "redirect:/register";
    }

    // ── Register: Verify OTP ─────────────────────────────────
    @PostMapping("/register/verify-otp")
    public String verifyRegistrationOtp(@RequestParam String otp,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        String storedOtp = (String) session.getAttribute("regOtp");
        java.time.LocalDateTime expiry = (java.time.LocalDateTime) session.getAttribute("regOtpExpiry");
        User pendingUser = (User) session.getAttribute("regUser");

        if (storedOtp == null || pendingUser == null) {
            redirectAttributes.addFlashAttribute("error", "Session expired. Please register again.");
            return "redirect:/register";
        }

        if (!storedOtp.equals(otp)) {
            redirectAttributes.addFlashAttribute("error", "Invalid OTP");
            redirectAttributes.addFlashAttribute("step", "otp");
            redirectAttributes.addFlashAttribute("email", pendingUser.getEmail());
            return "redirect:/register";
        }

        if (expiry != null && expiry.isBefore(java.time.LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "OTP has expired. Please request a new one.");
            redirectAttributes.addFlashAttribute("step", "otp");
            redirectAttributes.addFlashAttribute("email", pendingUser.getEmail());
            return "redirect:/register";
        }

        // OTP verified — create the user
        String result = userService.registerUser(pendingUser);
        if (!"success".equals(result)) {
            redirectAttributes.addFlashAttribute("error", result);
            return "redirect:/register";
        }

        // Clean up session
        session.removeAttribute("regUser");
        session.removeAttribute("regOtp");
        session.removeAttribute("regOtpExpiry");

        redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
        return "redirect:/login";
    }

    // ── Register: Resend OTP ─────────────────────────────────
    @PostMapping("/register/resend-otp")
    public String resendRegistrationOtp(HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        User pendingUser = (User) session.getAttribute("regUser");
        if (pendingUser == null) {
            redirectAttributes.addFlashAttribute("error", "Session expired. Please register again.");
            return "redirect:/register";
        }

        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        session.setAttribute("regOtp", otp);
        session.setAttribute("regOtpExpiry", java.time.LocalDateTime.now().plusMinutes(5));

        emailService.sendRegistrationOtpEmail(pendingUser.getEmail(), otp);

        redirectAttributes.addFlashAttribute("step", "otp");
        redirectAttributes.addFlashAttribute("email", pendingUser.getEmail());
        redirectAttributes.addFlashAttribute("success", "New OTP has been sent to " + pendingUser.getEmail());
        return "redirect:/register";
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
