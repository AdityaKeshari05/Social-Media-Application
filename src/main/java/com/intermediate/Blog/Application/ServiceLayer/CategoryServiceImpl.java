package com.intermediate.Blog.Application.ServiceLayer;

import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.Category;
import com.intermediate.Blog.Application.Repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService{


    @Autowired
    private CategoryRepository categoryRepo;


    @Override
    public Category createCategory(Category category) {
        return categoryRepo.save(category);
    }

    @Override
    public Category updateCategory(Category category, Long categoryId) {
        Category category1 = categoryRepo.findById(categoryId).orElseThrow(()-> new ResourceNotFoundException("Category", "id", categoryId));
        category1.setDescription(category.getDescription());
        category1.setName(category.getName());
        return category1;
    }

    @Override
    public Category getCategoryById(Long categoryId) {
        return categoryRepo.findById(categoryId).orElseThrow(()-> new ResourceNotFoundException("Category", "id", categoryId));

    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }

    @Override
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepo.findById(categoryId).orElseThrow(()-> new ResourceNotFoundException("Category","id",categoryId));
        categoryRepo.delete(category);
    }
}
