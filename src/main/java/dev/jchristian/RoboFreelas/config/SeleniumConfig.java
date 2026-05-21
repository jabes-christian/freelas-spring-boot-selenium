package dev.jchristian.RoboFreelas.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Slf4j
@Configuration
public class SeleniumConfig {

    @Value("${selenium.remote.url:}")
    private String remoteUrl;

    @Bean
    public ChromeOptions chromeOptions() {
        ChromeOptions options = new ChromeOptions();

        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");

        // User-agent de browser real — reduz chance de bloqueio por bot detection
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/124.0.0.0 Safari/537.36");
        return options;
    }

    @Bean
    public WebDriver webDriver(ChromeOptions chromeOptions) {
        if (remoteUrl != null && !remoteUrl.isBlank()) {
            log.info("[Selenium] Modo REMOTO — conectando em: {}", remoteUrl);
            try {
                return new RemoteWebDriver(URI.create(remoteUrl).toURL(), chromeOptions);
            } catch (Exception e) {
                throw new RuntimeException("Falha ao conectar no Selenium remoto: " + remoteUrl, e);
            }
        }

        log.info("[Selenium] Modo LOCAL — usando ChromeDriver");
        WebDriverManager.chromedriver().setup();
        return new ChromeDriver(chromeOptions);
    }
}