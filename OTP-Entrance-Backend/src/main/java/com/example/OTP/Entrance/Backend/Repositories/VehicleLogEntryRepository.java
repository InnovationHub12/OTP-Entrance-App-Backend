package com.example.OTP.Entrance.Backend.Repositories;

import com.example.OTP.Entrance.Backend.Entities.VehicleLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface VehicleLogEntryRepository extends JpaRepository<VehicleLogEntry, Long> {
    List<VehicleLogEntry> findByEntryDate(LocalDate date);
}

