package com.example.qimo.service.impl;

import com.example.qimo.entity.Comment;
import com.example.qimo.entity.Book;
import com.example.qimo.entity.User;
import com.example.qimo.repository.CommentRepository;
import com.example.qimo.repository.UserRepository;
import com.example.qimo.repository.BookRepository;
import com.example.qimo.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {
    
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    
    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, UserRepository userRepository, BookRepository bookRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }
    
    @Override
    @Transactional // 确保方法在事务内执行
    public void toggleLike(Long commentId, String currentUsername) {
        // 1. 先查询用户，避免后续操作中再次查询
        User user = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + currentUsername));
        
        // 2. 使用findById方法（已添加@EntityGraph）查询评论，确保likedBy集合被正确加载
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new EntityNotFoundException("评论不存在: " + commentId));
        
        // 3. 操作点赞状态 - 先检查是否已经在集合中
        Set<User> likedUsers = comment.getLikedBy();
        if (likedUsers != null) {
            boolean isLiked = false;
            // 手动检查用户是否已点赞，避免懒加载问题
            for (User likedUser : likedUsers) {
                if (likedUser.getId().equals(user.getId())) {
                    isLiked = true;
                    break;
                }
            }
            
            if (isLiked) {
                // 已点赞，取消点赞
                likedUsers.remove(user);
            } else {
                // 未点赞，添加点赞
                likedUsers.add(user);
            }
        }
        
        // 4. 显式保存评论（确保更新被写入数据库）
        commentRepository.saveAndFlush(comment);
    }
    
    @Override
    public Comment findById(Long id) {
        return commentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("评论不存在: " + id));
    }
    
    @Override
    public List<Comment> getCommentsByBookId(Long bookId) {
        return commentRepository.findByBookIdOrderByCreatedAtDesc(bookId);
    }
    
    @Override
    public void addComment(Long bookId, String content, String username) {
        // 验证书籍是否存在
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new EntityNotFoundException("书籍不存在: " + bookId));
        
        // 验证用户是否存在
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        
        // 创建评论
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setBook(book);
        comment.setUser(user);
        
        // 保存评论
        commentRepository.save(comment);
    }
}