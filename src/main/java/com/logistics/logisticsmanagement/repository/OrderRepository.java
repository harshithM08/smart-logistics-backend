package com.logistics.logisticsmanagement.repository;

import com.logistics.logisticsmanagement.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}