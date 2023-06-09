package com.flightlogger.model.planespotting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SightingImageRepository extends JpaRepository<SightingImage, Integer> {

    List<SightingImage> findAll();

    Optional<SightingImage> findById(Integer id);

    Optional<List<SightingImage>> findBySighting(Sighting sighting);
}
