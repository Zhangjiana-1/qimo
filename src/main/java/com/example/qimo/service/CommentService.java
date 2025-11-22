package com.example.qimo.service;

import com.example.qimo.entity.Comment;
import javax.persistence.EntityNotFoundException;
import java.util.List;

public interface CommentService {
    
    /**
     * 根据评论ID切换点赞状态
     * @param commentId 评论ID
     * @param currentUsername 当前用户名
     * @throws EntityNotFoundException 当评论不存在时抛出
     */
    void toggleLike(Long commentId, String currentUsername);
    
    /**
     * 根据ID查找评论
     * @param id 评论ID
     * @return 评论对象
     * @throws EntityNotFoundException 当评论不存在时抛出
     */
    Comment findById(Long id);
    
    /**
     * 根据书籍ID获取评论列表
     * @param bookId 书籍ID
     * @return 评论列表
     */
    List<Comment> getCommentsByBookId(Long bookId);
    
    /**
     * 添加评论
     * @param bookId 书籍ID
     * @param content 评论内容
     * @param username 用户名
     */
    void addComment(Long bookId, String content, String username);
}