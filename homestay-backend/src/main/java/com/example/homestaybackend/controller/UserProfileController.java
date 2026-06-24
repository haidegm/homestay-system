package com.example.homestaybackend.controller;

import com.example.homestaybackend.entity.User;
import com.example.homestaybackend.entity.UserProfile;
import com.example.homestaybackend.repository.UserProfileRepository;
import com.example.homestaybackend.repository.UserRepository;
import com.example.homestaybackend.security.JwtUtil; // 假设你的工具类在这里
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user/profile")
public class UserProfileController {

    @Autowired
    private UserProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil; // 用来从 Token 获取 ID

    // 获取资料
    @GetMapping("/get")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
        Long uid = jwtUtil.getUserIdFromToken(token.substring(7)); // 假设你的 token 带 Bearer

        User user = userRepository.findById(uid).orElse(null);
        UserProfile profile = profileRepository.findById(uid).orElse(new UserProfile());

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("profile", profile);
        return ResponseEntity.ok(result);
    }

    // 保存资料
    @PostMapping("/save")
    @Transactional // 必须加这个，保证两个表要么都成功，要么都失败
    public ResponseEntity<?> saveProfile(@RequestHeader("Authorization") String token, @RequestBody UserProfile profile) {
        // 1. 获取 UID
        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        Long uid = jwtUtil.getUserIdFromToken(jwt);

        // 2. 保存详细资料 (职业、地点等)
        profile.setUserId(uid);
        profileRepository.save(profile);

        // 3. 【重点】如果你想在点“完成”时顺便把头像也最终确认（如果有变动）
        // 其实你之前的 uploadAvatar 已经存过一次 user 表了，这里主要是 profile 表的持久化

        System.out.println("用户 " + uid + " 的资料已更新");
        return ResponseEntity.ok("保存成功");
    }

    // 头像上传
    @PostMapping("/upload/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestHeader("Authorization") String token,
                                          @RequestParam("file") MultipartFile file) throws IOException {
        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        Long uid = jwtUtil.getUserIdFromToken(jwt);
        // 1. 确定存储路径（确保该文件夹在 D 盘已创建）
        String basePath = "D:/wlh/homestay_upload/useravator/";
        File dir = new File(basePath);
        if (!dir.exists()) dir.mkdirs();

        // 3. 【核心修改】固定文件名为 avatar_用户ID + 后缀
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 结果类似于: avatar_1.jpg
        String newFileName = "avatar_" + uid + suffix;

        // 4. 保存物理文件到 D 盘
        // 注意：这样写会自动替换掉该用户之前的同名头像文件
        File targetFile = new File(basePath + newFileName);
        file.transferTo(targetFile);

        // 4. 生成数据库存储的虚拟路径（对应 WebConfig 里的映射）
        String avatarUrl = "/uploads/avatar/" + newFileName;

        // 5. 解析 Token 获取用户 ID 并更新 User 表


        User user = userRepository.findById(uid).orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setAvatar(avatarUrl);
        userRepository.save(user); // JPA 自动更新头像字段

        return ResponseEntity.ok(avatarUrl); // 返回给前端新头像的 URL
    }
}