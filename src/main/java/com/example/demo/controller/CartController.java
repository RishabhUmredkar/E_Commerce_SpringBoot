package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepo;

    @PostMapping("/add-to-cart")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            Principal principal) {
        if (principal == null) return "redirect:/login";

        cartService.addToCart(productId, principal.getName(), quantity);
        return "redirect:/";
    }

    @GetMapping("/view-cart")
    public String viewCart(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        User user = userRepo.findByEmail(principal.getName());
        model.addAttribute("cartItems", cartService.getCartItems(user));
        model.addAttribute("total", cartService.getCartTotal(user));
        return "user/cart";
    }

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
    @PostMapping("/decrease-from-cart")
    public String decreaseQuantity(@RequestParam("productId") Long productId, Principal principal) {
        cartService.decreaseQuantity(principal.getName(), productId);
        return "redirect:/"; // Stay on Home Page
    }


}
