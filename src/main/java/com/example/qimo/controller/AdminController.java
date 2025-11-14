package com.example.qimo.controller;

import com.example.qimo.entity.User;
import com.example.qimo.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }
    
    @PostMapping("/admin/users/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleUserEnabled(@PathVariable Long id) {
        userService.toggleUserEnabled(id);
        return "redirect:/admin/users"; // 操作后重定向回用户列表页
    }
    
    @PostMapping("/admin/users/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public String resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return "redirect:/admin/users"; // 操作后重定向回用户列表页
    }
}