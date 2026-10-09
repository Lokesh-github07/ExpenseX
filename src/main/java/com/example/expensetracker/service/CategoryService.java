package com.example.expensetracker.service;

import com.example.expensetracker.dto.request.CategoryRequest;
import com.example.expensetracker.dto.response.ApiResponse;
import com.example.expensetracker.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request);

    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategoryById(Long id);

    CategoryResponse updateCategory(Long id,
                                    CategoryRequest request);

    ApiResponse deleteCategory(Long id);
}