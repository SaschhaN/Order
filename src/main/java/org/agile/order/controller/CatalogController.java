package org.agile.order.controller;

import org.agile.order.model.Book;
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

    private final RestClient restClient = RestClient.builder()
            .baseUrl("http://localhost:8080/api/books/search")
            .build();

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keywords, Model model) {

        Book[] books = new Book[0]; // default empty list

        if (keywords != null && !keywords.isBlank()) {
            String[] keywordArray = keywords.split("\\s+");

            books = restClient.get()
                    .uri(uriBuilder -> {
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

    private final ShoppingCartService cartService;
    public CatalogController(ShoppingCartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam String isbn,
                            @RequestParam(required = false) String keywords) {

        Book book = restClient.get()
                .uri("http://localhost:8080/api/books/isbn/{isbn}", isbn)
                .retrieve()
                .body(Book.class);

        cartService.addToCart(book);

        return "redirect:/search?keywords=" + (keywords != null ? keywords : "");
    }


}
