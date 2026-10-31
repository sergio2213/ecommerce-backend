package com.ecommerce.service;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.mapper.CartItemMapper;
import com.ecommerce.mapper.CartMapper;
import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.exception.InvalidQuantityException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ProductOutOfStockException;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductService productService, CartMapper cartMapper, CartItemMapper cartItemMapper) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
        this.cartMapper = cartMapper;
        this.cartItemMapper = cartItemMapper;
    }

    public Cart createCartForUser(User user) {
        Cart newCart = new Cart();
        newCart.setUser(user);
        return this.cartRepository.save(newCart);
    }

    @Transactional
    public Optional<CartItemDTO> addProductToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity cannot be zero or negative");
        }
        Cart cart = getCartOrThrow(userId);
        Optional<Product> optionalProduct = this.productService.getProductById(productId);
        Product product = optionalProduct.orElseThrow(() -> new NoSuchElementException("Product not found with id: " + productId));
        if (product.getStock() == 0) {
            throw new ProductOutOfStockException("Product is out of stock");
        }
        CartItem savedItem;
        Optional<CartItem> optionalCartItem = this.cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
        int totalQuantity = optionalCartItem.map(CartItem::getQuantity).orElse(0) + quantity;
        if (totalQuantity > product.getStock()) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }
        CartItem cartItem = optionalCartItem.orElseGet(() -> {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            return newItem;
        });
        cartItem.setQuantity(totalQuantity);
        savedItem = this.cartItemRepository.save(cartItem);
        return Optional.of(this.cartItemMapper.toCartItemDto(savedItem));
    }

    public Cart saveCart(Cart cart) {
        return this.cartRepository.save(cart);
    }

    public Optional<CartDTO> getCartDTOByUserId(Long userId) {
        return this.cartRepository.findByUserId(userId).map(this::toCartDto);
    }

    public CartDTO toCartDto(Cart cart) {
        return this.cartMapper.toCartDto(cart);
    }

    @Transactional
    public void clearCartItems(Long cartId) {
        this.cartItemRepository.deleteAll(this.cartItemRepository.findByCartId(cartId));
    }

    public Cart getCartOrThrow(Long userId) {
        return this.cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Cart not found for user with ID: " + userId));
    }
}
