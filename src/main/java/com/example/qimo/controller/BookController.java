package com.example.qimo.controller;

import com.example.qimo.entity.Book;
import com.example.qimo.entity.Comment;
import com.example.qimo.entity.User;
import com.example.qimo.service.BookService;
import com.example.qimo.service.CommentService;
import com.example.qimo.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/books")
public class BookController {
    
    private final BookService bookService;
    private final CommentService commentService;
    private final FavoriteService favoriteService;
    
    @Autowired
    public BookController(BookService bookService, CommentService commentService, FavoriteService favoriteService) {
        this.bookService = bookService;
        this.commentService = commentService;
        this.favoriteService = favoriteService;
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
    public String bookDetail(@PathVariable("id") Long id, Model model, Authentication auth, @AuthenticationPrincipal UserDetails currentUser) {
        try {
            // 查询书籍
            Book book = bookService.findBookById(id);
            model.addAttribute("book", book);
            
            // 使用新方法查询书籍的主评论及回复
            List<Comment> comments = commentService.getCommentsWithRepliesByBookId(id);
            model.addAttribute("comments", comments);
            
            // 创建点赞状态映射
            Map<Long, Boolean> likedByCurrentUserMap = new HashMap<>();
            if (currentUser != null) {
                // 构建每个评论的点赞状态，包括主评论和回复
                for (Comment comment : comments) {
                    // 处理主评论
                    boolean isLiked = false;
                    for (User likedUser : comment.getLikedBy()) {
                        if (likedUser.getUsername().equals(currentUser.getUsername())) {
                            isLiked = true;
                            break;
                        }
                    }
                    likedByCurrentUserMap.put(comment.getId(), isLiked);
                    
                    // 处理回复
                    if (comment.getReplies() != null) {
                        for (Comment reply : comment.getReplies()) {
                            boolean replyLiked = false;
                            for (User likedUser : reply.getLikedBy()) {
                                if (likedUser.getUsername().equals(currentUser.getUsername())) {
                                    replyLiked = true;
                                    break;
                                }
                            }
                            likedByCurrentUserMap.put(reply.getId(), replyLiked);
                        }
                    }
                }
            }
            model.addAttribute("likedByCurrentUserMap", likedByCurrentUserMap);
            
            // 添加收藏状态
            boolean isFavorite = false;
            if (auth != null) {
                isFavorite = favoriteService.isBookFavoritedByUser(id, auth.getName());
            }
            model.addAttribute("isFavorite", isFavorite);
            
            // 为评论表单提供一个空的Comment对象
            model.addAttribute("newComment", new Comment());
            
            return "book/detail";
        } catch (Exception e) {
            // 处理书籍不存在的情况，返回404
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "书籍不存在", e);
        }
    }
    
    /**
     * 添加评论或回复
     */
    @PostMapping("/{id}/comments")
    public String addComment(
            @PathVariable Long id,
            @RequestParam String content,
            @RequestParam(required = false) Long parentId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            // 验证用户是否已登录
            if (userDetails == null) {
                redirectAttributes.addFlashAttribute("error", "请先登录再发表评论");
                return "redirect:/login?redirect=/books/" + id;
            }
            
            // 调用服务添加评论或回复
            commentService.addComment(id, content, userDetails.getUsername(), parentId);
            
            return "redirect:/books/" + id;
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/books/" + id;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/books/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "发表评论失败，请重试");
            return "redirect:/books/" + id;
        }
    }
    
    /**
     * 切换书籍收藏状态
     */
    @PostMapping("/{bookId}/favorite")
    public String toggleFavorite(
            @PathVariable Long bookId,
            Authentication authentication,
            @RequestHeader(value = "Referer", required = false) String referer) {
        favoriteService.toggleFavorite(bookId, authentication.getName());
        return "redirect:" + (referer != null ? referer : "/books/" + bookId);
    }
}