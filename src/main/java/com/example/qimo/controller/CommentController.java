package com.example.qimo.controller;

import com.example.qimo.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
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

    // 修改为@DeleteMapping，以正确处理由HiddenHttpMethodFilter转换后的DELETE请求
    @DeleteMapping("/comments/{id}")
    public String deleteComment(
            @PathVariable Long id,
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            Authentication authentication,
            @RequestHeader(value = "Referer", required = false) String referer,
            RedirectAttributes redirectAttributes) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                redirectAttributes.addFlashAttribute("error", "请先登录再操作");
                return "redirect:" + (redirectUrl != null ? redirectUrl : (referer != null ? referer : "/books"));
            }
            
            // 从Authentication获取当前用户名和角色
            String currentUsername = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            // 调用service层进行权限校验和删除
            commentService.deleteCommentById(id, currentUsername, isAdmin);
            
            // 重定向回原页面
            String targetUrl = redirectUrl != null ? redirectUrl : (referer != null ? referer : "/books");
            return "redirect:" + targetUrl;
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "评论已被删除或不存在");
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("error", "无权删除他人评论");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除评论失败，请重试");
            e.printStackTrace();
        }
        
        // 出错时的重定向目标
        String fallbackUrl = redirectUrl != null ? redirectUrl : (referer != null ? referer : "/books");
        return "redirect:" + fallbackUrl;
    }

    // 新增：处理书籍页发表评论（顶级评论）
    @PostMapping("/books/{bookId}/comments")
    public String addCommentToBook(
            @PathVariable Long bookId,
            @RequestParam("content") String content,
            Authentication authentication,
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            @RequestHeader(value = "Referer", required = false) String referer,
            RedirectAttributes redirectAttributes) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                redirectAttributes.addFlashAttribute("error", "请先登录再发表评论");
                return "redirect:" + (redirectUrl != null ? redirectUrl : (referer != null ? referer : "/books"));
            }

            String username = authentication.getName();
            commentService.addComment(bookId, content, username, null);

            String target = redirectUrl != null ? redirectUrl : (referer != null ? referer : "/books");
            return "redirect:" + target;
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "发表评论失败，请重试");
            e.printStackTrace();
        }
        String fallbackUrl = redirectUrl != null ? redirectUrl : (referer != null ? referer : "/books");
        return "redirect:" + fallbackUrl;
    }

    // 新增：处理对评论的回复（子评论）
    @PostMapping("/books/{bookId}/comments/{parentId}/reply")
    public String replyToComment(
            @PathVariable Long bookId,
            @PathVariable Long parentId,
            @RequestParam("content") String content,
            Authentication authentication,
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            @RequestHeader(value = "Referer", required = false) String referer,
            RedirectAttributes redirectAttributes) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                redirectAttributes.addFlashAttribute("error", "请先登录再回复评论");
                return "redirect:" + (redirectUrl != null ? redirectUrl : (referer != null ? referer : "/books"));
            }

            String username = authentication.getName();
            commentService.addComment(bookId, content, username, parentId);

            String target = redirectUrl != null ? redirectUrl : (referer != null ? referer : "/books");
            return "redirect:" + target;
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "回复评论失败，请重试");
            e.printStackTrace();
        }
        String fallbackUrl = redirectUrl != null ? redirectUrl : (referer != null ? referer : "/books");
        return "redirect:" + fallbackUrl;
    }
}