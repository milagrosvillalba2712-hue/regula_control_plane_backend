package com.regula.controlplane.service;

import java.time.OffsetDateTime;
import java.util.Map;

public record LeaseTokenResponse(
        String tokenType,
        String leaseToken,
        String kid,
        String algoritmo,
        OffsetDateTime emitidoEn,
        OffsetDateTime venceEn,
        OffsetDateTime graceUntil,
        Map<String, Object> payload
) {
}
