package com.zawser.DATN.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class User {
    //  Random id để không bị scan
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String account_id;

    String username;
    String password;
    String email;
    String requestcode;
    String name;
    String phone;
    String address;
    String avatar;
    String gender;
    LocalDate dob;

    @ManyToMany
    Set<Role> roles;



    String created_by;
    String updated_by;
    LocalDate created_at;
    LocalDate updated_at;
    String status;
}
