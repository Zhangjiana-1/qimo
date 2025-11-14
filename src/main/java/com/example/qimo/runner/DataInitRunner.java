package com.example.qimo.runner;

import com.example.qimo.entity.User;
import com.example.qimo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitRunner implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // 检查是否已存在管理员账户
        if (userRepository.findByUsername("admin") == null) {
            // 创建管理员账户
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setEmail("admin@example.com");
            admin.setRole(User.Role.ADMIN);
            
            userRepository.save(admin);
            System.out.println("管理员账户已创建：username=admin, password=Admin@123");
        }
    }
}