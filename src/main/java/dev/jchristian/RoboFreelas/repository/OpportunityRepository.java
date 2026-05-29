package dev.jchristian.RoboFreelas.repository;


import dev.jchristian.RoboFreelas.entity.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    boolean existsByLink(String link);

    Optional<Opportunity> findByLink(String link);

    List<Opportunity> findByNotificadoFalse();

    List<Opportunity> findByPlataforma(String plataforma);

    List<Opportunity> findByDataCapturaAfter(LocalDateTime dataCaptura);
}