package com.logistics.logisticsmanagement.repository;

import com.logistics.logisticsmanagement.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
}