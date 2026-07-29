package io.nthcristian.zplrdr.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.nthcristian.zplrdr.document.PdfDocument;
import picocli.CommandLine;

@DisplayName("zplrdr CLI")
class MainTest {

    @Test
    @DisplayName("root --help exits successfully")
    void rootHelpExitsSuccessfully() {
        StringWriter out = new StringWriter();
        CommandLine cmd = Main.createCommandLine();
        cmd.setOut(new PrintWriter(out));

        int code = cmd.execute("--help");

        assertEquals(0, code);
        assertTrue(out.toString().contains("convert"));
        assertTrue(out.toString().contains("print"));
        assertTrue(out.toString().contains("preset"));
        assertTrue(out.toString().contains("printers"));
    }

    @Test
    @DisplayName("convert without required options fails parsing")
    void convertWithoutOptionsFails() {
        CommandLine cmd = Main.createCommandLine();
        cmd.setErr(new PrintWriter(new StringWriter()));

        int code = cmd.execute("convert");

        assertTrue(code != 0);
    }

    @Test
    @DisplayName("preset without subcommand exits with usage error")
    void presetWithoutSubcommandFails() {
        StringWriter err = new StringWriter();
        CommandLine cmd = Main.createCommandLine();
        cmd.setErr(new PrintWriter(err));
        cmd.setOut(new PrintWriter(new StringWriter()));

        int code = cmd.execute("preset");

        assertEquals(2, code);
    }

    @Test
    @DisplayName("writePdfs writes a single PDF to the given file path")
    void writePdfsWritesSingleFile(@TempDir Path tempDir) throws Exception {
        Path output = tempDir.resolve("out.pdf");
        PdfDocument[] docs = { new PdfDocument("%PDF-1.1".getBytes()) };

        var written = CliSupport.writePdfs(docs, output);

        assertEquals(1, written.size());
        assertTrue(Files.isRegularFile(output));
        assertEquals("%PDF-1.1", Files.readString(output));
    }

    @Test
    @DisplayName("writePdfs writes numbered PDFs into a directory")
    void writePdfsWritesNumberedFilesIntoDirectory(@TempDir Path tempDir) throws Exception {
        Path outputDir = tempDir.resolve("pdfs");
        PdfDocument[] docs = {
                new PdfDocument("one".getBytes()),
                new PdfDocument("two".getBytes())
        };

        var written = CliSupport.writePdfs(docs, outputDir);

        assertEquals(2, written.size());
        assertTrue(Files.isRegularFile(outputDir.resolve("label-1.pdf")));
        assertTrue(Files.isRegularFile(outputDir.resolve("label-2.pdf")));
    }

}
