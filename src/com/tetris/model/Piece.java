package com.tetris.model;
import java.util.Random;

import com.tetris.model.Shape.Tetrominoe;

/**
 * Representa a peça (tetrominó) que está caindo.
 * Contém a forma, coordenadas, e a lógica de rotação.
 * Agora também armazena sua posição (x, y) no tabuleiro.
 */
public class Piece {

    private Shape.Tetrominoe pieceShape;
    private int[][] coords;
    private int x, y; // Posição da peça no tabuleiro

    // NOVO: Gerador estático para garantir aleatoriedade
    private static final Random randomGenerator = new Random();

    public Piece() {
        coords = new int[4][2];
        setShape(Shape.Tetrominoe.NoShape);
    }

    public void setShape(Shape.Tetrominoe shape) {
        // ####################################################################
        // CORREÇÃO: Usa a tabela estática de Shape.java em vez de uma local.
        // ####################################################################
        int[][][] coordsTable = Shape.getCoordsTable();

        for (int i = 0; i < 4; i++) {
            System.arraycopy(coordsTable[shape.ordinal()][i], 0, this.coords[i], 0, 2);
        }
        pieceShape = shape;
    }

    // --- Getters e Setters para posição ---
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    // --- Getters e Setters para coordenadas relativas ---
    private void setCoordX(int index, int val) { coords[index][0] = val; }
    private void setCoordY(int index, int val) { coords[index][1] = val; }
    public int x(int index) { return coords[index][0]; }
    public int y(int index) { return coords[index][1]; }
    public Shape.Tetrominoe getShape() { return pieceShape; }

    public void setRandomShape() {
        // Usa o gerador estático
        int x = Math.abs(randomGenerator.nextInt()) % 7 + 1; // (1-7)
        Shape.Tetrominoe[] values = Shape.Tetrominoe.values();
        setShape(values[x]);
    }

    public int minX() {
        int m = coords[0][0];
        for (int i = 1; i < 4; i++) m = Math.min(m, coords[i][0]);
        return m;
    }

    public int minY() {
        int m = coords[0][1];
        for (int i = 1; i < 4; i++) m = Math.min(m, coords[i][1]);
        return m;
    }

    // Rotaciona a peça para a esquerda (sentido anti-horário)
    public Piece rotateLeft() {
        if (pieceShape == Shape.Tetrominoe.SquareShape) return this;
        Piece result = new Piece();
        result.pieceShape = this.pieceShape;
        for (int i = 0; i < 4; i++) {
            result.setCoordX(i, -y(i));
            result.setCoordY(i, x(i));
        }
        return result;
    }

    // Rotaciona a peça para a direita (sentido horário)
    public Piece rotateRight() {
        if (pieceShape == Shape.Tetrominoe.SquareShape) return this;
        Piece result = new Piece();
        result.pieceShape = this.pieceShape;
        for (int i = 0; i < 4; i++) {
            result.setCoordX(i, y(i));
            result.setCoordY(i, -x(i));
        }
        return result;
    }
}