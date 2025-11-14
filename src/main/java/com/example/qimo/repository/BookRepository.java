package com.example.qimo.repository;

import com.example.qimo.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    // 无需自定义查询方法，JpaRepository已提供基础CRUD操作
    
    /**
     * 根据书名关键词模糊搜索书籍（不区分大小写）
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 符合条件的书籍分页对象
     */
    Page<Book> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
}