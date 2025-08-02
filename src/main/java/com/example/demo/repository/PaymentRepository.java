package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.demo.entity.Order;
import com.example.demo.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

//	Payment findByOrderId(Long orderId); // assuming orderId is Long
    List<Payment> findByOrder(Order order);  // ✅ Correct way to fetch payments by Order
    Optional<Payment> findByOrder_Id(Long orderId); // Correct usage for nested Order ID

}
