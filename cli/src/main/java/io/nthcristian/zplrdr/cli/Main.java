package io.nthcristian.zplrdr.cli;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import io.nthcristian.zplrdr.builder.PresetServiceBuilder;
import io.nthcristian.zplrdr.builder.ZplConverterBuilder;

public class Main {
    private static final String EXAMPLE_ZPL = "^XA\\n^FO50,50^ADN,36,20^FDHello, World!^FS\\n^XZ";

    public static void main(String[] args) throws Exception {
        var zplConverter = ZplConverterBuilder.build();
        var presetService = PresetServiceBuilder.build();

        var preset = presetService.getPreset("example");
        if (preset == null) {
            preset = presetService.createPreset("example");
            presetService.savePreset(preset);
        }

        var pdf = zplConverter.convertAll(new InputStream[] { new ByteArrayInputStream(EXAMPLE_ZPL.getBytes()) },
                preset)[0];

        var filePath = Path.of(System.getProperty("user.home"), "example.pdf");

        Files.write(filePath, pdf.data());

        System.out.println("PDF successfully written to: " + filePath.toAbsolutePath());
    }
}
