package org.agile.order.controller;

import org.agile.order.model.Book;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;
import org.agile.order.service.ShoppingCartService;
import org.agile.order.model.ShoppingCart;

@Controller
public class CatalogController {

    private final String catalogServiceBaseUrl;
    private final RestClient restClient;
    private final ShoppingCartService cartService; // <--- HIER DEKLARIERT

    //Alle Initialisierungen in einem Konstruktor zusammenfassen
    public CatalogController(ShoppingCartService cartService,
                             @Value("${catalog.service.url}") String catalogServiceBaseUrl) {

        this.cartService = cartService;
        this.catalogServiceBaseUrl = catalogServiceBaseUrl;

        // RestClient mit der INJIZIERTEN Basis-URL erstellen
        this.restClient = RestClient.builder()
                .baseUrl(this.catalogServiceBaseUrl)
                .build();
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keywords, Model model) {
        Book[] books = new Book[0];

        if (keywords != null && !keywords.isBlank()) {
            String[] keywordArray = keywords.split("\\s+");

            books = restClient.get()
                    .uri("/api/books/search", uriBuilder -> {
                        for (String k : keywordArray) {
                            uriBuilder.queryParam("keywords", k);
                        }
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(Book[].class);
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
    public String addToCart(@RequestParam String isbn,
                            @RequestParam(required = false) String keywords) {

        Book book = restClient.get()
                .uri("/api/books/isbn/{isbn}", isbn)
                .retrieve()
                .body(Book.class);

        cartService.addToCart(book);

        return "redirect:/search?keywords=" + (keywords != null ? keywords : "");
    }
}