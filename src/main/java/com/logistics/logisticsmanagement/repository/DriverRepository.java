package com.logistics.logisticsmanagement.repository;

import com.logistics.logisticsmanagement.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRepository extends JpaRepository<Driver, Long> {
}