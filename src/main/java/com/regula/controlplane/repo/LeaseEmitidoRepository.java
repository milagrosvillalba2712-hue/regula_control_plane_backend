package com.regula.controlplane.repo;

import com.regula.controlplane.domain.LeaseEmitido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LeaseEmitidoRepository extends JpaRepository<LeaseEmitido, UUID> {
}
