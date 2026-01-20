package com.ecom.services;

import com.ecom.dao.CategoryDao;
import com.ecom.models.Category;
import com.ecom.utils.ValidationUtils;
import com.ecom.exceptions.DaoException;
import com.ecom.exceptions.DuplicateEntityException;
import com.ecom.exceptions.ValidationException;

import java.sql.SQLException;
import java.util.List;

/**
 * Service layer for category management; validates input and delegates to {@code CategoryDao}.
 */
public class CategoryService {
    private final CategoryDao categoryDAO;

    private static final CategoryService INSTANCE = new CategoryService(new CategoryDao());
    public static CategoryService getInstance() { return INSTANCE; }

    public CategoryService() {
        this.categoryDAO = new CategoryDao();
    }

    public CategoryService(CategoryDao categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    /**
     * Creates a new category with validation.
     * @throws ValidationException if category name is empty or invalid
     * @throws DuplicateEntityException if category with same name already exists
     * @throws DaoException if database operation fails
     */
    public void createCategory(Category category) throws ValidationException, DuplicateEntityException, DaoException {
        if (category == null) {
            throw new ValidationException("Category is required");
        }

        String name = category.getName();
        ValidationUtils.requireNonEmpty(name, "category name");

        // Check for duplicate category name (case-insensitive)
        try {
            List<Category> existingCategories = categoryDAO.findAll();
            for (Category existing : existingCategories) {
                if (existing.getName().equalsIgnoreCase(name.trim())) {
                    throw new DuplicateEntityException("Category with name '" + name + "' already exists");
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to check for duplicate category", e);
        }

        try {
            categoryDAO.create(category);
        } catch (SQLException e) {

            if (e.getSQLState() != null && e.getSQLState().equals("23505")) { // Postgres unique violation
                throw new DuplicateEntityException("Category with name '" + name + "' already exists");
            }

            if (e.getMessage() != null && (e.getMessage().contains("unique") ||
                e.getMessage().contains("duplicate"))) {
                throw new DuplicateEntityException("Category with name '" + name + "' already exists");
            }
            throw new DaoException("Failed to create category", e);
        }
    }

    /**
     * Updates an existing category with validation.
     * @throws ValidationException if category name is empty or invalid
     * @throws DuplicateEntityException if another category with same name exists
     * @throws DaoException if database operation fails
     */
    public void updateCategory(Category category) throws ValidationException, DuplicateEntityException, DaoException {
        if (category == null) {
            throw new ValidationException("Category is required");
        }

        if (category.getCategoryId() <= 0) {
            throw new ValidationException("Category ID is required for update");
        }

        String name = category.getName();
        ValidationUtils.requireNonEmpty(name, "category name");

        // Check for duplicate category name (excluding current category)
        try {
            List<Category> existingCategories = categoryDAO.findAll();
            for (Category existing : existingCategories) {
                if (existing.getCategoryId() != category.getCategoryId() && 
                    existing.getName().equalsIgnoreCase(name.trim())) {
                    throw new DuplicateEntityException("Category with name '" + name + "' already exists");
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to check for duplicate category", e);
        }

        try {
            categoryDAO.update(category);
        } catch (SQLException e) {
            // Check if it's a unique constraint violation
            if (e.getMessage() != null && (e.getMessage().contains("unique") || 
                e.getMessage().contains("duplicate"))) {
                throw new DuplicateEntityException("Category with name '" + name + "' already exists");
            }
            throw new DaoException("Failed to update category", e);
        }
    }

    /**
     * Deletes a category.
     * @throws DaoException if database operation fails or category is in use
     */
    public void deleteCategory(int categoryId) throws DaoException {
        if (categoryId <= 0) {
            throw new DaoException("Invalid category ID");
        }

        try {
            // Check if category exists
            Category category = categoryDAO.findById(categoryId);
            if (category == null) {
                throw new com.ecom.exceptions.EntityNotFoundException("Category", categoryId);
            }

            categoryDAO.delete(categoryId);
        } catch (SQLException e) {
            // Check if it's a foreign key constraint violation (category in use)
            if (e.getMessage() != null && (e.getMessage().contains("foreign key") || 
                e.getMessage().contains("constraint") || 
                e.getMessage().contains("reference"))) {
                throw new DaoException("Cannot delete category: It is being used by one or more products", e);
            }
            throw new DaoException("Failed to delete category", e);
        } catch (com.ecom.exceptions.EntityNotFoundException e) {
            throw new DaoException(e.getMessage(), e);
        }
    }

    /**
     * Gets all categories.
     */
    public List<Category> getAllCategories() throws DaoException {
        try {
            return categoryDAO.findAll();
        } catch (SQLException e) {
            throw new DaoException("Failed to fetch categories", e);
        }
    }

    /**
     * Gets a category by ID.
     */
    public Category getCategoryById(int categoryId) throws DaoException {
        try {
            Category category = categoryDAO.findById(categoryId);
            if (category == null) {
                throw new com.ecom.exceptions.EntityNotFoundException("Category", categoryId);
            }
            return category;
        } catch (SQLException e) {
            throw new DaoException("Failed to fetch category", e);
        } catch (com.ecom.exceptions.EntityNotFoundException e) {
            throw new DaoException(e.getMessage(), e);
        }
    }
}
