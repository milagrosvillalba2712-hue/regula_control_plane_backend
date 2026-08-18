package com.regula.controlplane.repo;

import com.regula.controlplane.domain.DocumentoLegal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentoLegalRepository extends JpaRepository<DocumentoLegal, Long> {

    Optional<DocumentoLegal> findFirstByTipoAndActivoTrueOrderByVersionDesc(String tipo);

    List<DocumentoLegal> findByActivoTrueOrderByTipoAscVersionDesc();

    List<DocumentoLegal> findByTipoOrderByVersionDesc(String tipo);
}
