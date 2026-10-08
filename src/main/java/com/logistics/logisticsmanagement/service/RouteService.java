package com.logistics.logisticsmanagement.service;

import com.logistics.logisticsmanagement.entity.Route;
import com.logistics.logisticsmanagement.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RouteService {

    private final RouteRepository routeRepository;

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public Optional<Route> getRouteById(Long id) {
        return routeRepository.findById(id);
    }

    public Route createRoute(Route route) {
        return routeRepository.save(route);
    }

    public Route updateRoute(Long id, Route routeDetails) {

        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        route.setSource(routeDetails.getSource());
        route.setDestination(routeDetails.getDestination());
        route.setDistance(routeDetails.getDistance());
        route.setEstimatedTime(routeDetails.getEstimatedTime());

        return routeRepository.save(route);
    }

    public void deleteRoute(Long id) {
        routeRepository.deleteById(id);
    }
}