package com.example.OTP.Entrance.Backend.Repositories;

import com.example.OTP.Entrance.Backend.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByRegNumber(String regNumber);
    Optional<User> findByIdNumber(String idNumber);
    boolean existsByRegNumber(String regNumber);
}
