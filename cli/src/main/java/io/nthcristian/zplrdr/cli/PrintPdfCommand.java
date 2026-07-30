package io.nthcristian.zplrdr.cli;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import io.nthcristian.prt.Dimensions;
import io.nthcristian.prt.PrinterService;
import io.nthcristian.zplrdr.document.PdfDocument;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "print-pdf",
        description = "Print existing PDF files on a label printer.")
class PrintPdfCommand implements Callable<Integer> {

    @Option(names = { "--device" }, required = true,
            description = "Printer device address (tcp://host:9100 or /dev/usb/lp0).")
    String device;

    @Option(names = { "--width" }, required = true,
            description = "Label width in inches.")
    float widthInches;

    @Option(names = { "--height" }, required = true,
            description = "Label height in inches.")
    float heightInches;

    @Option(names = { "--dpmm" }, required = true,
            description = "Printer resolution in dots per mm (e.g. 8).")
    float dpmm;

    @Parameters(paramLabel = "PDF", arity = "1..*", description = "One or more PDF files to print.")
    List<Path> pdfFiles;

    @Override
    public Integer call() throws Exception {
        PdfDocument[] documents = CliSupport.loadPdfs(pdfFiles);

        float dpi = dpmm * 25.4f;
        int wMm = Math.round(widthInches * 25.4f);
        int hMm = Math.round(heightInches * 25.4f);
        Dimensions dims = new Dimensions(wMm, hMm, dpi);

        PrinterService printerService = CliSupport.printerService();
        printerService.printAll(documents, device, dims);
        System.out.println("Printed " + documents.length + " page(s) to '" + device + "'.");
        return 0;
    }

}
