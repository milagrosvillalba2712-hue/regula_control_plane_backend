package com.regula.controlplane.repo;

import com.regula.controlplane.domain.HeartbeatInstalacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeartbeatInstalacionRepository extends JpaRepository<HeartbeatInstalacion, Long> {
}
