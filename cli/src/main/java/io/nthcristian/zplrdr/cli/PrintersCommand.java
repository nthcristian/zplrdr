package io.nthcristian.zplrdr.cli;

import java.util.concurrent.Callable;

import io.nthcristian.prt.PrinterService;
import picocli.CommandLine.Command;

@Command(
        name = "printers",
        description = "List locally-attached printer devices.")
class PrintersCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        String[] devices = PrinterService.listDevices();
        if (devices.length == 0) {
            System.out.println("No local printer devices found.");
            System.out.println("Try a TCP address: tcp://<ip>:9100");
            return 0;
        }
        for (String device : devices) {
            System.out.println(device);
        }
        return 0;
    }

}
