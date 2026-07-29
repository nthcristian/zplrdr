package io.nthcristian.prt;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.Size2DSyntax;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.printing.Orientation;
import org.apache.pdfbox.printing.PDFPageable;

/**
 * Layout tuned for direct-thermal label printers (e.g. Tomate MDK-006 at 203 DPI).
 * Uses the PDF page MediaBox as the physical label size with a full-bleed printable
 * area so content is not clipped or offset by Letter/A4 margins.
 */
final class LabelPrintLayout {

    /** Native resolution of the Tomate MDK-006 and Labelary {@code 8dpmm}. */
    static final float PRINTER_DPI = 203f;

    private LabelPrintLayout() {
    }

    static Pageable createPageable(PDDocument document) {
        // ACTUAL_SIZE via PDFPageable, top-left aligned (center=false), no border,
        // rasterized at printer DPI for crisp barcodes on thermal heads.
        return new PDFPageable(document, Orientation.AUTO, false, PRINTER_DPI, false);
    }

    static PrintRequestAttributeSet createAttributes(PDDocument document) {
        PageSizeInches size = pageSizeInches(document.getPage(0));

        HashPrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
        attrs.add(new MediaPrintableArea(0, 0, size.widthInches(), size.heightInches(),
                MediaPrintableArea.INCH));
        attrs.add(size.widthInches() <= size.heightInches()
                ? OrientationRequested.PORTRAIT
                : OrientationRequested.LANDSCAPE);
        attrs.add(Chromaticity.MONOCHROME);

        MediaSizeName mediaName = MediaSize.findMedia(size.widthInches(), size.heightInches(),
                Size2DSyntax.INCH);
        if (mediaName != null) {
            attrs.add(mediaName);
        }

        return attrs;
    }

    static PageSizeInches pageSizeInches(PDPage page) {
        PDRectangle mediaBox = page.getMediaBox();
        float widthPt = mediaBox.getWidth();
        float heightPt = mediaBox.getHeight();
        int rotation = page.getRotation();
        if (rotation == 90 || rotation == 270) {
            float swap = widthPt;
            widthPt = heightPt;
            heightPt = swap;
        }
        return new PageSizeInches(widthPt / 72f, heightPt / 72f);
    }

    static boolean isFullBleed(PageFormat pageFormat) {
        return pageFormat.getImageableX() == 0
                && pageFormat.getImageableY() == 0
                && Math.abs(pageFormat.getImageableWidth() - pageFormat.getWidth()) < 0.01
                && Math.abs(pageFormat.getImageableHeight() - pageFormat.getHeight()) < 0.01;
    }

    record PageSizeInches(float widthInches, float heightInches) {
    }

}
