package com.example.qimo.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = {"likedBy", "book", "user", "parent", "replies"})
@Entity
@Table(name = "comments", schema = "book_db", catalog = "book_db")
@EntityListeners(AuditingEntityListener.class)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> replies = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(
        name = "comment_likes",
        joinColumns = @JoinColumn(name = "comment_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likedBy = new HashSet<>();
    
    // 辅助方法，判断当前用户是否已点赞
    public boolean isLikedBy(User user) {
        return likedBy.contains(user);
    }

    // 辅助方法，判断用户名对应的用户是否已点赞（用于模板中通过用户名判断）
    public boolean isLikedByUsername(String username) {
        if (username == null) return false;
        return likedBy.stream().anyMatch(u -> username.equals(u.getUsername()));
    }
    
    // 获取点赞数量
    public int getLikeCount() {
        return likedBy.size();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // 保留一组显式 getter/setter 以确保在某些构建环境中 Lombok 注解处理器失败时，仍然能编译
    public Long getId() { return this.id; }
    public String getContent() { return this.content; }
    public void setContent(String content) { this.content = content; }
    public java.time.LocalDateTime getCreatedAt() { return this.createdAt; }
    public Book getBook() { return this.book; }
    public void setBook(Book book) { this.book = book; }
    public User getUser() { return this.user; }
    public void setUser(User user) { this.user = user; }
    public Comment getParent() { return this.parent; }
    public void setParent(Comment parent) { this.parent = parent; }
    public List<Comment> getReplies() { return this.replies; }
    public Set<User> getLikedBy() { return this.likedBy; }
}