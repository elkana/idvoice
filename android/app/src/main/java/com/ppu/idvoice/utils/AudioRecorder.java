package com.ppu.idvoice.utils;

import android.media.AudioFormat;
import android.media.MediaRecorder;

import com.ppu.idvoice.AudioRecord;
import com.ppu.idvoice.dialogs.OnSpeechLengthUpdate;
import com.ppu.idvoice.dialogs.OnStopRecording;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import net.idrnd.voicesdk.media.SpeechSummaryEngine;
import net.idrnd.voicesdk.media.SpeechSummaryStream;

public class AudioRecorder {
    private enum Status {
        IDLE,
        WORKING
    }

    private static final int RECORDER_SAMPLERATE = 48000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private final SpeechSummaryStream speechSummaryStream;
    private android.media.AudioRecord recorder;
    private int bufferSize;
    private Thread recordingThread;
    private List<byte[]> audioData;
    private Status status = Status.IDLE;
    private OnStopRecording onStopRecording;
    private OnSpeechLengthUpdate onSpeechLengthUpdate;
    private byte[] buffer;

    private static final float minSpeechLength = 800f;
    private static final float maxSilenceLength = 500f;

    public AudioRecorder() {
        // 1) Create speech summary stream with speech summary engine
        speechSummaryStream = EngineManager.getInstance().speechSummaryEngine.createStream(RECORDER_SAMPLERATE);

        // 2) Set buffer size
        bufferSize = android.media.AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING) * 3;

        // 3) Check if desired buffer size was too big for hardware and decrease it if necessary
        if (bufferSize < 0) {
            bufferSize = 4000;
        }

        // 4) Allocate memory for buffer
        buffer = new byte[bufferSize];

        // 5) Init Android audio recorder
        recorder = new android.media.AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, bufferSize);

        audioData = new ArrayList<>();
    }

    public void startRecording() {
        if (status == Status.IDLE) {
            recorder.startRecording();

            status = Status.WORKING;

            // 1) Run buffer callback in background thread
            recordingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    bufferCallback();
                }
            }, "AudioRecorder Thread");

            // 2) Reset stateful speech summary stream
            speechSummaryStream.reset();

            recordingThread.start();
        }
    }

    private void bufferCallback() {
        int read;

        while (status == Status.WORKING) {
            // 1) Read audio buffer from recorder
            read = recorder.read(buffer, 0, bufferSize);

            if (read != android.media.AudioRecord.ERROR_INVALID_OPERATION) {
                // 2.1) Collect audio samples
                audioData.add(buffer.clone());

                // 2.2) Add audio samples to speech summary stream
                speechSummaryStream.addSamples(buffer);

                // 2.3) Retrieve speech summary
                float speechLength = speechSummaryStream.getTotalSpeechInfo().getSpeechLengthMs();
                float currentBackgroundLength = speechSummaryStream.getCurrentBackgroundLength();

                // 2.4) Invoke speech length update listener (it updates UI)
                onSpeechLengthUpdate.onSpeechLengthUpdate(speechLength);

                // 2.5) Check if user stopped talking
                if (speechLength > minSpeechLength && currentBackgroundLength > maxSilenceLength) {
                    onStopRecording.onStop(stopRecording());
                }
            }
        }
    }

    public void setOnStopRecoding(OnStopRecording listener) {
        onStopRecording = listener;
    }

    public void setOnSpeechLengthUpdate(OnSpeechLengthUpdate listener) {
        onSpeechLengthUpdate = listener;
    }

    synchronized public AudioRecord stopRecording() {
        if (status == Status.WORKING) {

            status = Status.IDLE;

            try {
                recorder.stop();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }

            recordingThread = null;

            // Convert ArrayList of audio buffers to raw array
            byte[] audioDataArray = new byte[audioData.size() * bufferSize];

            for (int i = 0; i < audioData.size(); i++) {
                System.arraycopy(
                        audioData.get(i),
                        0,
                        audioDataArray,
                        i * bufferSize,
                        bufferSize
                );
            }

            return new AudioRecord(
                    audioDataArray,
                    RECORDER_SAMPLERATE);
        } else {
            return null;
        }
    }
}