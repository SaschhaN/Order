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
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import org.testcontainers.containers.Network;

// Tells JUnit to look for @Container fields
@Testcontainers
// Using RANDOM_PORT because the Order Service is being started by this test
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@UsePlaywright
public class BookstoreTest {

    // 1. Das gemeinsame Netzwerk (muss static sein)
    static Network network = Network.newNetwork();

    // 2. Der Datenbank-Container (Generic oder PostgreSQLContainer)
    @Container
    static GenericContainer<?> catalogDb = new GenericContainer<>("postgres:15-alpine")
            .withNetwork(network)
            .withNetworkAliases("catalog-db") // Das ist der Hostname für den Catalog-Service
            .withEnv("POSTGRES_DB", "catalogdb")
            .withEnv("POSTGRES_USER", "user")
            .withEnv("POSTGRES_PASSWORD", "password")
            // Wir warten, bis die DB wirklich bereit ist
            .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 1));

    // 3. Der Catalog-Service
    @Container
    static GenericContainer<?> catalogService = new GenericContainer<>("agileproject-catalog-service:latest")
            .withNetwork(network)
            .withExposedPorts(8080)
            // HIER WIRD DIE VERBINDUNG DEFINIERT:
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://catalog-db:5432/catalogdb")
            .withEnv("SPRING_DATASOURCE_USERNAME", "user")
            .withEnv("SPRING_DATASOURCE_PASSWORD", "password")
            .dependsOn(catalogDb) // Ganz wichtig: DB muss zuerst da sein
            .waitingFor(Wait.forHttp("/actuator/health").forStatusCode(200));

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        // Hier sagen wir dem ORDER-SERVICE (der lokal läuft), wo er den CATALOG findet
        String catalogUrl = String.format("http://%s:%d",
                catalogService.getHost(),
                catalogService.getMappedPort(8080));
        registry.add("catalog.service.url", () -> catalogUrl);
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