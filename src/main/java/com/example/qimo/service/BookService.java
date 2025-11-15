package com.example.qimo.service;

import com.example.qimo.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {
    
    /**
     * 获取所有书籍，按创建时间倒序排列
     */
    List<Book> findAllBooks();
    
    /**
     * 分页获取所有书籍
     * @param pageable 分页参数
     * @return 书籍分页对象
     */
    Page<Book> findAll(Pageable pageable);
    
    /**
     * 根据书名关键词搜索书籍
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 符合条件的书籍分页对象
     */
    Page<Book> searchBooksByTitle(String keyword, Pageable pageable);
    
    /**
     * 根据ID查找书籍（新方法）
     * @param id 书籍ID
     * @return 书籍对象
     * @throws javax.persistence.EntityNotFoundException 当书籍不存在时抛出
     */
    Book findById(Long id);
    
    /**
     * 根据ID查找书籍（兼容旧方法）
     * @param id 书籍ID
     * @return 书籍对象
     * @throws javax.persistence.EntityNotFoundException 当书籍不存在时抛出
     */
    Book findBookById(Long id);
    
    /**
     * 保存书籍
     * @param book 书籍对象
     * @return 保存后的书籍对象
     */
    Book save(Book book);
    
    /**
     * 根据ID删除书籍
     * @param id 书籍ID
     */
    void deleteById(Long id);
}