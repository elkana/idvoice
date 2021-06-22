package com.ppu.idvoice.dialogs;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ppu.idvoice.AudioRecord;
import com.ppu.idvoice.utils.AudioRecorder;
import com.skyfishjy.library.RippleBackground;

import java.util.concurrent.atomic.AtomicBoolean;

import com.ppu.idvoice.R;

public class RecordDialog extends DialogFragment {

    private AudioRecorder recorder;
    private OnStopRecording listener;
    private RippleBackground rippleBackground;
    private AtomicBoolean isStop = new AtomicBoolean(false);

    public void setOnStopListener(OnStopRecording listener) {
        this.listener = listener;
    }

    public static RecordDialog newInstance(String title) {
        RecordDialog frag = new RecordDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_record, container, false);
        recorder = new AudioRecorder();

        recorder.setOnStopRecoding(new OnStopRecording() {
            @Override
            public void onStop(AudioRecord recordObject) {
                if (recordObject != null) {
                    onStopRecord(recordObject);
                }
            }
        });

        final TextView speechLengthView = rootView.findViewById(R.id.speech_length);

        recorder.setOnSpeechLengthUpdate(new OnSpeechLengthUpdate() {
            @Override
            public void onSpeechLengthUpdate(final float speechLength) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        speechLengthView.setText((speechLength / 1000) + "s");
                    }
                });

            }
        });

        rippleBackground = rootView.findViewById(R.id.img);

        rootView.findViewById(R.id.stopBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioRecord recordObject = recorder.stopRecording();
                if (recordObject != null) {
                    onStopRecord(recordObject);
                }
            }
        });
        return rootView;
    }

    private void onStopRecord(final AudioRecord recordObject) {
        if (getActivity() == null) return;
        if (!isStop.getAndSet(true)) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                if (getDialog() != null) {
                    getDialog().dismiss();
                }

                if (listener != null) {
                    listener.onStop(recordObject);
                }
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        rippleBackground.startRippleAnimation();
        recorder.startRecording();
    }

    @Override
    public void onStop() {
        super.onStop();
        AudioRecord recordObject = recorder.stopRecording();
        if (recordObject != null) {
            onStopRecord(recordObject);
        }
    }
}
