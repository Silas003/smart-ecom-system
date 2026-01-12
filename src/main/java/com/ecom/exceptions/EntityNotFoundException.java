package com.ecom.exceptions;

public class EntityNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    private final String entityName;
    private final Object lookupValue;

    public EntityNotFoundException(String entityName, Object lookupValue) {
        super(String.format("%s not found: %s", entityName, lookupValue));
        this.entityName = entityName;
        this.lookupValue = lookupValue;
    }

    public EntityNotFoundException(String entityName, Object lookupValue, Throwable cause) {
        super(String.format("%s not found: %s", entityName, lookupValue), cause);
        this.entityName = entityName;
        this.lookupValue = lookupValue;
    }

    public String getEntityName() {
        return entityName;
    }

    public Object getLookupValue() {
        return lookupValue;
    }
}
