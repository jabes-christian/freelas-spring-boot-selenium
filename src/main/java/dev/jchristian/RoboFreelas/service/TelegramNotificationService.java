package dev.jchristian.RoboFreelas.service;

import dev.jchristian.RoboFreelas.entity.Opportunity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TelegramNotificationService {

    private final RestClient restClient;
    private final String chatId;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private static final String TELEGRAM_API = "https://api.telegram.org/bot";

    public TelegramNotificationService(
            @Value("${telegram.bot.token}") String token,
            @Value("${telegram.bot.chat-id}") String chatId) {
        this.chatId = chatId;
        this.restClient = RestClient.builder()
                .baseUrl(TELEGRAM_API + token)
                .build();
    }

    public void enviarNotificacao(Opportunity opportunity) {
        String mensagem = formatarMensagem(opportunity);
        enviar(mensagem);
    }

    private void enviar(String texto) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("chat_id", chatId);
            body.put("text", texto);
            body.put("parse_mode", "Markdown");

            restClient.post()
                    .uri("/sendMessage")
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("[Telegram] Mensagem enviada com sucesso");

        } catch (Exception e) {
            log.error("[Telegram] Falha ao enviar mensagem: {}", e.getMessage());
            throw new RuntimeException("Erro ao enviar notificação no Telegram", e);
        }
    }


    private String formatarMensagem(Opportunity opportunity) {
        String dataFormatada = opportunity.getDataCaptura() != null
                ? opportunity.getDataCaptura().format(FORMATTER)
                : "Data não disponível";

        String valor = (opportunity.getValor() != null && !opportunity.getValor().isBlank())
                ? opportunity.getValor()
                : "Não informado";

        return String.format("""
                🚀 *Nova Oportunidade Encontrada!*

                📌 *Projeto:* %s
                🌐 *Plataforma:* %s
                💰 *Valor:* %s
                🔗 *Link:* [Ver Projeto](%s)
                🕐 *Analisado em:* %s
                """,
                opportunity.getTitulo(),
                opportunity.getPlataforma(),
                valor,
                opportunity.getLink(),
                dataFormatada
        );
    }
}