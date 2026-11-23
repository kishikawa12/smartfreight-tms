package com.smartfreight.tmsapi.repository;

import com.smartfreight.tmsapi.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, String> {

    // Always eagerly fetch the carrier so route search can filter by mode without N+1
    @Query("SELECT r FROM Route r JOIN FETCH r.carrier")
    List<Route> findAllWithCarrier();
}
