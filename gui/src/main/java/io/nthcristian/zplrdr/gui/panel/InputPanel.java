package io.nthcristian.zplrdr.gui.panel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import io.nthcristian.zplrdr.gui.service.ServiceProvider;
import io.nthcristian.zplrdr.gui.table.ZplFileTableModel;
import io.nthcristian.zplrdr.preset.Preset;

/**
 * Painel de entrada para seleção de arquivos ZPL e escolha de
 * predefinição de conversão.
 *
 * <p>Usa um {@link JFileChooser} para permitir que o usuário selecione
 * arquivos de etiqueta ZPL. Os arquivos selecionados são exibidos em
 * uma tabela e podem ser removidos. Uma caixa de seleção lista as
 * predefinições disponíveis carregadas via
 * {@link ServiceProvider#presetService()}. Os botões de ação disparam
 * a conversão no {@link MainPanel} pai.</p>
 *
 * <p>Thread-safety: todos os componentes Swing são acessados na EDT.</p>
 */
public class InputPanel extends JPanel {

    private final MainPanel parent;
    private final ZplFileTableModel fileTableModel;
    private final JTable fileTable;
    private final JComboBox<String> presetCombo;
    private final List<Path> selectedPaths;

    /**
     * Constrói o painel de entrada.
     *
     * @param parent o painel raiz que media as ações de conversão
     */
    public InputPanel(MainPanel parent) {
        this.parent = parent;
        this.selectedPaths = new ArrayList<>();

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createTitledBorder("Entrada"));

        // --- Tabela de arquivos ---
        fileTableModel = new ZplFileTableModel();
        fileTable = new JTable(fileTableModel);
        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileTable.setFillsViewportHeight(true);

        var fileButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        var addButton = new JButton("Adicionar arquivos ZPL...");
        addButton.addActionListener(e -> addFiles());
        fileButtons.add(addButton);

        var loadPdfButton = new JButton("Carregar PDFs...");
        loadPdfButton.addActionListener(e -> loadPdfs());
        fileButtons.add(loadPdfButton);

        var removeButton = new JButton("Remover");
        removeButton.addActionListener(e -> removeSelected());
        fileButtons.add(removeButton);

        var filePanel = new JPanel(new BorderLayout());
        filePanel.add(fileButtons, BorderLayout.NORTH);
        filePanel.add(new JScrollPane(fileTable), BorderLayout.CENTER);

        // --- Seletor de predefinição ---
        var presetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        presetPanel.add(new JLabel("Predefinição:"));
        presetCombo = new JComboBox<>();
        refreshPresets();
        presetPanel.add(presetCombo);

        // --- Botões de ação ---
        var actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        var convertButton = new JButton("Converter");
        convertButton.addActionListener(e -> parent.onConvert());
        actionPanel.add(convertButton);

        var convertPrintButton = new JButton("Converter e Imprimir");
        convertPrintButton.addActionListener(e -> parent.onConvertAndPrint());
        actionPanel.add(convertPrintButton);

        // --- Montagem ---
        var topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(filePanel);
        topPanel.add(Box.createVerticalStrut(4));
        topPanel.add(presetPanel);
        topPanel.add(Box.createVerticalStrut(4));
        topPanel.add(actionPanel);

        add(topPanel, BorderLayout.CENTER);
    }

    /**
     * Abre um JFileChooser para carregar arquivos PDF existentes
     * e os encaminha ao painel principal para impressão direta.
     */
    public void loadPdfs() {
        var chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setDialogTitle("Selecionar arquivos PDF");

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            var files = chooser.getSelectedFiles();
            var paths = new java.util.ArrayList<Path>(files.length);
            for (var file : files) {
                paths.add(file.toPath().toAbsolutePath().normalize());
            }
            parent.onPdfsLoaded(paths);
        }
    }

    /**
     * Abre um JFileChooser e adiciona os arquivos ZPL selecionados à tabela.
     */
    public void addFiles() {
        var chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setDialogTitle("Selecionar arquivos de etiqueta ZPL");

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            for (var file : chooser.getSelectedFiles()) {
                Path path = file.toPath().toAbsolutePath().normalize();
                if (!selectedPaths.contains(path)) {
                    selectedPaths.add(path);
                    fileTableModel.addFile(path);
                }
            }
        }
    }

    /**
     * Remove as linhas selecionadas da tabela de arquivos.
     */
    public void removeSelected() {
        int[] rows = fileTable.getSelectedRows();
        for (int i = rows.length - 1; i >= 0; i--) {
            fileTableModel.removeFile(rows[i]);
            selectedPaths.remove(rows[i]);
        }
    }

    /**
     * Recarrega a caixa de seleção de predefinições a partir do
     * PresetService compartilhado.
     */
    public void refreshPresets() {
        String current = (String) presetCombo.getSelectedItem();
        presetCombo.removeAllItems();

        List<Preset> presets = ServiceProvider.presetService().listPresets();
        presets.stream()
                .map(Preset::name)
                .sorted(Comparator.naturalOrder())
                .forEach(presetCombo::addItem);

        if (current != null) {
            presetCombo.setSelectedItem(current);
        }
    }

    /**
     * Retorna os caminhos dos arquivos atualmente selecionados pelo usuário.
     *
     * @return uma cópia defensiva dos caminhos selecionados
     */
    public List<Path> getSelectedPaths() {
        return List.copyOf(selectedPaths);
    }

    /**
     * Retorna o nome da predefinição atualmente selecionada, ou null.
     *
     * @return o nome da predefinição selecionada
     */
    public String getSelectedPresetName() {
        return (String) presetCombo.getSelectedItem();
    }
}
