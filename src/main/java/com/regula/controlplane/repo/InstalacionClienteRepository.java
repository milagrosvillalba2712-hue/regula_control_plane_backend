package com.regula.controlplane.repo;

import com.regula.controlplane.domain.InstalacionCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface InstalacionClienteRepository extends JpaRepository<InstalacionCliente, UUID> {
}

