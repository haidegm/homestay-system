package com.example.homestaybackend.security;

import com.example.homestaybackend.entity.User;
import com.example.homestaybackend.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        // 跳过静态资源和公开接口的JWT验证
        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 只对API请求打印日志
        if (requestURI.startsWith("/api/")) {
            System.out.println("JwtFilter 处理请求: " + method + " " + requestURI);
        }

        String header = request.getHeader("Authorization");
        
        if (requestURI.startsWith("/api/")) {
            System.out.println("Authorization Header: " + (header != null ? "存在" : "null"));
        }

        // 1. 检查 Header 是否包含有效的 Bearer Token
        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);
                if (requestURI.startsWith("/api/")) {
                    System.out.println("提取的 Token: " + token.substring(0, Math.min(20, token.length())) + "...");
                }

                // 2. 解析 Token
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(jwtUtil.getKey())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String username = claims.getSubject();
                String role = claims.get("role", String.class); // 对应数据库中的 role 字段

                if (requestURI.startsWith("/api/")) {
                    System.out.println("解析出的用户名: " + username + ", 角色: " + role);
                }

                if (username != null && role != null) {
                    // 检查用户状态
                    User user = userRepository.findByUsername(username);
                    if (user == null) {
                        System.out.println("用户不存在: " + username);
                        SecurityContextHolder.clearContext();
                        filterChain.doFilter(request, response);
                        return;
                    }
                    
                    if ("DISABLED".equals(user.getStatus())) {
                        System.out.println("用户已被禁用: " + username);
                        SecurityContextHolder.clearContext();
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"msg\":\"账号已被禁用，请联系管理员\"}");
                        return;
                    }
                    
                    // 3. 角色处理：Spring Security hasRole 依赖 "ROLE_" 前缀
                    // 如果数据库里存的是 "USER"，这里会变成 "ROLE_USER"
                    String formattedRole = role.toUpperCase().startsWith("ROLE_") ?
                            role.toUpperCase() : "ROLE_" + role.toUpperCase();

                    if (requestURI.startsWith("/api/")) {
                        System.out.println("格式化后的角色: " + formattedRole);
                    }

                    List<SimpleGrantedAuthority> authorities =
                            Collections.singletonList(new SimpleGrantedAuthority(formattedRole));

                    // 4. 构建认证对象
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);

                    // 5. 存入安全上下文（这一步成功，Security 才会认为你已登录）
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    if (requestURI.startsWith("/api/")) {
                        System.out.println("认证成功，已设置到 SecurityContext");
                    }
                }
            } catch (Exception e) {
                // Token 异常（如过期、秘钥不对）
                System.out.println("JWT 认证失败: " + e.getMessage());
                e.printStackTrace();
                SecurityContextHolder.clearContext();
            }
        } else {
            if (requestURI.startsWith("/api/")) {
                System.out.println("没有找到 Authorization Header 或格式不正确");
            }
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // 跳过静态资源
        if (path.matches(".+\\.(jpg|jpeg|png|gif|svg|ico|css|js|woff|woff2|ttf|eot)$")) {
            return true;
        }
        
        // 跳过上传目录
        if (path.startsWith("/uploads/") || path.startsWith("/house/")) {
            return true;
        }
        
        return false;
    }
}