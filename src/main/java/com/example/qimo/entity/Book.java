package com.example.qimo.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private String description;
    private String coverImage;
    private Integer year;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 修改为可选关联，允许category为null
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "category_id", nullable = true)
    private Category category;
    
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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // getter和setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCoverImage() {
        return coverImage;
    }
    
    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    // 兼容旧模板使用的 coverUrl 属性
    public String getCoverUrl() {
        return this.coverImage;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverImage = coverUrl;
    }
    
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Category getter和setter
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
}