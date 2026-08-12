package com.regula.controlplane.repo;

import com.regula.controlplane.domain.SuscripcionCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SuscripcionClienteRepository extends JpaRepository<SuscripcionCliente, Long> {
    Optional<SuscripcionCliente> findTopByEmpresaIdOrderByFechaFinDesc(UUID empresaId);
}

