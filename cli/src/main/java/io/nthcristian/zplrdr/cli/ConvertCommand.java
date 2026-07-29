package io.nthcristian.zplrdr.cli;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import io.nthcristian.zplrdr.document.PdfDocument;
import io.nthcristian.zplrdr.preset.Preset;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "convert",
        description = "Convert ZPL label files to PDF using a preset.")
class ConvertCommand implements Callable<Integer> {

    @Option(names = { "-p", "--preset" }, required = true, description = "Preset name to use for conversion.")
    String presetName;

    @Option(names = { "-o", "--output" }, required = true,
            description = "Output PDF file, or a directory when multiple PDFs are produced.")
    Path output;

    @Parameters(paramLabel = "ZPL", arity = "1..*", description = "One or more ZPL files to convert.")
    List<Path> zplFiles;

    @Override
    public Integer call() throws Exception {
        var presetService = CliSupport.presetService();
        Preset preset = CliSupport.requirePreset(presetService, presetName);
        PdfDocument[] documents = CliSupport.convert(CliSupport.zplConverter(), zplFiles, preset);
        List<Path> written = CliSupport.writePdfs(documents, output);
        for (Path path : written) {
            System.out.println("Wrote " + path);
        }
        return 0;
    }

}
