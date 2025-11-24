package com.example.qimo.repository;

import com.example.qimo.entity.User;
import com.example.qimo.entity.Book;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    
    // 检查用户是否已收藏指定书籍
    boolean existsByUsernameAndFavoriteBooksId(String username, Long bookId);
    
    // 修改后的查询：使用JOIN查询用户收藏的书籍
    @Query("SELECT b FROM User u JOIN u.favoriteBooks b WHERE u.username = :username")
    Page<Book> findFavoriteBooksByUsername(@Param("username") String username, Pageable pageable);
}