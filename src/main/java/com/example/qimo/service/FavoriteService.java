package com.example.qimo.service;

import com.example.qimo.entity.User;
import com.example.qimo.entity.Book;
import com.example.qimo.repository.UserRepository;
import com.example.qimo.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriteService {
    
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    
    @Autowired
    public FavoriteService(UserRepository userRepository, BookRepository bookRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }
    
    // 检查用户是否已收藏指定书籍（根据用户ID和书籍ID）
    @Transactional(readOnly = true)
    public boolean existsFavoriteByUserIdAndBookId(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        return user.getFavoriteBooks().stream()
                .anyMatch(book -> book.getId().equals(bookId));
    }
    
    // 添加书籍到收藏
    @Transactional
    public void addToFavorite(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("书籍不存在"));
        
        user.getFavoriteBooks().add(book);
        userRepository.save(user);
    }
    
    // 从收藏中移除书籍
    @Transactional
    public void removeFromFavorite(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("书籍不存在"));
        
        user.getFavoriteBooks().remove(book);
        userRepository.save(user);
    }
}