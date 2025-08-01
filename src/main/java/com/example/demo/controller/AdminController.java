package com.example.demo.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.Order;
import com.example.demo.entity.Product;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private UserRepository userRepository;
//	@Autowired
//	private ImageService imageService;
//
//	@Autowired
//	private OrderRepository orderRepository;
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private ProductService productService;
	@Autowired
	private OrderService orderService;

	@GetMapping("/login")
	public String adminLogin() {
		return "admin/login"; // ➡️ Return Thymeleaf view: templates/admin/login.html
	}
	@GetMapping("/dashboard")
	public String dashboard(
	        Model model,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "5") int size,
	        @RequestParam(required = false) String name,
	        @RequestParam(required = false) String status,
	        @RequestParam(required = false) String dateFrom,
	        @RequestParam(required = false) String dateTo) {

	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
	        return "redirect:/admin/login?sessionExpired";
	    }

	    long totalUsers = userRepository.count();
	    long totalProducts = productRepository.count();

	    Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
	    Page<Order> filteredOrdersPage = orderService.getFilteredOrders(pageable, name, status, dateFrom, dateTo);

	    long ordersTodayCount = orderService.countOrdersToday();
	    long pendingPayments = orderService.countPendingPayments();

	    model.addAttribute("pendingPaymentsCount", pendingPayments);
	    model.addAttribute("ordersTodayCount", ordersTodayCount);
	    model.addAttribute("totalUsers", totalUsers);
	    model.addAttribute("totalProducts", totalProducts);
	    model.addAttribute("filteredOrders", filteredOrdersPage.getContent());
	    model.addAttribute("currentPage", page);
	    model.addAttribute("totalPages", filteredOrdersPage.getTotalPages());

	    // Filters to persist
	    model.addAttribute("paramName", name);
	    model.addAttribute("paramStatus", status);
	    model.addAttribute("paramDateFrom", dateFrom);
	    model.addAttribute("paramDateTo", dateTo);

	    return "admin/dashboard";
	}

	@GetMapping("/products")
	public String showProducts(@RequestParam(defaultValue = "0") int page, Model model) {

		int pageSize = 8; // Show 8 products per page

		Page<Product> productPage = productService.getPaginatedProducts(page, pageSize);
		List<Product> products = productPage.getContent();

		Map<Long, String> imageMap = new HashMap<>();
		for (Product p : products) {
		    if (p.getImage() != null) {
		        imageMap.put(p.getId(), Base64.getEncoder().encodeToString(p.getImage()));
		    }
		}

		// ✅ Don't override with all products
		model.addAttribute("products", products);
		model.addAttribute("imageMap", imageMap);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", productPage.getTotalPages());

		return "admin/products"; 
	}
//	
//	@GetMapping("/admin/products/delete/{id}")
//	public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
//	    productService.deleteById(id); // This will now work
//	    redirectAttributes.addFlashAttribute("message", "Product deleted successfully!");
//	    return "redirect:/admin/products";
//	}
	// ProductController.java
	@GetMapping("/products/delete/{id}")
	public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
	    productService.deleteProductById(id);
	    redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
	    return "redirect:/admin/products";
	}

	  
	    // ✅ Show form
	    @GetMapping("/add")
	    public String showAddProductForm(Model model) {
	        model.addAttribute("product", new Product());
	        return "admin/add-product"; // /templates/admin/add-product.html
	    }

	    // ✅ Save product
