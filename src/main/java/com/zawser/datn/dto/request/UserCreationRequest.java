package com.zawser.datn.dto.request;

import java.time.LocalDate;

import com.zawser.datn.validator.DobContraints;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {

    @Size(min = 3, message = "INVALID_USERNAME")
    String username;

    @Size(min = 8, message = "INVALID_PASSWORD")
    String password;


    @Email
    String email;

    String name;

    @DobContraints(min = 18, message = "INVALID_DOB")
    LocalDate dob;

    String phone;
    String address;
    String gender;
}
