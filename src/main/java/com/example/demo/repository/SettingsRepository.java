package com.example.demo.repository;


import com.example.demo.entity.Settings;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingsRepository extends JpaRepository<Settings, Long> {
    // You can add custom queries if needed, for example:
    // Optional<Settings> findByStoreName(String storeName);
	 @Query("SELECT s FROM Settings s ORDER BY s.createdDate DESC")
	    Settings findLatestSettings();
	 
	 @Query("SELECT s FROM Settings s ORDER BY s.createdDate DESC")
	 List<Settings> findAllSettings();

}
