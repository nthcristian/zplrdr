package io.nthcristian.zplrdr.cli;

import io.nthcristian.zplrdr.cli.preset.PresetCommands;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

@Command(
        name = "zplrdr",
        mixinStandardHelpOptions = true,
        version = "zplrdr 1.0",
        description = "Convert ZPL labels to PDF and print them on thermal label printers.",
        subcommands = {
                ConvertCommand.class,
                PrintCommand.class,
                PrintPdfCommand.class,
                PresetCommands.class,
                PrintersCommand.class
        })
public class Main implements Runnable {

    public static void main(String[] args) {
        int exitCode = createCommandLine().execute(args);
        System.exit(exitCode);
    }

    static CommandLine createCommandLine() {
        return new CommandLine(new Main())
                .setExecutionExceptionHandler(new CliExceptionHandler());
    }

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }

    static final class CliExceptionHandler implements IExecutionExceptionHandler {
        @Override
        public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult) {
            String message = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
            commandLine.getErr().println("Error: " + message);
            return commandLine.getCommandSpec().exitCodeOnExecutionException();
        }
    }

}
