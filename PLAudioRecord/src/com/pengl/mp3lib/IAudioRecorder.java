package com.pengl.mp3lib;

/**
 * 接口回调
 */
public interface IAudioRecorder {

    /**
     * 设置回调
     *
     * @param audioListener 回调接口
     */
    void setAudioListener(AudioRecordListener audioListener);

    /**
     * 开始录制音频
     *
     * @param path 录制文件的地址
     */
    void start(String path);

    /**
     * 暂停录制
     */
    void onPause();

    /**
     * 继续录制
     */
    void onResume();

    /**
     * 停止录制
     *
     * @return 返回录制的时长 ，millisecond
     */
    long stop();
}
