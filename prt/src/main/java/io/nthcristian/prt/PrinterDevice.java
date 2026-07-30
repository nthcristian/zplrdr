package io.nthcristian.prt;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Sends raw byte data to a printer device without going through any
 * operating-system print driver.
 *
 * <p>Supports two transport mechanisms selected by address format:</p>
 * <ul>
 *   <li>{@code tcp://host:port} — raw TCP socket (e.g. port 9100,
 *       the standard JetDirect / raw-printing port). Works on any OS.</li>
 *   <li>an absolute filesystem path — treated as a device file
 *       (Linux {@code /dev/usb/lp0}, macOS {@code /dev/cu.usb*},
 *       Windows {@code \\.\COM3}).</li>
 * </ul>
 */
public final class PrinterDevice {

    private PrinterDevice() {
    }

    /**
     * Sends the given bytes to the printer at {@code address}.
     *
     * @param address device address ({@code tcp://…} or a device path)
     * @param data    raw bytes to send (typically TSPL commands)
     * @throws IOException if the connection fails or the device is unreachable
     */
    public static void send(String address, byte[] data) throws IOException {
        if (address == null || address.isBlank()) {
            throw new IOException("Printer address must not be blank");
        }

        if (address.startsWith("tcp://")) {
            sendTcp(address, data);
        } else {
            Files.write(Path.of(address), data);
        }
    }

    /**
     * Discovers locally-attached raw printer devices.
     *
     * <p>Scanning is best-effort and platform-specific. The returned
     * list is never null; users should also be able to type custom
     * addresses.</p>
     *
     * @return discovered device addresses (may be empty)
     */
    public static String[] listDevices() {
        List<String> devices = new ArrayList<>();

        // Linux — USB printer class devices
        globDevices(devices, "/dev/usb/lp");

        // macOS — USB modem / serial devices
        globDevices(devices, "/dev/cu.usb");

        // Windows — we can't scan easily; user configures manually
        // (most thermal printers on Windows use network port 9100 anyway)

        return devices.toArray(String[]::new);
    }

    private static void sendTcp(String address, byte[] data) throws IOException {
        String hostPort = address.substring("tcp://".length());
        int colon = hostPort.lastIndexOf(':');
        if (colon < 0) {
            throw new IOException("Invalid TCP address: " + address
                    + " (expected tcp://host:port)");
        }
        String host = hostPort.substring(0, colon);
        int port = Integer.parseInt(hostPort.substring(colon + 1));

        try (Socket socket = new Socket(host, port);
             OutputStream out = socket.getOutputStream()) {
            out.write(data);
            out.flush();
        }
    }

    private static void globDevices(List<String> into, String prefix) {
        Path parent = Path.of(prefix).getParent();
        String glob = Path.of(prefix).getFileName() + "*";
        if (parent == null || !Files.isDirectory(parent)) {
            return;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(parent, glob)) {
            for (Path entry : stream) {
                into.add(entry.toString());
            }
        } catch (IOException ignored) {
            // best-effort
        }
    }
}
