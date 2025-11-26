package org.agile.order.e2e;

import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

// Startet den eingebetteten Spring Boot Server auf einem zufälligen Port für den Test
// Wichtiger Hinweis: Obwohl die Anforderung Port 8081 nennt, mussten wir für den Test den
// dynamischen Port verwenden, der vom @SpringBootTest zugewiesen wird, um einen
// "Connection Refused"-Fehler zu vermeiden.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@UsePlaywright
public class BookstoreTest {

    // Wird vom Spring Boot Test-Framework mit dem dynamisch zugewiesenen Port injiziert
    @LocalServerPort
    private int port;

    private Page page;

    // Wird vor jedem Test ausgeführt, um das Playwright Page-Objekt zu initialisieren
    @BeforeEach
    void setUp(Page page) {
        this.page = page;
    }

    @Test
    void searchAndAddBookToCartTest() {
        // Angenommene Daten für den Test. Passen Sie diese an die tatsächlichen Daten an.
        final String SEARCH_TERM = "Java";
        final String EXPECTED_BOOK_TITLE = "Effective Java";

        // Die Basis-URL verwendet den dynamisch zugewiesenen Port
        String baseUrl = "http://localhost:" + port;

        // 1. Browser unter der korrekten Startseite (/search) öffnen
        page.navigate(baseUrl + "/search");

        // 2. Wert im Suchfeld eingeben (Selector: input[name='keywords'])
        page.locator("input[name='keywords']").fill(SEARCH_TERM);

        // 3. Such-Button betätigen (Selector: button mit Text 'Search')
        page.locator("button:has-text('Search')").click();

        // Optional: Warten, bis die Ergebnisse geladen sind
        page.waitForLoadState();

        // --- 4. Überprüfen, ob die erwarteten Bücher angezeigt werden ---
        // Wir suchen die Tabellenzeile (tr), die den erwarteten Buchtitel enthält.
        Locator bookRowLocator = page.locator("table tr")
                .filter(new Locator.FilterOptions().setHasText(EXPECTED_BOOK_TITLE));

        // FEHLER BEHOBEN: .as(...) entfernt, da es in Playwright-Assertions nicht existiert.
        assertThat(bookRowLocator)
                .isVisible();

        // --- 5. Ein Buch in den Warenkorb legen ---
        // Wir suchen den "Add to Cart"-Button *innerhalb* der gefundenen Buchzeile.
        Locator addToCartButton = bookRowLocator.locator("button:has-text('Add to Cart')");
        addToCartButton.click();

        // Nach dem Klick wird die Seite wahrscheinlich zur Suchseite zurückgeleitet (dank des "keywords" hidden input).

        // --- 6. Zum Warenkorb navigieren und überprüfen, ob das ausgewählte Buch enthalten ist ---
        page.navigate(baseUrl + "/cart");


        Locator cartItemLocator = page.locator("table tr")
                .filter(new Locator.FilterOptions().setHasText(EXPECTED_BOOK_TITLE));

        assertThat(cartItemLocator)
                .isVisible();

        System.out.println("E2E-Test erfolgreich: Buch gesucht, gefunden und zum Warenkorb hinzugefügt und verifiziert.");
    }
}