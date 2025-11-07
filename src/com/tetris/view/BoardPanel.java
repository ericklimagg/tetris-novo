package com.tetris.view;

import com.tetris.model.Board;
import com.tetris.model.Piece;
import com.tetris.model.Shape;
import com.tetris.model.Theme;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

/**
 * Painel responsável por desenhar o tabuleiro de jogo e as peças.
 * ATUALIZADO: Fundo/Grade só são desenhados SE o jogo estiver ativo.
 */
public class BoardPanel extends JPanel {

    private Board board;
    private Theme currentTheme;

    public BoardPanel() {
        this.currentTheme = Theme.AVAILABLE_THEMES[0];
        setPreferredSize(new Dimension(getSquareSize() * Board.BOARD_WIDTH, getSquareSize() * Board.BOARD_HEIGHT));
    }

    public void updateBoard(Board board) {
        this.board = board;
    }

    public void updateTheme(Theme theme) {
        this.currentTheme = theme;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // --- CORREÇÃO DO FUNDO DO MENU ---
        // A lógica de desenho do tabuleiro/grade só deve
        // rodar se o jogo (board) existir e estiver iniciado.
        if (board == null || !board.isStarted() || currentTheme == null) {
            // Se o jogo não começou (estamos no menu),
            // o OverlayPanel é quem vai desenhar o fundo sólido.
            // Não fazemos nada aqui.
            return;
        }
        // --- FIM DA CORREÇÃO ---
        
        // O restante da lógica só roda se o jogo estiver ativo
        drawBoardBackground(g);
        drawGrid(g);
        drawPlacedPieces(g);
        drawGhostPiece(g); 
        drawCurrentPiece(g);
        drawLinedClearAnimation(g);
    }

    private void drawBoardBackground(Graphics g) {
        g.setColor(currentTheme.boardBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawGrid(Graphics g) {
        g.setColor(currentTheme.grid());
        int squareSize = getSquareSize();
        for (int i = 0; i <= Board.BOARD_WIDTH; i++) {
            g.drawLine(i * squareSize, 0, i * squareSize, getHeight());
        }
        for (int i = 0; i <= Board.BOARD_HEIGHT; i++) {
            g.drawLine(0, i * squareSize, getWidth(), i * squareSize);
        }
    }

    private void drawPlacedPieces(Graphics g) {
        int squareSize = getSquareSize();
        for (int i = 0; i < Board.BOARD_HEIGHT; i++) {
            
            if (board.isAnimatingLineClear() && board.getLinesBeingCleared().contains(i)) {
                continue;
            }

            for (int j = 0; j < Board.BOARD_WIDTH; j++) {
                Shape.Tetrominoe shape = board.shapeAt(j, i);
                if (shape != Shape.Tetrominoe.NoShape) {
                    drawSquare(g, j * squareSize, (Board.BOARD_HEIGHT - 1 - i) * squareSize, shape, false);
                }
            }
        }
    }

    private void drawCurrentPiece(Graphics g) {
        if (board.isAnimatingLineClear()) {
            return;
        }
        
        Piece currentPiece = board.getCurrentPiece();
        if (board.isStarted() && currentPiece.getShape() != Shape.Tetrominoe.NoShape) {
            int squareSize = getSquareSize();
            for (int i = 0; i < 4; i++) {
                int x = currentPiece.getX() + currentPiece.x(i);
                int y = currentPiece.getY() - currentPiece.y(i);
                
                if (y < Board.BOARD_HEIGHT) { 
                    drawSquare(g, x * squareSize, (Board.BOARD_HEIGHT - 1 - y) * squareSize, currentPiece.getShape(), false);
                }
            }
        }
    }

    private void drawGhostPiece(Graphics g) {
        if (!board.isGhostPieceEnabled() || !board.isStarted() || board.isAnimatingLineClear()) {
            return;
        }
        
        Piece currentPiece = board.getCurrentPiece();
        if (currentPiece.getShape() == Shape.Tetrominoe.NoShape) {
            return;
        }

        int ghostY = board.getGhostPieceY();
        int squareSize = getSquareSize();

        for (int i = 0; i < 4; i++) {
            int x = currentPiece.getX() + currentPiece.x(i);
            int y = ghostY - currentPiece.y(i);
             if (y < Board.BOARD_HEIGHT) {
                drawSquare(g, x * squareSize, (Board.BOARD_HEIGHT - 1 - y) * squareSize, currentPiece.getShape(), true);
            }
        }
    }

    private void drawLinedClearAnimation(Graphics g) {
        if (!board.isAnimatingLineClear()) {
            return;
        }

        int timer = board.getLineClearTimer();
        
        Color flashColor;
        if (timer % 2 == 0) {
            flashColor = Color.WHITE;
        } else {
            flashColor = currentTheme.boardBackground();
        }
        
        g.setColor(flashColor);

        int squareSize = getSquareSize();
        for (int row : board.getLinesBeingCleared()) {
            int y = (Board.BOARD_HEIGHT - 1 - row) * squareSize;
            g.fillRect(0, y, getWidth(), squareSize);
        }
    }


    private void drawSquare(Graphics g, int x, int y, Shape.Tetrominoe shape, boolean isGhost) {
        int size = getSquareSize();
        Color[] colors = currentTheme.pieceColors();
        Color color = colors[shape.ordinal()];

        if (isGhost) {
            g.setColor(color.darker()); 
            g.drawRect(x + 1, y + 1, size - 2, size - 2); 
        } else {
            g.setColor(color);
            g.fillRect(x + 1, y + 1, size - 2, size - 2);

            g.setColor(color.brighter());
            g.drawLine(x, y + size - 1, x, y);
            g.drawLine(x, y, x + size - 1, y);

            g.setColor(color.darker());
            g.drawLine(x + 1, y + size - 1, x + size - 1, y + size - 1);
            g.drawLine(x + size - 1, y + size - 1, x + size - 1, y + 1);
        }
    }
    
    private int getSquareSize() {
        return 40;
    }
}