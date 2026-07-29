package io.nthcristian.zplrdr.gui;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import io.nthcristian.zplrdr.gui.dialog.AboutDialog;
import io.nthcristian.zplrdr.gui.dialog.PresetManagerDialog;
import io.nthcristian.zplrdr.gui.panel.MainPanel;

/**
 * Janela principal do aplicativo zplrdr.
 *
 * <p>Cria o JFrame com barra de menus (Arquivo, Predefinições, Ajuda) e um
 * {@link MainPanel} como painel de conteúdo raiz. Os serviços de backend são
 * fornecidos por {@link io.nthcristian.zplrdr.gui.service.ServiceProvider}
 * e conectados à hierarquia de painéis por esta janela.</p>
 *
 * <p>Thread-safety: construída e exibida na EDT (veja {@link Main}).</p>
 */
public final class GuiApplication extends JFrame {

    private final MainPanel mainPanel;

    /**
     * Constrói a janela principal do aplicativo.
     *
     * <p>Configura o chrome da janela, barra de menus e painel de conteúdo.
     * A janela não é tornada visível aqui — o chamador deve invocar
     * {@link #setVisible} após a construção.</p>
     */
    public GuiApplication() {
        super("zplrdr");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        mainPanel = new MainPanel();
        setJMenuBar(createMenuBar());
        setContentPane(mainPanel);

        setSize(960, 640);
        setLocationRelativeTo(null);
    }

    /**
     * Constrói a barra de menus com os menus Arquivo, Predefinições e Ajuda.
     *
     * @return a barra de menus completamente montada
     */
    private JMenuBar createMenuBar() {
        var menuBar = new JMenuBar();

        // --- Menu Arquivo ---
        var fileMenu = new JMenu("Arquivo");
        fileMenu.setMnemonic(KeyEvent.VK_A);

        var openZplItem = new JMenuItem("Abrir arquivos ZPL...");
        openZplItem.setMnemonic(KeyEvent.VK_B);
        openZplItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        openZplItem.addActionListener(e -> mainPanel.requestAddFiles());
        fileMenu.add(openZplItem);

        fileMenu.add(new JSeparator());

        var exitItem = new JMenuItem("Sair");
        exitItem.setMnemonic(KeyEvent.VK_S);
        exitItem.addActionListener(e -> dispose());
        fileMenu.add(exitItem);

        // --- Menu Predefinições ---
        var presetMenu = new JMenu("Predefinições");
        presetMenu.setMnemonic(KeyEvent.VK_P);

        var managePresetsItem = new JMenuItem("Gerenciar predefinições...");
        managePresetsItem.setMnemonic(KeyEvent.VK_G);
        managePresetsItem.addActionListener(e -> openPresetManager());
        presetMenu.add(managePresetsItem);

        // --- Menu Ajuda ---
        var helpMenu = new JMenu("Ajuda");
        helpMenu.setMnemonic(KeyEvent.VK_J);

        var aboutItem = new JMenuItem("Sobre");
        aboutItem.setMnemonic(KeyEvent.VK_S);
        aboutItem.addActionListener(e -> openAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(presetMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    /**
     * Abre o diálogo modal de gerenciamento de predefinições e atualiza
     * a caixa de seleção de predefinições se houve alterações.
     */
    private void openPresetManager() {
        var dialog = new PresetManagerDialog(this,
                io.nthcristian.zplrdr.gui.service.ServiceProvider.presetService());
        dialog.setVisible(true);
        if (dialog.isModified()) {
            mainPanel.refreshPresets();
        }
    }

    /**
     * Abre o diálogo "Sobre".
     */
    private void openAboutDialog() {
        new AboutDialog(this).setVisible(true);
    }
}