//	    @PostMapping("/products/add")
//	    public String saveProduct(@ModelAttribute Product product,
//	                              @RequestParam("productImage") MultipartFile productImage) {
//
//	        try {
//	            System.out.println("Image original name: " + productImage.getOriginalFilename());
//	            System.out.println("Image size: " + productImage.getSize());
//	            System.out.println("Image content type: " + productImage.getContentType());
//
//	            if (productImage != null && !productImage.isEmpty()) {
//	                product.setImage(productImage.getBytes());
//	            }
//
//	            productRepository.save(product);
//	            return "redirect:/admin/products?added";
//
//	        } catch (IOException e) {
//	            e.printStackTrace();
//	            return "redirect:/admin/products?error=img";
//	        }
//	    }

	    @PostMapping("/products/add")
	    public String saveProduct(@ModelAttribute Product product,
	                              @RequestParam("productImage") MultipartFile file,
	                              RedirectAttributes redirectAttributes) {

	        // Check if it's an edit
	        if (product.getId() != null) {
	            Product existing = productRepository.findById(product.getId()).orElseThrow(null);
	            product.setImage(existing.getImage()); // Keep old image if not changed
	        }

	        // Save image if provided
	        if (!file.isEmpty()) {
	            try {
	                product.setImage(file.getBytes());
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }

	        productRepository.save(product);
	        redirectAttributes.addFlashAttribute("success", product.getId() != null ? "Product updated successfully!" : "Product added successfully!");
	        return "redirect:/admin/products";
	    }

	    // ✅ Serve image
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
	
	 // GET: Show edit form
	    @GetMapping("/products/edit/{id}")
	    public String showEditForm(@PathVariable Long id, Model model) {
	        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
	        model.addAttribute("product", product);
	        model.addAttribute("isEdit", true); // Flag to show it's edit mode
	        return "admin/add-product"; // Reuse the same form
	    }

	    
	    
		/*
		 * @GetMapping("/orders") public String viewOrders(@RequestParam(defaultValue =
		 * "0") int page, Model model) { int pageSize = 10;
		 * 
		 * Page<Order> orderPage = orderService.getAllOrders(page, pageSize);
		 * 
		 * model.addAttribute("orders", orderPage.getContent());
		 * model.addAttribute("currentPage", page); model.addAttribute("totalPages",
		 * orderPage.getTotalPages());
		 * 
		 * return "admin/orders"; // view name }
		 */
	    @GetMapping("/orders")
	    public String manageOrders(
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "") String status,
	        @RequestParam(defaultValue = "") String keyword,
	        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
	        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
	        Model model
	    ) {
	        int pageSize = 8;

	        Page<Order> orderPage = orderService.searchOrders(
	            status,
	            keyword,
	            fromDate,
	            toDate,
	            page,
	            pageSize
	        );
	     // Convert dates
	        LocalDateTime from = (fromDate != null) ? fromDate.atStartOfDay() : null;
	        LocalDateTime to = (toDate != null) ? toDate.atTime(LocalTime.MAX) : null;

	        // Totals
	        long totalOrders = orderService.getTotalOrderCount(status, keyword, fromDate, toDate);
	        BigDecimal totalRevenue = orderService.getTotalOrderRevenue(status, keyword, fromDate, toDate);

	        // Add to model
	        model.addAttribute("totalOrders", totalOrders);
	        model.addAttribute("totalRevenue", totalRevenue);

	        model.addAttribute("orders", orderPage.getContent());
	        model.addAttribute("currentPage", page);
	        model.addAttribute("totalPages", orderPage.getTotalPages());
	        model.addAttribute("status", status);
	        model.addAttribute("keyword", keyword);
	        model.addAttribute("fromDate", fromDate);
	        model.addAttribute("toDate", toDate);

	        return "admin/orders";
	    }


	    @PostMapping("/orders/updateStatus")
	    public String updateOrderStatus(@RequestParam Long orderId,
	                                    @RequestParam String status,
	                                    RedirectAttributes redirectAttributes) {
	        orderService.updateStatus(orderId, status);
	        redirectAttributes.addFlashAttribute("success", "Order status updated!");
	        return "redirect:/admin/orders";
	    }

	    @GetMapping("/orders/delete/{id}")
	    public String deleteOrder(@PathVariable Long id,
	                              RedirectAttributes redirectAttributes) {
	        orderService.deleteOrderById(id);
	        redirectAttributes.addFlashAttribute("success", "Order deleted!");
	        return "redirect:/admin/orders";
	    }

	/*
	 * @PostMapping("/admin/products/save") public String
	 * saveProduct(@RequestParam("name") String name,
	 * 
	 * @RequestParam("price") Double price,
	 * 
	 * @RequestParam("stock") int stock,
	 * 
	 * @RequestParam("active") boolean active,
	 * 
	 * @RequestParam("image") MultipartFile file, Model model) { try {
	 * productService.saveProduct(name, price, stock, active, file);
	 * model.addAttribute("message", "Product saved successfully!"); } catch
	 * (Exception e) { model.addAttribute("message", "Error saving product!"); }
	 * 
	 * return "redirect:/admin/products"; }
	 * 
	 * @GetMapping("/admin/products/image/{id}")
	 * 
	 * @ResponseBody public ResponseEntity<byte[]> getProductImage(@PathVariable
	 * Long id) { Product product =
	 * productRepository.findById(id).orElseThrow(null); return
	 * ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(product.getImage()
	 * ); }
	 */

}
