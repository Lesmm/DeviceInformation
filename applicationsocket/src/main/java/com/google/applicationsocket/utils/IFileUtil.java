package com.google.applicationsocket.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;

public class IFileUtil {
	
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
			Method md = clsFileUtils.getDeclaredMethod("setPermissions", new Class[] { String.class, int.class, int.class, int.class });
			md.setAccessible(true);
			md.invoke(clsFileUtils, new Object[] { fileName, perms, uid, gid });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String readFileToText(String fileName) {
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
			while(( length = dataInputStream.read(buffer)) != -1) {
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
	
	
	public static byte[] readFileToByte(String fileName) {
		if (!new File(fileName).exists()) {
			return null;
		}
		
		byte[] result = null;
		FileInputStream fileInputStream = null;
		ByteArrayOutputStream byteArrayOutputStream = null;
		
		try {
			fileInputStream = new FileInputStream(fileName);
			byteArrayOutputStream = new ByteArrayOutputStream();
			
			// read
			// do not use new File(fileName).length to init a buffer. /proc/PID/status file length is 0, but it has contents
			int length = -1;
			byte[] buffer = new byte[1024 * 128];
			while ((length = fileInputStream.read(buffer)) != -1) {
				byteArrayOutputStream.write(buffer, 0, length);
			}
			result = byteArrayOutputStream.toByteArray();

			// release
			fileInputStream.close();
			fileInputStream = null;
			byteArrayOutputStream.close();
			byteArrayOutputStream = null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileInputStream != null) {
					fileInputStream.close();
				}
				if (byteArrayOutputStream != null) {
					byteArrayOutputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return result;
	}
	
	public static String getFileContents(String fileName) {
		if (!new File(fileName).exists()) {
			return null;
		}
		
		String result = null;
		DataInputStream dataInputStream = null;
		ByteArrayOutputStream outputStream = null;
		try {
			dataInputStream = new DataInputStream(new FileInputStream(fileName));
			outputStream = new ByteArrayOutputStream();
			
			// read
			// do not use new File(fileName).length to init a buffer. /proc/PID/status file length is 0, but it has contents
			int length = -1;
			byte[] buffer = new byte[1024 * 100];
			while ((length = dataInputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, length);
			}
			byte[] bytes = outputStream.toByteArray();
			
			// release
			dataInputStream.close();
			dataInputStream = null;
			outputStream.close();
			outputStream = null;
			
			result = new String(bytes, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (dataInputStream != null) {
					dataInputStream.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return result;
	}
	

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
	
	public static boolean writeBytesToFile(byte[] bytes, String fileName) {
		boolean result = false;
		OutputStream fileOutputStream = null;
		try {
			File fileText = new File(fileName);
			File fileParent = fileText.getParentFile();
			if (fileParent != null && !fileParent.exists()) {
				fileParent.mkdirs();
			}
			if (!fileText.exists()) {
				fileText.createNewFile();
			}
			fileOutputStream = new FileOutputStream(fileText, false);
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

	public static boolean appendTextToFile(String text, String fileName) {
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
			fileWriter = new FileWriter(fileText, true);
			fileWriter.write(text);
			fileWriter.flush();
			fileWriter.close();
			return true;
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
		return false;
	}

	public static void devideFile(File f) {
		BufferedInputStream bufferedInputStream = null;
		try {
			int sizeOfFiles = (int) (f.length() / 1.8);
			byte[] buffer = new byte[sizeOfFiles];
			String fileName = f.getName();

			// try-with-resources to ensure closing stream
			int i = 0;
			bufferedInputStream = new BufferedInputStream(new FileInputStream(f));

			int bytesAmount = 0;
			while ((bytesAmount = bufferedInputStream.read(buffer)) > 0) {
				i++;
				// write each chunk of data into separate file with different number in name
				if (i == 2) {
					File newFile = new File(f.getParent(), fileName);
					FileOutputStream out = new FileOutputStream(newFile);
					out.write(buffer, 0, bytesAmount);
					out.flush();
					out.close();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bufferedInputStream != null) {
				try {
					bufferedInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	
	public static void splitFile(File file) {
		try {
			long fileSize = file.length();
			String tempFileName = file.getAbsolutePath() + ".half";
			
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
			randomAccessFile.seek(fileSize / 2);
			
			FileWriter writer = new FileWriter(tempFileName);
			
			byte[] buffer = new byte[1024 * 1024];
			int readLength = 0;
            while ((readLength = randomAccessFile.read(buffer)) > 0) {
            	String content = new String(buffer, 0, readLength);
            	writer.write(content);
            }
            writer.flush();
            writer.close();
			
            randomAccessFile.close();
            
            file.delete();
            new File(tempFileName).renameTo(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void deleteFileOrDirectory(String fileOrDirectoryPath) {
		File file = new File(fileOrDirectoryPath);
		
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File subFile : files) {
					deleteFileOrDirectory(subFile.getPath());
				}
			}
		}
		
		boolean result = file.delete();
	}
	
	public static boolean clearDirectory(String fileOrDirectoryPath) {
		File file = new File(fileOrDirectoryPath);
		
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File subFile : files) {
					deleteFileOrDirectory(subFile.getPath());
				}
			}
			
			return file.list().length == 0;
		}
		
		return true;	// not directory, true now ...
	}
	
	public static boolean copyIf(String srcFilePath, String desFilePath) {
		copy(srcFilePath, desFilePath);
		return new File(desFilePath).exists();
	}

	public static void copy(String srcFilePath, String desFilePath) {
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;

		FileInputStream fileInputStream = null;
		FileOutputStream fileOutputStream = null;

		try {
			
			File desFile = new File(desFilePath);

			if (!desFile.exists()) {
				File parentFile = desFile.getParentFile();
				if (parentFile != null && !parentFile.exists()) {
					parentFile.mkdirs();
				}
				try {
					desFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			fileInputStream = new FileInputStream(srcFilePath);
			inputChannel = fileInputStream.getChannel();
			fileOutputStream = new FileOutputStream(desFilePath);
			outputChannel = fileOutputStream.getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				
				if (inputChannel != null) {
					inputChannel.close();
				}
				if (outputChannel != null) {
					outputChannel.close();
				}
				if (fileInputStream != null) {
					fileInputStream.close();

				}
				if (fileOutputStream != null) {
					fileOutputStream.close();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
