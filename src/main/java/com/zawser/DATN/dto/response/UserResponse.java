package com.zawser.DATN.dto.response;

import com.zawser.DATN.entity.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
     String account_id;
     String username;
     String email;
     String requestcode;
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
