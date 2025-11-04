package com.tetris.model;

import java.awt.Color;

/**
 * Representa um tema visual para o jogo, contendo todas as cores necessárias.
 * Usamos um 'record' para uma definição concisa e imutável de um tema.
 */
public record Theme(
    String name,
    Color uiBackground,
    Color boardBackground,
    Color grid,
    Color[] pieceColors // Array com 9 cores: NoShape, 7 peças, e GarbageShape.
) {
    // --- Temas Pré-definidos ---

    /**
     * O tema escuro original do jogo.
     */
    public static final Theme CLASSIC_DARK = new Theme(
        "Clássico Escuro",
        new Color(40, 40, 55),
        new Color(20, 20, 30),
        new Color(50, 50, 70),
        new Color[] {
            new Color(0, 0, 0),       // NoShape
            new Color(204, 102, 102), // ZShape
            new Color(102, 204, 102), // SShape
            new Color(102, 102, 204), // LineShape
            new Color(204, 204, 102), // TShape
            new Color(204, 102, 204), // SquareShape
            new Color(102, 204, 204), // LShape
            new Color(218, 170, 0),    // MirroredLShape
            new Color(80, 80, 80)     // NOVO: GarbageShape
        }
    );

    /**
     * Um tema claro, com cores vibrantes.
     */
    public static final Theme LIGHT = new Theme(
        "Claro",
        new Color(220, 220, 230),
        new Color(240, 240, 255),
        new Color(200, 200, 210),
        new Color[] {
            new Color(0, 0, 0),
            new Color(255, 80, 80),
            new Color(80, 255, 80),
            new Color(80, 80, 255),
            new Color(255, 255, 80),
            new Color(255, 80, 255),
            new Color(80, 255, 255),
            new Color(255, 170, 0),
            new Color(130, 130, 130)  // NOVO: GarbageShape
        }
    );
    
    /**
     * Um tema retro que imita as cores de um Game Boy.
     */
    public static final Theme RETRO_GB = new Theme(
        "Retro GB",
        new Color(155, 188, 15), // Fundo UI verde-claro
        new Color(195, 228, 55), // Fundo do tabuleiro verde-claríssimo
        new Color(135, 168, 15), // Grelha verde-escuro
         new Color[] {
            new Color(15, 56, 15), // Todas as peças têm a mesma cor verde-escura
            new Color(15, 56, 15),
            new Color(15, 56, 15),
            new Color(15, 56, 15),
            new Color(15, 56, 15),
            new Color(15, 56, 15),
            new Color(15, 56, 15),
            new Color(15, 56, 15),
            new Color(15, 56, 15) // NOVO: GarbageShape (mesma cor)
        }
    );

    // Array que contém todos os temas disponíveis para fácil acesso.
    public static final Theme[] AVAILABLE_THEMES = { CLASSIC_DARK, LIGHT, RETRO_GB };
}