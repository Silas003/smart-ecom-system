package com.ecom.exceptions;

public class DuplicateEntityException extends ValidationException {
    private static final long serialVersionUID = 1L;

    private final String entityName;
    private final String duplicateField;

    public DuplicateEntityException(String entityName, String duplicateField, Object duplicateValue) {
        super(String.format("Duplicate %s: %s = %s", entityName, duplicateField, duplicateValue), duplicateField, duplicateValue);
        this.entityName = entityName;
        this.duplicateField = duplicateField;
    }

    public DuplicateEntityException(String entityName, String duplicateField, Object duplicateValue, Throwable cause) {
        super(String.format("Duplicate %s: %s = %s", entityName, duplicateField, duplicateValue), duplicateField, duplicateValue, cause);
        this.entityName = entityName;
        this.duplicateField = duplicateField;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getDuplicateField() {
        return duplicateField;
    }
}
