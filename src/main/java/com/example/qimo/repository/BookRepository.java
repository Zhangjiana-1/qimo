package com.example.qimo.repository;

import com.example.qimo.entity.Book;
import com.example.qimo.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByIsbn(String isbn);
    boolean existsByIsbnAndIdNot(String isbn, Long id);
    List<Book> findByCategory(Category category);
    Page<Book> findByCategoryId(Long categoryId, Pageable pageable);
    
    /**
     * 根据书名关键词模糊搜索书籍（不区分大小写）
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 符合条件的书籍分页对象
     */
    Page<Book> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
}