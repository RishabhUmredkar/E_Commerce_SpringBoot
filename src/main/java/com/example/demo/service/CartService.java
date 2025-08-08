package com.example.demo.service;

import com.example.demo.entity.CartItem;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepo;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private UserRepository userRepo;

    public void addToCart(Long productId, String email, int quantity) {
        User user = userRepo.findByEmail(email);
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem existing = cartItemRepo.findByUserAndProduct(user, product);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
        } else {
            existing = new CartItem();
            existing.setUser(user);
            existing.setProduct(product);
            existing.setQuantity(quantity);
        }
        cartItemRepo.save(existing);
    }

    public List<CartItem> getCartItems(User user) {
        return cartItemRepo.findByUser(user);
    }

    public double getCartTotal(User user) {
        return getCartItems(user).stream()
                .mapToDouble(item -> item.getQuantity() * item.getProduct().getPrice())
                .sum();
    }

    public void increaseQuantity(String email, Long productId) {
        Optional<CartItem> optionalItem = cartItemRepo.findByUserEmailAndProductId(email, productId);
        if (optionalItem.isPresent()) {
            CartItem item = optionalItem.get();
            item.setQuantity(item.getQuantity() + 1);
            cartItemRepo.save(item);
        }
    }

    public void decreaseQuantity(String email, Long productId) {
        User user = userRepo.findByEmail(email);
        Product product = productRepo.findById(productId).orElse(null);
        if (user != null && product != null) {
            CartItem item = cartItemRepo.findByUserAndProduct(user, product);
            if (item != null) {
                if (item.getQuantity() <= 1) {
                    cartItemRepo.delete(item); // ✅ remove from cart
                } else {
                    item.setQuantity(item.getQuantity() - 1);
                    cartItemRepo.save(item);
                }
            }
        }
    }
    public int getProductQuantityInCart(User user, Product product) {
        if (user == null || product == null) {
            return 0; // safely return 0 instead of throwing exception
        }

        Optional<CartItem> optionalCartItem = cartItemRepo.findByUserEmailAndProductId(user.getEmail(), product.getId());
        return optionalCartItem.map(CartItem::getQuantity).orElse(0);
    }

    public int getQuantity(String email, Long productId) {
    	User user = userRepo.findByEmail(email);
    	if (user == null) {
    	    throw new UsernameNotFoundException("User not found");
    	}

        Optional<CartItem> optionalCartItem = cartItemRepo.findByUserEmailAndProductId(email, productId);

        return optionalCartItem.map(CartItem::getQuantity).orElse(0);
    }
    public double calculateTotal(List<CartItem> cartItems) {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        return total;
    }
    @Transactional
    public void clearCart(User user) {
    	cartItemRepo.deleteByUser(user);
    }
    public void removeFromCart(String email, Long productId) {
        User user = userRepo.findByEmail(email);
        Product product = productRepo.findById(productId).orElse(null);
        if (user != null && product != null) {
            CartItem item = cartItemRepo.findByUserAndProduct(user, product);
            if (item != null) {
                cartItemRepo.delete(item);
            }
        }
    }


}
