package com.example.qimo.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "books")
@EntityListeners(AuditingEntityListener.class)
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String author;
    private String coverUrl;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE)
    private List<Comment> comments;
    
    // 添加收藏者关系
    @ManyToMany(mappedBy = "favoriteBooks")
    private Set<User> favoritedBy = new HashSet<>();

    /**
     * 占位方法：模板中会读取 `averageRating`，但当前项目尚未实现评分系统。
     * 为避免模板在不存在该属性时报错（Thymeleaf 在解析时会尝试读取属性），
     * 提供一个空实现，返回 null 表示暂无评分。
     */
    public Double getAverageRating() {
        return null;
    }
}