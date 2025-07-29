package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.demo.entity.Admin;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;

@SpringBootApplication
public class ECommerceWebAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(ECommerceWebAppApplication.class, args);
	}

	@Bean
	CommandLineRunner run(AdminRepository repo, BCryptPasswordEncoder encoder) {
		return args -> {
			if (repo.findByEmail("admin@shop.com") == null) {
				Admin admin = new Admin();
				admin.setEmail("admin@shop.com");
				admin.setPassword(encoder.encode("admin123"));
				admin.setName("Super Admin");
				repo.save(admin);
			}
		};
	}

	@Bean
	public CommandLineRunner loadData(ProductRepository productRepo) {
		return args -> {
			if (productRepo.count() == 0) {
				Product p1 = new Product();
				p1.setName("Samsung Galaxy S21");
				p1.setDescription("6.2-inch display, 128GB storage");
				p1.setPrice(69999);

				Product p2 = new Product();
				p2.setName("OnePlus 12R");
				p2.setDescription("Snapdragon 8 Gen2, 5000mAh");
				p2.setPrice(42999);

				Product p3 = new Product();
				p3.setName("iPhone 14");
				p3.setDescription("128GB, A15 Bionic Chip");
				p3.setPrice(79999);

				Product p4 = new Product();
				p4.setName("Realme Narzo 60");
				p4.setDescription("Dimensity 6100+, 6GB RAM");
				p4.setPrice(14999);

				// ✅ New Product (p5)
				Product p5 = new Product();
				p5.setName("Google Pixel 7a");
				p5.setDescription("128GB, Tensor G2, 8GB RAM");
				p5.setPrice(43999);

				// Save all products
				productRepo.save(p1);
				productRepo.save(p2);
				productRepo.save(p3);
				productRepo.save(p4);
				productRepo.save(p5);
			}
		};
	}

//		private byte[] loadImageFromURL(String imageUrl) {
//		    try (InputStream in = new URL(imageUrl).openStream();
//		         ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
//		
//		        byte[] data = new byte[1024];
//		        int bytesRead;
//		        while ((bytesRead = in.read(data, 0, data.length)) != -1) {
//		            buffer.write(data, 0, bytesRead);
//		        }
//		
//		        return buffer.toByteArray();
//		    } catch (Exception e) {
//		        e.printStackTrace();
//		        return null;
//		    }
//		}

	@Bean
	CommandLineRunner createSampleUser(UserRepository userRepo, BCryptPasswordEncoder encoder) {
		return args -> {
			if (userRepo.findByEmail("user@example.com") == null) {
				User user = new User();
				user.setName("Sample User");
				user.setEmail("user@example.com");
				user.setPassword(encoder.encode("user123"));
				userRepo.save(user);
			}
		};
	}

}
