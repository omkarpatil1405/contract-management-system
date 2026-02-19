package com.cms.service;

import com.cms.model.Contract;
import com.cms.model.User;
import com.cms.repository.ContractRepository;
import com.cms.repository.ContractSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ContractService {

    @Autowired
    private ContractRepository contractRepository;

    // ── CRUD ──────────────────────────────────────────────────
    public Contract saveContract(Contract contract) {
        return contractRepository.save(contract);
    }

    public Optional<Contract> findById(Long id) {
        return contractRepository.findById(id);
    }

    public void deleteContract(Long id) {
        contractRepository.deleteById(id);
    }

    // ── Role-based Listing ────────────────────────────────────
    public List<Contract> getContractsForUser(User user) {
        if (user.getRole() == User.Role.ADMIN) {
            return contractRepository.findAll();
        }
        return contractRepository.findByUserId(user.getId());
    }

    public List<Contract> getContractsForUserByStatus(User user, Contract.Status status) {
        if (user.getRole() == User.Role.ADMIN) {
            return contractRepository.findByStatus(status);
        }
        return contractRepository.findByUserIdAndStatus(user.getId(), status);
    }

    // ── Filtered Listing (Specification-based) ────────────────
    public List<Contract> filterContracts(User user,
                                          String keyword,
                                          Contract.Status status,
                                          Contract.Party party,
                                          String contractType,
                                          LocalDate fromDate,
                                          LocalDate toDate) {
        return contractRepository.findAll(
                ContractSpecification.withFilters(user, keyword, status, party, contractType, fromDate, toDate)
        );
    }

    // ── Statistics ────────────────────────────────────────────
    public Map<String, Long> getStatsForUser(User user) {
        Map<String, Long> stats = new HashMap<>();
        if (user.getRole() == User.Role.ADMIN) {
            stats.put("total", contractRepository.count());
            stats.put("running", contractRepository.countByStatus(Contract.Status.RUNNING));
            stats.put("signed", contractRepository.countByStatus(Contract.Status.SIGNED));
            stats.put("expired", contractRepository.countByStatus(Contract.Status.EXPIRED));
            stats.put("draft", contractRepository.countByStatus(Contract.Status.DRAFT));
        } else {
            stats.put("total", contractRepository.countByUserId(user.getId()));
            stats.put("running", contractRepository.countByUserIdAndStatus(user.getId(), Contract.Status.RUNNING));
            stats.put("signed", contractRepository.countByUserIdAndStatus(user.getId(), Contract.Status.SIGNED));
            stats.put("expired", contractRepository.countByUserIdAndStatus(user.getId(), Contract.Status.EXPIRED));
            stats.put("draft", contractRepository.countByUserIdAndStatus(user.getId(), Contract.Status.DRAFT));
        }
        return stats;
    }
}
