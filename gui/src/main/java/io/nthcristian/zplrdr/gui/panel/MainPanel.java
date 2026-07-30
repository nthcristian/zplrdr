package io.nthcristian.zplrdr.gui.panel;

import java.awt.BorderLayout;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import io.nthcristian.prt.Dimensions;
import io.nthcristian.zplrdr.document.PdfDocument;
import io.nthcristian.zplrdr.gui.service.ServiceProvider;
import io.nthcristian.zplrdr.gui.worker.ConvertWorker;
import io.nthcristian.zplrdr.gui.worker.PrintWorker;
import io.nthcristian.zplrdr.preset.Preset;

/**
 * Painel raiz que organiza as áreas de entrada e saída.
 *
 * <p>Atua como mediador entre {@link InputPanel} e {@link OutputPanel}:
 * quando o usuário dispara uma conversão, este painel inicia um
 * {@link ConvertWorker} e, quando os resultados chegam, os encaminha
 * ao painel de saída. Os painéis filhos se comunicam com este painel
 * via chamadas de método, sem referências diretas entre si.</p>
 */
public class MainPanel extends JPanel {

    private final InputPanel inputPanel;
    private final OutputPanel outputPanel;

    private PdfDocument[] lastResult;
    private Dimensions currentDims;

    /**
     * Constrói o painel principal com um split pane vertical:
     * área de entrada no topo, área de saída na parte inferior.
     */
    public MainPanel() {
        super(new BorderLayout());

        inputPanel = new InputPanel(this);
        outputPanel = new OutputPanel(this);

        var splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                inputPanel,
                outputPanel);
        splitPane.setResizeWeight(0.4);
        splitPane.setDividerLocation(256);

        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Retorna o resultado mais recente da conversão, ou null.
     *
     * @return os últimos documentos PDF produzidos
     */
    public PdfDocument[] getLastResult() {
        return lastResult;
    }

    /**
     * Limpa o último resultado para que o painel de saída possa ser reiniciado.
     */
    public void clearLastResult() {
        lastResult = null;
    }

    /**
     * Chamado quando o usuário clica em "Converter" no painel de entrada.
     *
     * <p>Abre streams de entrada dos caminhos ZPL selecionados, resolve
     * a predefinição escolhida e inicia uma conversão em segundo plano.
     * Em caso de sucesso, os resultados são passados ao painel de saída.
     * Em caso de falha, um diálogo de erro é exibido.</p>
     */
    /**
     * Called by {@link #onConvertAndPrint} after conversion completes.
     * Delegates to {@link #onPrintResults()} which handles the device
     * selection, progress, and user feedback.
     */
    private void printAfterConvert() {
        onPrintResults();
    }

    public void onConvert() {
        List<Path> paths = inputPanel.getSelectedPaths();
        if (paths == null || paths.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Selecione pelo menos um arquivo ZPL.",
                    "Nenhum arquivo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Preset preset = resolvePreset();
        if (preset == null) {
            return;
        }

        InputStream[] streams;
        try {
            streams = openStreams(paths);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Falha ao abrir arquivos ZPL: " + e.getMessage(),
                    "Erro de E/S", JOptionPane.ERROR_MESSAGE);
            return;
        }

        outputPanel.setProgress(0);
        currentDims = Dimensions.fromPreset(preset);
        var worker = new ConvertWorker(streams, preset, (docs, error) -> {
            try {
                if (error != null) {
                    JOptionPane.showMessageDialog(this,
                            "Falha na conversão: " + error.getMessage(),
                            "Erro de conversão", JOptionPane.ERROR_MESSAGE);
                    outputPanel.setProgress(0);
                } else {
                    lastResult = docs;
                    outputPanel.showResults(docs);
                }
            } finally {
                closeStreams(streams);
            }
        });
        worker.execute();
    }

    /**
     * Chamado quando o usuário clica em "Converter e Imprimir" no painel
     * de entrada.
     *
     * <p>Converte arquivos ZPL e imediatamente imprime os PDFs gerados
     * na impressora selecionada.</p>
     */
    public void onConvertAndPrint() {
        List<Path> paths = inputPanel.getSelectedPaths();
        if (paths == null || paths.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Selecione pelo menos um arquivo ZPL.",
                    "Nenhum arquivo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Preset preset = resolvePreset();
        if (preset == null) {
            return;
        }

        InputStream[] streams;
        try {
            streams = openStreams(paths);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Falha ao abrir arquivos ZPL: " + e.getMessage(),
                    "Erro de E/S", JOptionPane.ERROR_MESSAGE);
            return;
        }

        outputPanel.setProgress(0);
        currentDims = Dimensions.fromPreset(preset);
        var convertWorker = new ConvertWorker(streams, preset, (docs, error) -> {
            try {
                if (error != null) {
                    JOptionPane.showMessageDialog(this,
                            "Falha na conversão: " + error.getMessage(),
                            "Erro de conversão", JOptionPane.ERROR_MESSAGE);
                    outputPanel.setProgress(0);
                } else {
                    lastResult = docs;
                    outputPanel.showResults(docs);
                    printAfterConvert();
                }
            } finally {
                closeStreams(streams);
            }
        });
        convertWorker.execute();
    }

