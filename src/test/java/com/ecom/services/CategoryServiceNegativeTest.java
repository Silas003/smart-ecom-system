package com.ecom.services;

import com.ecom.models.Category;
import com.ecom.exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryServiceNegativeTest {
    private CategoryService service;

    @BeforeEach
    public void setup() {
        service = new CategoryService();
    }

    @Test
    public void createNullThrows() {
        assertThrows(ValidationException.class, () -> service.createCategory(null));
    }

    @Test
    public void updateWithoutIdThrows() {
        Category c = new Category(); c.setName("X");
        assertThrows(ValidationException.class, () -> service.updateCategory(c));
    }
}
