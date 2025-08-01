package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.CartItem;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.User;
import com.example.demo.repository.OrderRepository;

@Service
public class OrderService {


    @Autowired
    private EntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    public Order createOrder(User user, List<CartItem> cartItems, double total, String fullName,
            String phone, String address, String city, String postalCode, String paymentMode) {
Order order = new Order();
order.setUser(user);
order.setFullName(fullName);
order.setPhone(phone);
order.setAddress(address);
order.setCity(city);
order.setPostalCode(postalCode);
order.setTotal(total);
order.setPaymentMode(paymentMode);
order.setOrderDate(LocalDateTime.now());
order.setStatus("PENDING");

List<OrderItem> orderItems = new ArrayList<>();
for (CartItem cartItem : cartItems) {
OrderItem item = new OrderItem();
item.setOrder(order);  // Important: set order reference
item.setProduct(cartItem.getProduct());
item.setProductName(cartItem.getProduct().getName());  // Store product name in case product is deleted later
item.setPrice(cartItem.getProduct().getPrice());
item.setQuantity(cartItem.getQuantity());
orderItems.add(item);
}
order.setItems(orderItems);

// Save order - this should cascade save OrderItems
return orderRepository.save(order);
}

    public Order findById(Long id) {
        return orderRepository.findByIdWithItems(id)
                .orElse(null);
    }

    public List<Order> findOrdersByUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserId(Long userId) {
        // Eagerly fetch items and products to avoid lazy loading issues
        List<Order> orders = orderRepository.findByUserIdWithItems(userId);
        return orders;
    }

    public long countOrdersToday() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfNextDay = today.plusDays(1).atStartOfDay();
        return orderRepository.countOrdersBetween(startOfDay, startOfNextDay);
    }
    public long countPendingPayments() {
        return orderRepository.countPendingPayments();
    }
    public Page<Order> getFilteredOrders(Pageable pageable, String name, String status, String dateFrom, String dateTo) {
        StringBuilder query = new StringBuilder("select o from Order o join o.user u where 1=1");

        if (name != null && !name.isEmpty()) {
            query.append(" and lower(u.name) like lower(concat('%', :name, '%'))");
        }
        if (status != null && !status.isEmpty()) {
            query.append(" and o.status = :status");
        }
        if (dateFrom != null && !dateFrom.isEmpty()) {
            query.append(" and o.orderDate >= :dateFrom");
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            query.append(" and o.orderDate <= :dateTo");
        }

        // ✅ Append ORDER BY before creating TypedQuery
        query.append(" ORDER BY o.id DESC");

        TypedQuery<Order> typedQuery = entityManager.createQuery(query.toString(), Order.class);

        if (name != null && !name.isEmpty()) {
            typedQuery.setParameter("name", name);
        }
        if (status != null && !status.isEmpty()) {
            typedQuery.setParameter("status", status);
        }
        if (dateFrom != null && !dateFrom.isEmpty()) {
            typedQuery.setParameter("dateFrom", LocalDate.parse(dateFrom).atStartOfDay()); // ⬅️ Start of day
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            typedQuery.setParameter("dateTo", LocalDate.parse(dateTo).atTime(23, 59, 59)); // ⬅️ End of day
        }


        int firstResult = pageable.getPageNumber() * pageable.getPageSize();
        typedQuery.setFirstResult(firstResult);
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Order> orders = typedQuery.getResultList();
        long totalResults = countFilteredOrders(name, status, dateFrom, dateTo);

        return new PageImpl<>(orders, pageable, totalResults);
    }


    public long countFilteredOrders(String name, String status, String dateFrom, String dateTo) {
        // Build the count query dynamically based on the provided parameters
        StringBuilder countQuery = new StringBuilder("select count(o) from Order o join o.user u where 1=1");

        if (name != null && !name.isEmpty()) {
            countQuery.append(" and lower(u.name) like lower(concat('%', :name, '%'))");
        }

        if (status != null && !status.isEmpty()) {
            countQuery.append(" and o.status = :status");
        }

        if (dateFrom != null && !dateFrom.isEmpty()) {
            countQuery.append(" and o.orderDate >= :dateFrom");
        }

        if (dateTo != null && !dateTo.isEmpty()) {
            countQuery.append(" and o.orderDate <= :dateTo");
        }

        // Execute the count query
        TypedQuery<Long> typedCountQuery = entityManager.createQuery(countQuery.toString(), Long.class);

        if (name != null && !name.isEmpty()) {
            typedCountQuery.setParameter("name", "%" + name.toLowerCase() + "%");
        }
        if (status != null && !status.isEmpty()) {
            typedCountQuery.setParameter("status", status);
        }
        if (dateFrom != null && !dateFrom.isEmpty()) {
            typedCountQuery.setParameter("dateFrom", LocalDate.parse(dateFrom).atStartOfDay());
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            typedCountQuery.setParameter("dateTo", LocalDate.parse(dateTo).atTime(23, 59, 59)); // End of the day
        }

        return typedCountQuery.getSingleResult(); // Return the total count
    }
    
    public Page<Order> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        return orderRepository.findAll(pageable);
    }
    public List<Order> findAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public List<Order> findByStatus(String status) {
        return orderRepository.findByStatusOrderByOrderDateDesc(status);
    }

    public void updateStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId).orElseThrow(null);
        order.setStatus(status);
        orderRepository.save(order);
    }

    public void deleteOrderById(Long id) {
        orderRepository.deleteById(id);
    }
   

    public Page<Order> getOrdersByStatus(String status, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("orderDate").descending());
        return orderRepository.findByStatus(status, pageable);
    }
    
    public Page<Order> searchOrders(String status, String keyword, LocalDate fromDate, LocalDate toDate, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("orderDate").descending());

        // Convert LocalDate to LocalDateTime for JPA query compatibility
        LocalDateTime from = (fromDate != null) ? fromDate.atStartOfDay() : null;
        LocalDateTime to = (toDate != null) ? toDate.atTime(LocalTime.MAX) : null;

        // If empty strings are passed for status/keyword, treat them as null
        status = (status != null && !status.trim().isEmpty()) ? status : null;
        keyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword : null;

        return orderRepository.searchOrders(status, keyword, from, to, pageable);
    }
    public long getTotalOrderCount(String status, String keyword, LocalDate fromDate, LocalDate toDate) {
        return orderRepository.countOrdersWithFilters(status, keyword, fromDate, toDate);
    }

    public BigDecimal getTotalOrderRevenue(String status, String keyword, LocalDate fromDate, LocalDate toDate) {
        return orderRepository.sumOrderTotalWithFilters(status, keyword, fromDate, toDate);
    }



}