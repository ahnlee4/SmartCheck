package com.example.smartcheck;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.AppUpdaterUtils;
import com.github.javiersantos.appupdater.enums.AppUpdaterError;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.os.Environment;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    Activity activity;
    UpdateApp updateApp;
    String version;
    TextView app_version;
    TextView app_last_version;
    TextView db_version;
    TextView db_last_version;
    RelativeLayout progressBar;

    String lastversion = "";
    boolean isUpdate = false;

    public static int REQUEST_INSTALL = 1;
    public static int REQUEST_UNINSTALL = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity = this;
        getPermission(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button app_update = findViewById(R.id.app_update);
        app_version = findViewById(R.id.app_version);
        app_last_version = findViewById(R.id.app_last_version);
        progressBar = findViewById(R.id.progress);
        SharedPreferences sp = activity.getPreferences(Context.MODE_PRIVATE);


        try {
            sp.edit().putString("app_version", getIntent().getExtras().get("app_version").toString()).commit();
        }catch (Exception e){
        }
        version = sp.getString("app_version", "확인불가");
        app_version.setText("설치됨: " + version);

        AppUpdaterUtils appUpdaterUtils = new AppUpdaterUtils(activity)
                .setUpdateFrom(UpdateFrom.GITHUB)
                .setGitHubUserAndRepo("ahnlee4", "SmartCheck")
                .withListener(new AppUpdaterUtils.UpdateListener() {
                    @Override
                    public void onSuccess(com.github.javiersantos.appupdater.objects.Update update, Boolean isUpdateAvailable) {
                        lastversion = update.getLatestVersion();
                        isUpdate = isUpdateAvailable;
                        app_last_version.setText("최신: " + lastversion.replace("_","."));
                    }

                    @Override
                    public void onFailed(AppUpdaterError error) {
                        Log.d("AppUpdater Error", "Something went wrong");
                    }
                });
        appUpdaterUtils.start();

        app_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AppUpdater(activity)
                        .setTitleOnUpdateAvailable("최신 업데이트 가능")
                        .setContentOnUpdateAvailable("현재 업데이트 가능한 버전이 존재합니다.")
                        .setTitleOnUpdateNotAvailable("최신 버전입니다.")
                        .setContentOnUpdateNotAvailable("현재 최신 버전을 이용 중 입니다.")
                        .setButtonDoNotShowAgain("다시 보지 않기")
                        .setButtonUpdate("업데이트")
                        .setButtonUpdateClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri uri = Uri.fromParts("package", "com.example.jingu", null);
                                Intent delIntent = new Intent(Intent.ACTION_DELETE, uri);
                                activity.startActivityForResult(delIntent, MainActivity.REQUEST_INSTALL);
                            }
                        })
                        .setButtonDismiss("닫기")
                        .setUpdateFrom(UpdateFrom.GITHUB)
                        .setGitHubUserAndRepo("ahnlee4", "SmartCheck")
                        .setDisplay(Display.DIALOG)
                        .showAppUpdated(true)
                        .start();
            }
        });


        Button db_update = findViewById(R.id.db_update);
        db_version = findViewById(R.id.db_version);
        db_last_version = findViewById(R.id.db_last_version);

        db_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url_map = "https://github.com/ahnlee4/SmartCheck/raw/master/dblist/CROPMAP.txt";
                DownloadDB downloadDB_map = new DownloadDB(activity);
                downloadDB_map.execute(url_map, "CROPMAP.txt");

                String url_fert = "https://github.com/ahnlee4/SmartCheck/raw/master/dblist/CROPFERT.txt";
                DownloadDB downloadDB_fert = new DownloadDB(activity);
                downloadDB_fert.execute(url_fert, "CROPFERT.txt");

                String url_info = "https://github.com/ahnlee4/SmartCheck/raw/master/dblist/CROPINFO.txt";
                DownloadDB downloadDB_info = new DownloadDB(activity);
                downloadDB_info.execute(url_info, "CROPINFO.txt");

                Toast.makeText(activity, "다운로드 완료", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getPermission(Activity activity){
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WAKE_LOCK
                    },1000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_INSTALL) {
            progressBar.setVisibility(View.VISIBLE);
            String url = "https://github.com/ahnlee4/SmartCheck/raw/master/apklist/JinGu_"+lastversion.replace(".","_")+".apk";
            updateApp = new UpdateApp(activity);
            updateApp.execute(url);
        }else if (requestCode == REQUEST_UNINSTALL) {
            progressBar.setVisibility(View.GONE);
        }
    }


    public String getSibiPath() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/SIBI");

        if (!dir.exists()) {
            dir.mkdirs();
        }

        System.out.println(dir.getPath());

        return dir.getPath();
    }

}