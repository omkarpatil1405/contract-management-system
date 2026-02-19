package com.cms.controller;

import com.cms.model.Contract;
import com.cms.model.User;
import com.cms.service.ContractService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private ContractService contractService;

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String party,
            @RequestParam(required = false) String contractType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }

        // Stats (always unfiltered)
        Map<String, Long> stats = contractService.getStatsForUser(user);
        model.addAttribute("stats", stats);

        // Parse enums safely
        Contract.Status filterStatus = parseEnum(Contract.Status.class, status);
        Contract.Party filterParty = parseEnum(Contract.Party.class, party);
        String filterType = (contractType != null && !contractType.isEmpty()) ? contractType : null;

        // Check if any filter is active
        boolean hasFilters = (keyword != null && !keyword.trim().isEmpty())
                || filterStatus != null
                || filterParty != null
                || filterType != null
                || fromDate != null
                || toDate != null;

        // Fetch contracts
        List<Contract> contracts;
        if (hasFilters) {
            contracts = contractService.filterContracts(user, keyword, filterStatus, filterParty, filterType, fromDate, toDate);
        } else {
            contracts = contractService.getContractsForUser(user);
        }

        model.addAttribute("contracts", contracts);
        model.addAttribute("currentUser", user);

        // Preserve filter values
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedParty", party);
        model.addAttribute("selectedContractType", contractType);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("hasFilters", hasFilters);

        // Build contract type list: defaults + any custom types from existing contracts
        List<Contract> allContracts = contractService.getContractsForUser(user);
        Set<String> typeSet = new LinkedHashSet<>(Arrays.asList(Contract.DEFAULT_CONTRACT_TYPES));
        for (Contract c : allContracts) {
            if (c.getContractType() != null && !c.getContractType().isEmpty()) {
                typeSet.add(c.getContractType());
            }
        }
        model.addAttribute("contractTypes", typeSet);

        // Enum values for dropdowns
        model.addAttribute("statuses", Contract.Status.values());
        model.addAttribute("parties", Contract.Party.values());

        return "dashboard";
    }

    private <T extends Enum<T>> T parseEnum(Class<T> enumClass, String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
