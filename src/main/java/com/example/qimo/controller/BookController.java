package com.example.qimo.controller;

import com.example.qimo.entity.Book;
import com.example.qimo.entity.Comment;
import com.example.qimo.service.BookService;
import com.example.qimo.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;

@Controller
@RequestMapping("/books")
public class BookController {
    
    private final BookService bookService;
    private final CommentService commentService;  // 添加这一行来声明CommentService字段
    
    @Autowired
    public BookController(BookService bookService, CommentService commentService) {
        this.bookService = bookService;
        this.commentService = commentService;
    }
    
    /**
     * 显示所有书籍列表（支持分页和搜索）
     */
    @GetMapping
    public String listBooks(@RequestParam(defaultValue = "0") int page, 
                           @RequestParam(defaultValue = "6") int size, 
                           @RequestParam(required = false) String query,
                           Model model) {
        // 构造分页参数，默认按创建时间降序排序
        PageRequest pageRequest = PageRequest.of(page, size, 
                Sort.by("createdAt").descending());
        
        // 获取分页结果（支持搜索）
        Page<Book> booksPage = bookService.searchBooksByTitle(query, pageRequest);
        
        // 将分页对象放入Model
        model.addAttribute("books", booksPage);
        // 将搜索关键词放入Model供前端回显
        model.addAttribute("query", query);
        
        return "book/list";
    }
    
    /**
     * 显示书籍详情页
     */
    @GetMapping("/{id}")
    public String bookDetail(@PathVariable("id") Long id, Model model) {
        try {
            // 查询书籍
            Book book = bookService.findBookById(id);
            model.addAttribute("book", book);
            
            // 查询书籍的评论列表
            java.util.List<Comment> comments = commentService.getCommentsByBookId(id);
            model.addAttribute("comments", comments);
            
            // 为评论表单提供一个空的Comment对象
            model.addAttribute("newComment", new Comment());
            
            return "book/detail";
        } catch (Exception e) {
            // 处理书籍不存在的情况，返回404
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "书籍不存在", e);
        }
    }

     /**
     * 添加评论
     */
    @PostMapping("/{id}/comments")
    public String addComment(
            @PathVariable Long id,
            @RequestParam String content,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            // 验证用户是否已登录
            if (userDetails == null) {
                redirectAttributes.addFlashAttribute("error", "请先登录再发表评论");
                return "redirect:/login?redirect=/books/" + id;
            }
            
            // 验证评论内容
            if (content == null || content.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "评论内容不能为空");
                return "redirect:/books/" + id;
            }
            
            // 调用服务层添加评论
            commentService.addComment(id, content, userDetails.getUsername());
            
            // 添加成功消息
            redirectAttributes.addFlashAttribute("success", "评论发表成功");
            return "redirect:/books/" + id;
        } catch (Exception e) {
            // 添加错误消息
            redirectAttributes.addFlashAttribute("error", "评论发表失败: " + e.getMessage());
            return "redirect:/books/" + id;
        }
    }
}
