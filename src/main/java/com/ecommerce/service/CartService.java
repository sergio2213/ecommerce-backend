package com.ecommerce.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.CartItemDTO;
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
    private final UserService userService;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductService productService, UserService userService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
        this.userService = userService;
    }

    public Cart getOrCreateCartByUserId(Long userId) {
        return this.cartRepository.findByUserId(userId).orElseGet(() -> {
            // creamos un usuario de ejemplo
            User user = new User();
            user.setId(userId);
            // también un carrito para dicho usuario
            Cart newCart = new Cart();
            newCart.setUser(user);
            return this.cartRepository.save(newCart);
        });
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
        CartItem savedItem;
        Optional<CartItem> optionalCartItem = this.cartItemRepository.findByCartIdAndProductId(cartId, productId);
        if(optionalCartItem.isPresent()) {
            // existe el producto en el carrito
            CartItem existingItem = optionalCartItem.get();
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            savedItem = this.cartItemRepository.save(existingItem);
        } else {
            // no existe el producto en el carrito
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            savedItem = this.cartItemRepository.save(newItem);
        }
        return Optional.of(convertToCartItemDTO(savedItem));
    }

    public CartItemDTO convertToDto(CartItem cartItem) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(cartItem.getId());
        dto.setProductId(cartItem.getProduct().getId());
        dto.setProductName(cartItem.getProduct().getName());
        dto.setProductPrice(cartItem.getProduct().getPrice());
        dto.setQuantity(cartItem.getQuantity());
        return dto;
    }

    public Cart saveCart(Cart cart) {
        return this.cartRepository.save(cart);
    }

    public Optional<Cart> getCartById(Long id) {
        return this.cartRepository.findById(id);
    }

    public Optional<CartDTO> getCartDTOByUserId(Long userId) {
        return this.cartRepository.findByUserId(userId).map(this::convertToCartDTO);
    }

    public CartDTO convertToCartDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setUser(userService.convertToUserDTO(cart.getUser()));
        List<CartItemDTO> itemsDTOs = cart.getCartItems().stream().map(this::convertToCartItemDTO).toList();
        dto.setCartItems(itemsDTOs);
        return dto;
    }

    public CartItemDTO convertToCartItemDTO(CartItem cartItem) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(cartItem.getId());
        dto.setProductId(cartItem.getProduct().getId());
        dto.setProductName(cartItem.getProduct().getName());
        dto.setProductPrice(cartItem.getProduct().getPrice());
        dto.setQuantity(cartItem.getQuantity());
        return dto;
    }

}
