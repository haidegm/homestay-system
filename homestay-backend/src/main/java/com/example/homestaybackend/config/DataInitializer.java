package com.example.homestaybackend.config;

import com.example.homestaybackend.entity.User;
import com.example.homestaybackend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        User admin = userRepository.findByUsername("admin");
        if (admin == null) {
            admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123"));
            admin.setRole("ROLE_ADMIN");
            admin.setNickname("管理员");
            admin.setPhone("13800000000");
            admin.setStatus("ACTIVE");
            admin.setCreatedTime(LocalDateTime.now());
            userRepository.save(admin);
            System.out.println("[DataInitializer] 管理员账号已创建: admin / 123");
        } else {
            boolean changed = false;
            if (!"ROLE_ADMIN".equals(admin.getRole())) {
                admin.setRole("ROLE_ADMIN");
                changed = true;
            }
            if (!"ACTIVE".equals(admin.getStatus())) {
                admin.setStatus("ACTIVE");
                changed = true;
            }
            // 把密码重置为123，确保能登录
            admin.setPassword(passwordEncoder.encode("123"));
            changed = true;
            if (changed) {
                userRepository.save(admin);
                System.out.println("[DataInitializer] 管理员账号已修复: admin / 123");
            } else {
                System.out.println("[DataInitializer] 管理员账号正常，无需修复");
            }
        }
    }
}
