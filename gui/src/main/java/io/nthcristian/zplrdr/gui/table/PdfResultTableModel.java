package io.nthcristian.zplrdr.gui.table;

import javax.swing.table.AbstractTableModel;

import io.nthcristian.zplrdr.document.PdfDocument;
import io.nthcristian.zplrdr.gui.util.FormatUtil;

/**
 * Modelo de tabela para os resultados de documentos PDF convertidos.
 *
 * <p>Colunas: # (índice) | Tamanho. Cada linha representa um documento
 * PDF produzido pelo worker de conversão.</p>
 */
public class PdfResultTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = { "#", "Tamanho" };

    private PdfDocument[] documents;

    /**
     * Cria um modelo de tabela de resultados vazio.
     */
    public PdfResultTableModel() {
        this.documents = new PdfDocument[0];
    }

    /**
     * Substitui todas as linhas com os resultados de uma conversão.
     *
     * @param documents os documentos PDF convertidos
     */
    public void setResults(PdfDocument[] documents) {
        this.documents = documents != null ? documents : new PdfDocument[0];
        fireTableDataChanged();
    }

    /**
     * Remove todas as linhas.
     */
    public void clear() {
        this.documents = new PdfDocument[0];
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return documents.length;
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int col) {
        return COLUMN_NAMES[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        PdfDocument doc = documents[row];
        return switch (col) {
            case 0 -> row + 1;
            case 1 -> FormatUtil.formatSize(doc.data().length);
            default -> null;
        };
    }

}
