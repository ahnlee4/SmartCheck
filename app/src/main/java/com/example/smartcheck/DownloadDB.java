package com.example.smartcheck;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadDB extends AsyncTask<String, Integer, String> {
    Activity activity;

    File outputFile;
    public DownloadDB(Activity activity){
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... apkurl) {
        int count;
        int lenghtOfFile = 0;
        InputStream input = null;
        OutputStream fos = null;

        try {
            URL url = new URL(apkurl[0]);
            URLConnection connection = url.openConnection();
            connection.connect();

            lenghtOfFile = connection.getContentLength(); // 파일 크기를 가져옴

            String path = Environment.getExternalStorageDirectory() + "/SIBI/BackUp";
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            outputFile = new File(path, apkurl[1]);
            if (outputFile.exists()) { // 기존 파일 존재시 삭제하고 다운로드
                outputFile.delete();
            }

            input = new BufferedInputStream(url.openStream());
            fos = new FileOutputStream(outputFile);
            byte data[] = new byte[1024];
            long total = 0;

            while ((count = input.read(data)) != -1) {
                if (isCancelled()) {
                    input.close();
                    return String.valueOf(-1);
                }
                total = total + count;
                if (lenghtOfFile > 0) { // 파일 총 크기가 0 보다 크면
                    publishProgress((int) (total * 100 / lenghtOfFile));
                }
                fos.write(data, 0, count); // 파일에 데이터를 기록
            }

            fos.flush();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("UpdateAPP", "Update error! " + e.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch(IOException ioex) {
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                }
                catch(IOException ioex) {
                }
            }
        }
        return null;
    }

    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // 백그라운드 작업의 진행상태를 표시하기 위해서 호출하는 메소드
    }
}


