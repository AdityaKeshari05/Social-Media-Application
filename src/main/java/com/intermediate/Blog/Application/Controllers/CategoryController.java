package com.intermediate.Blog.Application.Controllers;



import com.intermediate.Blog.Application.Models.Category;
import com.intermediate.Blog.Application.Models.User;
import com.intermediate.Blog.Application.Repositories.PostRepository;
import com.intermediate.Blog.Application.Repositories.UserRepo;
import com.intermediate.Blog.Application.ServiceLayer.CategoryService;
import com.intermediate.Blog.Application.ServiceLayer.PostService;
import com.intermediate.Blog.Application.ServiceLayer.UserService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")

public class CategoryController {

        @Autowired
        private CategoryService categoryService;

        @Autowired
        private UserService userService;

        @Autowired
        private PostRepository postRepository;

        @Autowired
        private UserRepo userRepo;

        @Autowired
        private PostService postService;


        @PutMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Category> updateCategory(@RequestBody Category category , @PathVariable Long id){


                Category updatedCategory = categoryService.updateCategory(category,id);
                return ResponseEntity.ok(updatedCategory);
        }

        @GetMapping("/{id}")
        @PreAuthorize("permitAll()")
        public ResponseEntity<Category> getCategoryById(@PathVariable Long id){
            Category category = categoryService.getCategoryById(id);

            return ResponseEntity.ok(category);
        }


        @GetMapping
        @PreAuthorize("permitAll()")
        public ResponseEntity<List<Category>> getAllCategories(){
            return ResponseEntity.ok(categoryService.getAllCategories());
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<String> deleteCategory(@PathVariable Long id){
            categoryService.deleteCategory(id);
            return ResponseEntity.ok("Category Deleted Successfully");
        }
}