    /**
     * Atualiza a caixa de seleção de predefinições no painel de entrada
     * a partir do backend.
     */
    public void refreshPresets() {
        inputPanel.refreshPresets();
    }

    /**
     * Chamado pelo InputPanel quando o usuário carrega arquivos PDF
     * diretamente (sem conversão ZPL). Os PDFs são armazenados como
     * resultado para impressão imediata. As dimensões da etiqueta são
     * extraídas da predefinição selecionada.
     */
    public void onPdfsLoaded(List<Path> pdfPaths) {
        try {
            var docs = new PdfDocument[pdfPaths.size()];
            for (int i = 0; i < pdfPaths.size(); i++) {
                docs[i] = new PdfDocument(Files.readAllBytes(pdfPaths.get(i)));
            }
            lastResult = docs;

            // Resolve dimensions from the currently selected preset
            Preset preset = resolvePresetSilently();
            if (preset != null) {
                currentDims = Dimensions.fromPreset(preset);
            }

            outputPanel.showResults(docs);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Falha ao carregar PDFs: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Chamado pelo painel de saída quando o usuário clica em
     * "Imprimir resultados". Usa o último resultado de conversão,
     * o dispositivo selecionado e as dimensões atuais da etiqueta.
     */
    public void onPrintResults() {
        if (lastResult == null || lastResult.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Nenhum documento para imprimir.",
                    "Erro de impressão", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (currentDims == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma predefinição primeiro.",
                    "Erro de impressão", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String device = outputPanel.getSelectedDevice();
        if (device == null || device.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Selecione ou digite um endereço de dispositivo.",
                    "Erro de impressão", JOptionPane.ERROR_MESSAGE);
            return;
        }

        outputPanel.setPrinting(true);
        new PrintWorker(lastResult, device, currentDims, (v, error) -> {
            outputPanel.setPrinting(false);
            if (error != null) {
                JOptionPane.showMessageDialog(this,
                        "Falha na impressão: " + error.getMessage(),
                        "Erro de impressão", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Impressos " + lastResult.length + " documento(s) em " + device + ".",
                        "Impressão concluída", JOptionPane.INFORMATION_MESSAGE);
            }
        }).execute();
    }

    /**
     * Retorna as dimensões atuais da etiqueta da última predefinição usada.
     *
     * @return as dimensões atuais, ou null
     */
    public Dimensions getCurrentDims() {
        return currentDims;
    }

    /**
     * Encaminha uma solicitação de adicionar arquivos ao painel de entrada
     * (usado pela barra de menus).
     */
    public void requestAddFiles() {
        inputPanel.addFiles();
    }

    /**
     * Resolve a predefinição selecionada sem exibir diálogos de erro.
     *
     * @return o Preset, ou null se nenhuma predefinição estiver selecionada
     */
    private Preset resolvePresetSilently() {
        String presetName = inputPanel.getSelectedPresetName();
        if (presetName == null) {
            return null;
        }
        return ServiceProvider.presetService().getPreset(presetName);
    }

    /**
     * Resolve o nome da predefinição selecionada para um objeto
     * {@link Preset}, exibindo um diálogo de erro se não encontrada.
     */
    private Preset resolvePreset() {
        String presetName = inputPanel.getSelectedPresetName();
        if (presetName == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma predefinição.",
                    "Nenhuma predefinição", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        Preset preset = ServiceProvider.presetService().getPreset(presetName);
        if (preset == null) {
            JOptionPane.showMessageDialog(this,
                    "Predefinição '" + presetName + "' não encontrada.",
                    "Erro de predefinição", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return preset;
    }

    /**
     * Abre streams de entrada para os caminhos de arquivo fornecidos.
     */
    private InputStream[] openStreams(List<Path> paths) throws Exception {
        var streams = new InputStream[paths.size()];
        for (int i = 0; i < paths.size(); i++) {
            streams[i] = Files.newInputStream(paths.get(i));
        }
        return streams;
    }

    private static void closeStreams(InputStream[] streams) {
        for (InputStream s : streams) {
            try {
                s.close();
            } catch (Exception ignored) {
                // best-effort
            }
        }
    }
}
