package com.example.OTP.Entrance.Backend.Controllers;

import com.example.OTP.Entrance.Backend.Entities.User;
import com.example.OTP.Entrance.Backend.Repositories.UserRepository;
import com.example.OTP.Entrance.Backend.Services.OtpService;
import jakarta.servlet.http.HttpServletRequest;
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

    @Autowired
    private OtpService otpService;

    // ✅ LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String idNumber = request.get("idNumber");   // keep as string
        String password = request.get("password");

        return userRepository.findByIdNumber(idNumber)
                .map(user -> {
                    if (user.getPassword().equals(password)) {
                        String qrCodeBase64 = otpService.generateQrCodeForUser(user.getRegNumber());

                        return ResponseEntity.ok(Map.of(
                                "success", true,
                                "message", "Login successful",
                                "role", user.getRole(),
                                "name", user.getName(),
                                "regNumber", user.getRegNumber(),
                                "idNumber", user.getIdNumber(),
                                "qrCode", qrCodeBase64
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

    // ✅ UPDATE REG NUMBER
    @PutMapping("/updateRegNumber")
    public ResponseEntity<?> updateRegNumber(@RequestBody Map<String, String> request) {
        String idNumber = request.get("idNumber");
        String newRegNumber = request.get("regNumber");

        return userRepository.findByIdNumber(idNumber)
                .map(user -> {
                    user.setRegNumber(newRegNumber);
                    userRepository.save(user);

                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Registration number updated successfully",
                            "regNumber", user.getRegNumber()
                    ));
                })
                .orElse(ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "User not found"
                )));
    }

    // ✅ LOGOUT
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logout successful"
        ));
    }

    // ✅ FORGOT PASSWORD (send OTP)
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String idNumber = request.get("idNumber");

        return userRepository.findByIdNumber(idNumber)
                .map(user -> {
                    String otp = String.valueOf((int)(Math.random() * 900000) + 100000);
                    user.setOtpSecret(otp);
                    userRepository.save(user);

                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "OTP sent successfully",
                            "otp", otp // ⚠️ return only for testing; in production send via email/SMS
                    ));
                })
                .orElse(ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "User not found"
                )));
    }

    // ✅ RESET PASSWORD WITH OTP
    @PostMapping("/reset-password-with-otp")
    public ResponseEntity<?> resetPasswordWithOtp(@RequestBody Map<String, String> request) {
        String idNumber = request.get("idNumber");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        return userRepository.findByIdNumber(idNumber)
                .filter(user -> user.getOtpSecret() != null && user.getOtpSecret().equals(otp))
                .map(user -> {
                    user.setPassword(newPassword); // ⚠️ hash with BCrypt in production
                    user.setOtpSecret(null);
                    userRepository.save(user);

                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Password reset successful"
                    ));
                })
                .orElse(ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Invalid OTP"
                )));
    }

    // ✅ REGISTER
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            String idNumber = request.get("idNumber");
            String name = request.get("name");
            String regNumber = request.get("regNumber");
            String password = request.get("password");
            String role = request.get("role");

            if (userRepository.findByIdNumber(idNumber).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "User already exists"
                ));
            }

            User newUser = User.builder()
                    .idNumber(idNumber)
                    .name(name)
                    .regNumber(regNumber)
                    .password(password) // ⚠️ hash with BCrypt in production
                    .role(role)
                    .build();

            userRepository.save(newUser);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User registered successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Registration failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/login-with-id")
    public ResponseEntity<?> loginWithId(@RequestBody Map<String, String> request) {
        String idNumber = request.get("idNumber");

        return userRepository.findByIdNumber(idNumber)
                .map(user -> {
                    String qrCodeBase64 = otpService.generateQrCodeForUser(user.getRegNumber());

                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Login successful",
                            "role", user.getRole(),
                            "name", user.getName(),
                            "regNumber", user.getRegNumber(),
                            "idNumber", user.getIdNumber(),
                            "qrCode", qrCodeBase64
                    ));
                })
                .orElse(ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "User not found"
                )));
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            return ResponseEntity.ok(userRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to fetch users: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{idNumber}")
    public ResponseEntity<?> deleteUser(@PathVariable String idNumber) {
        return userRepository.findByIdNumber(idNumber)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "User deleted successfully"
                    ));
                })
                .orElse(ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "User not found"
                )));
    }
}
