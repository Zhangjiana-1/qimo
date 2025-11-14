package com.example.qimo.repository;

import com.example.qimo.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    // 无需自定义查询方法，JpaRepository已提供基础CRUD操作
}
