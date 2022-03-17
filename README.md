# PLAudioRecord
[![](https://jitpack.io/v/xm3366336/PLAudioRecord.svg)](https://jitpack.io/#xm3366336/PLAudioRecord)

音频录制工具，主要两个功能点：
 * 1、直接录制生成mp3格式的音频；
 * 2、将wav格式的音频转换成mp3格式；

## 1、解决的问题
 * 1、使用官方组件AudioRecorder时，只能输出未处理的PCM语音数据；
 * 2、使用MediaRecorder，能封装，但只能输出amr,wav格式的音频。amr兼容不好，iOS播放麻烦死了，wav巨大无比。
so....只要简单的调用dialog，本库直接输出mp3格式音频，如有wav，可转成mp3。

## 2、Demo下载
 * [本地下载](https://github.com/xm3366336/PLAudioRecord/blob/main/app/release/app-release.apk)
 * [Google Play](https://play.google.com/store/apps/details?id=com.pengl.audiorecord.demo)

## 3、使用

> 1、引入
```
implementation 'com.github.xm3366336:PLAudioRecord:1.1.1'
```

> 2、调起录音窗口
```
PLDialogRecord dialog = new PLDialogRecord(this);
dialog.setTitle("标题");
dialog.setContent("这里可以写一些提示之类的文字");
dialog.setOnRecordFixedListener(filePath -> tv_filepath.setText(filePath));
dialog.show();
```
录音完毕后，根据OnRecordFixedListener监听完成事件。生成的mp3音频存储于沙盒的 files/Music/PLAudioRecord/ 目录下。
这样就可以了。如果你现有项目不好改动，可以继续往下看，将现在的wav转成mp3
注意：Demo中没有申请录音权限，请自行处理。

> 3、已有wav转成mp3格式
```
WavFileReader reader = new WavFileReader();
# 现有的wav文件路径
String filePathWav = "xxx/xxx/xxx.wav";
# 输出的mp3文件路径
String filePathMp3 = "xxx/xxx/xxx.mp3";
try {
	if (reader.openFile(filePathWav)) {
		WavFileHeader header = reader.getmWavFileHeader();
		LameUtil.convert(filePathWav, filePathMp3, header.getmSampleRate());
		Log.i("wav", "SampleRate：" + header.getmSampleRate());
		Log.i("wav", "NumChannel：" + header.getmNumChannel());
		Log.i("wav", "ByteRate：" + header.getmByteRate());
	}
} catch (IOException e) {
	e.printStackTrace();
}
```
调用`LameUtil.convert()`方法即可。

## 4、图示

 <img src="https://github.com/xm3366336/PLAudioRecord/blob/main/app/release/screenshot.jpg" width="300" height="667" align=center />

## 5、更新记录

> 1.1.1

Demo加入申请权限，去除不必要的日志打印

> 1.1.0

支持wav转mp3格式。

> 1.0.0

首个版本，支持音频录制，存储为mp3格式；
