package com.aluracursos.screenmatch.model;

public enum Categoria {
    ACCION("Action", "Acción"),
    ROMANCE("Romance", "Romance"),
    COMEDIA("Comedy", "Comedia"),
    DRAMA("Drama", "Drama"),
    CRIMEN("Crime", "Crimen"),
    AVENTURA("Adventure", "Aventura");

    private String categoriaOmdb;
    private String categoriaSpanol;

    Categoria(String categoriaOmdb, String categoriaSpanol) {
        this.categoriaOmdb = categoriaOmdb;
        this.categoriaSpanol = categoriaSpanol;
    }

    public static Categoria fromString(String text) {
        for (Categoria categoria : Categoria.values()) {
            if (categoria.categoriaOmdb.equalsIgnoreCase(text)) {
                return categoria;
            }
        }
        throw new IllegalArgumentException("Ninguna categoria encontrada: " + text);
    }

    public static Categoria fromSpanol(String text) {
        for (Categoria categoria : Categoria.values()) {
            if (categoria.categoriaSpanol.equalsIgnoreCase(text)) {
                return categoria;
            }
        }
        throw new IllegalArgumentException("Ninguna categoria encontrada: " + text);
    }
}
