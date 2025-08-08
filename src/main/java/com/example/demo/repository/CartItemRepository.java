package com.example.demo.repository;

import com.example.demo.entity.CartItem;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    CartItem findByUserAndProduct(User user, Product product);
    Optional<CartItem> findByUserEmailAndProductId(String email, Long productId);
    void deleteByUser(User user);
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.user = :user AND c.product = :product")
    void removeItem(@Param("user") User user, @Param("product") Product product);

}