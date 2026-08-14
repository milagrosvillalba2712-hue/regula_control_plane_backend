package com.regula.controlplane.repo;

import com.regula.controlplane.domain.CatalogoVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CatalogoVersionRepository extends JpaRepository<CatalogoVersion, Long> {
    List<CatalogoVersion> findByActivoTrueOrderByCatalogoCodigoAsc();
    Optional<CatalogoVersion> findFirstByCatalogoCodigoAndVersionAndActivoTrue(String codigo, String version);
}

