package com.example.myapplicationaf;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;

/**
 * Classe Application personalizada.
 * Inicializa o Firebase assim que o processo do app sobe.
 *
 * Declarada no AndroidManifest com android:name=".BibliotecaApp"
 */
public class BibliotecaApp extends Application {

    private static final String TAG = "BibliotecaApp";

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        Log.d(TAG, "Firebase inicializado.");
    }
}
