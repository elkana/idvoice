package com.ppu.idvoice;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;
import com.ppu.idvoice.dialogs.OnStopRecording;
import com.ppu.idvoice.dialogs.RecordDialog;
import com.ppu.idvoice.utils.EngineManager;

import net.idrnd.voicesdk.verify.VerifyResult;
import net.idrnd.voicesdk.core.common.VoiceTemplate;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import io.flutter.embedding.android.FlutterFragmentActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class MainActivity extends FlutterFragmentActivity {

    private static final String CHANNEL = "flutter.my.channel";
    private MethodChannel channel;
    private final String TAG = "idvoice.native";
    // private VoiceTemplate[] voices = new VoiceTemplate[3];

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine);

        channel = new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL);

        channel.setMethodCallHandler((call, result) -> {

            Log.w(TAG, "RECEIVE method=" + call.method);
            // Note: this method is invoked on the main thread.
            if (call.method.equals("hello")) {
                result.success("Hallo Juga");
                // result.error("UNAVAILABLE", "Battery level not available.", null);
            } else if (call.method.equals("toast")) {
                Toast.makeText(getApplicationContext(), "Flutter Toast", Toast.LENGTH_LONG).show();
                result.success("ok");
            } else if (call.method.equals("enroll")) {
                String fileName = call.argument("file");
                String groupName = call.argument("group");

                File file = new File(fileName);

                result.success("ok");

                // Toast.makeText(getApplicationContext(), "Face " + groupName + " registered",
                // Toast.LENGTH_SHORT)
                // .show();
            } else if (call.method.equals("idvoice.clear")) {
                DataUtil.clearAllEnroll(getApplicationContext());

                result.success("ok");
            } else if (call.method.equals("idvoice.group")) {
                String groupName = call.argument("group");
                List<TrnEnroll> list = DataUtil.getEnrollList(getApplicationContext(), groupName);
                Gson gson = new Gson();
                String json = gson.toJson(list);
                result.success(json);
            } else if (call.method.equals("idvoice.enroll.voice")) {
                String group = call.argument("group");

                // 1) Init antispoofing and verification engines
                EngineManager engine = EngineManager.getInstance();
                engine.init(this);

                showEnrollDialog(engine, group);

                // Intent i = new Intent(getApplicationContext(), ActivityLive.class);
                // i.putExtra(ActivityLive.PARAM_SCORE, minScore);
                // startActivityForResult(i, ActivityLive.REQUEST_LIVE);

                Log.e(TAG, " startActivityForResult(i, REQUEST_LIVE);");
                result.success("...");
            } else if (call.method.equals("idvoice.audio.live")) {
                String score = call.argument("score.minimum");

                float minScore = score == null || score.trim().length() < 1 || score.equals("null") ? 0.7f
                        : Float.parseFloat(score);

                // verify
                // 1) Init antispoofing and verification engines
                EngineManager engine = EngineManager.getInstance();
                engine.init(this);

                showVerifyDialog(engine, result);

            } else if (call.method.equals("idvoice.merge.voice")) {
                String group = call.argument("group");

                List<TrnEnroll> list = DataUtil.getEnrollList(getApplicationContext(), group);

                VoiceTemplate[] voices = new VoiceTemplate[list.size()];

                for (int i = 0; i < list.size(); i++) {
                    voices[i] = VoiceTemplate.loadFromFile(list.get(i).getFileName());

                    // delete
                    if (DataUtil.deleteFromDB(getApplicationContext(), list.get(i).getUid())) {
                        Log.e(TAG, "delete " + list.get(i).getGroupName() + " file:" + list.get(i).getFileName());
                        new File(list.get(i).getFileName()).delete();
                    }
                }

                try {
                    File cacheFile = createTempAudioFile();

                    EngineManager.getInstance().voiceTemplateFactory.mergeVoiceTemplates(voices)
                            .saveToFile(cacheFile.getPath());

                    DataUtil.saveToDB(getApplicationContext(), new File(cacheFile.getPath()), group + "-merged", group);

                    result.success("...");

                } catch (IOException e) {
                    e.printStackTrace();
                    result.error("500", e.getMessage(), null);
                }

            } else {
                result.notImplemented();

                Toast.makeText(getApplicationContext(), "Not Implemented", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showEnrollDialog(final EngineManager engine, final String groupName) {
        FragmentManager fm = getSupportFragmentManager();
        RecordDialog editNameDialogFragment = RecordDialog.newInstance("Some Title");

        editNameDialogFragment.setOnStopListener(new OnStopRecording() {
            @Override
            public void onStop(AudioRecord recordObject) {
                // Create voice template and store it in array
                VoiceTemplate voice = engine.voiceTemplateFactory.createVoiceTemplate(recordObject.getSamples(),
                        recordObject.getSampleRate());

                TrnEnroll row = null;
                try {
                    row = saveUser(voice, groupName);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (row == null)
                    return;

                String json = new Gson().toJson(row);
                channel.invokeMethod("idvoice.add.result", json);
            }
        });

        editNameDialogFragment.show(fm, "fragment_edit_name");
    }

    private void showVerifyDialog(final EngineManager engine, final MethodChannel.Result result) {
        FragmentManager fm = getSupportFragmentManager();
        RecordDialog editNameDialogFragment = RecordDialog.newInstance("Some Title");

        editNameDialogFragment.setOnStopListener(new OnStopRecording() {
            @Override
            public void onStop(AudioRecord recordObject) {

                // 1) Get engines
                EngineManager engineManager = EngineManager.getInstance();

                // 2.1) Create voice template from verification audio
                VoiceTemplate verifyTemplate = engineManager.voiceTemplateFactory
                        .createVoiceTemplate(recordObject.getSamples(), recordObject.getSampleRate());

                // 2.2) Retrieve enrollment voice template from shared preferences
                List<TrnEnroll> list = DataUtil.getAllEnroll(getApplicationContext());

                float max = 0;
                TrnEnroll mostMatch = null;
                for (int i = 0; i < list.size(); i++) {
                    TrnEnroll e = list.get(i);

                    if (e.getFileName() == null || e.getFileName().length() < 1)
                        continue;

                    VoiceTemplate _audioFile = VoiceTemplate.loadFromFile(e.getFileName());

                    VerifyResult verificationResult = engineManager.voiceTemplateMatcher.matchVoiceTemplates(_audioFile,
                            verifyTemplate);

                    Log.e(TAG, "Scan " + (i + 1) + "/" + list.size() + ":verResult="
                            + verificationResult.getProbability() + "/" + max + " -> " + e);

                    if (verificationResult.getProbability() > max) {
                        max = verificationResult.getProbability();
                        mostMatch = e;
                    }
                }

                Log.e(TAG, "Finished.mostMatch = " + mostMatch + ",max is " + max + "(" + (max * 100) + ")");

                if ((max * 100) < 50) {
                    mostMatch = null;
                }

                if (mostMatch == null) {
                    channel.invokeMethod("idvoice.audio.live.result", "no match");
                    Log.e(TAG, "No Match");
                    result.success("No Match");
                } else {
                    String ret = "Hello " + mostMatch.getFullName() + "(" + (max * 100) + ")";

                    // suka ga kepanggil di awal2, jd buat lagi
//                     channel.invokeMethod("idvoice.audio.live.result", result);

                    result.success(ret);
                }

            }
        });

        editNameDialogFragment.show(fm, "fragment_edit_name");
    }

    private File createTempAudioFile() throws IOException {
        File cacheFile = File.createTempFile("tmp", ".audio");
        // clean up the last one
        if (cacheFile.exists())
            cacheFile.delete();

        // must create file first
        cacheFile.getParentFile().mkdirs();
        cacheFile.createNewFile();

        return cacheFile;
    }

    private TrnEnroll saveUser(VoiceTemplate voice, String groupName) throws IOException {
        File cacheFile = createTempAudioFile();

        Log.i(TAG, "saving to " + cacheFile.getPath());

        voice.saveToFile(cacheFile.getPath());

        return DataUtil.saveToDB(getApplicationContext(), cacheFile, groupName, groupName);
    }
}
