package com.logistics.logisticsmanagement.repository;

import com.logistics.logisticsmanagement.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
}