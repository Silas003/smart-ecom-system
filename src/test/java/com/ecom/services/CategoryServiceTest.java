package com.ecom.services;

import com.ecom.dao.CategoryDao;
import com.ecom.models.Category;
import com.ecom.exceptions.DaoException;
import com.ecom.exceptions.DuplicateEntityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CategoryServiceTest {
    private CategoryDao mockDao;
    private CategoryService service;

    @BeforeEach
    public void setup() {
        mockDao = Mockito.mock(CategoryDao.class);
        service = new CategoryService(mockDao);
    }

    @Test
    public void createCategory_duplicateThrows() throws Exception {
        Category c = new Category(); c.setName("Books");
        when(mockDao.findAll()).thenReturn(List.of(new Category(1, "Books")));
        assertThrows(DuplicateEntityException.class, () -> service.createCategory(c));
    }
}
