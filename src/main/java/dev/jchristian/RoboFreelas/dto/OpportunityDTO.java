package dev.jchristian.RoboFreelas.dto;

import java.time.LocalDateTime;

public record OpportunityDTO(
        String titulo,
        String plataforma,
        String valor,
        String link,
        String descricao,
        LocalDateTime dataCaptura
) {

    // Factory method
    public static OpportunityDTO of(
            String titulo,
            String plataforma,
            String valor,
            String link,
            String descricao
    ) {
        return new OpportunityDTO(
                titulo,
                plataforma,
                valor,
                link,
                descricao,
                LocalDateTime.now()
        );
    }

    public OpportunityDTO {
        if (link == null || link.isBlank()) {
            throw new IllegalArgumentException("Link não pode ser nulo ou vazio");
        }
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("Título não pode ser nulo ou vazio");
        }

        link = link.strip();
        titulo = titulo.strip();
    }
}
