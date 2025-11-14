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
}