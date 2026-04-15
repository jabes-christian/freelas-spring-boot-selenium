package dev.jchristian.RoboFreelas.scheduler;

import dev.jchristian.RoboFreelas.dto.OpportunityDTO;
import dev.jchristian.RoboFreelas.scraper.WorkanaScraper;
import dev.jchristian.RoboFreelas.service.OpportunityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class MonitorScheduler {

    private final WorkanaScraper workanaScraper;
    private final OpportunityService opportunityService;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm:ss");


    @Scheduled(fixedDelayString = "${scheduler.interval.ms:1800000}")
    public void executarCiclo() {
        String inicio = LocalDateTime.now().format(FORMATTER);
        log.info("========================================");
        log.info("[Scheduler] Novo ciclo iniciado em {}", inicio);
        log.info("========================================");

        try {
            renotificarPendentes();
            executarScrapers();

        } catch (Exception e) {
            log.error("[Scheduler] Erro inesperado no ciclo: {}", e.getMessage());
        }

        String fim = LocalDateTime.now().format(FORMATTER);
        log.info("[Scheduler] Ciclo finalizado em {}", fim);
        log.info("========================================");
    }

    private void executarScrapers() {
        executarScraper("Workana", workanaScraper::extrair);
        // Adicione novos scrapers aqui conforme forem implementados
        // executarScraper("99Freelas", freelasScraper::extrair);
        // executarScraper("Licitações", licitacaoScraper::extrair);
    }

    private void executarScraper(String nome, ScraperExecution scraper) {
        log.info("[Scheduler] Iniciando scraper: {}", nome);
        try {
            List<OpportunityDTO> oportunidades = scraper.executar();
            log.info("[Scheduler] {} — {} oportunidades extraídas", nome, oportunidades.size());
            opportunityService.processarOportunidades(oportunidades);
        } catch (Exception e) {
            log.error("[Scheduler] Falha no scraper {}: {}", nome, e.getMessage());
        }
    }

    private void renotificarPendentes() {
        try {
            log.info("[Scheduler] Verificando notificações pendentes...");
            opportunityService.renotificarPendentes();
        } catch (Exception e) {
            log.error("[Scheduler] Erro ao renotificar pendentes: {}", e.getMessage());
        }
    }

    @FunctionalInterface
    private interface ScraperExecution {
        List<OpportunityDTO> executar();
    }
}