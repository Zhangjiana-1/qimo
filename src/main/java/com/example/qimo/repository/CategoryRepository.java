package com.example.qimo.repository;

import com.example.qimo.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
    List<Category> findAllByOrderByNameAsc();
    
    @Query("SELECT COUNT(b) FROM Book b WHERE b.category.id = :categoryId")
    long countByBooksCategoryId(@Param("categoryId") Long categoryId);
}