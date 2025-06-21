package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.interfaces.CartInterface;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CartService implements CartInterface {

    private final CartRepository cartRepository;

    private final ProductRepository productRepository;

    private final CartItemRepository cartItemRepository;

    private final ModelMapper modelMapper;

    private final AuthUtil authUtil;

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {

        // find existing cart or create one

        Cart cart = createCart();

        // retrieve the product details

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", "Product Id"));

        // perform validations

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId);

        if (cartItem != null)
            throw new APIException("Product " + product.getProductName() + " already exists in cart");

        if (product.getQuantity() == 0)
            throw new APIException(product.getProductName() + " is not available");

        if (product.getQuantity() < quantity)
            throw new APIException("Please, make an order of the " + product.getProductName() + " less than or equal to the quantity: " + product.getQuantity());

        // create cart item

        CartItem newCartItem = new CartItem();

        newCartItem.setProduct(product);
        newCartItem.setQuantity(quantity);
        newCartItem.setCart(cart);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

        // save cart item

        cartItemRepository.save(newCartItem);

        //reduce stock when application is added to cart
//        product.setQuantity(product.getQuantity() - quantity);
        product.setQuantity(product.getQuantity());

        // update total price for cart
        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));

        productRepository.save(product);

        cartRepository.save(cart);

        // return updated information

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<CartItem> cartItems = cart.getCartItemList();

        Stream<ProductDTO> productDTOStream = cartItems
                .stream()
                .map(item -> {

                    ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);

                    map.setQuantity(item.getQuantity());

                    return map;
        });

        cartDTO.setProducts(productDTOStream.toList());

        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {

        List<Cart> carts = cartRepository.findAll();

        if (carts.isEmpty())
            throw new APIException("No carts found");

        List<CartDTO> cartDTOS = carts.stream()
                .map((cart) -> {
                    CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

                    // get cart products
                    List<ProductDTO> products = cart.getCartItemList().stream()
                            .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class))
                            .toList();

                    cartDTO.setProducts(products);

                    return cartDTO;
                }).toList();

        return cartDTOS;
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {

        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);

        if (cart == null)
            throw new ResourceNotFoundException("Cart", "cartId", "cartId");

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        cart.getCartItemList().forEach(c -> c.getProduct().setQuantity(c.getQuantity()));
        List<ProductDTO> products = cart.getCartItemList().stream()
                .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class))
                .toList();

        cartDTO.setProducts(products);

        return cartDTO;

    }

    @Override
    @Transactional
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        // check if cart exists
        String email = authUtil.loggedInEmail();

        Cart userCart = cartRepository.findCartByEmail(email);

        Long cartId = userCart.getCartId();

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", "Cart id"));

        // check if we have enough stock for the quantity
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", "Product Id"));

        if (product.getQuantity() == 0)
            throw new APIException(product.getProductName() + " is not available");

        if (product.getQuantity() < quantity)
            throw new APIException("Please, make an order of the " + product.getProductName() + " less than or equal to the quantity: " + product.getQuantity());

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null)
            throw new APIException("Product: " + product.getProductName() + " is not available in the cart");

        int newQuantity = cartItem.getQuantity() + quantity;

        if (newQuantity < 0)
            throw new APIException("The resulting quantity cannot be negative");

        cartItem.setProductPrice(product.getSpecialPrice());

        cartItem.setQuantity(cartItem.getQuantity() + quantity);

        cartItem.setDiscount(product.getDiscount());

        cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));

        cartRepository.save(cart);

        CartItem updatedItem = cartItemRepository.save(cartItem);

        if (updatedItem.getQuantity() < 0 || newQuantity == 0){
            cartItemRepository.deleteById(updatedItem.getCartItemId());
        }

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<CartItem> cartItems = cart.getCartItemList();

        Stream<ProductDTO> productStream = cartItems.stream()
                .map(item -> {

                    ProductDTO prd = modelMapper.map(item.getProduct(), ProductDTO.class);

                    prd.setQuantity(item.getQuantity());

                    return prd;
                });

        cartDTO.setProducts(productStream.toList());

        return cartDTO;

    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", "CartId"));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null)
            throw new APIException("Product: " + productId + " is not available in the cart");

        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);

        return "Product " + cartItem.getProduct().getProductName() + " has been removed from the cart";
    }

    private Cart createCart() {
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());

        if (userCart != null)
            return userCart;

        Cart cart = new Cart();

        cart.setTotalPrice(0.0);

        cart.setUser(authUtil.loggedInUser());

        Cart newCart = cartRepository.save(cart);

        return newCart;
    }
}
