package com.google.applicationjar.util;

import android.os.Build;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class IFileUtilEx {

    public static final int S_IRWXU = 00700;
    public static final int S_IRUSR = 00400;
    public static final int S_IWUSR = 00200;
    public static final int S_IXUSR = 00100;

    public static final int S_IRWXG = 00070;
    public static final int S_IRGRP = 00040;
    public static final int S_IWGRP = 00020;
    public static final int S_IXGRP = 00010;

    public static final int S_IRWXO = 00007;
    public static final int S_IROTH = 00004;
    public static final int S_IWOTH = 00002;
    public static final int S_IXOTH = 00001;

    public static final int SPERMS777 = S_IRUSR | S_IWUSR | S_IXUSR | S_IRGRP | S_IWGRP | S_IXGRP | S_IROTH | S_IWOTH | S_IXOTH;

    public static void chmod777(String fileName) {
        chmod(fileName, SPERMS777, -1, -1);
    }

    public static void chmod(String fileName, int perms, int uid, int gid) {
        try {
            Class<?> clsFileUtils = String.class.getClassLoader().loadClass("android.os.FileUtils");
            Method md = clsFileUtils.getDeclaredMethod("setPermissions", new Class[]{String.class, int.class, int.class, int.class});
            md.setAccessible(true);
            md.invoke(clsFileUtils, new Object[]{fileName, perms, uid, gid});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void create777Directories(String directory) {
        create777Directories(new File(directory));
    }

    public static void create777Directories(File directoryFile) {
        File parentDirectory = directoryFile.getParentFile();
        if (parentDirectory != null && !parentDirectory.exists()) {
            create777Directories(parentDirectory);
        }
        if (!directoryFile.exists()) {
            boolean isCreateSuccess = directoryFile.mkdir();
            IFileUtilEx.chmod777(directoryFile.getAbsolutePath());
        }
    }

    public static void write777File(byte[] contents, String fileName) {
        IFileUtil.writeBytesToFile(contents, fileName);
        IFileUtilEx.chmod777(fileName);
    }

    public static void write777File(String contents, String fileName) {
        IFileUtil.writeTextToFile(contents, fileName);
        IFileUtilEx.chmod777(fileName);
    }

    public static void append777File(String contents, String fileName) {
        IFileUtil.writeBytesToFile(contents.getBytes(), fileName, true);
        IFileUtilEx.chmod777(fileName);
    }

    public static long getFileCreateTime(String fileName) {
        long millis = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Path path = new File(fileName).toPath();
                BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
                millis = attr.creationTime().toMillis();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (millis == 0) {
            millis = new File(fileName).lastModified();
        }
        return millis;
    }

    public static long getFileModifiedTime(String fileName) {
        long millis = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Path path = new File(fileName).toPath();
                BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
                millis = attr.lastModifiedTime().toMillis();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (millis == 0) {
            millis = new File(fileName).lastModified();
        }
        return millis;
    }

}
