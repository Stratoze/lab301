package com.laptrinhjavaweb.util;

import com.laptrinhjavaweb.constant.SystemConstant;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.Base64;

public class FileUploadUtil {
    private static FileUploadUtil fileUploadUtil;

    private FileUploadUtil() {
    }

    public static FileUploadUtil getInstance() {
        if (fileUploadUtil == null) {
            fileUploadUtil = new FileUploadUtil();
        }
        return fileUploadUtil;
    }

    public String writeBase64(String base64, String parentFolder, String savedName, String extension) throws IOException {
//        Convert to ascii string
        savedName = Normalizer.normalize(savedName, Normalizer.Form.NFKD);
        String regex = "[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+";
        savedName = new String(savedName.replaceAll(regex, "").getBytes("ascii"), "ascii");

//        remove all special characters and whitespace
        savedName = savedName.replaceAll("([^A-Za-z0-9]|\\s)", "");
        savedName = savedName.concat(extension);

        String rawData = base64.split(",")[1];
        byte[] decodedImg = Base64.getDecoder().decode(rawData);
        Path destinationFolder = Paths.get(SystemConstant.BASE_UPLOAD_PATH, parentFolder);
        createFolderIfNotExisted(destinationFolder);

        Path destinationFile = Paths.get(destinationFolder.toString(), savedName);
        Files.write(destinationFile, decodedImg);

        return Paths.get(parentFolder, savedName).toString();
    }

    private void createFolderIfNotExisted(Path path) {
        File folder = path.toFile();
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }
}