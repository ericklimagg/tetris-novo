package com.tetris.view;

import com.tetris.controller.GameController; 
import com.tetris.model.Theme; 

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import java.awt.Dimension;
import java.awt.Color; 

/**
 * A janela principal do jogo (o JFrame).
 * ATUALIZADO: Corrige erro de compilação (JLayer-Pane).
 */
public class GameFrame extends JFrame {

    private GamePanel gamePanel;
    private OverlayPanel overlayPanel;
    private JLayeredPane layeredPane;

    public GameFrame() {
        initComponents();
    }

    private void initComponents() {
        layeredPane = new JLayeredPane();
        
        gamePanel = new GamePanel();
        overlayPanel = new OverlayPanel();

        // --- CORREÇÃO DE LAYOUT INICIAL ---
        // Força o GamePanel a calcular seu tamanho MÁXIMO (2P)
        gamePanel.setMode(GameController.GameMode.TWO_PLAYER);
        Dimension size = gamePanel.getPreferredSize();
        
        // Esconde os componentes P2 para o menu (modo 1P visualmente)
        gamePanel.setMode(GameController.GameMode.ONE_PLAYER); 
        // --- FIM DA CORREÇÃO ---
        
        layeredPane.setPreferredSize(size); // Usa o tamanho MÁXIMO
        
        // --- CORREÇÃO DA MARGEM BRANCA ---
        layeredPane.setOpaque(true);
        layeredPane.setBackground(Theme.AVAILABLE_THEMES[0].uiBackground()); 
        // --- FIM DA CORREÇÃO ---
        
        layeredPane.setLayout(null); 
        
        gamePanel.setBounds(0, 0, size.width, size.height);
        overlayPanel.setBounds(0, 0, size.width, size.height);
        overlayPanel.setOpaque(false);

        layeredPane.add(gamePanel, JLayeredPane.DEFAULT_LAYER);
        
        // --- 1. CORREÇÃO DO ERRO DE COMPILAÇÃO ---
        // JLayer-Pane -> JLayeredPane
        layeredPane.add(overlayPanel, JLayeredPane.PALETTE_LAYER); 
        // --- FIM DA CORREÇÃO ---
        
        add(layeredPane);

        setTitle("Tetris");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack(); // Pack inicial (agora com o tamanho correto de 2P)
        setLocationRelativeTo(null);
    }
    
    /**
     * Método chamado pelo Controller para redimensionar a janela
     * (encolher ou expandir) para 1P ou 2P.
     */
    public void packAndCenter() {
        // Atualiza os tamanhos dos painéis internos ANTES de empacotar
        Dimension size = gamePanel.getPreferredSize();
        layeredPane.setPreferredSize(size);
        
        // Atualiza os bounds do gamePanel E do overlayPanel para o novo tamanho
        gamePanel.setBounds(0, 0, size.width, size.height);
        overlayPanel.setBounds(0, 0, size.width, size.height);
        
        pack(); // Re-calcula o tamanho da janela (encolhe ou expande)
        setLocationRelativeTo(null); // Re-centraliza
    }

    // --- Métodos de acesso para o Controller ---

    public GamePanel getGamePanel() {
        return gamePanel;
    }

    public OverlayPanel getOverlayPanel() {
        return overlayPanel;
    }
}