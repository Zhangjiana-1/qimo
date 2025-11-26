package com.example.qimo.controller;

import com.example.qimo.entity.Book;
import com.example.qimo.entity.Comment;
import com.example.qimo.service.BookService;
import com.example.qimo.service.CategoryService;
import com.example.qimo.service.CommentService;
import com.example.qimo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/books")
public class BookController {
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    private final BookService bookService;
    private final CategoryService categoryService;
    private final CommentService commentService;
    private final UserService userService;

    @Autowired
    public BookController(BookService bookService, CategoryService categoryService, 
                         CommentService commentService, UserService userService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.commentService = commentService;
        this.userService = userService;
    }

    @GetMapping
    public String listBooks(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 6, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {

        // 获取所有分类（用于下拉框）
        model.addAttribute("allCategories", categoryService.getAllCategories());
        model.addAttribute("selectedCategoryId", categoryId);

        Page<Book> books;
        if (query != null && !query.trim().isEmpty()) {
            books = bookService.searchBooksByCategory(query.trim(), categoryId, pageable);
            model.addAttribute("query", query.trim());
        } else {
            books = bookService.findBooksByCategory(categoryId, pageable);
        }

        model.addAttribute("books", books);
        return "book/list";
    }

    @GetMapping("/{id}")
    public String showBook(@PathVariable Long id, Model model, Authentication authentication) {
        return bookService.getBookById(id)
                .map(book -> {
                    model.addAttribute("book", book);
                    
                    // 加载评论，这里应该使用CommentService获取评论
                    model.addAttribute("comments", commentService.getCommentsWithRepliesByBookId(id));
                    
                    // 检查当前用户是否已登录
                    if (authentication != null && authentication.isAuthenticated()) {
                        String username = authentication.getName();
                        model.addAttribute("isFavorite", userService.existsFavoriteByUsernameAndBookId(username, id));
                        model.addAttribute("username", username);
                    }
                    
                    return "book/detail";
                })
                .orElse("redirect:/books");
    }

    @GetMapping("/add")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "book/add";
    }

    @PostMapping("/add")
    public String addBook(@ModelAttribute Book book, @RequestParam(required = false) Long categoryId, RedirectAttributes redirectAttributes) {
        try {
            bookService.saveBook(book, categoryId);
            redirectAttributes.addFlashAttribute("success", "书籍添加成功");
            return "redirect:/books";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "书籍添加失败: " + e.getMessage());
            return "redirect:/books/add";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditBookForm(@PathVariable Long id, Model model) {
        return bookService.getBookById(id)
                .map(book -> {
                    model.addAttribute("book", book);
                    model.addAttribute("categories", categoryService.getAllCategories());
                    return "book/edit";
                })
                .orElse("redirect:/books");
    }

    @PostMapping("/{id}/update")
    public String updateBook(@PathVariable Long id, @ModelAttribute Book book, @RequestParam(required = false) Long categoryId, RedirectAttributes redirectAttributes) {
        book.setId(id);
        try {
            bookService.saveBook(book, categoryId);
            redirectAttributes.addFlashAttribute("success", "书籍更新成功");
            return "redirect:/books";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "书籍更新失败: " + e.getMessage());
            return "redirect:/books/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBook(id);
            redirectAttributes.addFlashAttribute("success", "书籍删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "书籍删除失败: " + e.getMessage());
        }
        return "redirect:/books";
    }

    // 按分类筛选书籍
    @GetMapping("/category/{categoryId}")
    public String listBooksByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        model.addAttribute("books", bookService.getBooksByCategoryId(categoryId, pageable));
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("allCategories", categoryService.getAllCategories());
        return "book/list";
    }
}