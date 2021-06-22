package com.ppu.idvoice;

import java.util.Arrays;

public class AudioRecord {
    private byte[] samples;
    private int sampleRate;

    public AudioRecord(byte[] samples, int sampleRate) {
        this.samples = samples;
        this.sampleRate = sampleRate;
    }

    public byte[] getSamples() {
        return samples;
    }

    public void setSamples(byte[] samples) {
        this.samples = samples;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }
    @Override
    public String toString() {
        return "AudioData{" +
                "samples=" + Arrays.toString(samples) +
                ", sampleRate=" + sampleRate +
                '}';
    }
}
