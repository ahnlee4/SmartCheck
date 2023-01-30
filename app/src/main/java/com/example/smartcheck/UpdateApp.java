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

public class UpdateApp extends AsyncTask<String, Integer, String> {
    Activity activity;

    File outputFile;
    public UpdateApp(Activity activity){
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

            String path = Environment.getExternalStorageDirectory() + "/SIBI";
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            outputFile = new File(path, "update.apk");
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

    protected void onPostExecute(String result) {
        if (result == null) {
            // 미디어 스캐닝
            MediaScannerConnection.scanFile(activity.getApplicationContext(), new String[]{outputFile.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String s, Uri uri) {
                }
            });

            // 다운로드한 파일 실행하여 업그레이드 진행하는 코드
            installApk(outputFile);
        } else {
            Toast.makeText(activity, "다운로드 에러", Toast.LENGTH_LONG).show();
        }
    }

    protected void onCancelled() {
        // cancel메소드를 호출하면 자동으로 호출되는 메소드
    }

    public void installApk(File file) {
        Uri uri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".fileprovider",file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivityForResult(intent, MainActivity.REQUEST_UNINSTALL);
    }
}


