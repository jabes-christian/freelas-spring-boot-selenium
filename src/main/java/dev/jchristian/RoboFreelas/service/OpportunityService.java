package dev.jchristian.RoboFreelas.service;

import dev.jchristian.RoboFreelas.dto.OpportunityDTO;
import dev.jchristian.RoboFreelas.entity.Opportunity;
import dev.jchristian.RoboFreelas.repository.OpportunityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpportunityService {

    private final OpportunityRepository repository;
    private final TelegramNotificationService telegramService;

    @Transactional
    public void processarOportunidades(List<OpportunityDTO> oportunidades) {
        if (oportunidades == null || oportunidades.isEmpty()) {
            log.info("[Service] Nenhuma oportunidade recebida para processar");
            return;
        }

        log.info("[Service] Iniciando processamento de {} oportunidades", oportunidades.size());

        int novas = 0;
        int duplicatas = 0;

        for (OpportunityDTO dto : oportunidades) {
            try {
                boolean processada = processarOportunidade(dto);
                if (processada) novas++;
                else duplicatas++;
            } catch (Exception e) {
                log.error("[Service] Erro ao processar oportunidade '{}': {}",
                        dto.titulo(), e.getMessage());
            }
        }

        log.info("[Service] Ciclo finalizado — {} novas, {} duplicatas ignoradas",
                novas, duplicatas);
    }

    private boolean processarOportunidade(OpportunityDTO dto) {

        // Coração da deduplicação — consulta apenas pelo link
        if (repository.existsByLink(dto.getLink())) {
            log.debug("[Service] Duplicata ignorada: {}", dto.titulo());
            return false;
        }
        Opportunity opportunity = toEntity(dto);
        repository.save(opportunity);
        log.info("[Service] Nova oportunidade salva: {}", dto.titulo());

        notificar(opportunity);

        return true;
    }

    private void notificar(Opportunity opportunity) {
        try {
            telegramService.enviarNotificacao(opportunity);

            opportunity.setNotificado(true);
            repository.save(opportunity);

            log.info("[Service] Notificação enviada para: {}", opportunity.getTitulo());
        } catch (Exception e) {
            log.error("[Service] Falha ao notificar '{}': {}", opportunity.getTitulo(), e.getMessage());
        }
    }

    @Transactional
    public void renotificarPendentes() {
        List<Opportunity> pendentes = repository.findByNotificadoFalse();

        if (pendentes.isEmpty()) {
            log.info("[Service] Nenhuma notificação pendente encontrada");
            return;
        }

        log.info("[Service] Reprocessando {} notificações pendentes", pendentes.size());
        pendentes.forEach(this::notificar);
    }

    private Opportunity toEntity(OpportunityDTO dto) {
        return Opportunity.builder()
                .titulo(dto.titulo())
                .plataforma(dto.plataforma())
                .valor(dto.valor())
                .link(dto.link())
                .descricao(dto.descricao())
                .notificado(false)
                .build();
    }
}