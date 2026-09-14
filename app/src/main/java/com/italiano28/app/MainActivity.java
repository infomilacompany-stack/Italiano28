package com.italiano28.app;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private WebView web;
    private TextToSpeech tts;
    private SpeechRecognizer recognizer;
    private boolean ttsReady = false;
    private String pendingText = null;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        web = new WebView(this);

        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setMediaPlaybackRequiresUserGesture(false);

        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient());
        web.setOverScrollMode(WebView.OVER_SCROLL_NEVER);

        web.addJavascriptInterface(new Bridge(), "Android");
        web.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        setContentView(web);

        web.loadUrl("file:///android_asset/index.html");
        web.getSettings().setTextZoom(100);

        inicializarTTS();
    }

    private void inicializarTTS() {

        ttsReady = false;

        tts = new TextToSpeech(this, status -> {

            if (status == TextToSpeech.SUCCESS) {

                int resultado = tts.setLanguage(Locale.ITALIAN);

                if (resultado == TextToSpeech.LANG_MISSING_DATA ||
                    resultado == TextToSpeech.LANG_NOT_SUPPORTED) {

                    ttsReady = false;

                    mostrarMensaje(
                        "No está instalada la voz italiana. Ve a Ajustes > Texto a voz y descarga italiano."
                    );

                } else {

                    ttsReady = true;

                    tts.setSpeechRate(0.85f);
                    tts.setPitch(1.0f);

                    if (pendingText != null) {
                        String texto = pendingText;
                        pendingText = null;
                        hablar(texto);
                    }
                }

            } else {

                ttsReady = false;

                mostrarMensaje(
                    "No se pudo iniciar el sistema de voz de Android."
                );
            }
        });
    }

    private void hablar(String texto) {

        if (tts == null || !ttsReady) {
            pendingText = texto;
            inicializarTTS();
            return;
        }

        int idioma = tts.setLanguage(Locale.ITALIAN);

        if (idioma == TextToSpeech.LANG_MISSING_DATA ||
            idioma == TextToSpeech.LANG_NOT_SUPPORTED) {

            mostrarMensaje(
                "La voz italiana no está disponible en este teléfono."
            );

            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            tts.speak(
                texto,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "italiano28"
            );

        } else {

            tts.speak(
                texto,
                TextToSpeech.QUEUE_FLUSH,
                null
            );
        }
    }

    private void mostrarMensaje(String mensaje) {

        if (web != null) {

            web.post(() -> {

                String js =
                    "alert(" +
                    jsQuote(mensaje) +
                    ")";

                web.evaluateJavascript(js, null);
            });
        }
    }

    public class Bridge {

       @JavascriptInterface
public void speak(String text) {

    mostrarMensaje("Recibí: " + text);

    if (text == null || text.trim().isEmpty()) {
        return;
    }

    hablar(text);
}
        @JavascriptInterface
        public void listen() {

            if (Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    44
                );

                return;
            }

            startListen();
        }
    }

    private void startListen() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            web.evaluateJavascript(
                "window.onSpeechResult('El reconocimiento de voz no está disponible en este teléfono.')",
                null
            );

            return;
        }

        if (recognizer != null) {
            recognizer.destroy();
        }

        recognizer =
            SpeechRecognizer.createSpeechRecognizer(this);

        recognizer.setRecognitionListener(
            new RecognitionListener() {

                public void onReadyForSpeech(Bundle b) {}

                public void onBeginningOfSpeech() {}

                public void onRmsChanged(float r) {}

                public void onBufferReceived(byte[] b) {}

                public void onEndOfSpeech() {}

                public void onError(int e) {

                    web.evaluateJavascript(
                        "window.onSpeechResult('')",
                        null
                    );
                }

                public void onPartialResults(Bundle b) {}

                public void onEvent(int a, Bundle b) {}

                public void onResults(Bundle b) {

                    ArrayList<String> r =
                        b.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        );

                    String x =
                        r != null && !r.isEmpty()
                        ? r.get(0)
                        : "";

                    web.evaluateJavascript(
                        "window.onSpeechResult(" +
                        jsQuote(x) +
                        ")",
                        null
                    );
                }
            }
        );

        Intent i =
            new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            );

        i.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            "it-IT"
        );

        i.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
            "it-IT"
        );

        i.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        i.putExtra(
            RecognizerIntent.EXTRA_MAX_RESULTS,
            3
        );

        recognizer.startListening(i);
    }

    private String jsQuote(String s) {

        return "'" +
            s.replace("\\", "\\\\")
             .replace("'", "\\'")
             .replace("\n", " ")
             .replace("\r", " ") +
            "'";
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode,
        String[] permissions,
        int[] grantResults
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        );

        if (
            requestCode == 44 &&
            grantResults.length > 0 &&
            grantResults[0] ==
                PackageManager.PERMISSION_GRANTED
        ) {

            startListen();
        }
    }

    @Override
    protected void onDestroy() {

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        if (recognizer != null) {
            recognizer.destroy();
        }

        super.onDestroy();
    }
}
