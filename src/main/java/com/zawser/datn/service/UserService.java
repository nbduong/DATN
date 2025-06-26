package com.zawser.datn.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import com.zawser.datn.dto.request.UserCreationRequest;
import com.zawser.datn.dto.request.UserUpdateRequest;
import com.zawser.datn.dto.response.UserResponse;
import com.zawser.datn.entity.User;
import com.zawser.datn.enums.Role;
import com.zawser.datn.exception.AppException;
import com.zawser.datn.exception.ErrorCode;
import com.zawser.datn.mapper.UserMapper;
import com.zawser.datn.repository.RoleRepository;
import com.zawser.datn.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;

    //      Tạo user mới
    public UserResponse createUser(UserCreationRequest request) {

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        //        Set role cho user moi
        user.setRoles(new HashSet<>(roleRepository.findAllById(List.of(Role.USER.name()))));

        user.setCreatedBy(request.getUsername());
        user.setLastModifiedBy(request.getUsername());
        user.setLastModifiedDate(LocalDate.now());
        user.setCreatedDate(LocalDate.now());
        user.setIsDeleted(false);
        user.setStatus("0");
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        return userMapper.toUserResponse(user);
    }

    //    Lấy tất cả user

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        log.info("Getting users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    //    Lấy 1 user theo id
    @PostAuthorize("returnObject.username == authentication.name or hasRole('ADMIN')")
    public UserResponse getUser(String id) {

        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    //    Lay thong tin
    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String Name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(Name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }

    //    Sửa user
    public UserResponse updateUser(String id, UserUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.updateUser(user, request);

        if (request.getRoles() != null) {
            var roles = roleRepository.findAllById(request.getRoles());
            user.setRoles(new HashSet<>(roles));
        }

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setLastModifiedBy(request.getName());
        user.setLastModifiedDate(LocalDate.now());
        user.setStatus("1");
        return userMapper.toUserResponse(userRepository.save(user));
    }
    //    Xóa user
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsDeleted(true);
        userRepository.save(user);
        //        userRepository.deleteById(id);
    }
}
