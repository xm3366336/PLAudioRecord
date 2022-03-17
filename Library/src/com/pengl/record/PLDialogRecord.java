package com.pengl.record;

import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;

import com.pengl.mp3lib.IAudioRecorder;
import com.pengl.mp3lib.Mp3Recorder;

import java.io.File;
import java.io.IOException;

/**
 * 录音窗口
 */
public class PLDialogRecord extends Dialog {

    private final AppCompatButton btn_read;
    private final SpectrumView mSpectrumView;
    private final ImageView ic_head, ic_progress;
    private final View progress;
    private final TextView tv_title, tv_content, tv_len;
    private final SendView mSendView;

    private IAudioRecorder mRecorder;
    private String filePath;
    private OnRecordFixedListener mOnRecordFixedListener;
    private MediaPlayer mediaPlayer;

    public PLDialogRecord(Context context) {
        super(context, R.style.AppDialogTransBg);
        setContentView(R.layout.dialog_record);

        btn_read = findViewById(R.id.btn_read);
        ic_progress = findViewById(R.id.ic_progress);
        progress = findViewById(R.id.progress);
        mSpectrumView = findViewById(R.id.mSpectrumView);
        tv_title = findViewById(R.id.tv_title);
        tv_content = findViewById(R.id.tv_content);
        ic_head = findViewById(R.id.ic_head);
        tv_len = findViewById(R.id.tv_len);
        mSendView = findViewById(R.id.mSendView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;
        getWindow().setAttributes(lp);

        RecorderView mRecorderView = findViewById(R.id.mRecorderView);
        mRecorderView.setRecorderViewListener(new RecorderView.RecorderViewListener() {
            @Override
            public void onStart() {

                if (TextUtils.isEmpty(filePath)) {
                    createFilePath();
                }

                vibrate();
                btn_read.setVisibility(View.INVISIBLE);
                ic_head.setVisibility(View.INVISIBLE);
                tv_len.setVisibility(View.GONE);
                ic_progress.setVisibility(View.INVISIBLE);
                progress.setVisibility(View.INVISIBLE);
                mSpectrumView.setVisibility(View.VISIBLE);
                mSpectrumView.start();

                mRecorder.start(filePath);
            }

            @Override
            public void onStop() {
                mSpectrumView.stop();
                mSpectrumView.setVisibility(View.INVISIBLE);

                long duration = mRecorder.stop();
                int sec = (int) (duration / 1000);
                if (sec < 1) {
                    File f = new File(filePath);
                    if (f.exists()) {
                        f.delete();
                    }

                    Toast.makeText(getContext(), "太短了，重录吧", Toast.LENGTH_SHORT).show();
                    return;
                }

                tv_len.setText(Utils.secToTime(sec));
                setChatBgSize(sec);

                btn_read.setVisibility(View.VISIBLE);
                ic_head.setVisibility(View.VISIBLE);
                tv_len.setVisibility(View.VISIBLE);
                ic_progress.setVisibility(View.VISIBLE);
                progress.setVisibility(View.INVISIBLE);

                mRecorderView.setVisibility(View.INVISIBLE);
                mSendView.startAnim();
            }
        });

        findViewById(R.id.btn_close).setOnClickListener(view -> dismiss());
        ic_head.setImageResource(R.mipmap.ic_head_women);

        tv_len.setOnClickListener(view -> {
            if (isPlay()) {
                stopPlay();
            } else {
                startPlay();
            }
        });

        btn_read.setOnClickListener(view -> {
            if (isPlay()) {
                stopPlay();
            } else {
                startPlay();
            }
        });

        mSendView.backLayout.setOnClickListener(v -> {
            createFilePath();
            mSendView.stopAnim();
            mRecorderView.setVisibility(View.VISIBLE);

            btn_read.setVisibility(View.INVISIBLE);
            ic_head.setVisibility(View.INVISIBLE);
            tv_len.setVisibility(View.GONE);
            ic_progress.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.INVISIBLE);
        });
        mSendView.selectLayout.setOnClickListener(v -> {
            if (null != mOnRecordFixedListener) {
                mOnRecordFixedListener.getFilePath(filePath);
            }
            dismiss();
        });

        mRecorder = new Mp3Recorder();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setVolume(0.8f, 0.8f);
        mediaPlayer.setLooping(false);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onStop() {
        mRecorder.stop();
        release();
        super.onStop();
    }

    /**
     * 传入文件路径
     *
     * @param filePath 文件路径
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;

        // 如果文件已存在
        if (new File(filePath).exists()) {
            btn_read.setVisibility(View.VISIBLE);
            ic_head.setVisibility(View.VISIBLE);
            ic_progress.setVisibility(View.VISIBLE);
            tv_len.setText("点击播放");
            tv_len.setVisibility(View.VISIBLE);
            progress.setVisibility(View.INVISIBLE);
            mSpectrumView.setVisibility(View.INVISIBLE);
        }
    }

    public void setTitle(String title) {
        tv_title.setText(title);
    }

    public void setContent(String content) {
        tv_content.setText(content);
    }

    public void setOnRecordFixedListener(OnRecordFixedListener mOnRecordFixedListener) {
        this.mOnRecordFixedListener = mOnRecordFixedListener;
    }

    /**
     * 创建文件路径
     */
    private void createFilePath() {
        String destDir = getContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC).getPath() + "/PLAudioRecord";

        File f = new File(destDir);
        if (!f.exists()) {
            f.mkdirs();
        }

        filePath = destDir + "/" + Utils.yyyyMMddHHmmss() + "_" + Utils.getRandom(4) + ".mp3";
    }

    /**
     * 根据秒数显示大小
     * 长度是 60 - 240 之间
     *
     * @param len 时长，秒数
     */
    private void setChatBgSize(int len) {
        ViewGroup.LayoutParams params = btn_read.getLayoutParams();
        if (len > 60) {
            params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 240, getContext().getResources().getDisplayMetrics());
        } else {
            params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60 + len * 3, getContext().getResources().getDisplayMetrics());
        }
        btn_read.setLayoutParams(params);
    }

    /**
     * 开始播放
     */
    private void startPlay() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setVolume(0.8f, 0.8f);
        mediaPlayer.setLooping(false);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mediaPlayer.setDataSource(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnPreparedListener(MediaPlayer::start);
        mediaPlayer.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.start();

            if (TextUtils.equals(tv_len.getText().toString(), "点击播放")) {
                int sec = mediaPlayer.getDuration() / 1000;
                tv_len.setText(Utils.secToTime(sec));
                setChatBgSize(sec);
            }
        });
        mediaPlayer.setOnCompletionListener(mp -> stopPlay());

        ic_progress.setVisibility(View.INVISIBLE);
        progress.setVisibility(View.VISIBLE);
    }

    private void stopPlay() {
        if (null != mediaPlayer) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        ic_progress.setVisibility(View.VISIBLE);
        progress.setVisibility(View.INVISIBLE);
    }

    private boolean isPlay() {
        if (null == mediaPlayer) {
            return false;
        }

        boolean isPlay = false;
        try {
            isPlay = mediaPlayer.isPlaying();
        } catch (Exception ignored) {
        }
        return isPlay;
    }

    private void release() {
        if (null != mediaPlayer) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);
        if (null == v) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(60);
        }
    }
}
