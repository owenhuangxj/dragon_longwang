/**
 * Copyright shopez.cn.
 * @author:xiongbo
 */
package com.trenska.longwang.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
public final class FileUtil {


    private static FileUtil instance = new FileUtil();

    private static String servletClassesPath = null;

    private FileUtil() { // prevent instantiation
    }

    public static void checkGoodFilePath(String str) throws Exception {
        byte[] s = str.getBytes();
        int length = s.length;
        byte b = 0;

        for (int i = 0; i < length; i++) {
            b = s[i];
            if ((b == '*') ||
                (b == '?') ||
                (b == '<') ||
                (b == '>') ||
                (b == '"') ||
                (b == '|') ||
                (b == '\0')) {//null char : is it correct ????
                // not good char, throw an Exception
                //@todo : localize me
                throw new Exception("文件名不合法！");
            }
        }// for
    }

    public static void checkGoodFileName(String str) throws Exception {
        // must be a good file path first
        checkGoodFilePath(str);
        byte[] s = str.getBytes();
        int length = s.length;
        byte b = 0;

        for (int i = 0; i < length; i++) {
            b = s[i];
            if ((b == '/') ||
                (b == '\\') ||
                (b == ':')) {
                // not good char, throw an Exception
                //@todo : localize me
                throw new Exception("文件名不合法！");
            }
        }// for
    }

    public static void createDir(String dir, boolean ignoreIfExitst) throws IOException {
        File file = new File(dir);

        if (ignoreIfExitst && file.exists()) {
            return;
        }

        if ( file.mkdir() == false) {
            throw new IOException("Cannot create the directory = " + dir);
        }
    }

