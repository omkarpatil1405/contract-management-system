package com.cms.controller;

import com.cms.model.Contract;
import com.cms.model.User;
import com.cms.service.ContractService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ReportsController {

    @Autowired
    private ContractService contractService;

    @GetMapping("/reports")
    public String reports(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        // Stats for cards
        Map<String, Long> stats = contractService.getStatsForUser(user);
        model.addAttribute("stats", stats);

        // Get all contracts for chart data
        List<Contract> contracts = contractService.getContractsForUser(user);
        model.addAttribute("contracts", contracts);

        // Count by status
        model.addAttribute("draftCount", stats.getOrDefault("draft", 0L));
        model.addAttribute("signedCount", stats.getOrDefault("signed", 0L));
        model.addAttribute("runningCount", stats.getOrDefault("running", 0L));
        model.addAttribute("expiredCount", stats.getOrDefault("expired", 0L));

        // Count by party
        long internalCount = contracts.stream().filter(c -> c.getParty() == Contract.Party.INTERNAL).count();
        long externalCount = contracts.stream().filter(c -> c.getParty() == Contract.Party.EXTERNAL).count();
        long govCount = contracts.stream().filter(c -> c.getParty() == Contract.Party.GOVERNMENT).count();
        long vendorCount = contracts.stream().filter(c -> c.getParty() == Contract.Party.VENDOR).count();
        long clientCount = contracts.stream().filter(c -> c.getParty() == Contract.Party.CLIENT).count();
        long noParty = contracts.stream().filter(c -> c.getParty() == null).count();

        model.addAttribute("internalCount", internalCount);
        model.addAttribute("externalCount", externalCount);
        model.addAttribute("govCount", govCount);
        model.addAttribute("vendorCount", vendorCount);
        model.addAttribute("clientCount", clientCount);
        model.addAttribute("noPartyCount", noParty);

        // Count by type — dynamically group by String value
        Map<String, Long> typeCounts = new LinkedHashMap<>();
        for (Contract c : contracts) {
            String type = c.getContractType();
            if (type == null || type.isEmpty()) {
                typeCounts.merge("Unspecified", 1L, Long::sum);
            } else {
                typeCounts.merge(type, 1L, Long::sum);
            }
        }
        model.addAttribute("typeCounts", typeCounts);

        model.addAttribute("currentUser", user);
        return "reports";
    }
}
