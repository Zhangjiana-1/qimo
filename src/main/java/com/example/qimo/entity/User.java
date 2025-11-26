package com.example.qimo.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = {"likedComments", "favoriteBooks"})
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email")
})
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // 将使用 BCrypt 加密

    @Column(nullable = false)
    private String email;

    private String nickname;

    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'USER'")
    private Role role = Role.USER;
    
    // 添加enabled字段，默认为true（启用状态）
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean enabled = true;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @ManyToMany(mappedBy = "likedBy")
    private Set<Comment> likedComments = new HashSet<>();
    
    // 添加收藏书籍的多对多关系
    @ManyToMany
    @JoinTable(
        name = "user_favorite_books",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    private Set<Book> favoriteBooks = new HashSet<>();

    public enum Role {
        USER, ADMIN
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Explicit getters/setters for environments where Lombok fails
    public Long getId() { return this.id; }
    public String getUsername() { return this.username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return this.password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }
    public String getNickname() { return this.nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public Role getRole() { return this.role; }
    public void setRole(Role role) { this.role = role; }
    public Boolean getEnabled() { return this.enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Set<Book> getFavoriteBooks() { return this.favoriteBooks; }
}
