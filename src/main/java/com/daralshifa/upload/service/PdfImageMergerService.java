package com.daralshifa.upload.service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.AffineTransform;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.layout.element.Image;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PdfImageMergerService {
    public void signPdf(String src, String dest, List<String> signatures) throws Exception {
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(src), new PdfWriter(dest));
        for(String signature :signatures){
            final int pageNumber = getPageNumber(signature);
            signPage(pdfDoc,signature,pageNumber);
        }
        pdfDoc.close();
    }

    private int getPageNumber(String signature) {
        if(signature!=null) {
            final String prefix = signature.substring(0,signature.indexOf("_"));
           // System.out.println(prefix);
            String filterd = signature.replace(prefix,"").replace("_","").replace(".png", "").replace("Signature_", "");
            return Integer.parseInt(filterd);
        }
        return 0;
    }

    private void signPage(PdfDocument pdfDoc,String imgSrc,int pageNumber) throws IOException{
        System.out.println("imgSrc = "+ imgSrc);
        ImageData image = ImageDataFactory.create(imgSrc);
        Image imageModel = new Image(image);
        final PdfPage page = pdfDoc.getPage(pageNumber + 1);
       final float width = page.getPageSize().getWidth();
       final float height = page.getPageSize().getHeight();
        AffineTransform at = AffineTransform.getTranslateInstance(width/2, 0f);
        at.concatenate(AffineTransform.getScaleInstance(40f,40f));
        PdfCanvas canvas = new PdfCanvas(page);
        float[] matrix = new float[6];
        at.getMatrix(matrix);
        canvas.addImage(image, matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5]);
    }

    public static void main(String[] args) {
        final PdfImageMergerService service = new PdfImageMergerService();
        List<String> list = List.of(
                "/Users/sonbati/Desktop/dash/med-sig/signatures/Signature_0.png",
                "/Users/sonbati/Desktop/dash/med-sig/signatures/Signature_1.png",
                "/Users/sonbati/Desktop/dash/med-sig/signatures/Signature_2.png",
                "/Users/sonbati/Desktop/dash/med-sig/signatures/Signature_3.png"
        );
        try {
            service.signPdf("/Users/sonbati/Desktop/dash/med-sig/invoice/inv.pdf",
                    "/Users/sonbati/Desktop/dash/med-sig/signed/signed.pdf",list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
