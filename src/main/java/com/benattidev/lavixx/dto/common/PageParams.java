package com.benattidev.lavixx.dto.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/** Normaliza os parametros de paginacao vindos da query string. */
public final class PageParams {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    private PageParams() {
    }

    /** A ordenacao fica a cargo de cada consulta. */
    public static Pageable of(Integer page, Integer size) {
        int p = page == null || page < 0 ? 0 : page;
        int s = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        return PageRequest.of(p, s);
    }

    /** Termo de busca aparado; null quando vazio. */
    public static String search(String search) {
        return search == null || search.isBlank() ? null : search.trim();
    }

    /** Padrao LIKE case-insensitive (o termo e comparado com lower() no banco). */
    public static String likePattern(String search) {
        return "%" + escapeLike(search.toLowerCase()) + "%";
    }

    /**
     * Padrao LIKE para placa: so letras e numeros, maiusculo (a coluna e normalizada igual).
     * Sem letras/numeros no termo retorna "", que nunca casa (placa vazia e gravada como null).
     */
    public static String platePattern(String search) {
        String plate = search.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        return plate.isEmpty() ? "" : "%" + plate + "%";
    }

    /**
     * Padrao LIKE com apenas os digitos do termo (documento, telefone).
     * Sem digitos retorna "", que nunca casa (documento/telefone vazios sao gravados como null).
     */
    public static String digitsPattern(String search) {
        String digits = search.replaceAll("\\D", "");
        return digits.isEmpty() ? "" : "%" + digits + "%";
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
