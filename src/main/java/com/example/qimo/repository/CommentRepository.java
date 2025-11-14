package com.example.qimo.repository;

import com.example.qimo.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    /**
     * 根据书籍ID查询评论，并按创建时间降序排序
     * @param bookId 书籍ID
     * @return 评论列表
     */
    List<Comment> findByBookIdOrderByCreatedAtDesc(Long bookId);
}