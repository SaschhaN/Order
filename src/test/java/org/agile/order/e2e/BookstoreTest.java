package org.agile.order.e2e;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

// Tells JUnit to look for @Container fields
@Testcontainers
// Using RANDOM_PORT because the Order Service is being started by this test
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@UsePlaywright
public class BookstoreTest {

    // 1. Defining the Catalog Container
    @Container
    static GenericContainer<?> catalogService = new GenericContainer<>("bookstore_catalog-catalog-service:latest")
            .withExposedPorts(8080); // Expose the internal Catalog port (8080)

    // 2. Dynamically set the Catalog Service URL
    // This method runs before the Spring context is created and overrides the default catalog.service.url
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        // Order Service (running locally on a random port) needs to know the Catalog Service's dynamic IP/Port
        registry.add("catalog.service.url", () ->
                String.format("http://%s:%d",
                        catalogService.getHost(),
                        catalogService.getMappedPort(8080)));
    }

    // The E2E Test Setup
    @LocalServerPort
    private int orderPort;
    private String orderBaseUrl;

    private Page page;

    @BeforeEach
    void setUp(Page page) {
        this.page = page;
        // The browser hits the Order Service which is started by the test
        this.orderBaseUrl = "http://localhost:" + orderPort;
    }

    @Test
    void searchAndAddBookToCartTest() {
        final String SEARCH_TERM = "Java";
        final String EXPECTED_BOOK_TITLE = "Effective Java";

        // Navigation uses the dynamic port of the Order Service
        page.navigate(orderBaseUrl + "/search");

        page.locator("input[name='keywords']").fill(SEARCH_TERM);
        page.locator("button:has-text('Search')").click();

        // Check if the Order Service successfully called the Testcontainer Catalog Service
        Locator bookRow = page.locator("table tr")
                .filter(new Locator.FilterOptions().setHasText(EXPECTED_BOOK_TITLE));
        assertThat(bookRow).isVisible();

        Locator addToCartBtn = bookRow.locator("button:has-text('Add to Cart')");
        addToCartBtn.click();
        page.waitForTimeout(500);

        page.navigate(orderBaseUrl + "/cart");
        Locator cartItem = page.locator("table tr")
                .filter(new Locator.FilterOptions().setHasText(EXPECTED_BOOK_TITLE));
        assertThat(cartItem).isVisible();
    }
}