package com.logistics.logisticsmanagement.service;

import com.logistics.logisticsmanagement.entity.Delivery;
import com.logistics.logisticsmanagement.entity.Driver;
import com.logistics.logisticsmanagement.entity.Order;
import com.logistics.logisticsmanagement.entity.Route;
import com.logistics.logisticsmanagement.entity.Vehicle;
import com.logistics.logisticsmanagement.repository.DeliveryRepository;
import com.logistics.logisticsmanagement.repository.DriverRepository;
import com.logistics.logisticsmanagement.repository.OrderRepository;
import com.logistics.logisticsmanagement.repository.RouteRepository;
import com.logistics.logisticsmanagement.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final RouteRepository routeRepository;

    public DeliveryService(
            DeliveryRepository deliveryRepository,
            OrderRepository orderRepository,
            VehicleRepository vehicleRepository,
            DriverRepository driverRepository,
            RouteRepository routeRepository) {

        this.deliveryRepository = deliveryRepository;
        this.orderRepository = orderRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.routeRepository = routeRepository;
    }

    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }

    public Optional<Delivery> getDeliveryById(Long id) {
        return deliveryRepository.findById(id);
    }

    // =========================
    // CREATE DELIVERY
    // =========================

    public Delivery createDelivery(Delivery delivery) {

        validateReferences(delivery);

        if ("In Transit".equalsIgnoreCase(delivery.getStatus())) {
            validateResources(delivery, null);
        }

        Delivery savedDelivery = deliveryRepository.save(delivery);

        synchronizeStatus(savedDelivery);

        return savedDelivery;
    }

    // =========================
    // UPDATE DELIVERY
    // =========================

    public Delivery updateDelivery(
            Long id,
            Delivery deliveryDetails) {

        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Delivery not found"));

        validateReferences(deliveryDetails);

        Long oldVehicleId = delivery.getVehicleId();
        Long oldDriverId = delivery.getDriverId();
        String oldStatus = delivery.getStatus();

        boolean vehicleChanged =
                !equalsLong(oldVehicleId, deliveryDetails.getVehicleId());

        boolean driverChanged =
                !equalsLong(oldDriverId, deliveryDetails.getDriverId());

        boolean becomingInTransit =
                "In Transit".equalsIgnoreCase(
                        deliveryDetails.getStatus());

        /*
         * Whenever the delivery is going to be In Transit,
         * make sure the vehicle and driver are actually available.
         */
        if (becomingInTransit) {
            validateResources(deliveryDetails, id);
        }

        delivery.setOrderId(deliveryDetails.getOrderId());
        delivery.setVehicleId(deliveryDetails.getVehicleId());
        delivery.setDriverId(deliveryDetails.getDriverId());
        delivery.setRouteId(deliveryDetails.getRouteId());
        delivery.setDeliveryDate(deliveryDetails.getDeliveryDate());
        delivery.setStatus(deliveryDetails.getStatus());

        Delivery updatedDelivery = deliveryRepository.save(delivery);

        /*
         * If the old delivery was In Transit and its
         * vehicle/driver changed, release the old resources.
         */
        if ("In Transit".equalsIgnoreCase(oldStatus)) {

            if (vehicleChanged ||
                    !equalsLong(
                            oldVehicleId,
                            updatedDelivery.getVehicleId())) {

                updateVehicleStatus(oldVehicleId, "Available");
            }

            if (driverChanged ||
                    !equalsLong(
                            oldDriverId,
                            updatedDelivery.getDriverId())) {

                updateDriverStatus(oldDriverId, "Available");
            }
        }

        synchronizeStatus(updatedDelivery);

        return updatedDelivery;
    }

    // =========================
    // VALIDATE REFERENCES
    // =========================

    private void validateReferences(Delivery delivery) {

        if (delivery.getOrderId() == null) {
            throw new RuntimeException("Order is required");
        }

        if (delivery.getVehicleId() == null) {
            throw new RuntimeException("Vehicle is required");
        }

        if (delivery.getDriverId() == null) {
            throw new RuntimeException("Driver is required");
        }

        if (delivery.getRouteId() == null) {
            throw new RuntimeException("Route is required");
        }

        if (!orderRepository.existsById(delivery.getOrderId())) {
            throw new RuntimeException("Selected order does not exist");
        }

        if (!vehicleRepository.existsById(delivery.getVehicleId())) {
            throw new RuntimeException("Selected vehicle does not exist");
        }

        if (!driverRepository.existsById(delivery.getDriverId())) {
            throw new RuntimeException("Selected driver does not exist");
        }

        if (!routeRepository.existsById(delivery.getRouteId())) {
            throw new RuntimeException("Selected route does not exist");
        }
    }

    // =========================
    // VALIDATE VEHICLE + DRIVER
    // =========================

    private void validateResources(
            Delivery delivery,
            Long currentDeliveryId) {

        Optional<Vehicle> vehicleOptional =
                vehicleRepository.findById(
                        delivery.getVehicleId());

        if (vehicleOptional.isEmpty()) {
            throw new RuntimeException("Vehicle not found");
        }

        Vehicle vehicle = vehicleOptional.get();

        if (!"Available".equalsIgnoreCase(vehicle.getStatus())) {
            throw new RuntimeException(
                    "Vehicle is not available");
        }

        /*
         * Extra protection:
         * check existing active deliveries too.
         */
        boolean vehicleAlreadyAssigned =
                deliveryRepository.findAll()
                        .stream()
                        .anyMatch(existing ->

                                !equalsLong(
                                        existing.getId(),
                                        currentDeliveryId)

                                && equalsLong(
                                        existing.getVehicleId(),
                                        delivery.getVehicleId())

                                && "In Transit".equalsIgnoreCase(
                                        existing.getStatus())
                        );

        if (vehicleAlreadyAssigned) {
            throw new RuntimeException(
                    "Vehicle is already assigned to another active delivery");
        }

        Optional<Driver> driverOptional =
                driverRepository.findById(
                        delivery.getDriverId());

        if (driverOptional.isEmpty()) {
            throw new RuntimeException("Driver not found");
        }

        Driver driver = driverOptional.get();

        if (!"Available".equalsIgnoreCase(driver.getStatus())) {
            throw new RuntimeException(
                    "Driver is not available");
        }

        boolean driverAlreadyAssigned =
                deliveryRepository.findAll()
                        .stream()
                        .anyMatch(existing ->

                                !equalsLong(
                                        existing.getId(),
                                        currentDeliveryId)

                                && equalsLong(
                                        existing.getDriverId(),
                                        delivery.getDriverId())

                                && "In Transit".equalsIgnoreCase(
                                        existing.getStatus())
                        );

        if (driverAlreadyAssigned) {
            throw new RuntimeException(
                    "Driver is already assigned to another active delivery");
        }
    }

    // =========================
    // SYNCHRONIZE STATUS
    // =========================

    private void synchronizeStatus(Delivery delivery) {

        String status = delivery.getStatus();

        // Synchronize order
        updateOrderStatus(
                delivery.getOrderId(),
                status
        );

        if ("In Transit".equalsIgnoreCase(status)) {

            updateVehicleStatus(
                    delivery.getVehicleId(),
                    "In Use"
            );

            updateDriverStatus(
                    delivery.getDriverId(),
                    "Assigned"
            );

        } else if ("Delivered".equalsIgnoreCase(status)) {

            updateVehicleStatus(
                    delivery.getVehicleId(),
                    "Available"
            );

            updateDriverStatus(
                    delivery.getDriverId(),
                    "Available"
            );

        } else if ("Pending".equalsIgnoreCase(status)) {

            updateVehicleStatus(
                    delivery.getVehicleId(),
                    "Available"
            );

            updateDriverStatus(
                    delivery.getDriverId(),
                    "Available"
            );
        }
    }

    // =========================
    // UPDATE ORDER
    // =========================

    private void updateOrderStatus(
            Long orderId,
            String status) {

        if (orderId == null || status == null) {
            return;
        }

        Optional<Order> orderOptional =
                orderRepository.findById(orderId);

        if (orderOptional.isPresent()) {

            Order order = orderOptional.get();

            order.setStatus(status);

            orderRepository.save(order);
        }
    }

    // =========================
    // UPDATE VEHICLE
    // =========================

    private void updateVehicleStatus(
            Long vehicleId,
            String status) {

        if (vehicleId == null) {
            return;
        }

        Optional<Vehicle> vehicleOptional =
                vehicleRepository.findById(vehicleId);

        if (vehicleOptional.isPresent()) {

            Vehicle vehicle = vehicleOptional.get();

            vehicle.setStatus(status);

            vehicleRepository.save(vehicle);
        }
    }

    // =========================
    // UPDATE DRIVER
    // =========================

    private void updateDriverStatus(
            Long driverId,
            String status) {

        if (driverId == null) {
            return;
        }

        Optional<Driver> driverOptional =
                driverRepository.findById(driverId);

        if (driverOptional.isPresent()) {

            Driver driver = driverOptional.get();

            driver.setStatus(status);

            driverRepository.save(driver);
        }
    }

    // =========================
    // DELETE DELIVERY
    // =========================

    public void deleteDelivery(Long id) {

        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Delivery not found"));

        /*
         * If an active delivery is deleted,
         * release its vehicle and driver.
         */
        if ("In Transit".equalsIgnoreCase(
                delivery.getStatus())) {

            updateVehicleStatus(
                    delivery.getVehicleId(),
                    "Available"
            );

            updateDriverStatus(
                    delivery.getDriverId(),
                    "Available"
            );

            /*
             * Put the order back to Pending
             * if there is no other active delivery
             * for that order.
             */
            boolean anotherActiveDelivery =
                    deliveryRepository.findAll()
                            .stream()
                            .anyMatch(existing ->

                                    !equalsLong(
                                            existing.getId(),
                                            delivery.getId())

                                    && equalsLong(
                                            existing.getOrderId(),
                                            delivery.getOrderId())

                                    && "In Transit".equalsIgnoreCase(
                                            existing.getStatus())
                            );

            if (!anotherActiveDelivery) {
                updateOrderStatus(
                        delivery.getOrderId(),
                        "Pending"
                );
            }
        }

        deliveryRepository.deleteById(id);
    }

    // =========================
    // SAFE LONG COMPARISON
    // =========================

    private boolean equalsLong(Long first, Long second) {

        if (first == null && second == null) {
            return true;
        }

        if (first == null || second == null) {
            return false;
        }

        return first.equals(second);
    }
}