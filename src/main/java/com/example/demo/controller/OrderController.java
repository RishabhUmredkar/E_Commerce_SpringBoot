package com.example.demo.controller;

import com.example.demo.entity.CartItem;
import com.example.demo.entity.Order;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CartService;
import com.example.demo.service.EmailService;
import com.example.demo.service.OrderService;
import com.razorpay.Utils;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserRepository userRepo;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    @Autowired
    private EmailService emailService;

    public OrderController(CartService cartService, OrderService orderService, UserRepository userRepo) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.userRepo = userRepo;
    }

    @PostMapping("/confirm")
    public String confirmOrder(
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam String city,
            @RequestParam String postalCode,
            @RequestParam String paymentMode,
            @RequestParam(required = false) String razorpayPaymentId,
            @RequestParam(required = false) String razorpayOrderId,
            @RequestParam(required = false) String razorpaySignature,
            Principal principal,
            Model model) throws Exception {

        User user = userRepo.findByEmail(principal.getName());
        List<CartItem> cartItems = cartService.getCartItems(user);

        if (cartItems.isEmpty()) {
            model.addAttribute("error", "Your cart is empty.");
            return "redirect:/cart";
        }

        double total = cartService.calculateTotal(cartItems);

        if ("RAZORPAY".equalsIgnoreCase(paymentMode)) {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", razorpayOrderId);
            options.put("razorpay_payment_id", razorpayPaymentId);
            options.put("razorpay_signature", razorpaySignature);

            boolean isValid = Utils.verifyPaymentSignature(options, razorpaySecret);
            if (!isValid) {
                model.addAttribute("error", "Payment verification failed. Please try again.");
                return "paymentError";
            }
        }

        try {
            // Create Order with PENDING status inside service method
            Order order = orderService.createOrder(user, cartItems, total, fullName, phone, address, city, postalCode, paymentMode);

            // Clear cart after successful order creation
            cartService.clearCart(user);

            // Send confirmation email
            String subject = "Order Confirmation - Order #" + order.getId();
            String emailBody = "Dear " + fullName + ",\n\n" +
                    "Thank you for your order!\n\n" +
                    "Order ID: " + order.getId() + "\n" +
                    "Placed On: " + order.getOrderDate() + "\n" +
                    "Total: ₹" + total + "\n\n" +
                    "Delivery Address:\n" +
                    fullName + "\n" +
                    address + ", " + city + " - " + postalCode + "\n" +
                    "Phone: " + phone + "\n\n" +
                    "We will notify you once your order is out for delivery.\n\n" +
                    "Regards,\nYour Store Team";

            emailService.sendOrderConfirmation(user.getEmail(), subject, emailBody);

            return "redirect:/order/confirmation/" + order.getId();
        } catch (Exception e) {
            e.printStackTrace(); // Log exception for debugging
            model.addAttribute("error", "Something went wrong while placing the order. Please try again.");
            return "orderError";
        }
    }

    @GetMapping("/confirmation/{orderId}")
    public String orderConfirmation(@PathVariable Long orderId, Model model) {
        Order order = orderService.findById(orderId);
        if (order == null) {
            return "redirect:/"; // or display a 404 page
        }
        model.addAttribute("order", order);
        return "user/order-confirmation";
    }
    
    @GetMapping("/history")
    public String orderHistory(Principal principal, Model model) {
        // Get logged-in user
        User user = userRepo.findByEmail(principal.getName());

        // Fetch orders for this user
        List<Order> orders = orderService.findOrdersByUser(user);

        // Add to model
        model.addAttribute("orders", orders);
        model.addAttribute("username", user.getName()); // optional, for greeting

        return "user/order-history";  // Thymeleaf template to create next
    }

}
