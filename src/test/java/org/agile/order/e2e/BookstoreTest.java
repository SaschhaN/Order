package org.agile.order.e2e;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@UsePlaywright
public class BookstoreTest {

    // 1. Das gemeinsame Netzwerk
    static Network network = Network.newNetwork();

    // 2. Datenbank definieren
    @Container
    static GenericContainer<?> catalogDb = new GenericContainer<>("postgres:15-alpine")
            .withNetwork(network)
            .withNetworkAliases("catalog-db") // Dieser Name muss auflösbar sein
            .withEnv("POSTGRES_DB", "catalogdb")
            .withEnv("POSTGRES_USER", "user")
            .withEnv("POSTGRES_PASSWORD", "password")
            .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 1));

    // 3. Catalog Service definieren
    @Container
    static GenericContainer<?> catalogService = new GenericContainer<>("saschhan/catalog:latest")
            .withNetwork(network)
            .withNetworkAliases("catalog-service")
            .withExposedPorts(8080)
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://catalog-db:5432/catalogdb")
            .withEnv("SPRING_DATASOURCE_USERNAME", "user")
            .withEnv("SPRING_DATASOURCE_PASSWORD", "password")
            // ZWINGT den Catalog zu warten, bis die DB wirklich läuft
            .dependsOn(catalogDb)
            .waitingFor(Wait.forLogMessage(".*Started CatalogApplication.*\\n", 1));

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        String catalogUrl = "http://" + catalogService.getHost() + ":" + catalogService.getMappedPort(8080);
        registry.add("catalog.service.url", () -> catalogUrl);
    }

    @LocalServerPort
    private int orderPort;
    private String orderBaseUrl;
    private Page page;

    @BeforeEach
    void setUp(Page page) {
        this.page = page;
        this.orderBaseUrl = "http://localhost:" + orderPort;
    }

    @Test
    void searchAndAddBookToCartTest() {
        final String SEARCH_TERM = "Java";
        final String EXPECTED_BOOK_TITLE = "Effective Java";

        page.navigate(orderBaseUrl + "/search");

        // Suche ausführen
        page.locator("input[name='keywords']").fill(SEARCH_TERM);
        page.locator("button:has-text('Search')").click();

        // Prüfen, ob das Buch vom Catalog-Service geliefert wurde
        Locator bookRow = page.locator("table tr")
                .filter(new Locator.FilterOptions().setHasText(EXPECTED_BOOK_TITLE));
        assertThat(bookRow).isVisible();

        // In den Warenkorb legen
        Locator addToCartBtn = bookRow.locator("button:has-text('Add to Cart')");
        addToCartBtn.click();

        // Kurz warten auf Redirect/Verarbeitung
        page.waitForURL("**/search*");

        // Warenkorb prüfen
        page.navigate(orderBaseUrl + "/cart");
        Locator cartItem = page.locator("table tr")
                .filter(new Locator.FilterOptions().setHasText(EXPECTED_BOOK_TITLE));
        assertThat(cartItem).isVisible();
    }
}