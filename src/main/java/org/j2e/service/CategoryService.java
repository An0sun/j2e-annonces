package org.j2e.service;

import org.j2e.bean.Category;
import org.j2e.dao.CategoryRepository;

import java.util.List;

/**
 * Service métier pour les Catégories.
 */
public class CategoryService {

    private CategoryRepository categoryRepository;

    public CategoryService() {
        this.categoryRepository = new CategoryRepository();
    }

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }


    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public void createCategory(Category category) {
        categoryRepository.save(category);
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }
}
