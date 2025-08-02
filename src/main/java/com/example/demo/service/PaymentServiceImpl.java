package com.example.demo.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Payment;
import com.example.demo.repository.PaymentRepository;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public Page<Payment> getFilteredPayments(String keyword, String status, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        Specification<Payment> spec = buildSpecification(keyword, status, fromDate, toDate);
        return paymentRepository.findAll(spec, pageable);
    }

    @Override
    public List<Payment> getFilteredPayments(String keyword, String status, LocalDate fromDate, LocalDate toDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Payment> spec = buildSpecification(keyword, status, fromDate, toDate);
        return paymentRepository.findAll(spec, pageable).getContent();
    }

    @Override
    public long countFilteredPayments(String keyword, String status, LocalDate fromDate, LocalDate toDate) {
        Specification<Payment> spec = buildSpecification(keyword, status, fromDate, toDate);
        return paymentRepository.count(spec);
    }

    @Override
    public void savePayment(Payment payment) {
        paymentRepository.save(payment);
    }

    // Common specification method used by all
    private Specification<Payment> buildSpecification(String keyword, String status, LocalDate fromDate, LocalDate toDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isEmpty()) {
                Predicate byUser = cb.like(cb.lower(root.get("userName")), "%" + keyword.toLowerCase() + "%");

                // Cast order.id (Long) to String for 'like' search
                Predicate byOrderId = cb.like(
                    cb.function("str", String.class, root.get("order").get("id")),
                    "%" + keyword + "%"
                );

                predicates.add(cb.or(byUser, byOrderId));
            }

            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("paymentDate"), fromDate));
            }

            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("paymentDate"), toDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


}
