package com.example.qimo.service;

import com.example.qimo.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    Category createCategory(String name);
    void updateCategory(Long id, String name);
    void deleteCategory(Long id);
    Category findById(Long id);
}