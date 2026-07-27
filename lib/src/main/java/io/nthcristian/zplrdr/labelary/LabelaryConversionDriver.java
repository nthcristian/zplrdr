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

import io.nthcristian.zplrdr.contract.ConversionDriver;
import io.nthcristian.zplrdr.error.ConversionException;
import io.nthcristian.zplrdr.preset.util.Preset;
import io.nthcristian.zplrdr.document.PDFDocument;
import io.nthcristian.zplrdr.document.ZPLDocument;
import io.nthcristian.zplrdr.labelary.document.ZPLLabel;
import io.nthcristian.zplrdr.labelary.util.LabelaryConfig;

public class LabelaryConversionDriver implements ConversionDriver {
    private static final int BATCH_SIZE = 50;
    private static final LabelaryConfig config = new LabelaryConfig(
            "http://api.labelary.com/v1/printers/{dpmm}/labels/{width}x{height}/", null);

    @Override
    public PDFDocument[] requestConversion(ZPLDocument[] zplFiles, Preset preset) throws ConversionException {
        String dpmm = preset.getFieldValue("dpmm");
        String width = preset.getFieldValue("width");
        String height = preset.getFieldValue("height");

        String url = config.baseUrl()
                .replace("{dpmm}", dpmm)
                .replace("{width}", width)
                .replace("{height}", height);

        List<PDFDocument> results = new ArrayList<>();
        for (ZPLDocument zplFile : zplFiles) {
            ZPLLabel[][] batches = splitIntoBatches(zplFile);
            for (ZPLLabel[] batch : batches) {
                results.add(sendBatch(url, batch));
            }
        }

        return results.toArray(new PDFDocument[0]);
    }

    private ZPLLabel[][] splitIntoBatches(ZPLDocument zplFile) {
        var encoded = new String(zplFile.data(), StandardCharsets.UTF_8);

        Queue<String> splitted = new LinkedList<>();
        var rawSplitted = encoded.split("\\^XZ");
        for (String split : rawSplitted) {
            if (split.isBlank()) {
                continue;
            }
            splitted.add(split.concat("^XZ"));
        }

        var batches = new Vector<List<ZPLLabel>>();

        var currentBatchSize = 0;
        var batch = new Vector<ZPLLabel>();
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
                batch = new Vector<ZPLLabel>();
                currentBatchSize = 0;
            }

            batch.add(new ZPLLabel(polled.getBytes(StandardCharsets.UTF_8)));
            currentBatchSize++;
        } while (true);

        ZPLLabel[][] result = new ZPLLabel[batches.size()][];
        for (int i = 0; i < batches.size(); i++) {
            List<ZPLLabel> batchList = batches.get(i);
            result[i] = batchList.toArray(new ZPLLabel[0]);
        }

        return result;
    }

    private PDFDocument sendBatch(String url, ZPLLabel[] zplBatch) throws ConversionException {
        try {
            var apiUrl = URI.create(url).toURL();
            var connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/pdf");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            ByteArrayOutputStream bodyStream = new ByteArrayOutputStream();
            for (ZPLLabel label : zplBatch) {
                bodyStream.write(label.data());
            }

            try (OutputStream os = connection.getOutputStream()) {
                os.write(bodyStream.toByteArray());
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new ConversionException("Labelary API returned HTTP " + responseCode);
            }

            byte[] pdfData;
            try (InputStream is = connection.getInputStream()) {
                pdfData = is.readAllBytes();
            }

            return new PDFDocument(pdfData);
        } catch (IOException e) {
            throw new ConversionException("Failed to send batch to Labelary", e);
        }
    }
}