package com.example.qimo.controller;

import com.example.qimo.entity.User;
import com.example.qimo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String login() {
        return "login";
    }
}