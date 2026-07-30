package io.nthcristian.zplrdr.cli;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import io.nthcristian.prt.Dimensions;
import io.nthcristian.prt.PrinterService;
import io.nthcristian.zplrdr.document.PdfDocument;
import io.nthcristian.zplrdr.preset.Preset;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "print", description = "Convert ZPL label files to PDF and print them.")
class PrintCommand implements Callable<Integer> {

    @Option(names = { "-p", "--preset" }, required = true, description = "Preset name to use for conversion.")
    String presetName;

    @Option(names = { "--device" }, required = true,
            description = "Printer device address (tcp://host:9100 or /dev/usb/lp0).")
    String device;

    @Parameters(paramLabel = "ZPL", arity = "1..*", description = "One or more ZPL files to convert and print.")
    List<Path> zplFiles;

    @Override
    public Integer call() throws Exception {
        var presetService = CliSupport.presetService();
        Preset preset = CliSupport.requirePreset(presetService, presetName);
        PdfDocument[] documents = CliSupport.convert(CliSupport.zplConverter(), zplFiles, preset);

        Dimensions dims = Dimensions.fromPreset(preset);
        PrinterService printerService = CliSupport.printerService();
        printerService.printAll(documents, device, dims);
        System.out.println("Printed " + documents.length + " page(s) to '" + device + "'.");
        return 0;
    }

}
