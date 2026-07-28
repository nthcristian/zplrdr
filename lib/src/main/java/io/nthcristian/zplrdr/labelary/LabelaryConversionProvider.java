package io.nthcristian.zplrdr.labelary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;

import io.nthcristian.zplrdr.contract.ConversionProvider;
import io.nthcristian.zplrdr.error.ConversionProviderException;
import io.nthcristian.zplrdr.preset.util.Preset;
import jakarta.validation.constraints.NotNull;
import io.nthcristian.zplrdr.document.PdfDocument;
import io.nthcristian.zplrdr.document.ZplDocument;
import io.nthcristian.zplrdr.labelary.document.ZplLabel;
import io.nthcristian.zplrdr.labelary.util.LabelaryClientConfig;

public class LabelaryConversionProvider implements ConversionProvider {
    private static final int BATCH_SIZE = 50;
    private final LabelaryClientConfig config;

    public LabelaryConversionProvider() {
        this.config = new LabelaryClientConfig(
                "http://api.labelary.com/v1/printers/{dpmm}/labels/{width}x{height}/", null);
    }

    public LabelaryConversionProvider(@NotNull LabelaryClientConfig config) {
        this.config = config;
    }

    @Override
    public PdfDocument[] convert(ZplDocument[] zplFiles, Preset preset) throws ConversionProviderException {
        String dpmm = preset.getProperty("dpmm");
        String width = preset.getProperty("width");
        String height = preset.getProperty("height");

        String url = config.baseUrl()
                .replace("{dpmm}", dpmm)
                .replace("{width}", width)
                .replace("{height}", height);

        List<PdfDocument> results = new ArrayList<>();
        for (ZplDocument zplFile : zplFiles) {
            ZplLabel[][] batches = splitIntoBatches(zplFile);
            for (ZplLabel[] batch : batches) {
                results.add(sendBatch(url, batch));
            }
        }

        return results.toArray(new PdfDocument[0]);
    }

    private ZplLabel[][] splitIntoBatches(ZplDocument zplFile) {
        var encoded = new String(zplFile.data(), StandardCharsets.UTF_8);

        Queue<String> splitted = new LinkedList<>();
        var rawSplitted = encoded.split("\\^XZ");
        for (String split : rawSplitted) {
            if (split.isBlank()) {
                continue;
            }
            splitted.add(split.concat("^XZ"));
        }

        var batches = new Vector<List<ZplLabel>>();

        var currentBatchSize = 0;
        var batch = new Vector<ZplLabel>();
        do {
            var polled = splitted.poll();
            if (polled == null) {
                if (!batch.isEmpty()) {
                    batches.add(batch);
                }
                break;
            }

            if (currentBatchSize == BATCH_SIZE) {
                batches.add(batch);
                batch = new Vector<ZplLabel>();
                currentBatchSize = 0;
            }

            batch.add(new ZplLabel(polled.getBytes(StandardCharsets.UTF_8)));
            currentBatchSize++;
        } while (true);

        ZplLabel[][] result = new ZplLabel[batches.size()][];
        for (int i = 0; i < batches.size(); i++) {
            List<ZplLabel> batchList = batches.get(i);
            result[i] = batchList.toArray(new ZplLabel[0]);
        }

        return result;
    }

    private PdfDocument sendBatch(String url, ZplLabel[] zplBatch) throws ConversionProviderException {
        try {
            var apiUrl = URI.create(url).toURL();
            var connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/pdf");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            ByteArrayOutputStream bodyStream = new ByteArrayOutputStream();
            for (ZplLabel label : zplBatch) {
                bodyStream.write(label.data());
            }

            try (OutputStream os = connection.getOutputStream()) {
                os.write(bodyStream.toByteArray());
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new ConversionProviderException("Labelary API returned HTTP " + responseCode);
            }

            byte[] pdfData;
            try (InputStream is = connection.getInputStream()) {
                pdfData = is.readAllBytes();
            }

            return new PdfDocument(pdfData);
        } catch (IOException e) {
            throw new ConversionProviderException("Failed to send batch to Labelary", e);
        }
    }
}