package com.example.qimo.service;

import com.example.qimo.entity.Book;
import com.example.qimo.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface BookService {
    List<Book> getAllBooks();
    Page<Book> getBooks(Pageable pageable);
    Optional<Book> getBookById(Long id);
    Book saveBook(Book book, Long categoryId);
    void deleteBook(Long id);
    boolean existsByIsbn(String isbn);
    boolean existsByIsbnAndIdNot(String isbn, Long id);
    List<Book> getBooksByCategory(Category category);
    Page<Book> getBooksByCategoryId(Long categoryId, Pageable pageable);
    
    /**
     * 按分类筛选书籍
     * @param categoryId 分类ID，为null时返回所有书籍
     * @param pageable 分页参数
     * @return 符合条件的书籍分页对象
     */
    Page<Book> findBooksByCategory(Long categoryId, Pageable pageable);
    
    /**
     * 按关键词和分类筛选书籍
     * @param keyword 搜索关键词
     * @param categoryId 分类ID
     * @param pageable 分页参数
     * @return 符合条件的书籍分页对象
     */
    Page<Book> searchBooksByCategory(String keyword, Long categoryId, Pageable pageable);
}