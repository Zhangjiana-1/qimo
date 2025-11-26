package com.example.qimo.repository;

import com.example.qimo.entity.Book;
import com.example.qimo.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    /**
     * 根据书名或作者关键词和分类ID搜索书籍
     * @param keyword 搜索关键词
     * @param categoryId 分类ID
     * @param pageable 分页参数
     * @return 符合条件的书籍分页对象
     */
    @Query("SELECT b FROM Book b WHERE (b.title LIKE %:keyword% OR b.author LIKE %:keyword%) AND b.category.id = :categoryId")
    Page<Book> findByTitleContainingOrAuthorContainingAndCategoryId(@Param("keyword") String keyword, @Param("categoryId") Long categoryId, Pageable pageable);
    
    /**
     * 根据书名或作者关键词搜索书籍
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 符合条件的书籍分页对象
     */
    @Query("SELECT b FROM Book b WHERE b.title LIKE %:keyword% OR b.author LIKE %:keyword%")
    Page<Book> findByTitleContainingOrAuthorContaining(@Param("keyword") String keyword, Pageable pageable);
}