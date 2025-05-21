package com.zawser.datn.dto.request;

import java.time.LocalDate;
import java.util.List;

import com.zawser.datn.validator.DobContraints;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {

    @Size(min = 8, message = "INVALID_PASSWORD")
    String password;

    @Email
    String email;

    String name;

    @DobContraints(min = 13, message = "INVALID_DOB")
    LocalDate dob;

    String phone;
    String address;
    String gender;

    List<String> roles;
}
