package com.sani.ai;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.*;
import android.graphics.Color;
import android.net.Uri;
import java.util.*;

public class MainActivity extends Activity {
    private static final int SPEECH_REQUEST = 501;
    private TextView status, transcript;
    private EditText commandBox;
    private TextToSpeech tts;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                int lang = tts.setLanguage(new Locale("bn", "BD"));
                if (lang == TextToSpeech.LANG_MISSING_DATA || lang == TextToSpeech.LANG_NOT_SUPPORTED) {
                    status.setText("বাংলা TTS সমর্থিত নয়; ফোনের Text-to-Speech ভাষা সেটিংস দেখুন।");
                }
            }
        });
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 32, 24, 24);
        root.setBackgroundColor(Color.rgb(246,248,252));

        TextView title = new TextView(this);
        title.setText("SANI AI");
        title.setTextSize(30);
        title.setTextColor(Color.rgb(30,45,90));
        title.setPadding(0,0,0,8);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("বাংলায় বলো — আমি সমর্থিত ফোনের কাজ করার চেষ্টা করব");
        subtitle.setTextSize(16);
        subtitle.setTextColor(Color.DKGRAY);
        root.addView(subtitle);

        status = new TextView(this);
        status.setText("প্রথমে Accessibility অনুমতি চালু করো।");
        status.setTextSize(14);
        status.setPadding(0,18,0,12);
        root.addView(status);

        Button mic = new Button(this);
        mic.setText("🎙 বাংলায় বলো");
        mic.setOnClickListener(v -> listen());
        root.addView(mic);

        commandBox = new EditText(this);
        commandBox.setHint("অথবা এখানে নির্দেশ লেখো");
        root.addView(commandBox);

        Button run = new Button(this);
        run.setText("নির্দেশ চালাও");
        run.setOnClickListener(v -> handleCommand(commandBox.getText().toString().trim()));
        root.addView(run);

        Button settings = new Button(this);
        settings.setText("স্ক্রিন নিয়ন্ত্রণ চালু করার সেটিংস");
        settings.setOnClickListener(v -> {
            try { startActivity(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
            catch (Exception e) { status.setText("Accessibility Settings খোলা যায়নি।"); }
        });
        root.addView(settings);

        transcript = new TextView(this);
        transcript.setText("উদাহরণ: YouTube খোলো | Chrome খোলো | https://example.com খোলো | স্ক্রল নিচে | পিছনে যাও | হোমে যাও");
        transcript.setTextSize(14);
        transcript.setTextColor(Color.DKGRAY);
        transcript.setPadding(0,20,0,0);
        root.addView(transcript);
        setContentView(root);
    }

    private void listen() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD");
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-BD");
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "বাংলায় নির্দেশ বলুন");
        try { startActivityForResult(i, SPEECH_REQUEST); }
        catch (Exception e) {
            status.setText("এই ফোনে Speech Recognition পাওয়া যায়নি। ইন্টারনেট বা Google অ্যাপের ভাষা সেটিংস পরীক্ষা করুন।");
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                commandBox.setText(results.get(0));
                handleCommand(results.get(0));
            }
        }
    }

    private void say(String message) {
        status.setText(message);
        if (tts != null) tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "sani_reply");
    }

    private void handleCommand(String raw) {
        if (raw == null || raw.trim().isEmpty()) { say("একটি নির্দেশ বলো বা লেখো।"); return; }
        String c = raw.trim().toLowerCase(new Locale("bn", "BD"));
        transcript.setText("তোমার নির্দেশ: " + raw);

        if (c.contains("ইউটিউব") || c.contains("youtube")) { launchPackage("com.google.android.youtube", "YouTube"); return; }
        if (c.contains("হোয়াটসঅ্যাপ") || c.contains("হোয়াটস অ্যাপ") || c.contains("whatsapp")) { launchPackage("com.whatsapp", "WhatsApp"); return; }
        if (c.contains("ক্রোম") || c.contains("chrome")) { launchPackage("com.android.chrome", "Chrome"); return; }
        if (c.contains("সেটিংস") || c.contains("settings")) {
            try { startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS)); say("সেটিংস খুলছি।"); }
            catch (Exception e) { say("সেটিংস খোলা যায়নি।"); }
            return;
        }
        if (c.contains("পিছনে") || c.contains("back")) {
            PhoneControlService.performBack();
            say("পিছনে যাওয়ার চেষ্টা করছি।");
            return;
        }
        if (c.contains("হোমে") || c.contains("হোম স্ক্রিন") || c.contains("home")) {
            PhoneControlService.performHome();
            say("হোম স্ক্রিনে যাওয়ার চেষ্টা করছি।");
            return;
        }
        if (c.contains("স্ক্রল নিচে") || c.contains("নিচে স্ক্রল")) {
            PhoneControlService.performScroll(true);
            say("নিচে স্ক্রল করার চেষ্টা করছি।");
            return;
        }
        if (c.contains("স্ক্রল উপরে") || c.contains("উপরে স্ক্রল")) {
            PhoneControlService.performScroll(false);
            say("উপরে স্ক্রল করার চেষ্টা করছি।");
            return;
        }
        String url = extractUrl(raw);
        if (url != null) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) url = "https://" + url;
            try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); say("ওয়েবসাইট খুলছি: " + url); }
            catch (Exception e) { say("ওয়েবসাইট খোলা যায়নি।"); }
            return;
        }
        if (c.startsWith("লিখে দাও ") || c.startsWith("টাইপ করো ")) {
            String text = raw.substring(raw.indexOf(' ') + 1).trim();
            if (PhoneControlService.typeText(text)) say("লেখাটি টাইপ করার চেষ্টা করেছি। আগে যেখানে লিখতে হবে সেখানে কার্সর রাখো।");
            else say("টাইপ করতে Accessibility চালু করো এবং কোনো লেখার ঘরে কার্সর রাখো।");
            return;
        }
        say("এই সংস্করণে নির্দেশটি এখনো যোগ করা হয়নি। আপাতত YouTube খোলো, WhatsApp খোলো, Chrome খোলো, ওয়েবসাইট খোলো, পিছনে যাও বা স্ক্রল বলো।");
    }

    private String extractUrl(String raw) {
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("((https?://)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/[\\\\S]*)?)")
            .matcher(raw);
        return m.find() ? m.group(1) : null;
    }

    private void launchPackage(String pkg, String label) {
        try {
            Intent launch = getPackageManager().getLaunchIntentForPackage(pkg);
            if (launch == null) { say(label + " অ্যাপটি ইনস্টল নেই।"); return; }
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launch);
            say(label + " খুলছি।");
        } catch (Exception e) { say(label + " খোলা যায়নি।"); }
    }

    @Override public void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
