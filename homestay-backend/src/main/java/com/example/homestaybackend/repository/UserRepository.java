package com.example.homestaybackend.repository;

import com.example.homestaybackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);
    
    // 管理员功能：统计角色数量
    Long countByRole(String role);
    
    // 管理员功能：按角色查询
    Page<User> findByRole(String role, Pageable pageable);
    
    // 管理员功能：按角色和状态查询
    Page<User> findByRoleAndStatus(String role, String status, Pageable pageable);
    
    // 管理员功能：按角色和关键词查询
    Page<User> findByRoleAndUsernameContainingOrNicknameContaining(
        String role, String username, String nickname, Pageable pageable);
}