package com.example.OTP.Entrance.Backend.Controllers;

import com.example.OTP.Entrance.Backend.Entities.User;
import com.example.OTP.Entrance.Backend.Entities.VehicleLogEntry;
import com.example.OTP.Entrance.Backend.Repositories.UserRepository;
import com.example.OTP.Entrance.Backend.Repositories.VehicleLogEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicle-log")
@CrossOrigin(origins = "http://localhost:4200")
public class VehicleLogEntryController {

    @Autowired
    private VehicleLogEntryRepository repository;

    @Autowired
    private UserRepository userRepository;


    @PostMapping("/entry/{idNumber}")
    public ResponseEntity<?> logEntry(@PathVariable String idNumber,
                                      @RequestBody VehicleLogEntry entry) {
        return userRepository.findByIdNumber(idNumber).map(user -> {
            entry.setUser(user);
            entry.setEntryDate(LocalDate.now());
            entry.setEntryTime(LocalTime.now());
            VehicleLogEntry saved = repository.save(entry);

            return ResponseEntity.ok(Map.of(
                    "entryId", saved.getId(),
                    "idNumber", user.getIdNumber(),
                    "registrationNumber", user.getRegNumber(),
                    "entryDate", saved.getEntryDate(),
                    "entryTime", saved.getEntryTime()
            ));
        }).orElse(ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "User not found"
        )));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<VehicleLogEntry> editExitAndRegistration(
            @PathVariable Long id,
            @RequestBody VehicleLogEntry updateData) {

        VehicleLogEntry entry = repository.findById(id).orElseThrow();

        if (updateData.getExitTime() != null) {
            entry.setExitTime(updateData.getExitTime());
        } else {
            entry.setExitTime(LocalTime.now());
        }

        if (updateData.getRegistrationNumber() != null) {
            entry.setRegistrationNumber(updateData.getRegistrationNumber());
        }

        return ResponseEntity.ok(repository.save(entry));
    }

    @GetMapping("/today")
    public List<VehicleLogEntry> getTodayLogs() {

        return repository.findByEntryDate(LocalDate.now());
    }

    @GetMapping("/user/{idNumber}")
    public ResponseEntity<List<VehicleLogEntry>> getLogsForUser(@PathVariable String idNumber) {
        return userRepository.findByIdNumber(idNumber)
                .map(user -> ResponseEntity.ok(repository.findByUser(user)))
                .orElse(ResponseEntity.badRequest().body(List.of()));
    }



}
