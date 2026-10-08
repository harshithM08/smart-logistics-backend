package com.logistics.logisticsmanagement.repository;

import com.logistics.logisticsmanagement.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
}