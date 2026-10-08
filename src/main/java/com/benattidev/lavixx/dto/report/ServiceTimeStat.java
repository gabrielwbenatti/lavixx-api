package com.benattidev.lavixx.dto.report;

import java.util.UUID;

/**
 * Tempo de execucao de um servico, medido (inicio -> conclusao da OS) nas OS do periodo em
 * que ele foi o unico servico de quantidade 1 (so assim o tempo da OS e atribuivel a ele).
 *
 * @param estimatedMinutes duracao cadastrada no servico (null se nao informada)
 * @param samples          quantidade de OS medidas
 * @param averageMinutes   media em minutos
 * @param medianMinutes    mediana em minutos (menos sensivel a OS concluida com atraso)
 */
public record ServiceTimeStat(
        UUID serviceId,
        String name,
        Short estimatedMinutes,
        long samples,
        long averageMinutes,
        long medianMinutes) {
}
