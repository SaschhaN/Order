# 📦 Bookstore – Order Service

Der **Order Service** ist ein zentraler Bestandteil unseres Microservice-Ökosystems.  
Er ist für die Abwicklung von Bestellungen sowie die Verwaltung des Warenkorbs zuständig.

Um Informationen über verfügbare Bücher zu erhalten, kommuniziert dieser Service direkt mit dem **Catalog Service**.

---

## 🚀 Kernfunktionen

- **Produktsuche**  
  Leitet Suchanfragen an den Catalog Service weiter.

- **Warenkorb-Logik**  
  Verwalten von Artikeln, die ein Nutzer kaufen möchte.

- **E2E-Integration**  
  Automatisierte End-to-End-Tests mittels **Playwright** und **Testcontainers**,  
  die das Zusammenspiel zwischen Datenbank und Catalog Service simulieren.

---

## 🛠 Technologie-Stack

- **Java 21** mit **Spring Boot 3**
- **Maven** für Build- & Dependency-Management
- **Docker & Testcontainers** für Integrationstests
- **Playwright** für UI-gestützte End-to-End-Tests

---

## 🏗 Deployment & Start (Docker Compose)

Um das gesamte System (**Order Service**, **Catalog Service** und Datenbanken) lokal zu starten, nutzen wir **Docker Compose**.

👉 Der Code muss **nicht manuell gebaut** werden.  
Das Compose-File zieht die aktuellen Images direkt von **Docker Hub**.

### Voraussetzungen

- Installiertes **Docker Desktop**

### Startanleitung

1. Navigiere in das Verzeichnis mit der `docker-compose.yml` Datei.
2. Führe folgenden Befehl aus:

```bash
docker-compose up -d
