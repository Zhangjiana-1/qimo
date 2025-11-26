package com.example.qimo.controller.admin;

import com.example.qimo.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/categories";
    }

    @PostMapping
    public String createCategory(@RequestParam("name") String name) {
        try {
            categoryService.createCategory(name);
            return "redirect:/admin/categories?success=分类创建成功";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/categories?error=" + e.getMessage();
        }
    }

    @DeleteMapping("/{id}")
    public String deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return "redirect:/admin/categories?success=分类删除成功";
        } catch (IllegalStateException e) {
            return "redirect:/admin/categories?error=" + e.getMessage();
        }
    }

    @PostMapping("/{id}/update")
    public String updateCategory(@PathVariable Long id, @RequestParam("name") String name) {
        try {
            categoryService.updateCategory(id, name);
            return "redirect:/admin/categories?success=分类更新成功";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/categories?error=" + e.getMessage();
        }
    }
}