package com.example.OTP.Entrance.Backend.Entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "id_number", nullable = false, unique = true)
    private Long idNumber;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String regNumber;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;
}
