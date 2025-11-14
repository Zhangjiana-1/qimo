package com.example.qimo.service;

import com.example.qimo.entity.Book;
import com.example.qimo.repository.BookRepository;
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
public class BookService {
    
    private final BookRepository bookRepository;
    
    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    
    /**
     * 获取所有书籍，按创建时间倒序排列
     */
    public List<Book> findAllBooks() {
        return bookRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }
    
    /**
     * 分页获取所有书籍
     * @param pageable 分页参数
     * @return 书籍分页对象
     */
    public Page<Book> findAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }
    
    /**
     * 根据ID查找书籍
     * @param id 书籍ID
     * @return 书籍对象
     * @throws EntityNotFoundException 当书籍不存在时抛出
     */
    public Book findBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("书籍不存在: " + id));
    }
}