package com.example.demo.service;

import com.example.demo.entity.Product;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImageService {

    public Map<Long, String> getBase64ImagesForProducts(List<Product> products) {
        Map<Long, String> imageMap = new HashMap<>();

        for (Product product : products) {
            byte[] imageBytes = product.getImage(); // Make sure your Product entity has getImage()
            if (imageBytes != null && imageBytes.length > 0) {
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                imageMap.put(product.getId(), base64Image);
            }
        }

        return imageMap;
    }
}
