package com.pengl.audiorecord.demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.pengl.record.OnRecordFixedListener;
import com.pengl.record.PLDialogRecord;

public class MainActivity extends AppCompatActivity {

    private TextView tv_filepath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_filepath = findViewById(R.id.tv_filepath);
    }

    public void onClickRecord(View view) {
        int hasPermission = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.RECORD_AUDIO);
        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            showDialogRecord();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
    }

    private void showDialogRecord() {
        PLDialogRecord dialog = new PLDialogRecord(this);
        dialog.setTitle("title");
        dialog.setContent("Here you can write some text such as hints");
        dialog.setOnRecordFixedListener(filePath -> tv_filepath.setText(filePath));
        dialog.show();
    }

    public void onClickWavToMp3(View view) {
        Toast.makeText(this, "look code", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showDialogRecord();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                    new AlertDialog.Builder(this)
                            .setMessage("give me permission to record audio pls")
                            .setPositiveButton("OK", (dialog1, which) ->
                                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1))
                            .setNegativeButton("Cancel", null)
                            .create()
                            .show();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}