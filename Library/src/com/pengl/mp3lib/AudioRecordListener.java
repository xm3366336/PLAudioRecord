package com.pengl.mp3lib;

/**
 * 接口：获取录制音量的大小
 */
public interface AudioRecordListener {
    /**
     * 获取录制音量的大小
     *
     * @param volume 大小
     */
    void onGetVolume(int volume);
}
