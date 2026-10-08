package com.logistics.logisticsmanagement.controller;

import com.logistics.logisticsmanagement.entity.Delivery;
import com.logistics.logisticsmanagement.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deliveries")
@CrossOrigin(origins = "http://localhost:5173")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    // =========================
    // GET ALL DELIVERIES
    // =========================

    @GetMapping
    public List<Delivery> getAllDeliveries() {
        return deliveryService.getAllDeliveries();
    }

    // =========================
    // GET DELIVERY BY ID
    // =========================

    @GetMapping("/{id}")
    public ResponseEntity<Delivery> getDeliveryById(
            @PathVariable Long id) {

        return deliveryService.getDeliveryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================
    // CREATE DELIVERY
    // =========================

    @PostMapping
    public ResponseEntity<?> createDelivery(
            @RequestBody Delivery delivery) {

        try {

            return ResponseEntity.ok(
                    deliveryService.createDelivery(delivery)
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }

    // =========================
    // UPDATE DELIVERY
    // =========================

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDelivery(
            @PathVariable Long id,
            @RequestBody Delivery delivery) {

        try {

            return ResponseEntity.ok(
                    deliveryService.updateDelivery(
                            id,
                            delivery
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }

    // =========================
    // DELETE DELIVERY
    // =========================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDelivery(
            @PathVariable Long id) {

        try {

            deliveryService.deleteDelivery(id);

            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }
}