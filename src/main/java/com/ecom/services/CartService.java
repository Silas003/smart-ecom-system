package com.ecom.services;

import com.ecom.dao.CartDao;
import com.ecom.dao.CartItemDao;
import com.ecom.models.Cart;
import com.ecom.models.CartItem;
import com.ecom.models.Product;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton service for managing the user's shopping cart in-memory.
 */
public class CartService {
    private static CartService instance;
    private Map<Integer, Integer> cart; // productId -> quantity
    private Map<Integer, List<CartItem>> cartItems;
    private CartDao cartDao;
    private CartItemDao cartItemDao;
    private SessionService sessionService;
    private CartService() {
    }

    public static CartService getInstance() {
        if (instance == null) {
            instance = new CartService();
        }
        return instance;
    }

    public void addToCart(Product product, int quantity) {
        cart.put(product.getProductId(), cart.getOrDefault(product.getProductId(), 0) + quantity);
        Cart cart = cartDao.getCartActiveByUserId(sessionService.getCurrentUser().getUserId());
        if (cart == null){
            cart = new Cart(sessionService.getCurrentUser().getUserId());
            cartDao.createCart(cart);

        }
        CartItem item = cartItemDao.findByCartIdAndProductId(cart.getId(), product.getProductId());
        if(item != null){
            item.setQuantity(item.getQuantity() + quantity);
            item.setTotalPrice(item.getUnitPrice() * item.getQuantity());
            cartItemDao.updateCartItemQuantity(item.getCartId(),item.getQuantity(),item.getTotalPrice());
            return;
        }
        CartItem cartItem = new CartItem();
        cartItem.setProductId(product.getProductId());
        cartItem.setUnitPrice(product.getPrice());
        cartItem.setTotalPrice(product.getPrice()*quantity);
        cartItem.setQuantity(quantity);
        cartItem.setCartId(cart.getId());
        cartItemDao.createCartItem(cartItem);
    }

    public void removeFromCart(int productId) {
        cart.remove(productId);
    }

    public void updateQuantity(int productId, int quantity) {
        if (quantity <= 0) {
            cart.remove(productId);
        } else {
            cart.put(productId, quantity);
        }
    }

    public int getQuantity(int productId) {
        return cart.getOrDefault(productId, 0);
    }

    public Map<Integer, Integer> getCart() {
        return new HashMap<>(cart);
    }

    public void clearCart() {
        cart.clear();
    }

    public int getTotalItems() {
        return cart.values().stream().mapToInt(Integer::intValue).sum();
    }

    public boolean isEmpty() {
        return cart.isEmpty();
    }
}
