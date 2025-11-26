package org.agile.order.service;

import org.agile.order.model.Book;
import org.agile.order.model.ShoppingCart;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

@Service
@SessionScope
public class ShoppingCartService {

    private final ShoppingCart cart = new ShoppingCart();

    public void addToCart(Book book) {
        cart.addBook(book);
    }

    public ShoppingCart getCart() {
        return cart;
    }
}
