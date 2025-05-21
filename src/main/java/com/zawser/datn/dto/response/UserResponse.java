package com.zawser.datn.dto.response;

import java.time.LocalDate;
import java.util.Set;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String username;
    String email;
    String name;
    String phone;
    String address;
    String avatar;
    String gender;
    LocalDate dob;

    Set<RoleResponse> roles;

    String created_by;
    String updated_by;
    LocalDate created_at;
    LocalDate updated_at;
    String status;
}
