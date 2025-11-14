package com.example.qimo.controller;

import com.example.qimo.entity.Book;
import com.example.qimo.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/books")
public class BookController {
    
    private final BookService bookService;
    
    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }
    
    /**
     * 显示所有书籍列表
     */
    @GetMapping
    public String listBooks(Model model) {
        List<Book> books = bookService.findAllBooks();
        model.addAttribute("books", books);
        return "book/list";
    }
    
    /**
     * 显示书籍详情页
     * 注意：评论功能后续实现，这里暂时传入空列表
     */
    @GetMapping("/{id}")
    public String bookDetail(@PathVariable("id") Long id, Model model) {
        try {
            Book book = bookService.findBookById(id);
            model.addAttribute("book", book);
            // 评论功能后续实现，暂时传入空列表
            model.addAttribute("comments", new ArrayList<>());
            return "book/detail";
        } catch (Exception e) {
            // 处理书籍不存在的情况，返回404
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "书籍不存在", e);
        }
    }
}
