package com.daralshifa.upload.service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.AffineTransform;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.layout.element.Image;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class PdfImageMergerService {
    public void manipulatePdf(String src, String dest,String imgSrc,int pageNumber) throws IOException{
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(src), new PdfWriter(dest));
        ImageData image = ImageDataFactory.create(imgSrc);
        Image imageModel = new Image(image);
        AffineTransform at = AffineTransform.getTranslateInstance(36, 300);
        at.concatenate(AffineTransform.getScaleInstance(imageModel.getImageScaledWidth(),
                imageModel.getImageScaledHeight()));
        PdfCanvas canvas = new PdfCanvas(pdfDoc.getPage(pageNumber));
        float[] matrix = new float[6];
        at.getMatrix(matrix);
        canvas.addImage(image, matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5]);
        pdfDoc.close();
    }
}
