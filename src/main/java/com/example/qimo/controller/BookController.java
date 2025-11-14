package com.example.qimo.controller;

import com.example.qimo.entity.Book;
import com.example.qimo.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;

@Controller
@RequestMapping("/books")
public class BookController {
    
    private final BookService bookService;
    
    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }
    
    /**
     * 显示书籍列表（支持分页和搜索）
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
