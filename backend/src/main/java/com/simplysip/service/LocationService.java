package com.simplysip.service;

import com.simplysip.dto.CreateLocationRequest;
import com.simplysip.dto.LocationResponse;
import com.simplysip.model.Location;
import com.simplysip.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public List<Location> findAll() {
        return locationRepository.findAll();
    }

    public List<LocationResponse> findAllResponses() {
        return locationRepository.findAll().stream().map(LocationResponse::new).toList();
    }

    public Optional<Location> findById(Long id) {
        return locationRepository.findById(id);
    }

    public Optional<LocationResponse> findResponseById(Long id) {
        return locationRepository.findById(id).map(LocationResponse::new);
    }

    public Location create(CreateLocationRequest request) {
        Location location = new Location();
        apply(location, request);
        return locationRepository.save(location);
    }

    public LocationResponse createResponse(CreateLocationRequest request) {
        return new LocationResponse(create(request));
    }

    public Optional<Location> update(Long id, Location updates) {
        return locationRepository.findById(id).map(existing -> {
            existing.setCafeName(updates.getCafeName());
            existing.setLocationName(updates.getLocationName());
            existing.setStreetAddress(updates.getStreetAddress());
            existing.setCity(updates.getCity());
            existing.setState(updates.getState());
            existing.setZip(updates.getZip());
            existing.setLatitude(updates.getLatitude());
            existing.setLongitude(updates.getLongitude());
            existing.setImageUrl(updates.getImageUrl());
            return locationRepository.save(existing);
        });
    }

    public boolean delete(Long id) {
        if (!locationRepository.existsById(id)) {
            return false;
        }
        locationRepository.deleteById(id);
        return true;
    }

    private static void apply(Location location, CreateLocationRequest request) {
        location.setCafeName(request.getCafeName());
        location.setLocationName(request.getLocationName());
        location.setStreetAddress(request.getStreetAddress());
        location.setCity(request.getCity());
        location.setState(request.getState());
        location.setZip(request.getZip());
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setImageUrl(request.getImageUrl());
    }
}
