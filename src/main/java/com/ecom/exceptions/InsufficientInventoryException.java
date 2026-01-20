package com.ecom.exceptions;

public class InsufficientInventoryException extends Exception {
    private static final long serialVersionUID = 1L;

    private final long productId;
    private final int requested;
    private final int available;

    public InsufficientInventoryException(long productId, int requested, int available) {
        super(String.format("Insufficient inventory for product %d: requested=%d available=%d", productId, requested, available));
        this.productId = productId;
        this.requested = requested;
        this.available = available;
    }

    public InsufficientInventoryException(long productId, int requested, int available, Throwable cause) {
        super(String.format("Insufficient inventory for product %d: requested=%d available=%d", productId, requested, available), cause);
        this.productId = productId;
        this.requested = requested;
        this.available = available;
    }

    public long getProductId() {
        return productId;
    }

    public int getRequested() {
        return requested;
    }

    public int getAvailable() {
        return available;
    }
}
