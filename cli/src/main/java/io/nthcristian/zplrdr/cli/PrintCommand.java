package io.nthcristian.zplrdr.cli;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import io.nthcristian.prt.PrinterService;
import io.nthcristian.zplrdr.document.PdfDocument;
import io.nthcristian.zplrdr.preset.Preset;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "print",
        description = "Convert ZPL label files to PDF and print them.")
class PrintCommand implements Callable<Integer> {

    @Option(names = { "-p", "--preset" }, required = true, description = "Preset name to use for conversion.")
    String presetName;

    @Option(names = { "--printer" }, description = "Printer name. Uses the system default when omitted.")
    String printerName;

    @Parameters(paramLabel = "ZPL", arity = "1..*", description = "One or more ZPL files to convert and print.")
    List<Path> zplFiles;

    @Override
    public Integer call() throws Exception {
        var presetService = CliSupport.presetService();
        Preset preset = CliSupport.requirePreset(presetService, presetName);
        PdfDocument[] documents = CliSupport.convert(CliSupport.zplConverter(), zplFiles, preset);

        PrinterService printerService = CliSupport.printerService();
        if (printerName == null || printerName.isBlank()) {
            printerService.printAll(documents);
            System.out.println("Printed " + documents.length + " PDF document(s) to the default printer.");
        } else {
            printerService.printAll(documents, printerName);
            System.out.println("Printed " + documents.length + " PDF document(s) to '" + printerName + "'.");
        }
        return 0;
    }

}
