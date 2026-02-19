package com.cms.repository;

import com.cms.model.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long>, JpaSpecificationExecutor<Contract> {

    List<Contract> findByUserId(Long userId);

    long countByStatus(Contract.Status status);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, Contract.Status status);

    List<Contract> findByStatus(Contract.Status status);

    List<Contract> findByUserIdAndStatus(Long userId, Contract.Status status);
}
