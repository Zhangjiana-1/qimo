package com.example.qimo.controller;

import com.example.qimo.entity.Book;
import com.example.qimo.service.BookService;
import com.example.qimo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final BookService bookService;
    private final UserService userService;

    @Autowired
    public AdminController(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    /**
     * 管理员查看用户列表
     */
    @GetMapping("/users")
    public String listUsers(Model model) {
        try {
            model.addAttribute("users", userService.getAllUsers());
            return "admin/users";
        } catch (Exception e) {
            logger.error("获取用户列表失败", e);
            model.addAttribute("error", "获取用户列表失败: " + e.getMessage());
            return "admin/users";
        }
    }

    /**
     * 启用/禁用用户
     */
    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id) {
        try {
            userService.toggleUserEnabled(id);
            return "redirect:/admin/users?success=" + urlEncode("操作成功");
        } catch (Exception e) {
            logger.error("切换用户状态失败, id={}", id, e);
            return "redirect:/admin/users?error=" + urlEncode("切换用户状态失败: " + e.getMessage());
        }
    }

    /**
     * 重置用户密码（管理员操作）
     */
    @PostMapping("/users/{id}/reset-password")
    public String resetUserPassword(@PathVariable Long id) {
        try {
            userService.resetPassword(id);
            return "redirect:/admin/users?success=" + urlEncode("重置密码成功");
        } catch (Exception e) {
            logger.error("重置用户密码失败, id={}", id, e);
            return "redirect:/admin/users?error=" + urlEncode("重置密码失败: " + e.getMessage());
        }
    }

    /**
     * 获取分页书籍列表
     */
    @GetMapping("/books")
    public String listBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        try {
            // 构造分页参数，按创建时间倒序排序
            PageRequest pageRequest = PageRequest.of(page, size, 
                    Sort.by("createdAt").descending());
            
            // 调用bookService.findAll(Pageable)获取分页结果
            Page<Book> books = bookService.findAll(pageRequest);
            
            // 添加到model
            model.addAttribute("books", books);
            model.addAttribute("page", page);
            model.addAttribute("size", size);
        } catch (Exception e) {
            logger.error("获取书籍列表失败", e);
            model.addAttribute("error", "获取书籍列表失败: " + e.getMessage());
        }
        return "admin/books";
    }

    /**
     * 返回新增书籍表单页面
     */
    @GetMapping("/books/new")
    public String showNewBookForm(Model model) {
        try {
            Book book = new Book();
            model.addAttribute("book", book);
            return "admin/book-form";
        } catch (Exception e) {
            logger.error("显示新增书籍表单失败", e);
            return "redirect:/admin/books?error=" + urlEncode("加载表单失败");
        }
    }

    /**
     * 保存新书
     */
    @PostMapping("/books")
    public String saveBook(@ModelAttribute Book book) {
        try {
            // 处理空字符串
            if (book.getCoverUrl() != null && book.getCoverUrl().trim().isEmpty()) {
                book.setCoverUrl(null);
            }
            
            // 移除手动设置createdAt，让JPA审计自动处理
            bookService.save(book);
            return "redirect:/admin/books?page=0&size=10&success=" + urlEncode("书籍添加成功");
        } catch (Exception e) {
            logger.error("保存书籍失败", e);
            return "redirect:/admin/books?error=" + urlEncode("保存书籍失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询书籍，返回编辑表单页面
     */
    @GetMapping("/books/{id}/edit")
    public String showEditBookForm(
            @PathVariable Long id, 
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        try {
            logger.info("尝试编辑书籍ID: {}", id);
            // 使用findById方法，可能抛出EntityNotFoundException
            Book book = bookService.findById(id);
            model.addAttribute("book", book);
            model.addAttribute("page", page);
            model.addAttribute("size", size);
            return "admin/book-form";
        } catch (EntityNotFoundException e) {
            // 优雅处理不存在的书籍ID
            logger.warn("查找书籍失败，ID: {}", id);
            return "redirect:/admin/books?page=" + page + "&size=" + size + "&error=" + urlEncode("找不到指定书籍");
        } catch (Exception e) {
            logger.error("查找书籍失败，ID: {}", id, e);
            return "redirect:/admin/books?page=" + page + "&size=" + size + "&error=" + urlEncode("加载书籍信息失败");
        }
    }

    /**
     * 更新指定ID的书籍信息
     */
    @PostMapping("/books/{id}/update")
    public String updateBook(
            @PathVariable Long id, 
            @ModelAttribute Book bookForm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 查找现有书籍
            Book existingBook = bookService.findById(id);
            
            // 更新字段
            existingBook.setTitle(bookForm.getTitle());
            existingBook.setAuthor(bookForm.getAuthor());
            existingBook.setDescription(bookForm.getDescription());
            
            // 处理封面URL
            if (bookForm.getCoverUrl() != null && bookForm.getCoverUrl().trim().isEmpty()) {
                existingBook.setCoverUrl(null);
            } else {
                existingBook.setCoverUrl(bookForm.getCoverUrl());
            }
            
            // 移除手动设置updatedAt，让JPA审计自动处理
            bookService.save(existingBook);
            
            // 更新成功后重定向回原分页位置
            return "redirect:/admin/books?page=" + page + "&size=" + size + "&success=" + urlEncode("书籍更新成功");
        } catch (EntityNotFoundException e) {
            logger.warn("更新书籍失败，ID: {}", id);
            return "redirect:/admin/books?page=" + page + "&size=" + size + "&error=找不到指定书籍";
        } catch (Exception e) {
            logger.error("更新书籍失败，ID: {}", id, e);
            return "redirect:/admin/books?page=" + page + "&size=" + size + "&error=" + urlEncode("更新书籍失败: " + e.getMessage());
        }
    }

    /**
     * 删除指定ID的书籍，使用DELETE方法
     * 使用@DeleteMapping并正确配置hiddenmethod filter
     */
    @DeleteMapping("/books/{id}")
    public String deleteBook(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            bookService.deleteById(id);
            // 删除成功后重定向回原分页位置（对中文做 URL 编码，避免 Location header 编码异常）
            return "redirect:/admin/books?page=" + page + "&size=" + size + "&success=" + urlEncode("书籍删除成功");
        } catch (EntityNotFoundException e) {
            logger.warn("删除书籍失败，ID: {}", id);
            return "redirect:/admin/books?page=" + page + "&size=" + size + "&error=" + urlEncode("找不到指定书籍");
        } catch (Exception e) {
            logger.error("删除书籍失败，ID: {}", id, e);
            return "redirect:/admin/books?page=" + page + "&size=" + size + "&error=" + urlEncode("删除书籍失败: " + e.getMessage());
        }
        }

    /**
     * 兼容性删除（POST）：有些环境中 HiddenHttpMethodFilter/DELETE 可能不可用，提供 POST 版本的删除接口
     */
    @PostMapping("/books/{id}/delete")
    public String deleteBookPost(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return deleteBook(id, page, size);
    }

    private String urlEncode(String s) {
        if (s == null) return "";
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}