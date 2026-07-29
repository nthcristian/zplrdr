package io.nthcristian.zplrdr.cli;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import io.nthcristian.prt.PrinterService;
import io.nthcristian.zplrdr.PresetService;
import io.nthcristian.zplrdr.ZplConverter;
import io.nthcristian.zplrdr.builder.PresetServiceBuilder;
import io.nthcristian.zplrdr.builder.ZplConverterBuilder;
import io.nthcristian.zplrdr.document.PdfDocument;
import io.nthcristian.zplrdr.error.ZplConverterException;
import io.nthcristian.zplrdr.preset.Preset;

public final class CliSupport {

    private CliSupport() {
    }

    public static PresetService presetService() {
        return PresetServiceBuilder.build();
    }

    public static ZplConverter zplConverter() {
        return ZplConverterBuilder.build();
    }

    public static PrinterService printerService() {
        return new PrinterService();
    }

    public static Preset requirePreset(PresetService presetService, String name) throws CliException {
        Preset preset = presetService.getPreset(name);
        if (preset == null) {
            throw new CliException(
                    "Preset '%s' not found. Create one with: zplrdr preset create %s".formatted(name, name));
        }
        return preset;
    }

    public static InputStream[] openZplStreams(List<Path> files) throws CliException {
        if (files == null || files.isEmpty()) {
            throw new CliException("At least one ZPL file is required");
        }

        List<InputStream> streams = new ArrayList<>(files.size());
        try {
            for (Path file : files) {
                if (!Files.isRegularFile(file)) {
                    throw new CliException("ZPL file not found: " + file);
                }
                streams.add(Files.newInputStream(file));
            }
        } catch (IOException e) {
            closeQuietly(streams);
            throw new CliException("Failed to open ZPL file: " + e.getMessage(), e);
        }
        return streams.toArray(InputStream[]::new);
    }

    public static PdfDocument[] convert(ZplConverter converter, List<Path> zplFiles, Preset preset)
            throws CliException {
        InputStream[] streams = openZplStreams(zplFiles);
        try {
            return converter.convertAll(streams, preset);
        } catch (ZplConverterException e) {
            throw new CliException("Conversion failed: " + e.getMessage(), e);
        } finally {
            closeQuietly(List.of(streams));
        }
    }

    public static PdfDocument[] loadPdfs(List<Path> pdfFiles) throws CliException {
        if (pdfFiles == null || pdfFiles.isEmpty()) {
            throw new CliException("At least one PDF file is required");
        }

        PdfDocument[] documents = new PdfDocument[pdfFiles.size()];
        for (int i = 0; i < pdfFiles.size(); i++) {
            Path file = pdfFiles.get(i);
            if (!Files.isRegularFile(file)) {
                throw new CliException("PDF file not found: " + file);
            }
            try {
                documents[i] = new PdfDocument(Files.readAllBytes(file));
            } catch (IOException e) {
                throw new CliException("Failed to read PDF file: " + file, e);
            }
        }
        return documents;
    }

    public static List<Path> writePdfs(PdfDocument[] documents, Path output) throws CliException {
        if (documents.length == 0) {
            throw new CliException("Conversion produced no PDF documents");
        }

        try {
            boolean outputIsDirectory = Files.isDirectory(output);
            if (documents.length == 1 && !outputIsDirectory) {
                Path parent = output.toAbsolutePath().normalize().getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.write(output, documents[0].data());
                return List.of(output.toAbsolutePath().normalize());
            }

            Path directory;
            String stem;
            String fileName = output.getFileName() != null ? output.getFileName().toString() : "label";
            if (outputIsDirectory || !fileName.toLowerCase().endsWith(".pdf")) {
                Files.createDirectories(output);
                directory = output;
                stem = "label";
            } else {
                Path parent = output.getParent() != null ? output.getParent() : Path.of(".");
                Files.createDirectories(parent);
                directory = parent;
                stem = fileName.replaceFirst("(?i)\\.pdf$", "");
            }

            List<Path> written = new ArrayList<>(documents.length);
            for (int i = 0; i < documents.length; i++) {
                Path file = directory.resolve("%s-%d.pdf".formatted(stem, i + 1));
                Files.write(file, documents[i].data());
                written.add(file.toAbsolutePath().normalize());
            }
            return written;
        } catch (IOException e) {
            throw new CliException("Failed to write PDF output: " + e.getMessage(), e);
        }
    }

    private static void closeQuietly(List<? extends InputStream> streams) {
        for (InputStream stream : streams) {
            try {
                stream.close();
            } catch (IOException ignored) {
                // best-effort
            }
        }
    }

}
