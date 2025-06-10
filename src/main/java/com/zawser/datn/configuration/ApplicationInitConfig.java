package com.zawser.datn.configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.zawser.datn.entity.User;
import com.zawser.datn.enums.Role;
import com.zawser.datn.repository.RoleRepository;
import com.zawser.datn.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Configuration
@Slf4j
public class ApplicationInitConfig {

     PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User user = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("Duong19bg!"))
                        .email("nbduong1905@gmail.com")
                        .build();
                user.setRoles(new HashSet<>(roleRepository.findAllById(List.of(Role.ADMIN.name()))));
                userRepository.save(user);
                log.warn("Admin user created with default password: admin");
            }
        };
    }
    ;
}
