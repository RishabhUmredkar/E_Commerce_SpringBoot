package com.example.demo.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Product;
import com.example.demo.repository.ProductRepository;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepo;

    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }
    
  
    public void saveProduct(String name, Double price, int stock, boolean active, MultipartFile file) throws Exception {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        product.setActive(active);
        product.setImage(file.getBytes());

        productRepo.save(product);
    }

//    public Page<Product> getPaginatedProducts(int page, int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        return productRepo.findAll(pageable);
//    }
   
    public Page<Product> getPaginatedProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return productRepo.findAll(pageable);
    }

//
//    public Page<Product> searchPaginatedProducts(String keyword, int page, int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        return productRepo.findByNameContainingIgnoreCase(keyword, pageable);
//    }

 // ProductService.java
    public void deleteProductById(Long id) {
    	productRepo.deleteById(id);
    }

}