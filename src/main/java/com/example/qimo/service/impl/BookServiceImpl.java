package com.example.qimo.service.impl;

import com.example.qimo.entity.Book;
import com.example.qimo.repository.BookRepository;
import com.example.qimo.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
@Transactional
public class BookServiceImpl implements BookService {
    
    private final BookRepository bookRepository;
    
    @Autowired
    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    
    @Override
    public List<Book> findAllBooks() {
        return bookRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }
    
    @Override
    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }
    
    @Override
    public Page<Book> searchBooksByTitle(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            // 如果关键词为空，返回所有书籍
            return findAll(pageable);
        }
        // 否则进行关键词搜索
        return bookRepository.findByTitleContainingIgnoreCase(keyword.trim(), pageable);
    }
    
    @Override
    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("书籍不存在: " + id));
    }
    
    @Override
    public Book findBookById(Long id) {
        // 调用findById方法，保持代码一致性
        return findById(id);
    }
    
    @Override
    public Book save(Book book) {
        return bookRepository.save(book);
    }
    
    @Override
    public void deleteById(Long id) {
        // 先检查书籍是否存在
        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException("书籍不存在: " + id);
        }
        bookRepository.deleteById(id);
    }
}