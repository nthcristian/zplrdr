package io.nthcristian.zplrdr.cli;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import io.nthcristian.prt.PrinterService;
import io.nthcristian.zplrdr.document.PdfDocument;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "print-pdf",
        description = "Print existing PDF files on a label printer.")
class PrintPdfCommand implements Callable<Integer> {

    @Option(names = { "--printer" }, description = "Printer name. Uses the system default when omitted.")
    String printerName;

    @Parameters(paramLabel = "PDF", arity = "1..*", description = "One or more PDF files to print.")
    List<Path> pdfFiles;

    @Override
    public Integer call() throws Exception {
        PdfDocument[] documents = CliSupport.loadPdfs(pdfFiles);
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
