package dev.jchristian.RoboFreelas.scraper;

import dev.jchristian.RoboFreelas.dto.OpportunityDTO;
import dev.jchristian.RoboFreelas.scraper.base.BaseScraper;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class WorkanaScraper extends BaseScraper {

    private static final String URL_BASE        = "https://www.workana.com";
    private static final String URL_PROJETOS     = "https://www.workana.com/jobs?language=pt&category=it-programming";

    private static final List<String> TERMOS_BUSCA = List.of(
            "RPA", "automação", "robô", "selenium",
            "inteligência artificial", "machine learning",
            "IA", "agente de IA", "web scraping",
            "processamento automatizado", "bot"
    );

    public WorkanaScraper(WebDriver driver) {
        super(driver);
    }

    @Override
    public List<OpportunityDTO> extrair() {
        List<OpportunityDTO> oportunidades = new ArrayList<>();

        try {
            log.info("[Workana] Iniciando extração de oportunidades");

            navegarPara(URL_PROJETOS);
            aceitarCookiesSeNecessario();

            List<WebElement> cards = aguardarElementos(
                    By.cssSelector("div.project-item")
            );

            if (cards.isEmpty()) {
                log.warn("[Workana] Nenhum card de projeto encontrado — verificar seletor CSS");
                return oportunidades;
            }

            log.info("[Workana] {} cards encontrados na página", cards.size());

            for (WebElement card : cards) {
                try {
                    OpportunityDTO dto = extrairDadosDoCard(card);

                    if (dto != null && contemTermoRelevante(dto)) {
                        oportunidades.add(dto);
                        log.info("[Workana] Oportunidade relevante: {}", dto.titulo());
                    }

                } catch (Exception e) {
                    log.warn("[Workana] Erro ao processar card — pulando: {}", e.getMessage());
                }
            }

            log.info("[Workana] Extração finalizada — {} oportunidades relevantes encontradas",
                    oportunidades.size());

        } catch (Exception e) {
            log.error("[Workana] Falha geral na extração: {}", e.getMessage());
        }

        return oportunidades;
    }

    private OpportunityDTO extrairDadosDoCard(WebElement card) {
        try {
            String titulo = extrairTextoDoElemento(card, "h2.project-title a span");
            String link   = extrairAtributoDoElemento(card, "h2.project-title a", "href");
            String valor  = extrairTextoDoElemento(card, "p.budget span span");
            String desc   = extrairTextoDoElemento(card, "div.html-desc span");

            if (titulo.isBlank() || link.isBlank()) {
                log.warn("[Workana] Card sem título ou link — ignorando");
                return null;
            }

            if (!link.startsWith("http")) {
                link = URL_BASE + link;
            }

            return OpportunityDTO.of(titulo, "Workana", valor, link, desc);

        } catch (Exception e) {
            log.warn("[Workana] Erro ao extrair dados do card: {}", e.getMessage());
            return null;
        }
    }

    private boolean contemTermoRelevante(OpportunityDTO dto) {
        String conteudo = (dto.titulo() + " " + dto.descricao()).toLowerCase();

        return TERMOS_BUSCA.stream()
                .anyMatch(termo -> conteudo.contains(termo.toLowerCase()));
    }

    private String extrairTextoDoElemento(WebElement pai, String cssSelector) {
        try {
            return pai.findElement(By.cssSelector(cssSelector)).getText().strip();
        } catch (Exception e) {
            return "";
        }
    }

    private String extrairAtributoDoElemento(WebElement pai, String cssSelector, String atributo) {
        try {
            String valor = pai.findElement(By.cssSelector(cssSelector)).getAttribute(atributo);
            return valor != null ? valor.strip() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private void aceitarCookiesSeNecessario() {
        try {
            if (elementoExiste(By.cssSelector("button.cookie-accept"))) {
                clicar(By.cssSelector("button.cookie-accept"));
                log.info("[Workana] Banner de cookies aceito");
            }
        } catch (Exception e) {
            log.warn("[Workana] Não foi possível fechar banner de cookies: {}", e.getMessage());
        }
    }
}