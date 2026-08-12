package com.regula.controlplane.repo;

import com.regula.controlplane.domain.EmpresaCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface EmpresaClienteRepository extends JpaRepository<EmpresaCliente, UUID> {
}

