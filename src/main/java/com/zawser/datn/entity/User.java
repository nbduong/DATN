package com.zawser.datn.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class User {
    //  Random id để không bị scan
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String username;
    String password;

    @Column(nullable = false, unique = true)
    String email;
    String name;
    String phone;
    String address;
    String gender;
    LocalDate dob;

    @ManyToMany
    Set<Role> roles;

    String status;

    Boolean isDeleted;

    String createdBy;
    LocalDate createdDate;
    String lastModifiedBy;
    LocalDate lastModifiedDate;

     String resetPasswordToken;
     LocalDateTime resetPasswordTokenExpiry;

}
