package com.cms.service;

import com.cms.model.Contract;
import com.cms.model.Notification;
import com.cms.model.User;
import com.cms.repository.ContractRepository;
import com.cms.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ContractRepository contractRepository;

    // ── Fetch ─────────────────────────────────────────────────
    public List<Notification> getNotificationsForUser(User user) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(user.getId());
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByUserIdAndReadFalse(user.getId());
    }

    // ── Actions ───────────────────────────────────────────────
    public void markAsRead(Long notificationId, User user) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getUser().getId().equals(user.getId())) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }

    public void markAllAsRead(User user) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(user.getId());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    public void deleteNotification(Long id, User user) {
        notificationRepository.findById(id).ifPresent(n -> {
            if (n.getUser().getId().equals(user.getId())) {
                notificationRepository.delete(n);
            }
        });
    }

    // ── Create ────────────────────────────────────────────────
    public void createNotification(String title, String message, Notification.Type type, User user) {
        Notification n = new Notification(title, message, type, user);
        notificationRepository.save(n);
    }

    // ── Auto-generate expiry warnings ─────────────────────────
    public void checkAndGenerateExpiryAlerts(User user) {
        LocalDate today = LocalDate.now();
        LocalDate weekFromNow = today.plusDays(7);

        List<Contract> contracts;
        if (user.getRole() == User.Role.ADMIN) {
            contracts = contractRepository.findAll();
        } else {
            contracts = contractRepository.findByUserId(user.getId());
        }

        for (Contract contract : contracts) {
            if (contract.getStatus() == Contract.Status.EXPIRED) continue;

            // Already expired but not marked
            if (contract.getEndDate().isBefore(today)) {
                // Auto-update status to EXPIRED
                contract.setStatus(Contract.Status.EXPIRED);
                contractRepository.save(contract);

                String title = "Contract Expired";
                String message = "\"" + contract.getTitle() + "\" expired on " + contract.getEndDate();
                if (!isDuplicateNotification(user, title, message)) {
                    createNotification(title, message, Notification.Type.DANGER, user);
                }
            }
            // Expiring within 7 days
            else if (!contract.getEndDate().isAfter(weekFromNow)) {
                long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, contract.getEndDate());
                String title = "Expiring Soon";
                String message = "\"" + contract.getTitle() + "\" expires in " + daysLeft + " day" + (daysLeft != 1 ? "s" : "");
                if (!isDuplicateNotification(user, title, message)) {
                    createNotification(title, message, Notification.Type.WARNING, user);
                }
            }
        }
    }

    private boolean isDuplicateNotification(User user, String title, String message) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .anyMatch(n -> n.getTitle().equals(title) && n.getMessage().equals(message));
    }
}
