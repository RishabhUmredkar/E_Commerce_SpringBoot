package com.example.demo.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Settings {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // This could be auto-generated if needed

    private String storeName;
    private String storeAddress;
    private String storePhone;
    private String razorpayKey;
    private String razorpaySecret;
    private String smtpHost;
    private int smtpPort;
    private String smtpUser;
    private String smtpPassword;
   
    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
    }


    @Column(name = "created_date")
    private LocalDateTime createdDate;

    // Getters and setters
    
    
    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(LocalDateTime createdDate) {
		this.createdDate = createdDate;
	}

	public void setId(Long id) {
        this.id = id;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
    }

    public String getStorePhone() {
        return storePhone;
    }

    public void setStorePhone(String storePhone) {
        this.storePhone = storePhone;
    }

    public String getRazorpayKey() {
        return razorpayKey;
    }

    public void setRazorpayKey(String razorpayKey) {
        this.razorpayKey = razorpayKey;
    }

    public String getRazorpaySecret() {
        return razorpaySecret;
    }

    public void setRazorpaySecret(String razorpaySecret) {
        this.razorpaySecret = razorpaySecret;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getSmtpUser() {
        return smtpUser;
    }

    public void setSmtpUser(String smtpUser) {
        this.smtpUser = smtpUser;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }
}
