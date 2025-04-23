package com.zawser.DATN.service;

import com.zawser.DATN.dto.request.UserUpdateRequest;
import com.zawser.DATN.dto.response.UserResponse;
import com.zawser.DATN.entity.User;
import com.zawser.DATN.exception.AppException;
import com.zawser.DATN.exception.ErrorCode;
import com.zawser.DATN.mapper.UserMapper;
import com.zawser.DATN.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.zawser.DATN.dto.request.UserCreationRequest;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class UserService {

    UserRepository userRepository;

    UserMapper userMapper;
    //      Tạo user mới
    public User createUser(UserCreationRequest request){
        if(userRepository.existsByUsername(request.getUsername())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = userMapper.toUser(request);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(user);
    }
    //    Lấy tất cả user
    public List<User> getUsers(){
        return userRepository.findAll();
    }
    //    Lấy 1 user theo id
    public UserResponse getUser(String userId){
        return userMapper.toUserResponse(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")));
    }
    //    Sửa user
    public UserResponse updateUser(String userId, UserUpdateRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userMapper.updateUser(user, request);
        return userMapper.toUserResponse(userRepository.save(user));
    }
    //    Xóa user
    public void deleteUser(String userId){
        userRepository.deleteById(userId);
    }

}
