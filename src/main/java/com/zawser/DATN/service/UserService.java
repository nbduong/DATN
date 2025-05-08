package com.zawser.DATN.service;

import com.nimbusds.jose.proc.SecurityContext;
import com.zawser.DATN.dto.request.UserUpdateRequest;
import com.zawser.DATN.dto.response.UserResponse;
import com.zawser.DATN.entity.User;
import com.zawser.DATN.enums.Role;
import com.zawser.DATN.exception.AppException;
import com.zawser.DATN.exception.ErrorCode;
import com.zawser.DATN.mapper.UserMapper;
import com.zawser.DATN.repository.UserRepository;
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
import com.zawser.DATN.dto.request.UserCreationRequest;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;


    //      Tạo user mới
    public UserResponse createUser(UserCreationRequest request){

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

//        Set role cho user moi
        HashSet<String> roles = new HashSet<>();
        roles.add(Role.USER.name());
        user.setRoles(roles);


        user.setCreated_at(LocalDate.now());
        user.setUpdated_at(LocalDate.now());
        user.setCreated_by(request.getUsername());
        user.setUpdated_by(request.getUsername());
        user.setStatus("1");
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
        return userRepository
                .findAll()
                .stream()
                .map(userMapper::toUserResponse).toList();
    }
    //    Lấy 1 user theo id
    @PostAuthorize("returnObject.username == authentication.name or hasRole('ADMIN')")
    public UserResponse getUser(String account_id){
        return userMapper
                .toUserResponse(userRepository
                        .findById(account_id)
                        .orElseThrow(() -> new RuntimeException("User not found")));
    }


//    Lay thong tin
    public UserResponse getMyInfo(){
        var context = SecurityContextHolder.getContext();
        String Name =  context
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(Name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }

    //    Sửa user
    public UserResponse updateUser(String account_id, UserUpdateRequest request){
        User user = userRepository
                .findById(account_id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userMapper.updateUser(user, request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUpdated_at(LocalDate.now());
        user.setUpdated_by(user.getUsername());
        user.setStatus("1");
        return userMapper.toUserResponse(userRepository.save(user));
    }
    //    Xóa user
    public void deleteUser(String account_id){

        userRepository.deleteById(account_id);
    }

}
