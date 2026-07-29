package io.nthcristian.zplrdr.gui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * Diálogo "Sobre" exibindo versão do aplicativo e créditos.
 *
 * <p>Um diálogo modal leve com o nome do aplicativo, versão e uma
 * breve descrição do que o zplrdr faz.</p>
 */
public class AboutDialog extends JDialog {

    private static final String ABOUT_TEXT = """
            <html>
            <h2>zplrdr</h2>
            <p>Versão 1.0</p>
            <p>Converte etiquetas ZPL (Zebra Programming Language) para PDF<br>
            e imprime em impressoras térmicas de etiquetas.</p>
            <p>Desenvolvido com a <a href="http://api.labelary.com">API Labelary</a>.</p>
            </html>
            """;

    /**
     * Constrói o diálogo "Sobre".
     *
     * @param owner janela proprietária para modalidade e centralização
     */
    public AboutDialog(Frame owner) {
        super(owner, "Sobre o zplrdr", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        var content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        var label = new JLabel(ABOUT_TEXT);
        content.add(label, BorderLayout.CENTER);

        var buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        var closeButton = new JButton("Fechar");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        content.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(content);
        pack();
        setLocationRelativeTo(owner);
    }
}
