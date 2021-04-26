package org.keyko.vietkongquiz.service;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.FileUtils;
import org.keyko.vietkongquiz.Quiz;
import org.keyko.vietkongquiz.dto.QuizTableImage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.*;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Slf4j
@Service
public class OCRService {
    public static final String CHAR_WHITE_LIST =
            "#№ \"~:_,+-!?0123456789" +
                    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz" +
                    "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";
    public static final int RESIZE_COEFFICIENT = 2;

    @Value("${tesseract.datadir:/usr/local/tessdata}")
    private String tessdataDir;

    @Value("${tmp.images.dir:/tmp/quiz/}")
    private String tmpImagesDir;

    private final Tesseract tesseractInstance;

    public OCRService() {
        this.tesseractInstance = new Tesseract();  // JNA Interface Mapping
        tesseractInstance.setConfigs(Collections.singletonList("bazaar"));
        tesseractInstance.setDatapath(tessdataDir); // path to tessdata directory
        tesseractInstance.setLanguage("einstein");
        tesseractInstance.setTessVariable("tessedit_char_whitelist", CHAR_WHITE_LIST);
        tesseractInstance.setPageSegMode(13);
    }

    public String doOcr(QuizTableImage image, Quiz quiz) {
        try {
            File convertedFile = convertImageFile(image);
            Rectangle rectangle = new Rectangle(
                    quiz.getLeftBlockSize() * RESIZE_COEFFICIENT,
                    quiz.getHeaderBlockSize() * RESIZE_COEFFICIENT,
                    image.getWidth() * RESIZE_COEFFICIENT - quiz.getLeftBlockSize() * RESIZE_COEFFICIENT,
                    image.getHeight() * RESIZE_COEFFICIENT - quiz.getHeaderBlockSize() * RESIZE_COEFFICIENT);
            String tableString = tesseractInstance.doOCR(convertedFile, rectangle);

            /*if (!FileUtils.deleteQuietly(convertedFile)) {
                log.warn("Temporary file <{}> was not deleted.", convertedFile.getAbsolutePath());
            }*/

            log.info("File <{}> converted to string.", image.getFile().getName());

            return tableString;
        } catch (TesseractException e) {
            log.error("Something goes wrong while OCR!", e);
            return "";
        } catch (IOException | InterruptedException e) {
            log.error("Something goes wrong while converting file!", e);
            return "";
        }
    }

    private File convertImageFile(QuizTableImage image) throws IOException, InterruptedException {
        String filePath = image.getFile().getAbsolutePath();
        String fileName = image.getFile().getName();
        String convertedFilePath = tmpImagesDir +
                (tmpImagesDir.endsWith("/") ? "" : "/") +
                fileName;

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(
                "/usr/bin/convert",
                filePath,
                "-colorspace",
                "gray",
                "-separate",
                "-average",
                "-density",
                "300",
                "-resize",
                image.getWidth() * RESIZE_COEFFICIENT + "x" + image.getHeight() * RESIZE_COEFFICIENT,
                "-sharpen",
                "0x5",
                "-negate",
                "-define", "morphology:compose=darken",
                "-morphology", "Thinning", "Rectangle:1x30+0+0<",
                "-negate",
                convertedFilePath
        );
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        StreamGobbler streamGobbler =
                new StreamGobbler(process.getInputStream(), log::info);
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();
        assert exitCode == 0;

        return FileUtils.getFile(convertedFilePath);
    }

    private static class StreamGobbler implements Runnable {
        private final InputStream inputStream;
        private final Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }
    }
}
