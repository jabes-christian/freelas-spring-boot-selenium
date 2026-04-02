package dev.jchristian.RoboFreelas;

import dev.jchristian.RoboFreelas.config.SeleniumConfig;
import dev.jchristian.RoboFreelas.dto.OpportunityDTO;
import dev.jchristian.RoboFreelas.scraper.WorkanaScraper;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

public class WorkanaScraperTest {

    private WebDriver driver;
    private WorkanaScraper scraper;

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();

        // Sem headless — browser abre visualmente para debug
        options.addArguments("--start-maximized");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-notifications");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/124.0.0.0 Safari/537.36");

        WebDriverManager.chromedriver().setup();
        driver  = new ChromeDriver(options);
        scraper = new WorkanaScraper(driver);
    }

    @Test
    void deveRetornarOportunidadesDoWorkana() {
        List<OpportunityDTO> resultado = scraper.extrair();

        System.out.println("=== RESULTADO DA EXTRAÇÃO ===");
        System.out.println("Total encontrado: " + resultado.size());
        System.out.println("=============================");

        resultado.forEach(dto -> {
            System.out.println("Título    : " + dto.titulo());
            System.out.println("Plataforma: " + dto.plataforma());
            System.out.println("Valor     : " + dto.valor());
            System.out.println("Link      : " + dto.link());
            System.out.println("Descrição : " + dto.descricao());
            System.out.println("Capturado : " + dto.dataCaptura());
            System.out.println("-----------------------------");
        });
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}