    public static void createDirs(String dir, boolean ignoreIfExitst) throws IOException {
        File file = new File(dir);

        if (ignoreIfExitst && file.exists()) {
            return;
        }

        if ( file.mkdirs() == false) {
            throw new IOException("Cannot create directories = " + dir);
        }
    }

    
    /** fn-hd
     * rem: 删除指定文件
     * par: 
     * ret: 
     * log: 2007-10-30 下午06:45:55 创建
     * aut: 熊波
     */
    public static void deleteFile(String filename) throws IOException {
        File file = new File(filename);
        log.debug("Delete file = " + filename);
        if (file.isDirectory()) {
            throw new IOException("IOException -> Exception: not a file.");
        }
        if (file.exists() == false) {
            throw new IOException("IOException -> Exception: file is not exist.");
        }
        if (file.delete() == false) {
            throw new IOException("Cannot delete file. filename = " + filename);
        }
    }

    
    /** fn-hd
     * rem: 删除指定文件夹
     * par: 
     * ret: 
     * log: 2007-10-30 下午06:44:46 创建
     * aut: 熊波
     */
    public static void deleteDir(File dir) throws IOException {
        if (dir.isFile()) throw new IOException("IOException -> Exception: not a directory.");
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isFile()) {
                    file.delete();
                } else {
                    deleteDir(file);
                }
            }
        }//if
        dir.delete();
    }
    
    public static void deleteDir(String dirPath) throws IOException{
        File dir = new File(dirPath);
        if (dir.isFile()) throw new IOException("IOException -> Exception: not a directory.");
        deleteDir(dir);
    }

    
    /** fn-hd
     * rem: 获取目录大小
     * par: 
     * ret: 
     * log: 2007-10-30 下午06:44:27 创建
     * aut: 熊波
     */
    public static long getDirLength(File dir) throws IOException {
        if (dir.isFile()) throw new IOException("Exception: not a directory.");
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                long length = 0;
                if (file.isFile()) {
                    length = file.length();
                } else {
                    length = getDirLength(file);
                }
                size += length;
            }//for
        }//if
        return size;
    }

    
    /** fn-hd
     * rem: 获取目录占空间大小
     * par: 
     * ret: 
     * log: 2007-10-30 下午06:44:14 创建
     * aut: 熊波
     */
    public static long getDirLength_onDisk(File dir) throws IOException {
        if (dir.isFile()) throw new IOException("Exception: not a directory.");
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                long length = 0;
                if (file.isFile()) {
                    length = file.length();
                } else {
                    length = getDirLength_onDisk(file);
                }
                double mod = Math.ceil(((double)length)/512);
                if (mod == 0) mod = 1;
                length = ((long)mod) * 512;
                size += length;
            }
        }//if
        return size;
    }

    
    /** fn-hd
     * rem: 创建空文件
     * par: 
     * ret: 
     * log: 2007-10-30 下午06:44:03 创建
     * aut: 熊波
     */
    public static void emptyFile(String srcFilename) throws IOException {
        File srcFile = new File(srcFilename);
        if (!srcFile.exists()) {
            throw new FileNotFoundException("Cannot find the file: " + srcFile.getAbsolutePath());
        }
        if (!srcFile.canWrite()) {
            throw new IOException("Cannot write the file: " + srcFile.getAbsolutePath());
        }

        FileOutputStream outputStream = new FileOutputStream(srcFilename);
        outputStream.close();
    }
    

    
    /** fn-hd
     * rem: 判断某个文件或者文件夹是否存在
     * par: 
     * ret: 
     * log: 2008-3-29 下午07:38:06 创建
     * aut: 熊波
     */
    public static boolean checkIsExit(String srcFilename) throws IOException{
        File srcFile = new File(srcFilename);
        return srcFile.exists();
    }
    
    /** fn-hd
     * rem: 复制文件
     * par: 
     * ret: 
     * log: 2007-10-30 下午06:43:54 创建
     * aut: 熊波
     */
    public static void copyFile(String srcFilename, String destFilename) throws IOException {

        File srcFile = new File(srcFilename);
        if (!srcFile.exists()) {
            throw new FileNotFoundException("Cannot find the source file: " + srcFile.getAbsolutePath());
        }
        if (!srcFile.canRead()) {
            throw new IOException("Cannot read the source file: " + srcFile.getAbsolutePath());
        }
        
        int end =  destFilename.lastIndexOf(File.separator);
        if(end != -1){
            String dirs = destFilename.substring(0, end);
            createDirs(dirs,false);
        }
        


        File destFile = new File(destFilename);
      
        BufferedInputStream inputStream = null;
        BufferedOutputStream outputStream = null;
        byte[] block = new byte[1024];
        try {
            inputStream = new BufferedInputStream(new FileInputStream(srcFile));
            outputStream = new BufferedOutputStream(new FileOutputStream(destFile));
            while (true) {
                int readLength = inputStream.read(block);
                if (readLength == -1) break;// end of file
                outputStream.write(block, 0, readLength);
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    // just ignore
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    // just ignore
                }
            }
        }
    }
    
    /** fn-hd
     * rem: 复制文件
     * par: 
     * ret: 
     * log: 2007-10-30 下午06:43:54 创建
     * aut: 熊波
     */
    public static void copyFile(String srcFilename, String destFilename,String outEn) throws IOException {

        File srcFile = new File(srcFilename);
        if (!srcFile.exists()) {
            throw new FileNotFoundException("Cannot find the source file: " + srcFile.getAbsolutePath());
        }
        if (!srcFile.canRead()) {
            throw new IOException("Cannot read the source file: " + srcFile.getAbsolutePath());
        }
        
        int end =  destFilename.lastIndexOf(File.separator);
        if(end != -1){
            String dirs = destFilename.substring(0, end);
            createDirs(dirs,false);
        }
        


        File destFile = new File(destFilename);
      
        BufferedInputStream inputStream = null;
        BufferedOutputStream outputStream = null;
        byte[] block = new byte[1024];
        try {
            inputStream = new BufferedInputStream(new FileInputStream(srcFile));
            outputStream = new BufferedOutputStream(new FileOutputStream(destFile));
            while (true) {
                int readLength = inputStream.read(block);
                if (readLength == -1) break;// end of file
                outputStream.write(block, 0, readLength);
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    // just ignore
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    // just ignore
                }
            }
        }
    }
    
    /** fn-hd
     * rem: 合并两个文件
     * par: 
     * ret: 
     * log: 2007-2-3 下午01:21:32 创建
     * aut: 熊波
     */
    public static void combFile(String srcFilename, String destFilename) throws IOException {

        File srcFile = new File(srcFilename);
        if (!srcFile.exists()) {
            throw new FileNotFoundException("Cannot find the source file: " + srcFile.getAbsolutePath());
        }
        if (!srcFile.canRead()) {
            throw new IOException("Cannot read the source file: " + srcFile.getAbsolutePath());
        }
        
        int end =  destFilename.lastIndexOf(File.separator);
        if(end != -1){
            String dirs = destFilename.substring(0, end);
            createDirs(dirs,true);
        }


        File destFile = new File(destFilename);
      
        BufferedReader reader = null;

        PrintWriter out = null;

        try {
            FileInputStream fis = new FileInputStream(srcFile);
            reader = new BufferedReader(new InputStreamReader(fis,  "GBK"));
            out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(destFile,true),"GBK"));
           
            String readLine = reader.readLine();
            while (readLine != null) {
                out.println(readLine);
                readLine = reader.readLine();

            }
        } catch (FileNotFoundException fe) {
            log.error("Error", fe);
            throw fe;
        } catch (IOException e) {
            log.error("Error", e);
            throw e;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    // just ignore
                }
            }
            if (out != null) {
                
                out.close();
               
            }
        }
    }
    
    
    
    
    /** fn-hd
     * rem: 复制文件
     * par: 
     * ret: 
     * log: 2007-10-30 下午06:43:41 创建
     * aut: 熊波
     */
    public static void copyFile(String srcFilename, String destFilename, boolean overwrite) throws IOException {

        File srcFile = new File(srcFilename);
        if (!srcFile.exists()) {
            throw new FileNotFoundException("Cannot find the source file: " + srcFile.getAbsolutePath());
        }
        if (!srcFile.canRead()) {
            throw new IOException("Cannot read the source file: " + srcFile.getAbsolutePath());
        }

        File destFile = new File(destFilename);
        if (overwrite == false) {
            if (destFile.exists()) return;
        } else {
            
            if (destFile.exists()) {
                if (!destFile.canWrite()) {
                    throw new IOException("Cannot write the destination file: " + destFile.getAbsolutePath());
                }
            } else {
                if (!destFile.createNewFile()) {
                    throw new IOException("Cannot write the destination file: " + destFile.getAbsolutePath());
                }
            }
        }

        BufferedInputStream inputStream = null;
        BufferedOutputStream outputStream = null;
        byte[] block = new byte[1024];
        try {
            inputStream = new BufferedInputStream(new FileInputStream(srcFile));
            outputStream = new BufferedOutputStream(new FileOutputStream(destFile));
            while (true) {
                int readLength = inputStream.read(block);
                if (readLength == -1) break;// end of file
                outputStream.write(block, 0, readLength);
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    // just ignore
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    // just ignore
                }
            }
        }
    }

    //@todo: why this method does not close the inputStream ???
    public static byte[] getBytes(InputStream inputStream) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
        byte[] block = new byte[4096];
        while (true) {
            int readLength = bufferedInputStream.read(block);
            if (readLength == -1) break;// end of file
            byteArrayOutputStream.write(block, 0, readLength);
        }
        byte[] retValue = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return retValue;
    }
    
    public static byte[] getFileBytes(String files)throws IOException{
        File file = null;
        try {
            file = new File(files);
            if (file.isFile() == false) {
                throw new IOException("'" + files + "' is not a file.");
            }
        } finally {
            // we dont have to close File here
        }
    	try{
    		FileInputStream fis = new FileInputStream(files);
    		
    		return getBytes(fis);
    	}catch(Exception ex){
    		
    	}
    	
    	return null;
    	
    }
    
    
    /** fn-hd
     * rem: 获取文件名称
     * par: 
     * ret: 
     * log: 2007-10-30 下午06:43:30 创建
     * aut: 熊波
     */
    public static String getFileName(String fullFilePath) {
        if (fullFilePath == null) {
            return "";
        }
        int index1 = fullFilePath.lastIndexOf('/');
        int index2 = fullFilePath.lastIndexOf('\\');

        //index is the maximum value of index1 and index2
        int index = (index1 > index2) ? index1 : index2;
        if (index == -1) {
            // not found the path separator
            return fullFilePath;
        }
        String fileName = fullFilePath.substring(index + 1);
        return fileName;
    }

    /**
     * This method write srcFile to the output, and does not close the output
     * @param srcFile File the source (input) file
     * @param output OutputStream the stream to write to, this method will not buffered the output
     * @throws IOException
     */
    public static void popFile(File srcFile, OutputStream output) throws IOException {

        BufferedInputStream input = null;
        byte[] block = new byte[4096];
        try {
            input = new BufferedInputStream(new FileInputStream(srcFile), 4096);
            while (true) {
                int length = input.read(block);
                if (length == -1) break;// end of file
                output.write(block, 0, length);
            }
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                    // just ignore
                }
            }
        }
    }

    /**
     * This method could be used to override the path to WEB-INF/classes
     * It can be set when the web app is inited
     * @param path String : new path to override the default path
     */
    public static void setServletClassesPath(String path) {
        log.debug("FileUtil.setServletClassesPath called with path = " + path);

        servletClassesPath = path;

        if (servletClassesPath == null) {
            // From mvnForum.com thread 2243:
            // I am deploying the MVNForum as an ear in Linux box so context real path turns out to be null.
            return;
        }
        if (servletClassesPath.endsWith(File.separator) == false) {
            servletClassesPath = servletClassesPath + File.separatorChar;
            log.debug("FileUtil.setServletClassesPath change path to value = " + servletClassesPath);
        }
    }
        
    public static String filterFilePath(String path){
        if(path == null ||  path.length() < 0)
            return path;
        
        if (path.startsWith("/") == true && path.startsWith(""+File.separatorChar) == false){
            path = path.substring(1, path.length());
        }
        
        if("\\".equals( ""+File.separatorChar)){
            path = path.replaceAll("/", "\\\\");
            path = path.replaceAll("\\\\\\\\","");
           // path = path.replaceAll("\\","\\\\");
        }

       
        
        return path;
    }


    
    /** fn-hd
     * rem: 获取系统应用加载路径
     * par: 
     * ret: 
     * log: 2007-10-30 下午06:42:51 创建
     * aut: 熊波
     */
    public static String getServletClassesPath() {

        if (servletClassesPath == null || servletClassesPath.length() <= 0) {
            String strPath = "";//System.getProperty("user.dir");
            
            
            
            if (strPath != null && (strPath.length() > 0)) {
                servletClassesPath = strPath;
            } else {
                ClassLoader classLoader = instance.getClass().getClassLoader();
                URL url = classLoader.getResource("/");
                if (url == null) {
                    // not run on the Servlet environment
                    servletClassesPath = ".";
                } else {
                    servletClassesPath = url.getPath();
                }
            }
            log.debug("servletClassesPath = " + servletClassesPath);
            if (servletClassesPath.endsWith(File.separator) == false) {
                servletClassesPath = servletClassesPath + File.separatorChar;
                //log.warn("servletClassesPath does not end with /: " + servletClassesPath);
            }
        }

        
        if (servletClassesPath == null || servletClassesPath.length() <= 0 || ".".equals(servletClassesPath) || "./".equals(servletClassesPath) || ".\\".equals(servletClassesPath)) {
            servletClassesPath =System.getProperty("user.dir");
            
        }
       
        if(servletClassesPath.indexOf("WEB-INF/classes") != -1){
            servletClassesPath = servletClassesPath.replaceAll("WEB-INF/classes", "WEB-INF/");
        }
        
        System.setProperty("path", servletClassesPath);
        
        return servletClassesPath;
    }

    
    /** fn-hd
     * rem: 创建文件
     * par: 
     * ret: 
     * log: 2007-10-30 下午06:42:19 创建
     * aut: 熊波
     */
    public static void createFile(InputStream inputStream, String filePath)
        throws IOException {

        if (inputStream == null || filePath == null || filePath.length() <= 0) {
            throw new IllegalArgumentException("Does not accept null input");
        }
        
       
        
        int end =  filePath.lastIndexOf(File.separator);
        if(end != -1){
            String dirs = filePath.substring(0, end);
            createDirs(dirs,true);
        }
        
        BufferedOutputStream outputStream = null;
        try{
        	
        	byte[] srcByte = new byte[10240];
        	outputStream = new BufferedOutputStream(new FileOutputStream(filePath));
        	
            while (true) {
                int readLength = inputStream.read(srcByte);
                if (readLength == -1) break;// end of file
                outputStream.write(srcByte, 0, readLength);
            }
        	        	
        	outputStream.flush();
        	
        	
        }catch(Exception ex){
        	ex.printStackTrace();
        }finally { // this finally is very important
            inputStream.close();
            if (outputStream != null) outputStream.close();
        }
    }
    

    public static void createTextFile(InputStream inputStream, String textFile)
        throws IOException {

        if (inputStream == null) {
            throw new IllegalArgumentException("Does not accept null input");
        }
        OutputStream outputStream = null;
        try {
            byte[] srcByte = FileUtil.getBytes(inputStream);
            outputStream = new FileOutputStream(textFile);
            outputStream.write(srcByte);
            return;
        } catch (IOException e) {
            log.error("Error", e);
            throw e;
        } finally { // this finally is very important
            inputStream.close();
            if (outputStream != null) outputStream.close();
        }
    }

    public static void writeObjFile(Object content, String fileName) throws FileNotFoundException, IOException {

		File file = null;
		try {

			// System.out.println(fileName);

			if (fileName != null && fileName.length() > 0) {
				String dirs = fileName.substring(0, fileName
						.lastIndexOf(File.separator));
				file = new File(dirs);
				if (!file.exists()) {
					file.mkdirs();
				}

				file = new File(fileName);
				if (!file.exists()) {
					file.createNewFile();
				}
			}

		} finally {
			// we dont have to close File here
		}

		
		
		ObjectOutputStream ois = null;
		
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			ois = new ObjectOutputStream(new GZIPOutputStream(fos));
			ois.writeObject(content);
		
		} catch (FileNotFoundException fe) {
			log.error("Error", fe);
			throw fe;
		} catch (IOException e) {
			log.error("Error", e);
			throw e;
		} finally {
			try {
				if (ois != null)
					ois.close();
			} catch (IOException ex) {
			}
		}
	}
    
    
    /**
	 * Write content to a fileName with the destEncoding
	 * 
	 * @param content
	 *            String
	 * @param fileName
	 *            String
	 * @param destEncoding
	 *            String
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
    public static void writeFile(String content, String fileName, String destEncoding)
        throws FileNotFoundException, IOException {

        File file = null;
        try {
            
          //  System.out.println(fileName);
            
            if(fileName != null && fileName.length() > 0){
                String dirs = fileName.substring(0,fileName.lastIndexOf(File.separator));
                file = new File(dirs);
                if(!file.exists()){
                    file.mkdirs();
                }
                
                file = new File(fileName);
                if(!file.exists()){
                   file.createNewFile();
                }
            }

        } finally {
           
        }
    

        BufferedWriter out = null;
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            out = new BufferedWriter(new OutputStreamWriter(fos, destEncoding));

            out.write(content);
            out.flush();
            out.close();
            out = null;
        } catch (FileNotFoundException fe) {
            log.error("Error", fe);
            throw fe;
        } catch (IOException e) {
            log.error("Error", e);
            throw e;
        } finally {
            try {
                if (out != null) out.close();
            } catch (IOException ex) {}
            
           // System.out.println("--->>>");
        }
    }
    
    /**
     * Write content to a fileName with the destEncoding
     *
     * @param content byte[]
     * @param fileName String
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void writeFile(byte[] content, String fileName)
    throws FileNotFoundException, IOException {
    	
    	File file = null;
    	try {
    		
    		//System.out.println(fileName);
    		
    		if(fileName != null && fileName.length() > 0){
    			String dirs = fileName.substring(0,fileName.lastIndexOf(File.separator));
    			file = new File(dirs);
    			if(!file.exists()){
    				file.mkdirs();
    			}
    			
    			file = new File(fileName);
    			if(!file.exists()){
    				file.createNewFile();
    			}
    		}
    		
    	} finally {
    		// we dont have to close File here
    	}
    	
    	
    	BufferedOutputStream out = null;
    	try {
    		out = new BufferedOutputStream(new FileOutputStream(fileName));
    		
    		out.write(content);
    		out.flush();
    	} catch (FileNotFoundException fe) {
    		log.error("Error", fe);
    		throw fe;
    	} catch (IOException e) {
    		log.error("Error", e);
    		throw e;
    	} finally {
    		try {
    			if (out != null) out.close();
    		} catch (IOException ex) {}
    	}
    }
    
    public static void writeGzipFile(String content, String fileName, String destEncoding)
    throws FileNotFoundException, IOException {

    File file = null;
    try {
        
        //System.out.println(fileName);
        
	        if(fileName != null && fileName.length() > 0){
	            String dirs = fileName.substring(0,fileName.lastIndexOf(File.separator));
	            file = new File(dirs);
	            if(!file.exists()){
	                file.mkdirs();
	            }
	            
	            file = new File(fileName);
	            if(!file.exists()){
	               file.createNewFile();
	            }
	        }
	
	    } finally {
	        // we dont have to close File here
	    }
	
	
	    BufferedWriter out = null;
	    try {
	        FileOutputStream fos = new FileOutputStream(fileName);	     
	        out = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(fos),destEncoding));
	
	        out.write(content);
	        out.flush();
	    } catch (FileNotFoundException fe) {
	        log.error("Error", fe);
	        throw fe;
	    } catch (IOException e) {
	        log.error("Error", e);
	        throw e;
	    } finally {
	        try {
	            if (out != null) out.close();
	        } catch (IOException ex) {}
	    }
	}
    
    /** fn-hd
     * rem: 追加文件内容
     * par: 
     * ret: 
     * log: 2007-10-30 下午06:41:47 创建
     * aut: 熊波
     */
    public static void appendFile(String content, String fileName, String destEncoding)
    throws FileNotFoundException, IOException {

        File file = null;
        try {
            
            //System.out.println(fileName);
            
            if(fileName != null && fileName.length() > 0){
                String dirs = fileName.substring(0,fileName.lastIndexOf(File.separator));
                file = new File(dirs);
                if(!file.exists()){
                    file.mkdirs();
                }
                
                file = new File(fileName);
                if(!file.exists()){
                   file.createNewFile();
                }
            }

        } finally {
            // we dont have to close File here
        }
    
        PrintWriter out = null;
       // FileWriter fWriter = null;
        try {
            FileOutputStream fos = new FileOutputStream(fileName,true);
           // fWriter = new FileWriter(fileName, true);

            out = new PrintWriter(new OutputStreamWriter(fos, destEncoding));
            out.println(content);
           // out.write(content);
           // out.flush();
        } catch (FileNotFoundException fe) {
            log.error("Error", fe);
            throw fe;
        } catch (IOException e) {
            log.error("Error", e);
            throw e;
        } finally {
          //  if (fWriter != null) fWriter.close();
            
            if (out != null) out.close();
           /* try {
                if (out != null) out.close();
            } catch (IOException ex) {}*/
        }
    }
    
    
    /** fn-hd
     * rem: 修改文件名称
     * par: 
     * ret: 
     * log: 2007-10-30 下午06:41:37 创建
     * aut: 熊波
     */
    public static void renameFile(String srcFilename, String destFilename)throws IOException {
        File srcFile = null;
        File destFile = null;
        try {
            srcFile = new File(srcFilename);
            destFile = new File(destFilename);
            if (srcFile.isFile() == false) {
                throw new IOException("'" + srcFilename + "' is not a file.");
            }
            srcFile.renameTo(destFile);
            
        } finally {
            // we dont have to close File here
        }
    }

    public static void CvUtfGbkFile(String dirPath,String ext)throws Exception{
    	File file = null;
        
        try {
            file = new File(dirPath);

        } finally {
            //we dont have to close File here
        }
        
        File[] root = file.listFiles();
        for(File fi : root){
        	
        	if(fi.isDirectory()){
        		CvUtfGbkFile(dirPath + File.separator + fi.getName(),ext);
        	}else{
        	
        		if(fi.getName().indexOf(ext) != -1){
        			String cts = readFile(dirPath + File.separator + fi.getName(),"utf-8");
        		
        			writeFile(cts,dirPath + File.separator + fi.getName(),"GBK");
        		}
        		
        	
        	}
        }
    }
    
    public static Object readObjFile(String fileName)
    throws FileNotFoundException, IOException {
        File file = null;
        try {
            file = new File(fileName);
            if (file.isFile() == false) {
                throw new IOException("'" + fileName + "' is not a file.");
            }
        } finally {
            // we dont have to close File here
        }
        
		ObjectInputStream ois = null;
		try
		{
		
			ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(fileName)));
			
			return ois.readObject();
			
		}catch(Exception ex){
			log.error("readObjFile:", ex);
		}finally{
			if(ois != null){
				try{
					ois.close();
					ois = null;
				}catch(Exception ex){
					
				}
			}
		}
        
		return null;
    }
    
    
    /** fn-hd
     * rem: 读取文件内容
     * par: 
     * ret: 
     * log: 2007-10-30 下午06:41:24 创建
     * aut: 熊波
     */
    public static String readFile(String fileName, String srcEncoding)
        throws FileNotFoundException, IOException {

        File file = null;
        try {
            file = new File(fileName);
            if (file.isFile() == false) {
                throw new IOException("'" + fileName + "' is not a file.");
            }
        } finally {
            // we dont have to close File here
        }

        BufferedReader reader = null;
        try {
            StringBuffer result = new StringBuffer(1024);
            FileInputStream fis = new FileInputStream(fileName);
            reader = new BufferedReader(new InputStreamReader(fis, srcEncoding));

            char[] block = new char[512];
            while (true) {
                int readLength = reader.read(block);
                if (readLength == -1) break;// end of file
                result.append(block, 0, readLength);
            }
            return result.toString();
        } catch (FileNotFoundException fe) {
            log.error("Error", fe);
            throw fe;
        } catch (IOException e) {
            log.error("Error", e);
            throw e;
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException ex) {}
        }
    }
    
    
    /** fn-hd
     * rem: 将String型文件大小信息转换成long
     * par: 
     * ret: 
     * log: 2007-10-30 下午06:39:08 创建
     * aut: 熊波
     */
    public static long parseLongSizeValue(String propertyValue, int defaultValue) {
        try {
            String temp = propertyValue.trim();
            if (temp.endsWith("B") || temp.endsWith("b")) {
                temp = temp.substring(0, temp.length() - 1);
            }
            switch (temp.charAt(temp.length()-1)) {
                case 'K': case 'k':
                    //examples (ending 'B' was cut before): "1K", "1KB", "1k", "1kB", "1 K", "1 KB", "1 k", "1 kB"
                    return 1024 * Long.valueOf(temp.substring(0, temp.length() - 1).trim()).longValue();
                case 'M': case 'm':
                    //examples (ending 'B' was cut before): "1M", "1MB", "1m", "1mB", "1 M", "1 MB", "1 m", "1 mB"
                    return 1024 * 1024 * Long.valueOf(temp.substring(0, temp.length() - 1).trim()).longValue();
                default:
                    //examples (ending 'B' was cut before): "1", "1B", "1 B"
                    return Long.valueOf(temp.trim()).longValue();
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
            return defaultValue;
        }
    }


    
    /** fn-hd
     * rem: 获取文件最后几行数据
     * par: 
     * ret: 
     * log: 2007-10-30 下午06:38:48 创建
     * aut: 熊波
     */
    public static String[] getLastLines(File file, int linesToReturn)
        throws IOException, FileNotFoundException {

        final int AVERAGE_CHARS_PER_LINE = 250;
        final int BYTES_PER_CHAR = 2;

        RandomAccessFile randomAccessFile = null;
        StringBuffer buffer = new StringBuffer(linesToReturn * AVERAGE_CHARS_PER_LINE);
        int lineTotal = 0;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            long byteTotal = randomAccessFile.length();
            long byteEstimateToRead = linesToReturn * AVERAGE_CHARS_PER_LINE * BYTES_PER_CHAR;

            long offset = byteTotal - byteEstimateToRead;
            if (offset < 0) {
                offset = 0;
            }

            randomAccessFile.seek(offset);
            //log.debug("SKIP IS ::" + offset);

            String line = null;
            String lineUTF8 = null;
            while ((line = randomAccessFile.readLine()) != null) {
                lineUTF8 = new String(line.getBytes("ISO8859_1"), "UTF-8");
                lineTotal++;
                buffer.append(lineUTF8).append("\n");
            }
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException ex) {
                }
            }
        }

        String[] resultLines = new String[linesToReturn];
        BufferedReader in = null;
        try {
            in = new BufferedReader(new StringReader(buffer.toString()));

            int start = lineTotal /* + 2 */ - linesToReturn; // Ex : 55 - 10 = 45 ~ offset
            if (start < 0) start = 0; // not start line
            for (int i = 0; i < start; i++) {
                in.readLine(); // loop until the offset. Ex: loop 0, 1 ~~ 2 lines
            }

            int i = 0;
            String line = null;
            while ((line = in.readLine()) != null) {
                resultLines[i] = line;
                i++;
            }
        } catch (IOException ie) {
            log.error("Error" + ie);
            throw ie;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                }
            }
        }
        return resultLines;
    }

    

    
    
    /** fn-hd
     * rem: 将long型文件大小信息转换成String
     * par: 
     * ret: 
     * log: 2007-10-30 下午06:37:47 创建
     * aut: 熊波
     */
    public static String getHumanSize(long size) {

        int sizeToStringLength = String.valueOf(size).length();
        String humanSize = "";
        DecimalFormat formatter = new DecimalFormat("##0.##");
        if (sizeToStringLength > 9) {
            humanSize += formatter.format((double) size / (1024 * 1024 * 1024)) + " GB";
        } else if (sizeToStringLength > 6) {
            humanSize += formatter.format((double) size / (1024 * 1024)) + " MB";
        } else if (sizeToStringLength > 3) {
            humanSize += formatter.format((double) size / 1024) + " KB";
        } else {
            humanSize += String.valueOf(size) + " Bytes";
        }
        return humanSize;
    }
    
    /*public static void main(String arg[]){
        String[] model = {"pub","autogenerate","price","cms"};
        for(int j=0; j < model.length; j ++){
        	String[] fileList = getFileList("D:\\project\\nbw\\conf\\nbw\\sqlmap\\"+model[j]+"\\", ".xml");
        	if(fileList != null && fileList.length > 0)
        	for(int i=0; i<fileList.length; i++){
                try{
                    if(!"autogenerate".equals(model[j])){
                        DOM4JConfiguration conf = new DOM4JConfiguration(new File("D:\\project\\nbw\\conf\\nbw\\sqlmap\\"+model[j]+"\\" +fileList[i] ));
                    }
                    
                    System.out.println("<sqlMap resource=\"nbw/sqlmap/"+model[j]+"/"+fileList[i]+"\"/>");
                    
                }catch(Exception ex){
                    System.out.println(ex.getMessage());
                }
        		 
            }
            
        }
    }*/

    public static String getViewByBuffer(Map<String, Object> model, String tplPath) {

        VelocityEngine ve = new VelocityEngine();
//        Properties p = new Properties();
//        p.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, tplPath);
//        ve.init(p);
        ve.setProperty("resource.loader", "class");
        ve.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        ve.init();
        StringWriter w = new StringWriter();
        try {
            Context velocityContext = new VelocityContext(model);
            Template tpl = ve.getTemplate(tplPath, "UTF-8");
            tpl.initDocument();
            tpl.merge(velocityContext, w);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return w.toString();
    }

}
