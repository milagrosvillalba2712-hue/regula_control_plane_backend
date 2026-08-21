package com.regula.controlplane.repo;

import com.regula.controlplane.domain.PagoCliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PagoClienteRepository extends JpaRepository<PagoCliente, Long> {
    List<PagoCliente> findByEmpresaIdOrderByFechaHoraCreacionDesc(UUID empresaId);
    Optional<PagoCliente> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);
    Optional<PagoCliente> findTopByStripeSubscriptionIdOrderByFechaHoraCreacionDesc(String stripeSubscriptionId);
}
