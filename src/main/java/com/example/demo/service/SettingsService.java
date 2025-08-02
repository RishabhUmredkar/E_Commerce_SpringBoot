package com.example.demo.service;

import com.example.demo.entity.Settings;
import com.example.demo.entity.SettingsForm;
import com.example.demo.repository.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    @Autowired
    private SettingsRepository settingsRepository;  // Assuming you have a repository to handle DB operations

    public SettingsForm loadSettings() {
        Settings settings = settingsRepository.findLatestSettings();
        
        if (settings == null) {
            settings = new Settings(); // Initialize with default values or empty values
        }
        
        SettingsForm settingsForm = new SettingsForm();
        settingsForm.setStoreName(settings.getStoreName());
        settingsForm.setStoreAddress(settings.getStoreAddress());
        settingsForm.setStorePhone(settings.getStorePhone());
        settingsForm.setRazorpayKey(settings.getRazorpayKey());
        settingsForm.setRazorpaySecret(settings.getRazorpaySecret());
        settingsForm.setSmtpHost(settings.getSmtpHost());
        settingsForm.setSmtpPort(settings.getSmtpPort());
        settingsForm.setSmtpUser(settings.getSmtpUser());
        settingsForm.setSmtpPassword(settings.getSmtpPassword());

        return settingsForm;
    }


    public void saveSettings(SettingsForm settingsForm) {
        // Map settings form to entity and save it
        Settings settings = new Settings();
        settings.setStoreName(settingsForm.getStoreName());
        settings.setStoreAddress(settingsForm.getStoreAddress());
        settings.setStorePhone(settingsForm.getStorePhone());
        settings.setRazorpayKey(settingsForm.getRazorpayKey());
        settings.setRazorpaySecret(settingsForm.getRazorpaySecret());
        settings.setSmtpHost(settingsForm.getSmtpHost());
        settings.setSmtpPort(settingsForm.getSmtpPort());
        settings.setSmtpUser(settingsForm.getSmtpUser());
        settings.setSmtpPassword(settingsForm.getSmtpPassword());
        
        settingsRepository.save(settings);  // Save to the DB
    }
    
}
