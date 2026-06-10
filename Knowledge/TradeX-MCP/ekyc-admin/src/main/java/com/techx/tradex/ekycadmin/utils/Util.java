package com.techx.tradex.ekycadmin.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.common.exceptions.GeneralException;
import com.techx.tradex.common.utils.Pair;
import com.techx.tradex.ekycadmin.models.dto.EContractField;
import com.techx.tradex.ekycadmin.models.request.FptECExCallRequest;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.apache.commons.lang3.StringUtils;

public class Util {

    public static String toDateTimeFormat(LocalDateTime time) {
        return DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(time);
    }

    public static String toDateFormat(LocalDateTime time) {
        return DateTimeFormatter.ofPattern("yyyyMMdd").format(time);
    }

    public static Date addYear(Date time, Integer numberOfYear) {
        Calendar c = Calendar.getInstance();
        c.setTime(time);
        c.add(Calendar.YEAR, numberOfYear);
        return c.getTime();
    }

    public static PrivateKey getPrivateKey(String filename) {
        String key = null;
        try {
            key = new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String privateKeyPEM = key
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replaceAll(System.lineSeparator(), "")
            .replace("-----END PRIVATE KEY-----", "");
        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory kf = null;
        try {
            kf = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            return kf.generatePrivate(spec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decryptRSA(String encryptedMessageBase64, String path) {
        PrivateKey privateKeyFromString = getPrivateKey(path);
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKeyFromString);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        byte[] encryptedMessageBytes = Base64.getDecoder().decode(encryptedMessageBase64);
        byte[] decryptedMessageBytes = new byte[0];
        try {
            decryptedMessageBytes = cipher.doFinal(encryptedMessageBytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }

        return new String(decryptedMessageBytes);
    }

    public static File convertBase64ToPDF(String base64String, String fileName) {
        try {
            // Decode Base64 string to bytes
            byte[] decodedBytes = Base64.getDecoder().decode(base64String.getBytes());

            // Create a temporary file to hold the PDF
            File tempFile = File.createTempFile(fileName.split(".")[0], ".pdf");

            // Write the decoded bytes to the temporary PDF file
            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                outputStream.write(decodedBytes);
                return tempFile;
            } catch (IOException e) {
                throw new GeneralException("Cannot convert base64 to pdf");
            }
        } catch (IllegalArgumentException e) {
            throw new GeneralException("Cannot convert base64 to pdf");
        } catch (IOException e1) {
            throw new GeneralException("Cannot convert base64 to pdf");
        }
    }

    public static String getContractFileName(String requestData) {
        String fileName = "";
        ObjectMapper objectMapper = new ObjectMapper();
        FptECExCallRequest request;
        try {
            request = objectMapper.readValue(requestData, FptECExCallRequest.class);
        } catch (JsonProcessingException e) {
            throw new GeneralException("Cannot parse request data");
        }
        if (request.getBody() != null && request.getBody().getInputData() != null) {
            List<List<EContractField>> datas = request.getBody().getInputData().getDatas();
            if (datas != null && datas.size() > 0) {
                List<EContractField> data = datas.get(0);
                if (data != null && data.size() > 0) {
                    EContractField field = data.stream().filter(f -> f.getId().equals("envName")).findFirst().orElse(null);
                    if (field != null) {
                        fileName = field.getValue();
                    }
                }
            }
        }
        return fileName;
    }

    public static PublicKey getPublicKey(String filename) throws Exception {
        Path path = Paths.get(filename);
        String publicKeyPem = new String(Files.readAllBytes(path));
        publicKeyPem =
            publicKeyPem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PUBLIC KEY-----", "");
        byte[] byteKey = Base64.getDecoder().decode(publicKeyPem);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(new X509EncodedKeySpec(byteKey));
    }

    public static Boolean verifySignature(PublicKey publicKey, byte[] data, byte[] signature, String algorithm) throws Exception {
        Signature sig = Signature.getInstance(algorithm);
        sig.initVerify(publicKey);
        sig.update(data);
        return sig.verify(signature);
    }

    public static byte[] base64Decode(String base64String) {
        return Base64.getDecoder().decode(base64String);
    }

    public static Date toDate(String dateStr, String pattern) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.parse(dateStr);
    }

    public static String formatDate(Date date, String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(date);
    }

    public static long getBufferSize(BufferedImage img) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        baos.close();
        return baos.size();
    }

    public static Pair<byte[], String> compressAndConvertImageJPG(String path, Integer width, Integer height, Float quality, Long maxSize) throws Exception {
        URL url = new URL(path);
        BufferedImage img = readImageSynchronized(url);
        Dimension origin = new Dimension(img.getWidth(), img.getHeight());
        String fileName = url.getFile().substring(url.getFile().lastIndexOf("/") + 1);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersBySuffix("jpg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();

        Image resultingImage = img.getScaledInstance(origin.width, origin.height, Image.SCALE_DEFAULT);
        BufferedImage outputImage = new BufferedImage(origin.width, origin.height, BufferedImage.TYPE_INT_RGB);
        long fileSize = getBufferSize(img);
        if (fileSize > maxSize) {
            Dimension bond = new Dimension(width, height);
            Dimension newD = getScaledDimension(origin, bond);
            resultingImage = img.getScaledInstance(newD.width, newD.height, Image.SCALE_DEFAULT);
            outputImage = new BufferedImage(newD.width, newD.height, BufferedImage.TYPE_INT_RGB);
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
        }
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        IIOImage image = new IIOImage(outputImage, new ArrayList<>(), null);
        writer.setOutput(ImageIO.createImageOutputStream(bos));
        writer.write(null, image, param);
        bos.flush();
        writer.dispose();
        return new Pair<>(bos.toByteArray(), fileName);
    }

    public static synchronized BufferedImage readImageSynchronized(URL url) throws IOException {
        return ImageIO.read(url);
    }

    public static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {
        int original_width = imgSize.width;
        int original_height = imgSize.height;
        int bound_width = boundary.width;
        int bound_height = boundary.height;
        int new_width = original_width;
        int new_height = original_height;

        // first check if we need to scale width
        if (original_width > bound_width) {
            // scale width to fit
            new_width = bound_width;
            // scale height to maintain aspect ratio
            new_height = (new_width * original_height) / original_width;
        }
        // then check if we need to scale even with the new height
        if (new_height > bound_height) {
            // scale height to fit instead
            new_height = bound_height;
            // scale width to maintain aspect ratio
            new_width = (new_height * original_width) / original_height;
        }
        return new Dimension(new_width, new_height);
    }

    public static byte[] convertBase64toJPG(String png) throws IOException {
        byte[] imageBytes = Base64.getDecoder().decode(png);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
        BufferedImage i = ImageIO.read(bis);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedImage convertedImage = new BufferedImage(i.getWidth(), i.getHeight(), BufferedImage.TYPE_INT_RGB);
        convertedImage.createGraphics().drawImage(i, 0, 0, Color.WHITE, null);
        ImageWriter writer = ImageIO.getImageWritersBySuffix("jpg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        writer.setOutput(ImageIO.createImageOutputStream(bos));
        IIOImage image = new IIOImage(convertedImage, new ArrayList<>(), null);
        writer.write(null, image, param);
        byte[] jpgBytes = bos.toByteArray();
        bos.flush();
        return jpgBytes;
    }

    public static String strDateToStringDateFormat(String dateStr, String sourcePattern, String desPattern) throws Exception {
        if (StringUtils.isBlank(dateStr)) {
            return StringUtils.EMPTY;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(sourcePattern);
        Date date = formatter.parse(dateStr);
        DateFormat dateFormat = new SimpleDateFormat(desPattern);
        return dateFormat.format(date);
    }

    public static String getKeyForStorageServiceForFrontIDCardImage(String identifierId, String suffix) {
       return identifierId + suffix;
    }

    public static String getKeyForStorageServiceForBackIDCardImage(String identifierId, String suffix) {
        return identifierId + suffix;
    }
}
