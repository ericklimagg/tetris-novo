package com.tetris.view;

import com.tetris.controller.GameController;
import com.tetris.model.Theme;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
// Nenhum import de Box ou Component é mais necessário

/**
 * Painel principal que contém e organiza os outros componentes da view.
 * ATUALIZADO: Layout revertido para o original simples.
 * A centralização será feita pelo 'pack()' no GameFrame.
 */
public class GamePanel extends JPanel {

    private BoardPanel boardPanel1;
    private InfoPanel infoPanel1;
    private GarbageBarPanel garbageBar1; 
    
    private BoardPanel boardPanel2;
    private InfoPanel infoPanel2;
    private GarbageBarPanel garbageBar2; 

    public GamePanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // Cria os componentes para o Jogador 1
        infoPanel1 = new InfoPanel();
        boardPanel1 = new BoardPanel();
        garbageBar1 = new GarbageBarPanel(true); 
        
        // Cria os componentes para o Jogador 2
        infoPanel2 = new InfoPanel();
        boardPanel2 = new BoardPanel();
        garbageBar2 = new GarbageBarPanel(false); 

        // Adiciona os painéis na ordem visual simples
        add(infoPanel1);   
        add(boardPanel1);  
        add(garbageBar1);  
        
        add(garbageBar2);  
        add(boardPanel2);  
        add(infoPanel2);   

        // Define a cor de fundo inicial
        updateTheme(Theme.AVAILABLE_THEMES[0]);

        // Esconde os componentes de 2P por padrão (IMPORTANTE para o pack() do menu)
        garbageBar1.setVisible(false);
        garbageBar2.setVisible(false);
        boardPanel2.setVisible(false);
        infoPanel2.setVisible(false);
    }
    
    /**
     * Mostra ou esconde os componentes do Jogador 2.
     * Esta lógica agora é a única responsável pelo tamanho do painel.
     */
    public void setMode(GameController.GameMode mode) {
        boolean isTwoPlayer = (mode == GameController.GameMode.TWO_PLAYER);
        
        // P1's garbage bar é VISÍVEL APENAS no modo 2P
        garbageBar1.setVisible(isTwoPlayer); 
        
        // Componentes P2
        garbageBar2.setVisible(isTwoPlayer);
        boardPanel2.setVisible(isTwoPlayer);
        infoPanel2.setVisible(isTwoPlayer);
        
        revalidate(); // Avisa o layout para recalcular
    }

    /**
     * Atualiza o tema de todos os componentes visuais filhos.
     */
    public void updateTheme(Theme theme) {
        setBackground(theme.uiBackground());
        
        infoPanel1.updateTheme(theme);
        boardPanel1.updateTheme(theme);
        garbageBar1.updateTheme(theme); 
        
        infoPanel2.updateTheme(theme);
        boardPanel2.updateTheme(theme);
        garbageBar2.updateTheme(theme); 
    }


    // --- Getters para o Controller ---

    public BoardPanel getBoardPanel1() {
        return boardPanel1;
    }
    public InfoPanel getInfoPanel1() {
        return infoPanel1;
    }
    public GarbageBarPanel getGarbageBar1() { 
        return garbageBar1;
    }

    public BoardPanel getBoardPanel2() {
        return boardPanel2;
    }
    public InfoPanel getInfoPanel2() {
        return infoPanel2;
    }
    public GarbageBarPanel getGarbageBar2() { 
        return garbageBar2;
    }
}