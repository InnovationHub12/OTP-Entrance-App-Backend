package com.example.OTP.Entrance.Backend.Services;

import com.example.OTP.Entrance.Backend.Entities.User;
import com.example.OTP.Entrance.Backend.Repositories.UserRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Optional;

@Service
public class OtpService {

    private final UserRepository userRepository;
    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();

    private final CodeVerifier verifier =
            new DefaultCodeVerifier(new DefaultCodeGenerator(HashingAlgorithm.SHA1), new SystemTimeProvider());

    private final QrDataFactory qrDataFactory =
            new QrDataFactory(HashingAlgorithm.SHA1, 6, 30);

    public OtpService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Generate QR code (Base64 PNG) for Google Authenticator
    public String generateQrCodeForUser(String regNumber) {
        User user = userRepository.findByRegNumber(regNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtpSecret() == null) {
            user.setOtpSecret(secretGenerator.generate());
            userRepository.save(user);
        }

        QrData qrData = qrDataFactory.newBuilder()
                .label("OTP-Entrance:" + user.getRegNumber())
                .secret(user.getOtpSecret())
                .issuer("OTP-Entrance")
                .build();

        return generateQrImage(qrData.getUri()); // ✅ plain Base64 string
    }

    public boolean verifyOtp(String regNumber, String otp) {
        User user = userRepository.findByRegNumber(regNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return verifier.isValidCode(user.getOtpSecret(), otp);
    }

    private String generateQrImage(String otpAuthUri) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(
                    otpAuthUri,
                    BarcodeFormat.QR_CODE,
                    250,
                    250
            );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR Code", e);
        }
    }

    public boolean verifyQr(String regNumber) {
        return userRepository.existsByRegNumber(regNumber);
    }

    public Optional<User> findUserByRegNumber(String regNumber) {
        return userRepository.findByRegNumber(regNumber);
    }


}
