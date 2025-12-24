package com.intermediate.Blog.Application.ServiceLayer;


import com.intermediate.Blog.Application.Models.Category;

import java.util.List;


public interface CategoryService {

    Category createCategory(Category category);

    Category updateCategory(Category category, Long categoryId);

    Category getCategoryById(Long categoryId);

    List<Category> getAllCategories();

    void deleteCategory(Long categoryId);
}
