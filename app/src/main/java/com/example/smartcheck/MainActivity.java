package com.example.smartcheck;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Activity activity;
    UpdateApp updateApp;

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
            String url = "https://github.com/ahnlee4/SmartCheck/raw/master/app-debug.apk";
            updateApp = new UpdateApp(activity);
            updateApp.execute(url);
        }
    }
}