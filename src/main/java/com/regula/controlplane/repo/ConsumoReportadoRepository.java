package com.regula.controlplane.repo;

import com.regula.controlplane.domain.ConsumoReportado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumoReportadoRepository extends JpaRepository<ConsumoReportado, Long> {
}
