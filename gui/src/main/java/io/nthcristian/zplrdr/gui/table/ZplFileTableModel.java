package io.nthcristian.zplrdr.gui.table;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import io.nthcristian.zplrdr.gui.util.FormatUtil;

/**
 * Modelo de tabela para a lista de arquivos ZPL de entrada selecionados.
 *
 * <p>Colunas: Nome do arquivo | Tamanho | Status. Suporta adição e
 * remoção dinâmica de linhas conforme o usuário seleciona ou
 * remove arquivos.</p>
 */
public class ZplFileTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {
        "Nome do arquivo", "Tamanho", "Status"
    };

    private static final class Row {
        final Path path;
        final long size;
        String status;

        Row(Path path, long size, String status) {
            this.path = path;
            this.size = size;
            this.status = status;
        }
    }

    private final List<Row> rows;

    /**
     * Cria um modelo de tabela de arquivos vazio.
     */
    public ZplFileTableModel() {
        this.rows = new ArrayList<>();
    }

    /**
     * Adiciona um arquivo ao modelo e dispara um evento de inserção
     * de linha na tabela.
     *
     * @param file o caminho do arquivo ZPL a adicionar
     */
    public void addFile(Path file) {
        long size = fileSize(file);
        String status = size >= 0 ? "Pronto" : "Não encontrado";
        int newRow = rows.size();
        rows.add(new Row(file, size, status));
        fireTableRowsInserted(newRow, newRow);
    }

    /**
     * Remove o arquivo no índice de linha informado.
     *
     * @param rowIndex o índice da linha a remover
     */
    public void removeFile(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < rows.size()) {
            rows.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    /**
     * Remove todas as linhas.
     */
    public void clear() {
        int last = rows.size() - 1;
        if (last >= 0) {
            rows.clear();
            fireTableRowsDeleted(0, last);
        }
    }

    @Override
    public int getRowCount() {
        return rows.size();
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
        Row r = rows.get(row);
        return switch (col) {
            case 0 -> r.path.getFileName() != null
                    ? r.path.getFileName().toString()
                    : r.path.toString();
            case 1 -> r.size >= 0 ? FormatUtil.formatSize(r.size) : "—";
            case 2 -> r.status;
            default -> null;
        };
    }

    private static long fileSize(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            return -1;
        }
    }

}
