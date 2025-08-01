package com.example.demo.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/razorpay")
public class RazorpayController {

    @Value("${razorpay.key}")
    private String razorpayKey;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    @PostMapping("/createOrder")
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> data) throws Exception {
        int amount = (int) data.get("amount"); // amount in paise

        RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);

        JSONObject options = new JSONObject();
        options.put("amount", amount);  // amount in paise
        options.put("currency", "INR");
        options.put("payment_capture", 1);

        Order order = client.Orders.create(options);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.get("id"));
        response.put("amount", order.get("amount"));
        response.put("key", razorpayKey);

        return response;
    }
}
