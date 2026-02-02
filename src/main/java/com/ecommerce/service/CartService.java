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
    public Optional<CartItemDTO> addProductToCart(Long cartId, Long productId, int quantity) {
        Optional<Cart> optionalCart = this.cartRepository.findById(cartId);
        Cart cart = optionalCart.orElseThrow(() -> new NoSuchElementException("Cart not found with id: " + cartId));
        Optional<Product> optionalProduct = this.productService.getProductById(productId);
        Product product = optionalProduct.orElseThrow(() -> new NoSuchElementException("Product not found with id: " + productId));
        if (product.getStock() == 0) {
            throw new IllegalStateException("Product is out of stock");
        }
        if (product.getStock() < quantity) {
            throw new IllegalStateException("Insufficient stock for product: " + product.getName());
        }
        CartItem savedItem;
        Optional<CartItem> optionalCartItem = this.cartItemRepository.findByCartIdAndProductId(cartId, productId);
        if(optionalCartItem.isPresent()) {
            CartItem existingItem = optionalCartItem.get();
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            savedItem = this.cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            savedItem = this.cartItemRepository.save(newItem);
        }
        return Optional.of(this.cartItemMapper.toCartItemDto(savedItem));
    }

    public Cart saveCart(Cart cart) {
        return this.cartRepository.save(cart);
    }

    public Optional<Cart> getCartByUserId(Long userId) {
        return this.cartRepository.findById(userId);
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
}
