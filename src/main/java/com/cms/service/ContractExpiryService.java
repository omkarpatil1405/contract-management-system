package com.cms.service;

import com.cms.model.Contract;
import com.cms.repository.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ContractExpiryService {

    @Autowired
    private ContractRepository contractRepository;

    /**
     * Runs every hour to auto-expire contracts whose end date has passed.
     * Also runs once on startup (initialDelay = 0).
     */
    @Scheduled(fixedRate = 3600000, initialDelay = 0)
    public void autoExpireContracts() {
        LocalDate today = LocalDate.now();
        List<Contract> allContracts = contractRepository.findAll();

        for (Contract contract : allContracts) {
            if (contract.getStatus() != Contract.Status.EXPIRED
                    && contract.getEndDate() != null
                    && contract.getEndDate().isBefore(today)) {
                contract.setStatus(Contract.Status.EXPIRED);
                contractRepository.save(contract);
            }
        }
    }
}
