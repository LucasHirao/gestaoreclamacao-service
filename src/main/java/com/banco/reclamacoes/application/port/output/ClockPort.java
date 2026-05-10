package com.banco.reclamacoes.application.port.output;

import java.time.Instant;

public interface ClockPort {
    Instant agora();
}
