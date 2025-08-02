package com.example.demo.service;

import com.example.demo.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface PaymentService {

    Page<Payment> getFilteredPayments(String keyword, String status, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    void savePayment(Payment payment);

    List<Payment> getFilteredPayments(String keyword, String status, LocalDate fromDate, LocalDate toDate, int page, int size);

    long countFilteredPayments(String keyword, String status, LocalDate fromDate, LocalDate toDate);
}
