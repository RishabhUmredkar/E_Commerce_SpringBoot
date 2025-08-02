package com.example.demo.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Order;
import com.example.demo.entity.User;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find by User and sort by Order Date (Descending)
    List<Order> findByUserOrderByOrderDateDesc(User user);

    // Fetch orders by User ID, joining with items and products
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product WHERE o.user.id = :userId")
    List<Order> findByUserIdWithItems(Long userId);

    // Fetch by ID with items
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    // Get top 5 orders ordered by order date (descending)
    List<Order> findTop5ByOrderByOrderDateDesc();

    // Fetch orders with pagination, ordered by ID descending
    @Query("SELECT o FROM Order o ORDER BY o.id DESC")
    List<Order> findRecentOrders(Pageable pageable);

    // Fetch orders by username containing a string
    Page<Order> findByUserUsernameContaining(String username, Pageable pageable);

    // Fetch orders by status
    Page<Order> findByStatus(String status, Pageable pageable);

    // Find all orders with pagination
    Page<Order> findAll(Pageable pageable);

    // Filter by user name, order status, and order date range with pagination
    @Query("SELECT o FROM Order o WHERE LOWER(o.user.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
    	       "AND o.status = :status " +
    	       "AND o.orderDate BETWEEN :dateFrom AND :dateTo ORDER BY o.id DESC")
    	Page<Order> findByUser_NameContainingIgnoreCaseAndStatusAndOrderDateBetween(
    	    @Param("name") String name, 
    	    @Param("status") String status, 
    	    @Param("dateFrom") LocalDateTime dateFrom, 
    	    @Param("dateTo") LocalDateTime dateTo, 
    	    Pageable pageable);


    // Search by status and order date range with pagination and order by ID descending
    @Query("SELECT o FROM Order o WHERE o.status = :status " +
           "AND o.orderDate BETWEEN :dateFrom AND :dateTo ORDER BY o.id DESC")
    Page<Order> findByStatusAndOrderDateBetween(
        @Param("status") String status, 
        @Param("dateFrom") LocalDateTime dateFrom, 
        @Param("dateTo") LocalDateTime dateTo, 
        Pageable pageable);

    // Search by order date range only with pagination and order by ID descending
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :dateFrom AND :dateTo ORDER BY o.id DESC")
    Page<Order> findByOrderDateBetween(
        @Param("dateFrom") LocalDateTime dateFrom, 
        @Param("dateTo") LocalDateTime dateTo, 
        Pageable pageable);

    // Count orders between a specific date range
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :startOfDay AND o.orderDate < :startOfNextDay")
    long countOrdersBetween(@Param("startOfDay") LocalDateTime startOfDay,
                            @Param("startOfNextDay") LocalDateTime startOfNextDay);

    // Count pending payments (status is 'PENDING')
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'PENDING'")
    long countPendingPayments();
    
    // ✅ List orders by status and order by date descending
    List<Order> findByStatusOrderByOrderDateDesc(String status);

    // ✅ List all orders ordered by date descending
    List<Order> findAllByOrderByOrderDateDesc();
    
    @Query("SELECT o FROM Order o " +
    	       "WHERE (:status IS NULL OR o.status = :status) " +
    	       "AND (:keyword IS NULL OR LOWER(o.user.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
    	       "AND (:fromDate IS NULL OR o.orderDate >= :fromDate) " +
    	       "AND (:toDate IS NULL OR o.orderDate <= :toDate)")
    	Page<Order> searchOrders(@Param("status") String status,
    	                         @Param("keyword") String keyword,
    	                         @Param("fromDate") LocalDateTime fromDate,
    	                         @Param("toDate") LocalDateTime toDate,
    	                         Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE "
    	     + "(:status IS NULL OR o.status = :status) AND "
    	     + "(:keyword IS NULL OR LOWER(o.user.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND "
    	     + "(:fromDate IS NULL OR o.orderDate >= :fromDate) AND "
    	     + "(:toDate IS NULL OR o.orderDate <= :toDate)")
    	long countOrdersWithFilters(@Param("status") String status,
    	                            @Param("keyword") String keyword,
    	                            @Param("fromDate") LocalDateTime fromDate,
    	                            @Param("toDate") LocalDateTime toDate);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE "
    	     + "(:status IS NULL OR o.status = :status) AND "
    	     + "(:keyword IS NULL OR LOWER(o.user.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND "
    	     + "(:fromDate IS NULL OR o.orderDate >= :fromDate) AND "
    	     + "(:toDate IS NULL OR o.orderDate <= :toDate)")
    	BigDecimal sumOrderTotalWithFilters(@Param("status") String status,
    	                                    @Param("keyword") String keyword,
    	                                    @Param("fromDate") LocalDateTime fromDate,
    	                                    @Param("toDate") LocalDateTime toDate);


}
