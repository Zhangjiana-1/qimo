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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        model.addAttribute("books", bookService.getBooks(pageable));
        return "book/list";
    }

    @GetMapping("/{id}")
    public String showBook(@PathVariable Long id, Model model, Authentication authentication) {
        return bookService.getBookById(id)
                .map(book -> {
                    model.addAttribute("book", book);
                    
                    // 修改：使用getCommentsWithRepliesByBookId获取带有回复结构的评论列表
                    List<Comment> comments = commentService.getCommentsWithRepliesByBookId(id);
                    logger.debug("Book {} loaded with {} root comments", id, comments != null ? comments.size() : 0);
                    logger.debug("BookController: loaded {} comments (roots) for bookId={}", comments == null ? 0 : comments.size(), id);
                    model.addAttribute("comments", comments);
                    
                    // 添加用户收藏状态
                    if (authentication != null && authentication.isAuthenticated()) {
                        String username = authentication.getName();
                        boolean isFavorite = userService.existsFavoriteByUsernameAndBookId(username, id);
                        model.addAttribute("isFavorite", isFavorite);
                    } else {
                        model.addAttribute("isFavorite", false);
                    }
                    
                    return "book/detail";
                })
                .orElse("redirect:/books");
    }
    
    @PostMapping("/{id}/comments")
    public String addComment(@PathVariable Long id, @RequestParam("content") String content, 
                            Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "请先登录再发表评论");
            return "redirect:/books/" + id;
        }
        
        try {
            String username = authentication.getName();
            commentService.addComment(id, content, username, null);
            return "redirect:/books/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "发表评论失败: " + e.getMessage());
            return "redirect:/books/" + id;
        }
    }
    
    @PostMapping("/{bookId}/comments/{parentId}/reply")
    public String replyToComment(@PathVariable Long bookId, @PathVariable Long parentId, 
                                @RequestParam("content") String content, 
                                Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "请先登录再回复评论");
            return "redirect:/books/" + bookId;
        }
        
        try {
            String username = authentication.getName();
            commentService.addComment(bookId, content, username, parentId);
            return "redirect:/books/" + bookId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "回复评论失败: " + e.getMessage());
            return "redirect:/books/" + bookId;
        }
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "book/create";
    }

    @PostMapping
    public String saveBook(Book book, @RequestParam("categoryId") Long categoryId, RedirectAttributes redirectAttributes) {
        try {
            bookService.saveBook(book, categoryId);
            redirectAttributes.addFlashAttribute("success", "书籍保存成功");
            return "redirect:/books";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "书籍保存失败: " + e.getMessage());
            return "redirect:/books/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String showUpdateForm(@PathVariable Long id, Model model) {
        return bookService.getBookById(id)
                .map(book -> {
                    model.addAttribute("book", book);
                    model.addAttribute("categories", categoryService.getAllCategories());
                    return "book/edit";
                })
                .orElse("redirect:/books");
    }

    @PostMapping("/{id}")
    public String updateBook(@PathVariable Long id, Book book, @RequestParam("categoryId") Long categoryId, RedirectAttributes redirectAttributes) {
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
        return "book/list";
    }
}