package com.example.qimo.repository;

import com.example.qimo.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    /**
     * 根据书籍ID查询评论，并按创建时间降序排序
     * @param bookId 书籍ID
     * @return 评论列表
     */
    @EntityGraph(attributePaths = {"user", "likedBy"})
    List<Comment> findByBookIdOrderByCreatedAtDesc(Long bookId);
    
    /**
     * 根据ID查询评论，并预加载点赞用户集合
     * @param id 评论ID
     * @return 评论对象（带点赞用户集合）
     */
    @EntityGraph(attributePaths = {"user", "likedBy"})
    Optional<Comment> findById(Long id);
}