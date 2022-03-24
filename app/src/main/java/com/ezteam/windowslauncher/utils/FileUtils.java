package com.ezteam.windowslauncher.utils;

import android.text.TextUtils;

import com.ezteam.windowslauncher.R;

import org.apache.commons.io.FilenameUtils;

import java.text.DecimalFormat;
import java.util.Arrays;

public class FileUtils {

    public static final String EXCEL = "excel";
    public static final String WORD = "word";
    public static final String PDF = "pdf";
    public static final String POWERPOINT = "powerpoint";
    public static final String TXT = "text";
    public static final String HTML = "html";
    public static final String ZIP = "zip";
    public static final String APK = "apk";
    public static final String AUDIO = "audio";
    public static final String VIDEO = "video";

    public static final String[] fileExtension = new String[]{".csv", ".xlsx", ".xls", ".xlt", ".xlsm", ".xltm", ".xltx"
            , ".doc", ".docx", ".dot", ".dotx", ".dotm", ".txt", ".pot", ".pptm", ".potx", ".potm", ".ppt", ".pptx", ".pdf"};

    public static final String[] excelFileExtension = new String[]{".csv", ".xlsx", ".xls", ".xlt", ".xlsm", ".xltm", ".xltx"};
    public static final String[] wordlFileExtension = new String[]{".doc", ".docx", ".dot", ".dotx", ".dotm"};
    public static final String[] txtFileExtension = new String[]{".txt"};
    public static final String[] htmlFileExtension = new String[]{".html"};
    public static final String[] powerpointFileExtension = new String[]{".pot", ".pptm", ".potx", ".potm", ".ppt", ".pptx"};
    public static final String[] pdfFileExtension = new String[]{".pdf"};
    public static final String[] zipExtension = new String[]{".zip", ".rar", ".rar4"};
    public static final String[] apkExtension = new String[]{".apk", ".abb", ".xapk"};
    public static final String[] audioExtension = new String[]{
            ".3gp", ".aa", ".aac", ".aax", ".act", ".aiff", ".alac", ".amr", ".ape", ".au", ".awb",
            ".dss", ".dvf", ".flac", ".gsm", ".iklax", ".ivs", ".m4a", ".m4b", ".m4p", ".mmf", ".mp3",
            ".mpc", ".msv", ".nmf", ".opus", ".raw", ".rf64", ".sln", ".tta", ".voc", ".vox", ".wav", ".wma",
            ".wv", ".webm", ".8svx", ".cda", ".m4r"
    };
    public static final String[] videoExtension = new String[]{
            ".webm", ".mkv", ".flv", ".vob", ".drc", ".gif", ".gifv", ".mng", ".avi", ".mov", ".qt",
            ".wmv", ".yuv", ".rm", ".rmvb", ".viv", ".asf", ".amv", ".m4v", ".mp4", ".svi", ".3gp",
            ".3g2", ".mxf", ".roq", ".nsv", ".flv", ".f4v", ".f4p", ".f4a", ".f4b", ".mpg", ".mpeg",
            ".m2v", ".mp2", ".mpeg", ".mpe", ".mpv"
    };
    public static final String[] imageExtension = new String[]{
            ".jpg", ".png", ".webp", ".tiff"
    };

    public static String getFileType(String fileName) {
        if (TextUtils.isEmpty(fileName))
            return "";
        String endFile = "." + FilenameUtils.getExtension(fileName.toLowerCase());
        if (Arrays.asList(excelFileExtension).contains(endFile))
            return EXCEL;
        else if (Arrays.asList(wordlFileExtension).contains(endFile))
            return WORD;
        else if (Arrays.asList(txtFileExtension).contains(endFile)
                || Arrays.asList(htmlFileExtension).contains(endFile))
            return TXT;
        else if (Arrays.asList(powerpointFileExtension).contains(endFile))
            return POWERPOINT;
        else if (Arrays.asList(pdfFileExtension).contains(endFile))
            return PDF;
        else if (Arrays.asList(zipExtension).contains(endFile))
            return ZIP;
        else if (Arrays.asList(apkExtension).contains(endFile))
            return APK;
        else if (Arrays.asList(audioExtension).contains(endFile))
            return AUDIO;
        else if (Arrays.asList(videoExtension).contains(endFile))
            return VIDEO;
        else
            return "";
    }

    public static int getIconResId(String fileName) {
        String fileType = getFileType(fileName);
        switch (fileType) {
            case FileUtils.PDF:
                return R.drawable.ic_pdf;
            case FileUtils.EXCEL:
                return R.drawable.ic_xls;
            case FileUtils.WORD:
                return R.drawable.ic_docx;
            case FileUtils.POWERPOINT:
                return R.drawable.ic_ppt;
            case FileUtils.TXT:
                return R.drawable.ic_txt;
            case FileUtils.ZIP:
                return R.drawable.ic_zip_folder;
            case FileUtils.APK:
                return R.drawable.ic_apk_folder;
            case FileUtils.AUDIO:
                return R.drawable.ic_music_folder;
            case FileUtils.VIDEO:
                return R.drawable.ic_video_folder_2;
            default:
                return R.drawable.ic_empty_folder;
        }
    }

    public static String formatSize(long size) {
        if (size <= 0)
            return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static boolean isFileImageVideo(String url) {
        if (TextUtils.isEmpty(url))
            return false;
        if (url.contains(".jpg") || url.contains(".png") || url.contains(".webp")
                || url.contains(".mp4")
                || url.contains(".avi") || url.contains(".flv") || url.contains("wmv")
                || url.contains(".mov"))
            return true;
        return false;
    }

    public static boolean isFileImage(String url) {
        if (TextUtils.isEmpty(url))
            return false;
        return Arrays.asList(imageExtension).contains("." + FilenameUtils.getExtension(url));
    }

}
