package com.example.demo.controller;

import java.security.Principal;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.example.demo.entity.CartItem;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.entity.UserDTO;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CartService;



@Controller
public class UserController {

    @Autowired
    private UserRepository userRepo;

	/*
	 * @Autowired private OrderService orderService;
	 */
    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private ProductRepository productRepository;

    // ✅ Home Page: Show products + logged-in user info
    @Autowired
    private CartService cartService; // ✅

    @GetMapping("/")
    public String showHomePage(Model model,
                               @RequestParam(defaultValue = "0") int page,
                               Principal principal) {

        int pageSize = 6;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Product> productPage = productRepository.findAll(pageable);
		List<Product> products = productPage.getContent();

		Map<Long, String> imageMap = new HashMap<>();
		for (Product p : products) {
		    if (p.getImage() != null) {
		        imageMap.put(p.getId(), Base64.getEncoder().encodeToString(p.getImage()));
		    }
		}
        model.addAttribute("productPage", productPage);
        model.addAttribute("products", productPage.getContent());
		model.addAttribute("imageMap", imageMap);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());

        if (principal != null) {
            model.addAttribute("username", principal.getName());

            // ✅ Role check using SecurityContext
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isUser = auth.getAuthorities().stream()
                                 .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER"));

            if (isUser) {
                User user = userRepo.findByEmail(principal.getName());

                if (user != null) {
                    int cartCount = cartService.getCartItems(user).size();
                    model.addAttribute("cartCount", cartCount);

                    Map<Long, Integer> productQuantities = new HashMap<>();
                    for (Product product : productPage.getContent()) {
                        int qty = cartService.getProductQuantityInCart(user, product);
                        productQuantities.put(product.getId(), qty);
                    }
                    model.addAttribute("productQuantities", productQuantities);
                }
            }
        }

        return "user/home";
    }

    // ✅ Login Page
    @GetMapping("/login")
    public String showLogin() {
        return "user/login";
    }

    // ✅ Registration Page
    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("userDTO", new UserDTO());
        return "user/register";
    }

    // ✅ Register User and Auto-login
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("userDTO") UserDTO dto, RedirectAttributes redirectAttributes) {
        if (userRepo.findByEmail(dto.getEmail()) != null) {
            redirectAttributes.addFlashAttribute("error", "User already exists");
            return "redirect:/register";
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(encoder.encode(dto.getPassword()));
        userRepo.save(user);

        // ✅ Automatically authenticate after registration
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
        SecurityContextHolder.getContext().setAuthentication(authToken);

        redirectAttributes.addFlashAttribute("success", "Registered and logged in successfully!");
        return "redirect:/";
    }
    @GetMapping("/get-cart-quantity")
    @ResponseBody
    public Map<String, Object> getCartQuantity(@RequestParam Long productId, Principal principal) {
        int quantity = cartService.getQuantity(principal.getName(), productId);
        Map<String, Object> response = new HashMap<>();
        response.put("quantity", quantity);
        return response;
    }
    
	/*
	 * @GetMapping("/user/products/image/{id}")
	 * 
	 * @ResponseBody public ResponseEntity<byte[]> getProductImage(@PathVariable
	 * Long id) { Product product = productRepository.findById(id).orElse(null);
	 * 
	 * if (product == null) { System.out.println("❌ Product not found for ID: " +
	 * id); return ResponseEntity.notFound().build(); }
	 * 
	 * if (product.getImage() == null || product.getImage().length == 0) {
	 * System.out.println("⚠️ No image found for Product ID: " + id); return
	 * ResponseEntity.notFound().build(); }
	 * 
	 * System.out.println("✅ Serving image for Product ID: " + id + ", Image Size: "
	 * + product.getImage().length + " bytes");
	 * 
	 * // You can also guess MIME type based on byte signature if needed return
	 * ResponseEntity.ok() .contentType(MediaType.IMAGE_JPEG) // Keep this if you
	 * always save JPEGs .body(product.getImage()); }
	 */
    @GetMapping("/products/image/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null || product.getImage() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(product.getImage());
    }

    
    // ✅ Delivery Address Page (after "Checkout")
    @PostMapping("/order/delivery")
    public String showDeliveryPage(Model model, Principal principal) {
        User user = userRepo.findByEmail(principal.getName());
        List<CartItem> cartItems = cartService.getCartItems(user);
        double total = cartService.calculateTotal(cartItems);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        return "user/delivery-address"; // Collect address/payment details here
    }


	/*
	 * // ✅ Order Confirmation (processes order and redirects)
	 * 
	 * @PostMapping("/order/confirm") public String confirmOrder(
	 * 
	 * @RequestParam String fullName,
	 * 
	 * @RequestParam String phone,
	 * 
	 * @RequestParam String address,
	 * 
	 * @RequestParam String city,
	 * 
	 * @RequestParam String postalCode,
	 * 
	 * @RequestParam String paymentMode, // <--- add this param Principal principal,
	 * RedirectAttributes redirectAttributes) {
	 * 
	 * User user = userRepo.findByEmail(principal.getName()); List<CartItem>
	 * cartItems = cartService.getCartItems(user);
	 * 
	 * if (cartItems.isEmpty()) { redirectAttributes.addFlashAttribute("error",
	 * "Your cart is empty."); return "redirect:/"; // Or show error page }
	 * 
	 * double total = cartService.calculateTotal(cartItems);
	 * 
	 * // Save the order with payment mode Order savedOrder =
	 * orderService.createOrder(user, cartItems, total, fullName, phone, address,
	 * city, postalCode, paymentMode);
	 * 
	 * // Clear cart cartService.clearCart(user);
	 * 
	 * // Redirect using PRG return "redirect:/order/confirmation/" +
	 * savedOrder.getId(); }
	 */

	/*
	 * // ✅ Final Confirmation Page (safe GET)
	 * 
	 * @GetMapping("/order/confirmation/{orderId}") public String
	 * orderConfirmation(@PathVariable Long orderId, Model model) { Order order =
	 * orderService.findById(orderId); if (order == null) { return "redirect:/"; //
	 * Or 404 page }
	 * 
	 * model.addAttribute("order", order); return "user/order-confirmation"; // This
	 * is your Thymeleaf view }
	 */
}