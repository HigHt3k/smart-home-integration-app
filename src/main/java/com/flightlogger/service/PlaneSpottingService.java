package com.flightlogger.service;

import com.flightlogger.model.planespotting.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PlaneSpottingService {

    private final AircraftRepository aircraftRepository;
    private final SightingRepository sightingRepository;
    private final Logger logger = LoggerFactory.getLogger(PlaneSpottingService.class);

    @Autowired
    public PlaneSpottingService(AircraftRepository aircraftRepository, SightingRepository sightingRepository) {
        this.aircraftRepository = aircraftRepository;
        this.sightingRepository = sightingRepository;
    }

    @Autowired
    SightingImageRepository sightingImageRepository;

    public List<Sighting> getSightings() {
        return sightingRepository.findAll();
    }

    public List<SightingImage> getSightingImages() {
        return sightingImageRepository.findAll();
    }

    public void addSighting(Sighting sighting) {
        sightingRepository.save(sighting);
    }

    public void addSightingImage(SightingImage sightingImage) {
        sightingImageRepository.save(sightingImage);
    }

    public void removedSighting(Integer id) {
        Optional<Sighting> sighting = Objects.requireNonNull(sightingRepository.findById(id));
        sighting.ifPresent(value -> sightingRepository.delete(value));
    }

    public void addSightings(List<Sighting> sightings) {
        for(Sighting sighting : sightings) {
            String registration = sighting.getAircraftRegistration();
            Optional<Aircraft> aircraft = aircraftRepository.findByAircraftRegistration(registration);
            aircraft.ifPresent(value -> sighting.setAircraftType(value.getAircraftType()));
        }
        sightingRepository.saveAll(sightings);
    }

    public Optional<Sighting> getSightingById(Integer sightingId) {
        return sightingRepository.findById(sightingId);
    }

    public List<String> findImagePathsBySightingId(Integer sightingId) {
        List<String> imagePaths = new ArrayList<>();
        Optional<Sighting> sighting = sightingRepository.findById(sightingId);
        if(sighting.isPresent()) {
            Optional<List<SightingImage>> sightingImages =
                    sightingImageRepository.findBySighting(sighting.get());
            if(sightingImages.isPresent()) {
                for(SightingImage sightingImage : sightingImages.get()) {
                    imagePaths.add(sightingImage.getImagePath());
                }
            }
        }
        return imagePaths;
    }

    public List<Sighting> getAllSightings() {
        return sightingRepository.findAll();
    }

    public List<Aircraft> getAllAircrafts() {
        return aircraftRepository.findAll();
    }

    public List<String> getAllOperators() {
        return aircraftRepository.findDistinctOperators();
    }

    public String findOperatorOfAircraft(String aircraftRegistration) {
        if(aircraftRepository.findByAircraftRegistration(aircraftRegistration).isPresent()) {
            return aircraftRepository.findByAircraftRegistration(aircraftRegistration).get().getOperator();
        }
        return "";
    }

    public Aircraft findAircraftByRegistration(String aircraftRegistration) {
        Optional<Aircraft> aircraft = aircraftRepository.findByAircraftRegistration(aircraftRegistration);
        return aircraft.orElse(null);
    }
}
