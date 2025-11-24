package com.example.qimo.service;

import com.example.qimo.entity.Book;
import com.example.qimo.entity.User;
import com.example.qimo.repository.BookRepository;
import com.example.qimo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
public class FavoriteService {
    
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    
    @Autowired
    public FavoriteService(UserRepository userRepository, BookRepository bookRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }
    
    /**
     * 切换书籍收藏状态
     * @param bookId 书籍ID
     * @param currentUsername 当前用户名
     */
    @Transactional
    public void toggleFavorite(Long bookId, String currentUsername) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + currentUsername));
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("书籍不存在: " + bookId));
        
        if (user.getFavorites().contains(book)) {
            user.getFavorites().remove(book); // 取消收藏
        } else {
            user.getFavorites().add(book);    // 添加收藏
        }
        
        userRepository.save(user);
    }
    
    /**
     * 检查书籍是否被用户收藏
     * @param bookId 书籍ID
     * @param username 用户名
     * @return 是否已收藏
     */
    public boolean isBookFavoritedByUser(Long bookId, String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        return user != null && 
               userRepository.existsFavoriteByUserIdAndBookId(user.getId(), bookId);
    }
}
