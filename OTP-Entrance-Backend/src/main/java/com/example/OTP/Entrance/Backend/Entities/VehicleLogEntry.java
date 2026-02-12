package com.example.OTP.Entrance.Backend.Entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "vehicle_log_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String registrationNumber;
    private LocalDate entryDate;
    private LocalTime entryTime;
    private LocalTime exitTime;

    @ManyToOne
    @JoinColumn(name = "user_id_number", referencedColumnName = "id_number", nullable = false)
    @JsonIgnoreProperties({"password"})
    private User user;
}
