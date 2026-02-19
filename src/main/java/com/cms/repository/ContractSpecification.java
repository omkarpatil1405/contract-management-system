package com.cms.repository;

import com.cms.model.Contract;
import com.cms.model.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContractSpecification {

    public static Specification<Contract> withFilters(
            User user,
            String keyword,
            Contract.Status status,
            Contract.Party party,
            String contractType,
            LocalDate fromDate,
            LocalDate toDate) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Role-based: USER sees only their own contracts
            if (user.getRole() == User.Role.USER) {
                predicates.add(cb.equal(root.get("user").get("id"), user.getId()));
            }

            // Keyword search on title
            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("title")),
                        "%" + keyword.trim().toLowerCase() + "%"
                ));
            }

            // Status filter
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // Party filter
            if (party != null) {
                predicates.add(cb.equal(root.get("party"), party));
            }

            // Contract Type filter (now String)
            if (contractType != null && !contractType.isEmpty()) {
                predicates.add(cb.equal(root.get("contractType"), contractType));
            }

            // Date range: from
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), fromDate));
            }

            // Date range: to
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), toDate));
            }

            query.orderBy(cb.desc(root.get("id")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
