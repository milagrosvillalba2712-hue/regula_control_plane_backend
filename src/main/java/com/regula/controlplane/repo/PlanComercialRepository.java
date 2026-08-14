package com.regula.controlplane.repo;

import com.regula.controlplane.domain.PlanComercial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlanComercialRepository extends JpaRepository<PlanComercial, Long> {
    Optional<PlanComercial> findByCodigo(String codigo);
}

