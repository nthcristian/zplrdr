package io.nthcristian.zplrdr.gui.panel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import io.nthcristian.zplrdr.document.PdfDocument;
import io.nthcristian.zplrdr.gui.table.PdfResultTableModel;
import io.nthcristian.zplrdr.gui.worker.PrinterListWorker;

/**
 * Painel de saída para exibição dos resultados da conversão e controle
 * de impressão.
 *
 * <p>Após uma conversão bem-sucedida, recebe {@code PdfDocument[]} e
 * preenche uma tabela de resultados. Uma caixa de seleção mostra as
 * impressoras disponíveis (atualizadas via {@link PrinterListWorker}).
 * Botões de impressão e exportação são fornecidos.</p>
 */
public class OutputPanel extends JPanel {

    private final MainPanel parent;
    private final PdfResultTableModel resultTableModel;
    private final JTable resultTable;
    private final JComboBox<String> printerCombo;
    private final JProgressBar progressBar;

    /**
     * Constrói o painel de saída.
     *
     * @param parent o painel raiz que media as ações de conversão
     */
    public OutputPanel(MainPanel parent) {
        this.parent = parent;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createTitledBorder("Saída"));

        // --- Tabela de resultados ---
        resultTableModel = new PdfResultTableModel();
        resultTable = new JTable(resultTableModel);
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultTable.setFillsViewportHeight(true);

        // --- Seletor de dispositivo ---
        var printerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        printerPanel.add(new JLabel("Dispositivo:"));
        printerCombo = new JComboBox<>();
        printerCombo.setEditable(true);
        printerPanel.add(printerCombo);

        var refreshButton = new JButton("Atualizar");
        refreshButton.addActionListener(e -> refreshPrinters());
        printerPanel.add(refreshButton);

        // --- Botões de ação ---
        var actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        var printButton = new JButton("Imprimir resultados");
        printButton.addActionListener(e -> parent.onPrintResults());
        actionPanel.add(printButton);

        var saveAsButton = new JButton("Salvar como...");
        saveAsButton.addActionListener(e -> saveResults());
        actionPanel.add(saveAsButton);

        var clearButton = new JButton("Limpar");
        clearButton.addActionListener(e -> {
            clearResults();
            parent.clearLastResult();
        });
        actionPanel.add(clearButton);

        // --- Barra de progresso ---
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);

        // --- Montagem ---
        var bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.add(printerPanel);
        bottomPanel.add(actionPanel);
        bottomPanel.add(progressBar);

        add(new JScrollPane(resultTable), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Preenche a tabela de resultados com os PDFs convertidos e habilita
     * as ações de impressão e salvamento.
     *
     * @param documents os documentos PDF convertidos
     */
    public void showResults(PdfDocument[] documents) {
        resultTableModel.setResults(documents);
        progressBar.setValue(100);
    }

    /**
     * Limpa os resultados e redefine a barra de progresso.
     */
    public void clearResults() {
        resultTableModel.clear();
        progressBar.setValue(0);
    }

    /**
     * Atualiza a barra de progresso.
     *
     * @param percent valor de progresso (0–100)
     */
    public void setProgress(int percent) {
        progressBar.setIndeterminate(false);
        progressBar.setValue(Math.clamp(percent, 0, 100));
    }

    /**
     * Ativa ou desativa o modo indeterminado da barra de progresso
     * (usado durante a impressão, que não tem etapas mensuráveis).
     *
     * @param printing true para iniciar, false para concluir
     */
    public void setPrinting(boolean printing) {
        if (printing) {
            progressBar.setIndeterminate(true);
        } else {
            progressBar.setIndeterminate(false);
            progressBar.setValue(100);
        }
    }

    /**
     * Atualiza a lista de impressoras iniciando um PrinterListWorker.
     */
    public void refreshPrinters() {
        var worker = new PrinterListWorker((devices, error) -> {
            if (error != null) {
                JOptionPane.showMessageDialog(this,
                        "Falha ao listar dispositivos: " + error.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            printerCombo.removeAllItems();
            if (devices != null) {
                for (String device : devices) {
                    printerCombo.addItem(device);
                }
            }
        });
        worker.execute();
    }

    /**
     * Retorna o endereço do dispositivo atualmente selecionado.
     *
     * @return o endereço do dispositivo (tcp://host:9100 ou caminho), ou null
     */
    public String getSelectedDevice() {
        Object item = printerCombo.getSelectedItem();
        return item != null ? item.toString() : null;
    }

    /**
     * Abre um diálogo de seleção de diretório e salva cada PDF convertido
     * com nome baseado em GUID para evitar sobrescrita.
     */
    private void saveResults() {
        PdfDocument[] docs = parent.getLastResult();
        if (docs == null || docs.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Nenhum documento para salvar.",
                    "Erro ao salvar", JOptionPane.ERROR_MESSAGE);
            return;
        }

        var chooser = new JFileChooser();
        chooser.setDialogTitle("Selecionar diretório de destino");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path directory = chooser.getSelectedFile().toPath();
        try {
            Files.createDirectories(directory);

            int saved = 0;
            for (PdfDocument doc : docs) {
                String guid = UUID.randomUUID().toString();
                Path file = directory.resolve("etiqueta-" + guid + ".pdf");
                Files.write(file, doc.data());
                saved++;
            }

            JOptionPane.showMessageDialog(this,
                    "Salvos " + saved + " documento(s) em " + directory + ".",
                    "Salvamento concluído", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Falha ao salvar PDF: " + e.getMessage(),
                    "Erro ao salvar", JOptionPane.ERROR_MESSAGE);
        }
    }
}
