package org.agile.order.model;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {

    private final List<Book> items = new ArrayList<>();

    public void addBook(Book book) {
        items.add(book);
    }

    public List<Book> getItems() {
        return items;
    }
}
