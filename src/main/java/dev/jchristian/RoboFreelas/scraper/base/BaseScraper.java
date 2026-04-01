package dev.jchristian.RoboFreelas.scraper.base;

import dev.jchristian.RoboFreelas.dto.OpportunityDTO;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Slf4j
public abstract class BaseScraper {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);

    private static final Duration DELAY_BETWEEN_ACTIONS = Duration.ofSeconds(2);

    protected BaseScraper(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
    }

    public abstract List<OpportunityDTO> extrair();

    protected void navegarPara(String url) {
        try {
            log.info("Navegando para: {}", url);
            driver.get(url);
            aguardar(DELAY_BETWEEN_ACTIONS);
        } catch (Exception e) {
            log.error("Erro ao navegar para {}: {}", url, e.getMessage());
            throw new RuntimeException("Falha na navegação para: " + url, e);
        }
    }

    protected WebElement aguardarElemento(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (Exception e) {
            log.warn("Elemento não encontrado dentro do timeout: {}", locator);
            throw new RuntimeException("Elemento não encontrado: " + locator, e);
        }
    }

    protected WebElement aguardarElementoClicavel(By locator) {
        try {
            return wait.until(ExpectedConditions.elementToBeClickable(locator));
        } catch (Exception e) {
            log.warn("Elemento não clicável dentro do timeout: {}", locator);
            throw new RuntimeException("Elemento não clicável: " + locator, e);
        }
    }

    protected List<WebElement> aguardarElementos(By locator) {
        try {
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
            return driver.findElements(locator);
        } catch (Exception e) {
            log.warn("Nenhum elemento encontrado para: {}", locator);
            return Collections.emptyList();
        }
    }

    protected void clicar(By locator) {
        try {
            WebElement elemento = aguardarElementoClicavel(locator);
            elemento.click();
            aguardar(DELAY_BETWEEN_ACTIONS);
        } catch (Exception e) {
            log.error("Erro ao clicar no elemento {}: {}", locator, e.getMessage());
            throw new RuntimeException("Falha ao clicar em: " + locator, e);
        }
    }

    protected void digitar(By locator, String texto) {
        try {
            WebElement elemento = aguardarElemento(locator);
            elemento.clear();
            elemento.sendKeys(texto);
            aguardar(DELAY_BETWEEN_ACTIONS);
        } catch (Exception e) {
            log.error("Erro ao digitar no elemento {}: {}", locator, e.getMessage());
            throw new RuntimeException("Falha ao digitar em: " + locator, e);
        }
    }

    protected String extrairTexto(By locator) {
        try {
            WebElement elemento = aguardarElemento(locator);
            return elemento.getText().strip();
        } catch (Exception e) {
            log.warn("Não foi possível extrair texto de: {}", locator);
            return "";
        }
    }

    protected String extrairAtributo(By locator, String atributo) {
        try {
            WebElement elemento = aguardarElemento(locator);
            String valor = elemento.getAttribute(atributo);
            return valor != null ? valor.strip() : "";
        } catch (Exception e) {
            log.warn("Não foi possível extrair atributo '{}' de: {}", atributo, locator);
            return "";
        }
    }

    protected void aguardar(Duration duracao) {
        try {
            Thread.sleep(duracao.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Espera interrompida");
        }
    }

    protected boolean elementoExiste(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected void fechar() {
        try {
            if (driver != null) {
                driver.quit();
                log.info("WebDriver encerrado com sucesso");
            }
        } catch (Exception e) {
            log.error("Erro ao encerrar o WebDriver: {}", e.getMessage());
        }
    }
}