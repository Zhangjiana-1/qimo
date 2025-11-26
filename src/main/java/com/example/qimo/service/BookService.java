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
    Book saveBook(Book book, Long categoryId); // 修改此处，添加categoryId参数
    void deleteBook(Long id);
    boolean existsByIsbn(String isbn);
    boolean existsByIsbnAndIdNot(String isbn, Long id);
    List<Book> getBooksByCategory(Category category);
    Page<Book> getBooksByCategoryId(Long categoryId, Pageable pageable);
}