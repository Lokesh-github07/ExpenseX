package com.example.expensetracker.service.impl;

import com.example.expensetracker.dto.request.CategoryRequest;
import com.example.expensetracker.dto.response.ApiResponse;
import com.example.expensetracker.dto.response.CategoryResponse;
import com.example.expensetracker.exception.DuplicateResourceException;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.model.Category;
import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.security.UserPrincipal;
import com.example.expensetracker.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public CategoryResponse createCategory(
            CategoryRequest request) {

        if (categoryRepository.existsByName(
                request.getName())) {

            throw new DuplicateResourceException(
                    "Category already exists"
            );
        }

        Category category =
                modelMapper.map(
                        request,
                        Category.class
                );

        UserPrincipal principal = (UserPrincipal)
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();

        category.setUser(principal.getUser());

        category = categoryRepository.save(category);

        return modelMapper.map(
                category,
                CategoryResponse.class
        );
    }

    @Override
    public List<CategoryResponse> getAllCategories() {

        return categoryRepository.findAll()
                .stream()
                .map(category ->
                        modelMapper.map(
                                category,
                                CategoryResponse.class
                        ))
                .toList();
    }

    @Override
    public CategoryResponse getCategoryById(
            Long id) {

        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Category not found with id: " + id
                                ));

        return modelMapper.map(
                category,
                CategoryResponse.class
        );
    }

    @Override
    public CategoryResponse updateCategory(
            Long id,
            CategoryRequest request) {

        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Category not found with id: " + id
                                ));

        category.setName(
                request.getName()
        );

        category.setDescription(
                request.getDescription()
        );

        category =
                categoryRepository.save(category);

        return modelMapper.map(
                category,
                CategoryResponse.class
        );
    }

    @Override
    public ApiResponse deleteCategory(
            Long id) {

        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Category not found with id: " + id
                                ));

        categoryRepository.delete(category);

        return new ApiResponse(
                true,
                "Category deleted successfully"
        );
    }
}