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
import java.util.stream.Stream;

@Slf4j
@Component
public class FreelasScraper extends BaseScraper {

    private static final String URL_BASE     = "https://www.99freelas.com.br";
    private static final String URL_PROJETOS = "https://www.99freelas.com.br/projects?categoria=ti-e-programacao";

    // -------------------------------------------------------------------------
    // Termos de busca — mesmas categorias do WorkanaScraper
    // -------------------------------------------------------------------------

    private static final List<String> TERMOS_RPA = List.of(
            "rpa", "automação", "automacao", "robô", "robo",
            "automatizar", "automatização", "automatizacao",
            "processamento automático", "processamento automatico"
    );

    private static final List<String> TERMOS_SCRAPING = List.of(
            "scraping", "web scraping", "raspagem", "extração de dados",
            "extracao de dados", "crawler", "coleta de dados", "selenium"
    );

    private static final List<String> TERMOS_CHATBOT_IA = List.of(
            "chatbot", "chat bot", "agente de ia", "agente ia",
            "inteligência artificial", "inteligencia artificial",
            "llm", "gpt", "langchain", "bot inteligente",
            "assistente virtual", "assistente de ia", "ia conversacional"
    );

    private static final List<String> TERMOS_INTEGRACAO = List.of(
            "integração", "integracao", "api rest", "webhook",
            "sincronização", "sincronizacao", "middleware",
            "integrar sistemas", "integração de sistemas"
    );

    private static final List<String> TODOS_TERMOS = Stream.of(
                    TERMOS_RPA, TERMOS_SCRAPING, TERMOS_CHATBOT_IA, TERMOS_INTEGRACAO)
            .flatMap(List::stream)
            .toList();

    public FreelasScraper(WebDriver driver) {
        super(driver);
    }


    @Override
    public List<OpportunityDTO> extrair() {
        List<OpportunityDTO> oportunidades = new ArrayList<>();

        try {
            log.info("[99Freelas] Iniciando extração de oportunidades");

            navegarPara(URL_PROJETOS);
            fecharAvisoCookies();

            List<WebElement> cards = aguardarElementos(
                    By.cssSelector("li.result-item")
            );

            if (cards.isEmpty()) {
                log.warn("[99Freelas] Nenhum card encontrado — verificar seletor CSS");
                return oportunidades;
            }

            log.info("[99Freelas] {} cards encontrados na página", cards.size());

            for (WebElement card : cards) {
                try {
                    OpportunityDTO dto = extrairDadosDoCard(card);

                    if (dto == null) continue;

                    if (!contemTermoRelevante(dto)) {
                        log.debug("[99Freelas] Ignorado — fora do escopo: {}", dto.titulo());
                        continue;
                    }

                    oportunidades.add(dto);
                    log.info("[99Freelas] Oportunidade relevante: {}", dto.titulo());

                } catch (Exception e) {
                    log.warn("[99Freelas] Erro ao processar card — pulando: {}", e.getMessage());
                }
            }

            log.info("[99Freelas] Extração finalizada — {} oportunidades relevantes encontradas",
                    oportunidades.size());

        } catch (Exception e) {
            log.error("[99Freelas] Falha geral na extração: {}", e.getMessage());
        }

        return oportunidades;
    }

    private OpportunityDTO extrairDadosDoCard(WebElement card) {
        try {
            String titulo    = extrairTextoDoElemento(card, "h1.title a");
            String link      = extrairAtributoDoElemento(card, "h1.title a", "href");
            String categoria = extrairCategoria(card);
            String descricao = extrairDescricao(card);

            if (titulo.isBlank() || link.isBlank()) {
                log.warn("[99Freelas] Card sem título ou link — ignorando");
                return null;
            }

            // Garante que o link é absoluto
            if (!link.startsWith("http")) {
                link = URL_BASE + link;
            }

            // Remove parâmetros de tracking do link para deduplicação limpa
            // ex: /project/titulo-123?fs=t  →  /project/titulo-123
            if (link.contains("?")) {
                link = link.substring(0, link.indexOf("?"));
            }

            // Usa categoria no lugar do valor — 99Freelas não exibe orçamento na listagem
            return OpportunityDTO.of(titulo, "99Freelas", categoria, link, descricao);

        } catch (Exception e) {
            log.warn("[99Freelas] Erro ao extrair dados do card: {}", e.getMessage());
            return null;
        }
    }

    private String extrairCategoria(WebElement card) {
        try {
            String textoCompleto = extrairTextoDoElemento(card, "p.item-text.information");

            if (textoCompleto.isBlank()) return "";

            // Extrai apenas o primeiro segmento antes do primeiro "|"
            // ex: "Atendimento ao Consumidor | Iniciante | ..." → "Atendimento ao Consumidor"
            String[] partes = textoCompleto.split("\\|");
            return partes[0].strip();

        } catch (Exception e) {
            return "";
        }
    }

    private String extrairDescricao(WebElement card) {
        try {
            WebElement divDesc = card.findElement(
                    By.cssSelector("div.item-text.description.formatted-text")
            );

            // getAttribute("innerText") traz o texto visível sem as tags HTML
            // incluindo o conteúdo do span.details que pode estar oculto
            String texto = divDesc.getAttribute("innerText");

            if (texto == null || texto.isBlank()) return "";

            // Remove os links de "Expandir" e "Esconder" que o Selenium captura como texto
            return texto
                    .replace("… Expandir", "")
                    .replace("Esconder", "")
                    .strip();

        } catch (Exception e) {
            return "";
        }
    }

    private boolean contemTermoRelevante(OpportunityDTO dto) {
        String titulo    = dto.titulo().toLowerCase();
        String descricao = dto.descricao().toLowerCase();

        boolean tituloRelevante    = TODOS_TERMOS.stream().anyMatch(titulo::contains);
        boolean descricaoRelevante = TODOS_TERMOS.stream().anyMatch(descricao::contains);

        return tituloRelevante && descricaoRelevante;
    }

    private void fecharAvisoCookies() {
        try {
            // Clica no X para fechar o aviso — não é um botão, é um span.close
            if (elementoExiste(By.cssSelector("div.box-cookie.show span.close"))) {
                clicar(By.cssSelector("div.box-cookie.show span.close"));
                log.info("[99Freelas] Aviso de cookies fechado");
            }
        } catch (Exception e) {
            log.warn("[99Freelas] Não foi possível fechar aviso de cookies: {}", e.getMessage());
        }
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
}