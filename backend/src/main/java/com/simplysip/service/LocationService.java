package com.simplysip.service;

import com.simplysip.dto.CreateLocationRequest;
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

    public Optional<Location> findById(Long id) {
        return locationRepository.findById(id);
    }

    public Location create(CreateLocationRequest request) {
        Location location = new Location();
        apply(location, request);
        return locationRepository.save(location);
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
    }
}
