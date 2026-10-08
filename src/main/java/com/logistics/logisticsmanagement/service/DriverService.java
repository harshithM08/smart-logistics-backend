package com.logistics.logisticsmanagement.service;

import com.logistics.logisticsmanagement.entity.Driver;
import com.logistics.logisticsmanagement.repository.DriverRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    public Optional<Driver> getDriverById(Long id) {
        return driverRepository.findById(id);
    }

    public Driver createDriver(Driver driver) {
        return driverRepository.save(driver);
    }

    public Driver updateDriver(Long id, Driver driverDetails) {

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        driver.setName(driverDetails.getName());
        driver.setPhone(driverDetails.getPhone());
        driver.setLicenseNumber(driverDetails.getLicenseNumber());
        driver.setStatus(driverDetails.getStatus());

        return driverRepository.save(driver);
    }

    public void deleteDriver(Long id) {
        driverRepository.deleteById(id);
    }
}