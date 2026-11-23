package com.smartfreight.tmsapi.repository;

import com.smartfreight.tmsapi.model.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarrierRepository extends JpaRepository<Carrier, String> {
    List<Carrier> findByActive(int active);
    Optional<Carrier> findByCodeIgnoreCase(String code);
    List<Carrier> findByNameContainingIgnoreCase(String name);
}
