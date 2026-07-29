package io.nthcristian.zplrdr.cli;

import java.util.concurrent.Callable;

import picocli.CommandLine.Command;

@Command(
        name = "printers",
        description = "List installed printers available to the print service.")
class PrintersCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        String[] printers = CliSupport.printerService().listPrinters();
        if (printers.length == 0) {
            System.out.println("No printers found.");
            return 0;
        }
        for (String printer : printers) {
            System.out.println(printer);
        }
        return 0;
    }

}
