package com.alertaenchentes.repository;

import com.alertaenchentes.model.WeatherReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeatherReadingRepository extends JpaRepository<WeatherReading, Long> {

    Optional<WeatherReading> findFirstByOrderByFetchedAtDescIdDesc();
}
