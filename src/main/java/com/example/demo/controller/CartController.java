package com.example.demo.controller;

import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ProductRepository productRepository;
    // ✅ ADD TO CART WITH PAGE SUPPORT
    @PostMapping("/add-to-cart")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            @RequestParam(defaultValue = "0") int page,
                            Principal principal) {
        if (principal == null) return "redirect:/login";

        cartService.addToCart(productId, principal.getName(), quantity);
        return "redirect:/home?page=" + page; // Redirect back to current page
    }

    // ✅ DECREASE FROM CART WITH PAGE SUPPORT
    @PostMapping("/decrease-from-cart")
    public String decreaseQuantity(@RequestParam("productId") Long productId,
                                   @RequestParam(defaultValue = "0") int page,
                                   Principal principal) {
        cartService.decreaseQuantity(principal.getName(), productId);
        return "redirect:/home?page=" + page; // Stay on same page
    }

    // ✅ VIEW CART
    @GetMapping("/view-cart")
    public String viewCart(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        User user = userRepo.findByEmail(principal.getName());
        model.addAttribute("cartItems", cartService.getCartItems(user));
        model.addAttribute("total", cartService.getCartTotal(user));
        return "user/cart";
    }

    // ✅ UPDATE IN CART PAGE
    @PostMapping("/cart/update")
    public String updateCartItem(@RequestParam("productId") Long productId,
                                 @RequestParam("action") String action,
                                 Principal principal) {
        if (principal == null) return "redirect:/login";

        String email = principal.getName();
        if ("increase".equals(action)) {
            cartService.increaseQuantity(email, productId);
        } else if ("decrease".equals(action)) {
            cartService.decreaseQuantity(email, productId);
        }
        return "redirect:/view-cart";
    }
    @GetMapping("/cart/quantity/{productId}")
    @ResponseBody
    public Map<String, Integer> getQuantity(@PathVariable Long productId, Principal principal) {
        int qty = 0;
        if (principal != null) {
            User user = userRepo.findByEmail(principal.getName());
            Product product = productRepository.findById(productId).orElse(null);
            if (user != null && product != null) {
                qty = cartService.getProductQuantityInCart(user, product);
            }
        }
        return Collections.singletonMap("quantity", qty);
    }
    @PostMapping("/cart/remove")
    @ResponseBody
    public ResponseEntity<?> removeFromCart(@RequestParam Long productId, Principal principal) {
        if (principal != null) {
            cartService.removeFromCart(principal.getName(), productId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    @GetMapping("/cart/count")
    @ResponseBody
    public Map<String, Integer> getCartCount(Principal principal) {
        int cartCount = 0;

        if (principal != null) {
            User user = userRepo.findByEmail(principal.getName());
            cartCount = cartService.getCartItems(user).size();
        }

        return Collections.singletonMap("count", cartCount);
    }


}
