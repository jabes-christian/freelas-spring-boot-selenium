package dev.jchristian.RoboFreelas.repository;


import dev.jchristian.RoboFreelas.entity.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    // Coração da deduplicação — verifica se o link já existe antes de salvar
    boolean existsByLink(String link);

    // Busca por link caso precise recuperar o registro completo
    Optional<Opportunity> findByLink(String link);

    // Busca oportunidades ainda não notificadas — útil se o Telegram falhar e precisar reenviar
    List<Opportunity> findByNotificadoFalse();

    // Busca por plataforma — útil para relatórios ou debug por fonte
    List<Opportunity> findByPlataforma(String plataforma);

    // Busca oportunidades capturadas a partir de uma data — útil para o scheduler evitar reprocessar antigas
    List<Opportunity> findByDataCapturaAfter(LocalDateTime dataCaptura);
}