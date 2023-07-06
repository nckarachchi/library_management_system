package com.ecommerce.cartservice.service;

import com.ecommerce.cartservice.dto.cartItem.CartItemMapper;
import com.ecommerce.cartservice.dto.cartItem.CreateCartItemRequest;
import com.ecommerce.cartservice.dto.cartItem.UpdateCartItemRequest;
import com.ecommerce.cartservice.model.Cart;
import com.ecommerce.cartservice.model.CartItem;
import com.ecommerce.cartservice.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

import static com.ecommerce.common.util.StaticFunctions.toSingleton;


@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    private final CartRepository cartRepository;

    private final CartItemMapper cartItemMapper;
    public void save(String customerId, CreateCartItemRequest createCartItemRequest) {
        CartItem cartItem = cartItemMapper.createCartItemRequestToCartItem(createCartItemRequest);
        Optional<Cart> cart = cartRepository.findCartByCustomerId(customerId);

        if(cart.isPresent()) {
            Cart presentCart = cart.get();
            presentCart.getCartItems().add(cartItem);
            presentCart.setTotalPrice(getTotalPrice(presentCart));
            cartRepository.save(presentCart);
        }else{
            Cart newCart = Cart.builder()
                    .customerId(customerId)
                    .cartItems(List.of(cartItem))
                    .build();
            newCart.setTotalPrice(getTotalPrice(newCart));
            cartRepository.save(newCart);
        }


    }

    public List<Cart> getCarts() {
        return cartRepository.findAll();
    }

    public void updateQuantity(String customerId, UpdateCartItemRequest updateCartItemRequest) {
        Cart cart = cartRepository.findCartByCustomerId(customerId)
                .orElseThrow(()-> {
                    log.error("Cart with id: {} could not be found!", customerId);
                    throw new RuntimeException("Cart is not found with id :"+ customerId);
                });

        CartItem cartItem = cart
                .getCartItems()
                .stream()
                .filter((eachCart)-> eachCart.getProductId().equals(updateCartItemRequest.getProductId()))
                .collect(toSingleton());

        cartItem.setQuantity(updateCartItemRequest.getQuantity());
        cart.setTotalPrice(getTotalPrice(cart));
        cartRepository.save(cart);
    }

    private BigDecimal getTotalPrice(Cart cart) {
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getCartItems()) {
            BigDecimal subtotal = cartItem.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            totalPrice = totalPrice.add(subtotal);
        }

        return totalPrice;
    }

    //TODO: getTotalPrice generic hale getir
    //TODO: urun silme yap hic urun kalmadiysa orderi tamamen sil
    //TODO: herbir itemin total price ini yap

}
