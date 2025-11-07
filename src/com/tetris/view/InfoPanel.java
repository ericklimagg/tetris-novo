package com.tetris.view;

import com.tetris.model.Board;
import com.tetris.model.Piece;
import com.tetris.model.Shape;
import com.tetris.model.Theme;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Painel responsável por exibir as informações do jogo (pontuação, nível, etc.).
 * ATUALIZADO: Adiciona 'Vitórias' e usa HighScore estático.
 */
public class InfoPanel extends JPanel {

    private static final int PANEL_WIDTH = 250;
    private static final int SQUARE_PREVIEW_SIZE = 20;

    private Board board;
    private Theme currentTheme;

    public InfoPanel() {
        this.currentTheme = Theme.AVAILABLE_THEMES[0];
        setPreferredSize(new Dimension(PANEL_WIDTH, 1)); // A altura será definida pelo layout
        setBackground(currentTheme.uiBackground());
    }

    public void updateInfo(Board board) {
        this.board = board;
    }
    
    public void updateTheme(Theme theme) {
        this.currentTheme = theme;
        setBackground(theme.uiBackground());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (board == null || !board.isStarted()) {
            return;
        }

        drawGameInfo((Graphics2D) g);
    }
    
    /**
     * Desenha a interface de informações do jogo com um design aprimorado.
     * @param g2d O contexto gráfico 2D para um melhor desenho.
     */
    private void drawGameInfo(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color textColor = (currentTheme.uiBackground().getRed() < 128) ? Color.WHITE : Color.BLACK;
        
        int padding = 20;
        int blockWidth = PANEL_WIDTH - (2 * padding);
        int blockHeight = 60;
        int spacing = 15;
        
        int currentY = 40;

        // --- ATUALIZADO: Chama Board.getHighScore() (estático) ---
        currentY = drawInfoBlock(g2d, "HIGH SCORE", String.format("%06d", Board.getHighScore()), padding, currentY, blockWidth, blockHeight, textColor);
        currentY += spacing;
        
        // --- NOVO: Adiciona o card de Vitórias ---
        currentY = drawInfoBlock(g2d, "VITÓRIAS", String.format("%03d", board.getWins()), padding, currentY, blockWidth, blockHeight, textColor);
        currentY += spacing;
        // --- FIM NOVO ---
        
        currentY = drawInfoBlock(g2d, "PONTUAÇÃO", String.format("%06d", board.getScore()), padding, currentY, blockWidth, blockHeight, textColor);
        currentY += spacing;
        
        // Blocos lado a lado para Nível e Linhas
        int halfWidth = (blockWidth - spacing) / 2;
        drawInfoBlock(g2d, "NÍVEL", String.format("%02d", board.getLevel()), padding, currentY, halfWidth, blockHeight, textColor);
        drawInfoBlock(g2d, "LINHAS", String.format("%03d", board.getLinesCleared()), padding + halfWidth + spacing, currentY, halfWidth, blockHeight, textColor);
        currentY += blockHeight + spacing;

        drawInfoBlock(g2d, "TETRIS", String.format("%03d", board.getTetrisCount()), padding, currentY, halfWidth, blockHeight, textColor);
        drawInfoBlock(g2d, "PEÇAS", String.format("%04d", board.getTotalPieces()), padding + halfWidth + spacing, currentY, halfWidth, blockHeight, textColor);
        currentY += blockHeight + spacing;


        // Bloco da Próxima Peça com altura aumentada
        currentY = drawNextPiecePanel(g2d, "PRÓXIMA PEÇA", padding, currentY, blockWidth, 110, textColor);
        
        // Bloco de Dica de Controle na parte inferior
        drawControlHintBlock(g2d, "PAUSA (P)", padding, getHeight() - 85, blockWidth, 60, textColor);
    }

    /**
     * Helper para desenhar um bloco de informação estilizado.
     */
    private int drawInfoBlock(Graphics2D g, String title, String value, int x, int y, int width, int height, Color textColor) {
        Color blockColor = currentTheme.uiBackground().darker();
        Color borderColor = currentTheme.uiBackground().brighter();

        g.setColor(blockColor);
        g.fillRoundRect(x, y, width, height, 15, 15);
        
        g.setColor(borderColor);
        g.drawRoundRect(x, y, width, height, 15, 15);
        
        g.setColor(textColor);
        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        g.drawString(title, x + 15, y + 22);
        
        g.setFont(new Font("Consolas", Font.BOLD, 22));
        g.drawString(value, x + 15, y + 48);

        return y + height;
    }

    /**
     * Helper para desenhar o painel da próxima peça.
     */
    private int drawNextPiecePanel(Graphics2D g, String title, int x, int y, int width, int height, Color textColor) {
        Color blockColor = currentTheme.uiBackground().darker();
        Color borderColor = currentTheme.uiBackground().brighter();

        g.setColor(blockColor);
        g.fillRoundRect(x, y, width, height, 15, 15);
        g.setColor(borderColor);
        g.drawRoundRect(x, y, width, height, 15, 15);

        g.setColor(textColor);
        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        g.drawString(title, x + 15, y + 22);
        
        Piece nextPiece = board.getNextPiece();
        if (nextPiece != null) {
            int previewX = x + (width / 2) - (2 * SQUARE_PREVIEW_SIZE);
            int previewY = y + 45; 
            for (int i = 0; i < 4; i++) {
                int px = previewX + (nextPiece.x(i) + 1) * SQUARE_PREVIEW_SIZE;
                int py = previewY + (1 - nextPiece.y(i)) * SQUARE_PREVIEW_SIZE;
                drawSquare(g, px, py, nextPiece.getShape(), SQUARE_PREVIEW_SIZE);
            }
        }
        return y + height;
    }

    /**
     * Helper para desenhar o painel de dica de controle.
     */
    private void drawControlHintBlock(Graphics2D g, String text, int x, int y, int width, int height, Color textColor) {
        Color blockColor = currentTheme.uiBackground().darker();
        Color borderColor = currentTheme.uiBackground().brighter();

        g.setColor(blockColor);
        g.fillRoundRect(x, y, width, height, 15, 15);
        g.setColor(borderColor);
        g.drawRoundRect(x, y, width, height, 15, 15);

        g.setColor(textColor);
        g.setFont(new Font("Consolas", Font.BOLD, 18));
        
        int stringWidth = g.getFontMetrics().stringWidth(text);
        g.drawString(text, x + (width - stringWidth) / 2, y + (height / 2) + 7);
    }

    private void drawSquare(Graphics g, int x, int y, Shape.Tetrominoe shape, int size) {
        Color[] colors = currentTheme.pieceColors();
        Color color = colors[shape.ordinal()];

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