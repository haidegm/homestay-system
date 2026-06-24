package com.example.homestaybackend.controller;

import com.example.homestaybackend.dto.LoginDTO;
import com.example.homestaybackend.dto.RegisterDTO;
import com.example.homestaybackend.entity.User;
import com.example.homestaybackend.repository.UserRepository;
import com.example.homestaybackend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterDTO dto) {
        Map<String, Object> result = new HashMap<>();

        if (userRepository.findByUsername(dto.getUsername()) != null) {
            result.put("msg", "用户名已存在");
            return result;
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setNickname(dto.getNickname());
        user.setRole(dto.getRole() == null ? "ROLE_USER" : dto.getRole());
        user.setStatus("ACTIVE");  // 设置为ACTIVE，与管理端保持一致
        user.setCreatedTime(LocalDateTime.now());

        userRepository.save(user);

        result.put("msg", "注册成功");
        return result;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginDTO dto) {
        Map<String, Object> result = new HashMap<>();

        User user = userRepository.findByUsername(dto.getUsername());

        if (user == null) {
            result.put("msg", "用户不存在");
            return result;
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            result.put("msg", "密码错误");
            return result;
        }

        // 检查账号状态
        if ("DISABLED".equals(user.getStatus())) {
            result.put("msg", "账号已被禁用，请联系管理员");
            return result;
        }

        // 统一角色格式，确保以 ROLE_ 开头
        String normalizedRole = user.getRole().toUpperCase().startsWith("ROLE_") ?
                user.getRole().toUpperCase() : "ROLE_" + user.getRole().toUpperCase();

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), normalizedRole);

        result.put("token", token);
        result.put("role", normalizedRole);
        result.put("msg", "登录成功");

        return result;
    }
}