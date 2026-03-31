package dev.jchristian.RoboFreelas.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "opportunities", uniqueConstraints = {
        @UniqueConstraint(columnNames = "link", name = "uk_opportunity_link")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String plataforma;

    @Column
    private String valor;

    @Column(nullable = false, unique = true, length = 1000)
    private String link;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    private LocalDateTime dataCaptura;

    @Column(nullable = false)
    private boolean notificado = false;

    @PrePersist
    public void prePersist() {
        if (this.dataCaptura == null) {
            this.dataCaptura = LocalDateTime.now();
        }
    }
}