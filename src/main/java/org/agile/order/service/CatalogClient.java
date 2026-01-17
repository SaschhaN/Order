package org.agile.order.service;

import io.github.resilience4j.retry.annotation.Retry;
import org.agile.order.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class CatalogClient {

    private static final Logger log = LoggerFactory.getLogger(CatalogClient.class);
    private final RestClient restClient;

    public CatalogClient(@Value("${catalog.service.url}") String catalogServiceBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(catalogServiceBaseUrl)
                .build();
    }

    // RETRY für die Suche
    @Retry(name = "catalogSearch", fallbackMethod = "searchFallback")
    public Book[] searchBooks(String[] keywords) {
        log.info("Suche Catalog Service mit Keywords: {}", (Object) keywords);
        return restClient.get()
                .uri("/api/books/search", uriBuilder -> {
                    for (String k : keywords) {
                        uriBuilder.queryParam("keywords", k);
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .body(Book[].class);
    }

    // FALLBACK für die Suche (Gibt leere Liste zurück)
    public Book[] searchFallback(String[] keywords, Throwable t) {
        log.error("Suche fehlgeschlagen für {}. Grund: {}", keywords, t.getMessage());
        return new Book[0];
    }

    // RETRY für ISBN Abfrage (beim Warenkorb)
    @Retry(name = "catalogIsbn", fallbackMethod = "isbnFallback")
    public Book getBookByIsbn(String isbn) {
        log.info("Rufe Catalog Service für ISBN auf: {}", isbn);
        return restClient.get()
                .uri("/api/books/isbn/{isbn}", isbn)
                .retrieve()
                .body(Book.class);
    }

    // FALLBACK für ISBN (Gibt Platzhalter zurück)
    public Book isbnFallback(String isbn, Throwable t) {
        log.error("ISBN Abfrage fehlgeschlagen für {}. Grund: {}", isbn, t.getMessage());
        Book fallbackBook = new Book();
        fallbackBook.setIsbn(isbn);
        fallbackBook.setTitle("Service aktuell nicht verfügbar");
        fallbackBook.setAuthor("System");
        return fallbackBook;
    }
}