package com.example.qimo.repository;

import com.example.qimo.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    
    // 添加查找用户及其收藏列表的方法，使用EntityGraph预加载避免N+1查询
    @EntityGraph(attributePaths = "favorites")
    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findUserWithFavoritesByUsername(String username);
    
    // 检查用户是否已收藏特定书籍
    @Query("SELECT COUNT(*) > 0 FROM User u JOIN u.favorites f WHERE u.id = :userId AND f.id = :bookId")
    boolean existsFavoriteByUserIdAndBookId(Long userId, Long bookId);
}