package com.portfolio.jobtracker.service;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PdfService {
    public String extractTextFromPdf(MultipartFile file){
        try{
            Tika tika = new Tika();
            //tika lit le contenu du fichier PDF et le convertit en texte brut
            return tika.parseToString(file.getInputStream());
        } catch (Exception e){
            throw new RuntimeException("Failed to extract text from PDF"+ e.getMessage());
        }
    }
}
