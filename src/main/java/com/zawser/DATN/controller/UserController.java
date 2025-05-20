package com.zawser.DATN.controller;

import java.util.List;

import com.zawser.DATN.dto.request.ApiResponse;
import com.zawser.DATN.dto.request.UserCreationRequest;
import com.zawser.DATN.dto.request.UserUpdateRequest;
import com.zawser.DATN.dto.response.UserResponse;
import com.zawser.DATN.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<UserResponse>> getUsers() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info("Username: {}", authentication.getName());
        authentication.getAuthorities().forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));

        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getUsers())
                .build();
    }

    @GetMapping("/{account_id}")
    ApiResponse<UserResponse> getUser(@PathVariable("account_id") String userId) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUser(userId))
                .build();
    }

    @GetMapping("/myInfo")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @DeleteMapping("/{account_id}")
    ApiResponse<String> deleteUser(@PathVariable String account_id) {
        userService.deleteUser(account_id);
        return ApiResponse.<String>builder().result("User has been deleted").build();
    }

    @PutMapping("/{account_id}")
    ApiResponse<UserResponse> updateUser(@PathVariable String account_id, @RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(account_id, request))
                .build();
    }
}
