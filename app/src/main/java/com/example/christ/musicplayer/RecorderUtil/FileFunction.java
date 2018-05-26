package com.example.christ.musicplayer.RecorderUtil;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by christ on 2018/5/22.
 */

public class FileFunction {
    public static boolean IsExitsSdcard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static boolean IsFileExists(String path) {
        if (path == null || path.length() == 0) {
            return false;
        }
        return new File(path).exists();
    }

    private static void CreateDirectory(String path) {
        File dir = new File(path);

        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static void InitStorage(Context context) {
        if (!FileFunction.IsExitsSdcard()) {
            Variable.StorageDirectoryPath = context.getFilesDir().getAbsolutePath();
        } else {
            Variable.StorageDirectoryPath =
                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/MusicPlayer/";
        }

        Variable.ErrorFilePath = Variable.StorageDirectoryPath + "error.txt";

        CreateDirectory(Variable.StorageDirectoryPath);
    }

    public static void SaveFile(Context context, String url, String content) {
        SaveFile(context, url, content, true, false);
    }

    public static void SaveFile(Context context, String url, String content,
                                boolean cover, boolean append) {
        FileOutputStream out = null;
        File file = new File(url);

        try {
            if (file.exists()) {
                if (cover) {
                    file.delete();
                    file.createNewFile();
                }
            } else {
                file.createNewFile();
            }

            out = new FileOutputStream(file, append);
            out.write(content.getBytes());
            out.close();
            Toast.makeText(context,"保存文件" + url + "保存文件成功", Toast.LENGTH_SHORT)
                    .show();
        } catch (Exception e) {
            Toast.makeText(context,"保存文件" + url + e.toString(), Toast.LENGTH_SHORT)
                    .show();
            Log.e("保存文件" + url, e.toString());

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static void DeleteFile(Context context, String path) {
        if (path != null && path.length() > 0) {
            File file = new File(path);

            if (file.exists()) {
                try {
                    file.delete();
                } catch (Exception e) {
                    Toast.makeText(context,"删除本地文件失败", Toast.LENGTH_SHORT)
                            .show();
                    Log.e("删除本地文件失败", e.toString());
                }
            }
        }
    }

    public static void CopyFile(String oldPath, String newPath) {
        try {
            int byteRead;

            File oldFile = new File(oldPath);
            File newFile = new File(newPath);

            if (oldFile.exists()) { //文件存在时
                if (newFile.exists()) {
                    newFile.delete();
                }

                newFile.createNewFile();

                FileInputStream inputStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream outputStream = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];

                while ((byteRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, byteRead);
                }

                inputStream.close();
            }
        } catch (Exception e) {
            Log.e("复制单个文件操作出错", e.toString());
        }
    }

    public static FileInputStream GetFileInputStreamFromFile(String fileUrl) {
        FileInputStream fileInputStream = null;

        try {
            File file = new File(fileUrl);

            fileInputStream = new FileInputStream(file);
        } catch (Exception e) {
            // GetBufferedInputStreamFromFile异常
            Log.e("GetBufferInputStream异常", e.toString());
        }

        return fileInputStream;
    }

    public static FileOutputStream GetFileOutputStreamFromFile(String fileUrl) {
        FileOutputStream bufferedOutputStream = null;

        try {
            File file = new File(fileUrl);

            if (file.exists()) {
                file.delete();
            }

            file.createNewFile();

            bufferedOutputStream = new FileOutputStream(file);
        } catch (Exception e) {
            Log.e("GetFileOutputStream异常", e.toString());
        }

        return bufferedOutputStream;
    }

    public static BufferedOutputStream GetBufferedOutputStreamFromFile(String fileUrl) {
        BufferedOutputStream bufferedOutputStream = null;

        try {
            File file = new File(fileUrl);

            if (file.exists()) {
                file.delete();
            }

            file.createNewFile();

            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
        } catch (Exception e) {
            Log.e("GetBufferOutputStream异常", e.toString());
        }

        return bufferedOutputStream;
    }

    public static void RenameFile(String oldPath, String newPath) {
        if ((oldPath != null && oldPath.length() > 0) &&
                (newPath != null && newPath.length() > 0)) {
            File newFile = new File(newPath);

            if (newFile.exists()) {
                newFile.delete();
            }

            File oldFile = new File(oldPath);

            if (oldFile.exists()) {
                try {
                    oldFile.renameTo(new File(newPath));
                } catch (Exception e) {
                    Log.e("删除本地文件失败", e.toString());
                }
            }
        }
    }
}

