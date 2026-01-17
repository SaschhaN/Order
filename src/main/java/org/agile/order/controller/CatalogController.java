package org.agile.order.controller;

import org.agile.order.model.Book;
import org.agile.order.service.CatalogClient;
import org.agile.order.service.ShoppingCartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CatalogController {

    private final CatalogClient catalogClient;
    private final ShoppingCartService cartService;

    // Konstruktor: Nur noch diese zwei Services injizieren
    public CatalogController(ShoppingCartService cartService, CatalogClient catalogClient) {
        this.cartService = cartService;
        this.catalogClient = catalogClient;
    }

    @GetMapping("/search")
    public String search(@RequestParam(name = "keywords", required = false) String keywords, Model model) {
        Book[] books = new Book[0];

        if (keywords != null && !keywords.isBlank()) {
            String[] keywordArray = keywords.split("\\s+");
            // Nutzt den Client (mit dem @Retry darin)
            books = catalogClient.searchBooks(keywordArray);
        }

        model.addAttribute("books", books);
        model.addAttribute("keywords", keywords);
        return "search";
    }

    @GetMapping("/cart")
    public String viewCart(Model model) {
        model.addAttribute("cart", cartService.getCart());
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("isbn") String isbn,
                            @RequestParam(name = "keywords", required = false) String keywords) {

        // Nutzt den Client (mit dem @Retry darin)
        Book book = catalogClient.getBookByIsbn(isbn);

        if (book != null) {
            cartService.addToCart(book);
        }

        return "redirect:/search?keywords=" + (keywords != null ? keywords : "");
    }
}