package com.example.qimo.service.impl;

import com.example.qimo.entity.Comment;
import com.example.qimo.entity.Book;
import com.example.qimo.entity.User;
import com.example.qimo.repository.CommentRepository;
import com.example.qimo.repository.UserRepository;
import com.example.qimo.repository.BookRepository;
import com.example.qimo.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {
    
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    
    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, UserRepository userRepository, BookRepository bookRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }
    
    @Override
    @Transactional // 确保方法在事务内执行
    public void toggleLike(Long commentId, String currentUsername) {
        // 1. 先查询用户，避免后续操作中再次查询
        User user = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + currentUsername));
        
        // 2. 使用findById方法（已添加@EntityGraph）查询评论，确保likedBy集合被正确加载
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new EntityNotFoundException("评论不存在: " + commentId));
        
        // 3. 操作点赞状态 - 先检查是否已经在集合中
        Set<User> likedUsers = comment.getLikedBy();
        if (likedUsers != null) {
            boolean isLiked = false;
            // 手动检查用户是否已点赞，避免懒加载问题
            for (User likedUser : likedUsers) {
                if (likedUser.getId().equals(user.getId())) {
                    isLiked = true;
                    break;
                }
            }
            
            if (isLiked) {
                // 已点赞，取消点赞
                likedUsers.remove(user);
            } else {
                // 未点赞，添加点赞
                likedUsers.add(user);
            }
        }
        
        // 4. 显式保存评论（确保更新被写入数据库）
        commentRepository.saveAndFlush(comment);
    }
    
    @Override
    public Comment findById(Long id) {
        return commentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("评论不存在: " + id));
    }
    
    @Override
    public List<Comment> getCommentsByBookId(Long bookId) {
        return commentRepository.findByBookIdOrderByCreatedAtDesc(bookId);
    }
    
    @Override
    public List<Comment> getCommentsWithRepliesByBookId(Long bookId) {
        // 先一次性加载该书的所有评论（包含主评论和回复），用于构建 parentId -> children 映射
        List<Comment> allComments = commentRepository.findByBookIdOrderByCreatedAtDesc(bookId);

        // 构建 parentId -> List<comment> 映射
        java.util.Map<Long, java.util.List<Comment>> childrenMap = new java.util.HashMap<>();
        for (Comment c : allComments) {
            if (c.getParent() != null && c.getParent().getId() != null) {
                childrenMap.computeIfAbsent(c.getParent().getId(), k -> new java.util.ArrayList<>()).add(c);
            }
        }

        // 使用专门的查询获取数据库中实际的根评论（parent == null），避免因为内存中对象状态不一致而误判
        java.util.List<Comment> roots = commentRepository.findByBookIdAndParentIsNullOrderByCreatedAtDesc(bookId);

        // 去重根评论（有时由于左连接或查询原因数据库返回的列表可能包含重复的实体）
        java.util.Map<Long, Comment> uniqueRoots = new java.util.LinkedHashMap<>();
        for (Comment root : roots) {
            if (root != null && root.getId() != null) {
                uniqueRoots.putIfAbsent(root.getId(), root);
            }
        }

        java.util.List<Comment> dedupedRoots = new java.util.ArrayList<>(uniqueRoots.values());

        // 在内存中递归填充 replies 列表（不替换集合实例）
        for (Comment root : dedupedRoots) {
            populateRepliesFromMap(root, childrenMap);
        }

        return dedupedRoots;
    }
    
    @Override
    @Transactional
    public void addComment(Long bookId, String content, String username, Long parentId) {
        // 验证用户是否存在
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        
        if (parentId != null) {
            // 处理回复评论的情况
            Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("父评论不存在: " + parentId));
            
            // 验证父评论所属书籍是否与当前书籍一致
            if (!parentComment.getBook().getId().equals(bookId)) {
                throw new IllegalArgumentException("不能回复其他书籍的评论");
            }
            
            // 设置父评论关系
            comment.setParent(parentComment);
            comment.setBook(parentComment.getBook()); // 确保回复的书籍与父评论一致
            
            // 维护双向关系
            parentComment.getReplies().add(comment);
        } else {
            // 处理主评论的情况
            Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("书籍不存在: " + bookId));
            comment.setBook(book);
        }
        
        // 先保存评论，确保ID生成并持久化
        commentRepository.saveAndFlush(comment);

        // 如果有父评论，确保父对象的replies集合包含该子评论并刷新（有助于事务内一致性）
        if (parentId != null) {
            // parentComment 已在方法中加载并为持久化实体
            // 显式刷新父对象到数据库，以便在随后读取时能看到最新的子集合
            commentRepository.flush();
        }
    }

    @Override
    @Transactional
    public void deleteCommentById(Long commentId, String currentUsername, boolean isAdmin) {
        // 查询评论是否存在
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new EntityNotFoundException("评论不存在"));
        
        // 权限校验：本人 or 管理员
        if (!isAdmin && !comment.getUser().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("无权删除他人评论");
        }
        
        // 执行删除（级联删除子回复）
        // Comment实体中已配置了orphanRemoval = true和cascade = CascadeType.REMOVE
        commentRepository.deleteById(commentId);
    }

    /**
     * 递归加载指定评论的子回复并为每个子回复继续加载其子回复
     */
    private void populateRepliesFromMap(Comment parent, java.util.Map<Long, java.util.List<Comment>> childrenMap) {
        if (parent == null) return;

        java.util.List<Comment> children = childrenMap.get(parent.getId());
        java.util.List<Comment> existing = parent.getReplies();
        existing.clear();
        if (children != null && !children.isEmpty()) {
            existing.addAll(children);
            for (Comment child : children) {
                populateRepliesFromMap(child, childrenMap);
            }
        }
    }
}