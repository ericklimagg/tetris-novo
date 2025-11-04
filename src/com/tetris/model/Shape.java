package com.tetris.model;

/**
 * Enum que define todas as formas dos Tetrominós e a tabela de coordenadas.
 */
public class Shape {

    public enum Tetrominoe {
        NoShape, ZShape, SShape, LineShape, TShape, SquareShape, LShape, MirroredLShape,
        GarbageShape // NOVO: Bloco de lixo
    }

    private static int[][][] coordsTable;

    static {
        coordsTable = new int[][][] {
            { { 0, 0 },  { 0, 0 },  { 0, 0 },  { 0, 0 } }, // NoShape
            { { 0, -1 }, { 0, 0 },  { -1, 0 }, { -1, 1 } }, // ZShape
            { { 0, -1 }, { 0, 0 },  { 1, 0 },  { 1, 1 } },  // SShape
            { { 0, -1 }, { 0, 0 },  { 0, 1 },  { 0, 2 } },  // LineShape
            { { -1, 0 }, { 0, 0 },  { 1, 0 },  { 0, 1 } },  // TShape
            { { 0, 0 },  { 1, 0 },  { 0, 1 },  { 1, 1 } },  // SquareShape
            { { -1, -1 },{ 0, -1 }, { 0, 0 },  { 0, 1 } },  // LShape
            { { 1, -1 }, { 0, -1 }, { 0, 0 },  { 0, 1 } },  // MirroredLShape
            { { 0, 0 },  { 0, 0 },  { 0, 0 },  { 0, 0 } }  // NOVO: GarbageShape (dummy)
        };
    }

    public static int[][][] getCoordsTable() {
        return coordsTable;
    }
}