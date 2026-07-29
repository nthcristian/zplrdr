package io.nthcristian.zplrdr.gui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;

import io.nthcristian.zplrdr.PresetService;
import io.nthcristian.zplrdr.preset.Preset;

/**
 * Diálogo modal para gerenciamento de predefinições.
 *
 * <p>Suporta listar, criar, visualizar/editar campos e excluir
 * predefinições — espelhando o grupo de subcomandos {@code preset} da CLI.
 * Cada operação delega ao {@link PresetService} e exibe diálogos de erro
 * em caso de falha. Após uma mutação, a flag {@link #isModified()} é
 * ativada para que a janela proprietária possa atualizar sua caixa de
 * seleção de predefinições.</p>
 */
public class PresetManagerDialog extends JDialog {

    private final PresetService presetService;
    private final DefaultListModel<String> listModel;
    private final JList<String> presetList;
    private boolean modified;

    /**
     * Constrói um diálogo modal de gerenciamento de predefinições.
     *
     * @param owner         janela proprietária para modalidade
     * @param presetService instância compartilhada de PresetService
     */
    public PresetManagerDialog(Frame owner, PresetService presetService) {
        super(owner, "Gerenciar predefinições", true);
        this.presetService = presetService;
        this.listModel = new DefaultListModel<>();
        this.modified = false;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // --- Lista de predefinições ---
        presetList = new JList<>(listModel);
        presetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loadPresetList();

        // --- Botões ---
        var buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));

        var createButton = new JButton("Criar...");
        createButton.addActionListener(e -> onCreate());
        buttonPanel.add(createButton);

        var editButton = new JButton("Editar...");
        editButton.addActionListener(e -> onEdit());
        buttonPanel.add(editButton);

        var deleteButton = new JButton("Excluir");
        deleteButton.addActionListener(e -> onDelete());
        buttonPanel.add(deleteButton);

        // --- Montagem ---
        var content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(new JLabel("Predefinições:"), BorderLayout.NORTH);
        content.add(new JScrollPane(presetList), BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(content);
        setSize(400, 350);
        setLocationRelativeTo(owner);
    }

    /**
     * Retorna true se uma predefinição foi criada, editada ou excluída
     * desde que este diálogo foi aberto.
     *
     * @return se houve modificações nas predefinições
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Recarrega a lista de predefinições a partir do PresetService.
     */
    private void loadPresetList() {
        listModel.clear();
        List<Preset> presets = presetService.listPresets();
        presets.stream()
                .map(Preset::name)
                .sorted(Comparator.naturalOrder())
                .forEach(listModel::addElement);
    }

    /**
     * Abre um subdiálogo para criar uma nova predefinição com valores
     * padrão dos campos.
     */
    private void onCreate() {
        String name = JOptionPane.showInputDialog(this,
                "Digite um nome para a nova predefinição:",
                "Criar predefinição", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.isBlank()) {
            return;
        }

        try {
            Preset preset = presetService.createPreset(name);
            preset = editPresetFields(preset, true);
            if (preset != null) {
                presetService.savePreset(preset);
                modified = true;
                loadPresetList();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Falha ao criar predefinição: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Abre um subdiálogo para editar os campos da predefinição selecionada.
     */
    private void onEdit() {
        String selectedName = presetList.getSelectedValue();
        if (selectedName == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma predefinição para editar.",
                    "Nenhuma seleção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Preset preset = presetService.getPreset(selectedName);
        if (preset == null) {
            JOptionPane.showMessageDialog(this,
                    "Predefinição '" + selectedName + "' não encontrada.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Preset updated = editPresetFields(preset, false);
        if (updated != null) {
            try {
                presetService.savePreset(updated);
                modified = true;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Falha ao salvar predefinição: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Exclui a predefinição selecionada após confirmação.
     */
    private void onDelete() {
        String selectedName = presetList.getSelectedValue();
        if (selectedName == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma predefinição para excluir.",
                    "Nenhuma seleção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Excluir predefinição '" + selectedName + "'?\nEsta ação não pode ser desfeita.",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            presetService.deletePreset(selectedName);
            modified = true;
            loadPresetList();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Falha ao excluir predefinição: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Exibe um editor de campos simples para os campos chave-valor da
     * predefinição fornecida.
     *
     * @param preset a predefinição a editar
     * @param isNew  true se for uma predefinição recém-criada
     * @return a predefinição atualizada, ou null se o usuário cancelou
     */
    private Preset editPresetFields(Preset preset, boolean isNew) {
        var fields = preset.fields();
        var fieldNames = fields.keySet().stream().sorted().toList();

        var panel = new JPanel(new GridLayout(fieldNames.size(), 2, 8, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        var fieldInputs = new JTextField[fieldNames.size()];
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            panel.add(new JLabel(fieldName + ":"));
            var input = new JTextField(fields.getOrDefault(fieldName, ""));
            panel.add(input);
            fieldInputs[i] = input;
        }

        var dialog = new JDialog(this,
                isNew ? "Criar predefinição — " + preset.name()
                      : "Editar predefinição — " + preset.name(), true);
        var contentPanel = new JPanel(new BorderLayout(8, 8));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        contentPanel.add(panel, BorderLayout.CENTER);

        var buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var saveButton = new JButton("Salvar");
        var cancelButton = new JButton("Cancelar");
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        JPanel buttonContainer = new JPanel(new BorderLayout());
        buttonContainer.add(new JLabel(" "), BorderLayout.CENTER);
        buttonContainer.add(buttonPanel, BorderLayout.EAST);
        contentPanel.add(buttonContainer, BorderLayout.SOUTH);
        dialog.setContentPane(contentPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        final Preset[] result = { null };
        saveButton.addActionListener(e -> {
            Preset updated = preset;
            for (int i = 0; i < fieldNames.size(); i++) {
                updated = updated.withProperty(fieldNames.get(i), fieldInputs[i].getText());
            }
            result[0] = updated;
            dialog.dispose();
        });
        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
        return result[0];
    }
}
