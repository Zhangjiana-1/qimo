package com.example.qimo.controller;

import com.example.qimo.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityNotFoundException;

@Controller
public class CommentController {
    
    private final CommentService commentService;
    
    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }
    
    @PostMapping("/comments/{commentId}/like")
    public String toggleLike(
            @PathVariable Long commentId,
            Authentication authentication,
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            @RequestHeader(value = "Referer", required = false) String referer,
            RedirectAttributes redirectAttributes) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                redirectAttributes.addFlashAttribute("error", "请先登录再点赞");
                return "redirect:" + (redirectUrl != null ? redirectUrl : (referer != null ? referer : "/books"));
            }
            
            commentService.toggleLike(commentId, authentication.getName());
            
            // 优先使用表单提交的redirectUrl，然后是Referer，最后是默认值
            String targetUrl = redirectUrl != null ? redirectUrl : (referer != null ? referer : "/books");
            return "redirect:" + targetUrl;
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            // 捕获所有其他异常，避免500错误
            redirectAttributes.addFlashAttribute("error", "点赞操作失败，请重试");
            e.printStackTrace();
        }
        
        // 出错时的重定向目标
        String fallbackUrl = redirectUrl != null ? redirectUrl : (referer != null ? referer : "/books");
        return "redirect:" + fallbackUrl;
    }

}