package io.nthcristian.zplrdr.cli.preset;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.Callable;

import io.nthcristian.zplrdr.PresetService;
import io.nthcristian.zplrdr.cli.CliException;
import io.nthcristian.zplrdr.cli.CliSupport;
import io.nthcristian.zplrdr.preset.Preset;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "preset",
        description = "Manage label conversion presets.",
        subcommands = {
                PresetCommands.ListCmd.class,
                PresetCommands.CreateCmd.class,
                PresetCommands.ShowCmd.class,
                PresetCommands.SetCmd.class,
                PresetCommands.DeleteCmd.class
        })
public class PresetCommands implements Callable<Integer> {

    @Override
    public Integer call() {
        System.err.println("Missing preset subcommand. Use: list, create, show, set, or delete.");
        return 2;
    }

    @Command(name = "list", description = "List saved presets.")
    static class ListCmd implements Callable<Integer> {
        @Override
        public Integer call() {
            var presets = CliSupport.presetService().listPresets().stream()
                    .sorted(Comparator.comparing(Preset::name))
                    .toList();
            if (presets.isEmpty()) {
                System.out.println("No presets saved.");
                return 0;
            }
            for (Preset preset : presets) {
                System.out.println(preset.name());
            }
            return 0;
        }
    }

    @Command(name = "create", description = "Create and save a preset with Labelary defaults.")
    static class CreateCmd implements Callable<Integer> {
        @Parameters(index = "0", paramLabel = "NAME", description = "Preset name.")
        String name;

        @Override
        public Integer call() throws Exception {
            PresetService service = CliSupport.presetService();
            Preset preset = service.createPreset(name);
            service.savePreset(preset);
            System.out.println("Created preset '" + name + "'.");
            printFields(preset);
            return 0;
        }
    }

    @Command(name = "show", description = "Show a preset and its fields.")
    static class ShowCmd implements Callable<Integer> {
        @Parameters(index = "0", paramLabel = "NAME", description = "Preset name.")
        String name;

        @Override
        public Integer call() throws Exception {
            Preset preset = CliSupport.requirePreset(CliSupport.presetService(), name);
            System.out.println("name=" + preset.name());
            printFields(preset);
            return 0;
        }
    }

    @Command(name = "set", description = "Update preset fields and save.")
    static class SetCmd implements Callable<Integer> {
        @Parameters(index = "0", paramLabel = "NAME", description = "Preset name.")
        String name;

        @Option(names = "--dpmm", description = "Dots per mm, e.g. 8dpmm.")
        String dpmm;

        @Option(names = "--width", description = "Label width in inches.")
        String width;

        @Option(names = "--height", description = "Label height in inches.")
        String height;

        @Override
        public Integer call() throws Exception {
            if (dpmm == null && width == null && height == null) {
                throw new CliException("Provide at least one of --dpmm, --width, or --height.");
            }

            PresetService service = CliSupport.presetService();
            Preset preset = CliSupport.requirePreset(service, name);
            if (dpmm != null) {
                preset = preset.withProperty("dpmm", dpmm);
            }
            if (width != null) {
                preset = preset.withProperty("width", width);
            }
            if (height != null) {
                preset = preset.withProperty("height", height);
            }
            service.savePreset(preset);
            System.out.println("Updated preset '" + name + "'.");
            printFields(preset);
            return 0;
        }
    }

    @Command(name = "delete", description = "Delete a saved preset.")
    static class DeleteCmd implements Callable<Integer> {
        @Parameters(index = "0", paramLabel = "NAME", description = "Preset name.")
        String name;

        @Override
        public Integer call() throws Exception {
            CliSupport.presetService().deletePreset(name);
            System.out.println("Deleted preset '" + name + "'.");
            return 0;
        }
    }

    private static void printFields(Preset preset) {
        preset.fields().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> System.out.println(entry.getKey() + "=" + entry.getValue()));
    }

}
