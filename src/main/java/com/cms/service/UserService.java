package com.cms.service;

import com.cms.model.Contract;
import com.cms.model.User;
import com.cms.repository.ContractRepository;
import com.cms.repository.NotificationRepository;
import com.cms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private FileStorageService fileStorageService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ── Soft Delete (Deactivate) ─────────────────────────────
    @Transactional
    public void deactivateUser(User user) {
        user.setStatus(User.Status.INACTIVE);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // ── Restore User (Admin only) ────────────────────────────
    @Transactional
    public String restoreUser(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return "User not found";
        }
        User user = optionalUser.get();
        if (user.getStatus() == User.Status.ACTIVE) {
            return "User is already active";
        }
        user.setStatus(User.Status.ACTIVE);
        user.setDeletedAt(null);
        userRepository.save(user);
        return "success";
    }

    // ── Permanent Delete (Admin only) ────────────────────────
    @Transactional
    public String permanentlyDeleteUser(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return "User not found";
        }

        User user = optionalUser.get();

        // Delete uploaded files for user's contracts
        List<Contract> contracts = contractRepository.findByUserId(user.getId());
        for (Contract contract : contracts) {
            if (contract.getFileName() != null && !contract.getFileName().isEmpty()) {
                fileStorageService.deleteFile(contract.getFileName());
            }
        }

        // Delete contracts, notifications, then the user
        contractRepository.deleteAll(contracts);
        notificationRepository.deleteAll(notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));
        userRepository.delete(user);
        return "success";
    }

    // ── Registration ──────────────────────────────────────────
    public String registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return "Username already exists";
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return "Email already exists";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);
        return "success";
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // ── Login ─────────────────────────────────────────────────
    public User login(String username, String password) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            // Block INACTIVE users
            if (user.getStatus() == User.Status.INACTIVE) {
                return null;
            }
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    // ── OTP Generation ────────────────────────────────────────
    public String generateOtp(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return "Email not found";
        }

        User user = optionalUser.get();
        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);
        return otp;
    }

    // ── OTP Verification ──────────────────────────────────────
    public String verifyOtp(String email, String otp) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return "Email not found";
        }

        User user = optionalUser.get();
        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            return "Invalid OTP";
        }
        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            return "OTP has expired. Please request a new one.";
        }
        return "success";
    }

    // ── Password Reset ────────────────────────────────────────
    public String resetPassword(String email, String newPassword) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return "Email not found";
        }

        User user = optionalUser.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
        return "success";
    }

    // ── Queries ───────────────────────────────────────────────
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByStatus(User.Status status) {
        return userRepository.findByStatus(status);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // ── Profile Update ───────────────────────────────────────
    public String updateProfile(User currentUser, String fullName, String email) {
        // Check if email is taken by another user
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(currentUser.getId())) {
            return "Email already in use by another account";
        }

        currentUser.setFullName(fullName);
        currentUser.setEmail(email);
        userRepository.save(currentUser);
        return "success";
    }

    // ── Change Password ──────────────────────────────────────
    public String changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return "Current password is incorrect";
        }
        if (newPassword.length() < 6) {
            return "New password must be at least 6 characters";
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return "success";
    }
}
