package com.example.qimo.controller;

import com.example.qimo.entity.User;
import com.example.qimo.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(
        User user,
        @RequestParam("password") String password,
        @RequestParam("confirmPassword") String confirmPassword,
        Model model) {

    if (!password.equals(confirmPassword)) {
        model.addAttribute("error", "两次输入的密码不一致");
        return "register";
    }

    try {
        userService.register(user, password); // 保存并加密密码
        return "redirect:/login?registered";
    } catch (RuntimeException e) {
        model.addAttribute("error", e.getMessage());
        return "register";
    }
}

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String redirect, Model model) {
        // 将重定向参数传递给视图，以便在表单提交时保留
        if (redirect != null && !redirect.isEmpty()) {
            model.addAttribute("redirect", redirect);
        }
        return "login";
    }
    
    // 添加POST登录处理方法，支持重定向
    @PostMapping("/login")
    public String processLogin(
            @RequestParam(required = false) String redirect,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        // 检查用户是否已认证
        if (authentication != null && authentication.isAuthenticated()) {
            // 如果提供了重定向URL，则重定向到该URL
            if (redirect != null && !redirect.isEmpty()) {
                return "redirect:" + redirect;
            }
            // 否则重定向到默认页面
            return "redirect:/books";
        }
        
        // 认证失败，重定向到登录页并显示错误信息
        redirectAttributes.addFlashAttribute("error", "用户名或密码错误");
        return "redirect:/login" + (redirect != null ? "?redirect=" + redirect : "");
    }

    // 查看个人资料
    @GetMapping("/user/profile")
    public String showProfile(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        model.addAttribute("user", user);
        return "user/profile";
    }

    // 编辑个人资料
    @GetMapping("/user/edit-profile")
    public String showEditProfile(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        model.addAttribute("user", user);
        return "user/edit-profile";
    }

    // 提交个人资料修改
    @PostMapping("/user/profile")
    @org.springframework.transaction.annotation.Transactional
    public String updateProfile(
            Authentication authentication,
            @RequestParam("nickname") String nickname,
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            userService.updateProfile(username, nickname, email);
            return "redirect:/user/profile?success";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/user/edit-profile";
        }
    }

    // 显示修改密码页面
    @GetMapping("/user/change-password")
    public String showChangePassword() {
        return "user/change-password";
    }

    // 提交密码修改
    @PostMapping("/user/change-password")
    @org.springframework.transaction.annotation.Transactional
    public String changePassword(
            Authentication authentication,
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {
        
        // 验证新密码和确认密码是否一致
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addAttribute("error", "新密码和确认密码不一致");
            return "redirect:/user/change-password";
        }
        
        try {
            String username = authentication.getName();
            userService.changePassword(username, oldPassword, newPassword);
            return "redirect:/user/profile?success";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/user/change-password";
        }
    }
}