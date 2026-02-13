package com.example.OTP.Entrance.Backend.Controllers;

import com.example.OTP.Entrance.Backend.Services.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @GetMapping("/qr")
    public ResponseEntity<Map<String, String>> getQrCode(@RequestParam String regNumber) throws Exception {
        String qrCodeUrl = otpService.generateQrCodeForUser(regNumber);
        return ResponseEntity.ok(Map.of("qrCodeUrl", qrCodeUrl));
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, String> body) {
        String regNumber = body.get("regNumber");
        String otp = body.get("otp");

        boolean isValid = otpService.verifyOtp(regNumber, otp);

        return ResponseEntity.ok(Map.of(
                "success", isValid,
                "message", isValid ? "OTP verified successfully" : "Invalid OTP"
        ));
    }

    @PostMapping("/scan-verify")
    public ResponseEntity<Map<String, Object>> scanAndVerify(@RequestBody Map<String, String> body) {
        String regNumber = body.get("regNumber");

        return otpService.findUserByRegNumber(regNumber)
                .map(user -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "User verified successfully",
                        "user", Map.of(
                                "idNumber", user.getIdNumber(),
                                "regNumber", user.getRegNumber(),
                                "name", user.getName(),
                                "role", user.getRole()
                        )
                )))
                .orElse(ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Verification failed"
                )));
    }

}

