package com.example.qimo.service;

import com.example.qimo.entity.Book;
import com.example.qimo.entity.Comment;
import com.example.qimo.entity.User;
import com.example.qimo.repository.BookRepository;
import com.example.qimo.repository.CommentRepository;
import com.example.qimo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    
    public CommentService(CommentRepository commentRepository, 
                         BookRepository bookRepository, 
                         UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * 添加评论
     * @param bookId 书籍ID
     * @param content 评论内容
     * @param currentUsername 当前用户名
     */
    public void addComment(Long bookId, String content, String currentUsername) {
        // 验证评论内容不为空
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        
        // 查询书籍
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("书籍不存在: " + bookId));
        
        // 查询用户
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + currentUsername));
        
        // 创建评论
        Comment comment = new Comment();
        comment.setContent(content.trim());
        comment.setBook(book);
        comment.setUser(user);
        
        // 保存评论
        commentRepository.save(comment);
    }
    
    /**
     * 根据书籍ID获取评论列表
     * @param bookId 书籍ID
     * @return 评论列表
     */
    @Transactional(readOnly = true)
    public java.util.List<Comment> getCommentsByBookId(Long bookId) {
        return commentRepository.findByBookIdOrderByCreatedAtDesc(bookId);
    }
}