package io.nthcristian.zplrdr.gui.service;

import io.nthcristian.prt.PrinterService;
import io.nthcristian.zplrdr.PresetService;
import io.nthcristian.zplrdr.ZplConverter;
import io.nthcristian.zplrdr.builder.PresetServiceBuilder;
import io.nthcristian.zplrdr.builder.ZplConverterBuilder;

/**
 * Provides access to the three backend services used by the GUI.
 *
 * <p>Services are constructed via the existing builder classes
 * ({@link ZplConverterBuilder}, {@link PresetServiceBuilder}) and cached
 * for the lifetime of the application. This avoids redundant construction
 * while keeping callers free of wiring details — mirroring the
 * {@code CliSupport} pattern from the CLI module.</p>
 *
 * <p>Thread-safety: lazy initialization is not synchronized; concurrent
 * callers may race on first access but always receive the same instance
 * because the builders are idempotent.</p>
 */
public final class ServiceProvider {

    private static volatile ZplConverter converter;
    private static volatile PresetService presetService;
    private static volatile PrinterService printerService;

    private ServiceProvider() {
    }

    /**
     * Returns the shared {@link ZplConverter}, building it on first access.
     *
     * @return a {@code ZplConverter} backed by the Labelary conversion provider
     */
    public static ZplConverter zplConverter() {
        if (converter == null) {
            converter = ZplConverterBuilder.build();
        }
        return converter;
    }

    /**
     * Returns the shared {@link PresetService}, building it on first access.
     *
     * @return a {@code PresetService} backed by the Labelary preset schema
     *         and file-based repository
     */
    public static PresetService presetService() {
        if (presetService == null) {
            presetService = PresetServiceBuilder.build();
        }
        return presetService;
    }

    /**
     * Returns the shared {@link PrinterService}, building it on first access.
     *
     * @return a {@code PrinterService} wrapping the Java Print Service API
     */
    public static PrinterService printerService() {
        if (printerService == null) {
            printerService = new PrinterService();
        }
        return printerService;
    }
}
