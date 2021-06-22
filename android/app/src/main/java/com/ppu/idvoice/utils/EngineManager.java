package com.ppu.idvoice.utils;

import android.content.Context;

import net.idrnd.android.media.AssetsExtractor;
import net.idrnd.voicesdk.media.SpeechSummaryEngine;
import net.idrnd.voicesdk.verify.VoiceTemplateFactory;
import net.idrnd.voicesdk.verify.VoiceTemplateMatcher;

import java.io.File;

/*
 * Singleton engine manager
 */
public class EngineManager {
    public VoiceTemplateFactory voiceTemplateFactory;
    public VoiceTemplateMatcher voiceTemplateMatcher;
    public SpeechSummaryEngine speechSummaryEngine;

    private static EngineManager instance;

    private EngineManager() {}

    public static EngineManager getInstance() {
        if (instance == null) {
            instance = new EngineManager();
        }

        return instance;
    }

    public void init(Context context) {
        // 1) If init data was not extracted to external dir, extract it
        // this skips extraction if it was already done for this version
        AssetsExtractor assetsExtractor = new AssetsExtractor(context);
        File assetsDir = assetsExtractor.extractAssets();

        // 1.1) Retrieve init data path in external dir
        String initDataPath = assetsDir.getPath();

        // 2.1) Init voice verification engine for text-dependent mode
        String verifyInitDataPath = new File(initDataPath, AssetsExtractor.VERIFY_INIT_DATA_TD_SUBPATH).getPath();
        voiceTemplateFactory = new VoiceTemplateFactory(verifyInitDataPath);
        voiceTemplateMatcher = new VoiceTemplateMatcher(verifyInitDataPath);

        // 2.3) Init speech summary engine
        speechSummaryEngine = new SpeechSummaryEngine(
                new File(initDataPath, AssetsExtractor.SPEECH_SUMMARY_INIT_DATA_SUBPATH).getPath());
    }
}
