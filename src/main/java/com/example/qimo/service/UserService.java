package com.example.qimo.service;

import com.example.qimo.entity.User;
import com.example.qimo.entity.Book;
import com.example.qimo.repository.UserRepository;
import com.example.qimo.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, BookRepository bookRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(User user, String rawPassword) {
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    // 切换用户启用/禁用状态
    public void toggleUserEnabled(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setEnabled(!user.getEnabled());
            userRepository.save(user);
        }
    }
    
    // 重置用户密码为默认值
    public void resetPassword(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(passwordEncoder.encode("User@123"));
            userRepository.save(user);
        }
    }

    // 更新用户资料
    public void updateProfile(String currentUsername, String nickname, String email) {
        User user = userRepository.findByUsername(currentUsername).orElseThrow();
        if (userRepository.existsByEmailAndIdNot(email, user.getId())) {
            throw new IllegalArgumentException("该邮箱已被其他用户绑定");
        }
        user.setNickname(nickname);
        user.setEmail(email);
        userRepository.save(user);
    }

    // 修改密码
    public void changePassword(String currentUsername, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(currentUsername).orElseThrow();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("当前密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // 根据用户名获取用户
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
    }
    
    // 检查用户是否已收藏指定书籍
    public boolean existsFavoriteByUsernameAndBookId(String username, Long bookId) {
        return userRepository.existsByUsernameAndFavoriteBooksId(username, bookId);
    }
    
    // 获取用户收藏的书籍列表（分页）
    @Transactional
    public Page<Book> getFavoriteBooks(String username, Pageable pageable) {
        return userRepository.findFavoriteBooksByUsername(username, pageable);
    }
    
    // 添加书籍到收藏
    @Transactional
    public void addToFavorite(String username, Long bookId) {
        User user = getUserByUsername(username);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("书籍不存在"));
        
        user.getFavoriteBooks().add(book);
        userRepository.save(user);
    }
    
    // 从收藏中移除书籍
    @Transactional
    public void removeFromFavorite(String username, Long bookId) {
        User user = getUserByUsername(username);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("书籍不存在"));
        
        user.getFavoriteBooks().remove(book);
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .disabled(!user.getEnabled()) // 关联enabled字段到Spring Security的disabled状态
                .build();
    }
}