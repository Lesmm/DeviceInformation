package com.google.applicationjar.util;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IFileUtil {

    public static boolean writeTextToFile(String text, String fileName) {
        boolean result = false;
        FileWriter fileWriter = null;
        try {
            File fileText = new File(fileName);
            File fileParent = fileText.getParentFile();
            if (fileParent != null && !fileParent.exists()) {
                fileParent.mkdirs();
            }
            if (!fileText.exists()) {
                fileText.createNewFile();
            }
            fileWriter = new FileWriter(fileText, false);
            fileWriter.write(text);
            fileWriter.flush();
            fileWriter.close();
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    public static boolean appendBytesToFile(byte[] bytes, String fileName) {
        return writeBytesToFile(bytes, fileName, true);
    }

    public static boolean writeBytesToFile(byte[] bytes, String fileName) {
        return writeBytesToFile(bytes, fileName, false);
    }

    public static boolean writeBytesToFile(byte[] bytes, String fileName, boolean isAppend) {
        boolean result = false;
        OutputStream fileOutputStream = null;
        try {
            File file = new File(fileName);
            File fileParent = file.getParentFile();
            if (fileParent != null && !fileParent.exists()) {
                fileParent.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file, isAppend);
            fileOutputStream.write(bytes);
            fileOutputStream.flush();
            fileOutputStream.close();
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    public static void writeInputStreamToFile(InputStream inputStream, String fileName) {
        FileOutputStream outputStream = null;
        try {
            File file = new File(fileName);
            File fileParent = file.getParentFile();
            if (fileParent != null && !fileParent.exists()) {
                fileParent.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }

            outputStream = new FileOutputStream(file);
            int length = -1;
            byte[] buffer = new byte[512 * 1024];
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String readFileToText(String fileName) {
        if (fileName == null || !new File(fileName).exists()) {
            return null;
        }
        String result = null;
        try {

//			FileReader reader = new FileReader(new File(fileName));
//			CharArrayWriter writer = new CharArrayWriter();
//			char[] charBuffer = new char[1024];
//			while( reader.read(charBuffer) != -1) {
//				writer.write(charBuffer);
//			}
//			reader.close();
//
//			char[] charArray = writer.toCharArray();
//			writer.close();
//			String contents = writer.toString();
//			result = new String(charArray);

            FileInputStream fileInputStream = new FileInputStream(fileName);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int length = -1;
            // do not use new File(fileName).length to init a buffer. /proc/PID/status file length is 0, but it has contents
            byte[] buffer = new byte[1024 * 100];
            while ((length = dataInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            dataInputStream.close();

            byte[] bytes = outputStream.toByteArray();
            outputStream.close();

            // String contents = outputStream.toString();
            result = new String(bytes, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }



}
