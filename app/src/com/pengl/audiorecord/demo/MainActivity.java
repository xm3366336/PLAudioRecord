package com.pengl.audiorecord.demo;

import androidx.appcompat.app.AppCompatActivity;

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
        PLDialogRecord dialog = new PLDialogRecord(this);
        dialog.setTitle("标题");
        dialog.setContent("这里可以写一些提示之类的文字");
        dialog.setOnRecordFixedListener(filePath -> tv_filepath.setText(filePath));
        dialog.show();
    }

    public void onClickWavToMp3(View view) {
        Toast.makeText(this, "look code", Toast.LENGTH_SHORT).show();
    }

}