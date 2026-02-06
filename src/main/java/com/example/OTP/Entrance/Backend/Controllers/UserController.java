package com.example.OTP.Entrance.Backend.Controllers;

import com.example.OTP.Entrance.Backend.Entities.User;
import com.example.OTP.Entrance.Backend.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // --- Register a new user ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User request) {
        if (userRepository.existsByIdNumber(request.getIdNumber())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "ID Number already exists"));
        }
        if (userRepository.existsByRegNumber(request.getRegNumber())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Registration number already exists"));
        }

        userRepository.save(request);

        return ResponseEntity.ok(Map.of("success", true, "message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String idNumber = request.get("idNumber");
        String password = request.get("password");

        return userRepository.findById(Long.valueOf(idNumber))
                .map(user -> {
                    if (user.getPassword().equals(password)) {
                        return ResponseEntity.ok(Map.of(
                                "success", true,
                                "message", "Login successful",
                                "role", user.getRole(),
                                "name", user.getName()
                        ));
                    } else {
                        return ResponseEntity.badRequest().body(Map.of(
                                "success", false,
                                "message", "Invalid password"
                        ));
                    }
                })
                .orElse(ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "User not found"
                )));
    }


}
