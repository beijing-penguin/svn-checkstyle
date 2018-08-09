package com.fescotech.svn.checkstyle.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @Description: TODO
 * @author: dc
 * @date: 2018年7月16日 上午11:24:29
 * @version: V1.0
 */
public class SvnCheckScriptMain {
    /**
     * 
     * @Description: TODO
     * @param args TODO
     * @throws Exception TODO
     * @return: void
     */
    public static void main(String[] args) throws Exception {
        String tmpdir = null;
        try {
            tmpdir = args[4];
        }catch (Exception e) {
        }
        if(tmpdir==null) {
            tmpdir="";
        }
        String logFileStr = tmpdir+File.separator+"svncheck_error.log";
        File logfile = new File(logFileStr);
        if (!logfile.getParentFile().exists()) {
            logfile.getParentFile().mkdirs();
            logfile.createNewFile();
        }
        
        try {

            String repos = args[0];
            String txn = args[1];
            String checkstyle = args[2];
            String checkstyleConfig = args[3];

            String command = "svnlook changed -t " + txn + " " + repos;
            String str = new String(commandExecute(command));
            boolean success = true;
            for (String fileStr : str.split(System.getProperty("line.separator"))) {
                String svnFileName = fileStr.substring(4);

                if (svnFileName.endsWith(".java")) {
                    String fileName = "";
                    int index = svnFileName.lastIndexOf("/");
                    if (index != -1) {
                        fileName = svnFileName.substring(index + 1);
                    } else {
                        fileName = svnFileName;
                    }
                    //String myname = fileName.substring(0, fileName.lastIndexOf("."));
                    /*if (!classNameCheck(myname)) {
                        System.err.println(fileName + "文件名命名不规范");
                        success = false;
                        break;
                    }*/
                    System.err.println(fileName);
                    Files.write(Paths.get(logFileStr), fileName.getBytes(), StandardOpenOption.APPEND);
                    String c = "svnlook cat " + repos + " --transaction " + " " + txn + " \"" + svnFileName + "\"";

                    byte[] fileData = commandExecute(c);

                    String tmpCommitFilePath = tmpdir + File.separator + txn + File.separator + svnFileName;
                    File file = new File(tmpCommitFilePath);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    file.createNewFile();
                    Files.write(Paths.get(tmpCommitFilePath), fileData);

                    String checkCommand = "java -jar " + checkstyle + " -c " + checkstyleConfig + " " + ""
                            + Paths.get(tmpCommitFilePath) + "";

                    byte[] rtByte = commandExecute(checkCommand);
                    String rt = new String(rtByte, "utf-8");

                    String[] rtArr = rt.split("\n");
                    rtArr[rtArr.length - 1] = new String(rtByte).split("\n")[rtArr.length - 1];

                    String errorInfo = new String(rtByte).split("\n")[new String(rtByte).split("\n").length - 1];

                    if (errorInfo.contains("Checkstyle")) {
                        for (String r : rtArr) {
                            Files.write(Paths.get(logFileStr), r.getBytes(), StandardOpenOption.APPEND);
                            System.err.print(r);
                        }
                        success = false;
                        break;
                    }
                }
            }

            delAllFile(tmpdir + File.separator + txn);
            delFolder(tmpdir + File.separator + txn);
            if (!success) {
                System.exit(1);
            }
        } catch (Exception e) {
            String exInfo = getExceptionAllinformation(e);
            Files.write(Paths.get(logFileStr), exInfo.getBytes(), StandardOpenOption.APPEND);
            e.printStackTrace();
            throw e;
        }
    }
    /**
     * @param e e
     * @return String
     * @author dc
     */
    private static String getExceptionAllinformation(Throwable e){   
        StringWriter sw = new StringWriter();   
        PrintWriter pw = new PrintWriter(sw, true);   
        e.printStackTrace(pw);   
        pw.flush();   
        sw.flush();   
        return sw.toString();   
    } 
    /**
     * 
     * @Description:
     * @Title: commandExecute  
     * @param command command
     * @return byte[]
     * @throws Exception Exception
     * @author 段超   
     *
     */
    public static byte[] commandExecute(String command) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            Process process = Runtime.getRuntime().exec(command);
            InputStream is = process.getInputStream();

            byte[] buffer = new byte[4096];
            int n = 0;
            while (-1 != (n = is.read(buffer))) {
                output.write(buffer, 0, n);
            }
            process.waitFor();
            is.close();
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return output.toByteArray();
    }
    /**
     * 
     * @Description:
     * @Title: classNameCheck  
     * @param className className
     * @return  classNameCheck
     * 
     * @author 段超   
     * @date 2018年7月11日 上午10:44:17 
     *
     */
    public static boolean classNameCheck(String className) {
        for (int i = 0, len = className.toCharArray().length; i < len; i++) {
            char myc = className.charAt(i);
            if (!Character.isLetterOrDigit(myc) || isChineseByScript(myc)) {
                return false;
            }
            if (i == 0) {
                if (Character.isLowerCase(myc) || Character.isDigit(myc)) {
                    return false;
                }
            } else {
                if (Character.isUpperCase(myc) && Character.isUpperCase(className.charAt(i - 1))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 
     * @Description:
     * @Title: isChineseByScript  
     * @param c c
     * @return  isChineseByScript
     * 
     * @author 段超   
     * @date 2018年7月11日 上午10:44:30 
     *
     */
    private static boolean isChineseByScript(char c) {
        Character.UnicodeScript sc = Character.UnicodeScript.of(c);
        if (sc == Character.UnicodeScript.HAN) {
            return true;
        }
        return false;
    }

    /**
     * 
     * @Description:
     * @Title: delFolder  
     * @param folderPath  
     * 
     * @author 段超   
     * @date 2018年7月11日 上午10:44:43 
     *
     */
    private static void delFolder(String folderPath) {
        delAllFile(folderPath); // 删除完里面所有内容
        File myFilePath = new File(folderPath);
        myFilePath.delete(); // 删除空文件夹
    }

    /**
     * 
     * @Description:
     * @Title: delAllFile  
     * @param path path
     * @return   boolean
     * 
     * @author 段超   
     * @date 2018年7月11日 上午10:44:48 
     *
     */
    private static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]); // 先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]); // 再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }
}