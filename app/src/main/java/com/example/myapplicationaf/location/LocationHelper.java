package com.example.myapplicationaf.location;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Auxiliar para captura de localização GPS.
 *
 * Fluxo de uso na Activity:
 *   1. LocationHelper.hasPermissao(this)        → verifica se já tem permissão
 *   2. LocationHelper.solicitarPermissao(this)  → abre o dialog do sistema
 *   3. locationHelper.obterLocalizacao(this, callback) → captura lat/lng
 *   4. locationHelper.pararAtualizacoes()       → chama no onDestroy()
 */
public class LocationHelper {

    private static final String TAG = "LocationHelper";

    /** Código usado em onRequestPermissionsResult() da Activity. */
    public static final int REQUEST_CODE_LOCALIZACAO = 1001;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    /**
     * Verifica se o app já tem permissão de localização concedida.
     * Chamar antes de qualquer operação GPS.
     */
    public static boolean hasPermissao(Context context) {
        return ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                ||
                ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Abre o dialog do sistema pedindo permissão de localização.
     * O resultado chega em onRequestPermissionsResult() da Activity.
     */
    public static void solicitarPermissao(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                REQUEST_CODE_LOCALIZACAO
        );
    }

    /**
     * Obtém a localização atual em dois passos:
     *
     * 1. Tenta o último local conhecido (getLastLocation) — instantâneo,
     *    não liga o GPS, usa cache do sistema.
     *
     * 2. Se o cache estiver vazio (celular reiniciado, GPS nunca usado),
     *    solicita uma atualização única de alta precisão.
     *
     * O callback recebe o resultado em onLocationResult().
     */
    public void obterLocalizacao(Context context, LocationCallback callback) {
        if (!hasPermissao(context)) {
            Log.w(TAG, "obterLocalizacao chamado sem permissão.");
            return;
        }

        fusedLocationClient  = LocationServices.getFusedLocationProviderClient(context);
        locationCallback     = callback;

        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        // Tenta o cache primeiro
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        // Cache disponível: entrega imediatamente
                        LocationResult result = LocationResult.create(
                                java.util.Collections.singletonList(location));
                        callback.onLocationResult(result);
                    } else {
                        // Cache vazio: solicita atualização real do GPS
                        Log.d(TAG, "Último local nulo — solicitando atualização.");
                        solicitarAtualizacaoUnica(context);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Falha no getLastLocation: " + e.getMessage());
                    solicitarAtualizacaoUnica(context);
                });
    }

    /**
     * Solicita exatamente uma atualização de localização de alta precisão.
     * Usado como fallback quando o cache está vazio.
     */
    private void solicitarAtualizacaoUnica(Context context) {
        // setMaxUpdates(1) garante que o GPS para após a primeira leitura
        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 0)
                .setMaxUpdates(1)
                .setWaitForAccurateLocation(false)
                .build();

        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()   // callback na thread principal (UI)
        );
    }

    /**
     * Para as atualizações de localização.
     * SEMPRE chamar no onDestroy() da Activity para evitar leak de memória.
     */
    public void pararAtualizacoes() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            Log.d(TAG, "Atualizações de localização interrompidas.");
        }
    }

    /**
     * Converte latitude/longitude em endereço legível.
     *
     * ATENÇÃO: executa de forma síncrona — chamar sempre em
     * background thread (AsyncTask, Thread, Executor).
     * Em CadastroLivroActivity usamos um AsyncTask para isso.
     *
     * Retorna null se o Geocoder não estiver disponível ou falhar.
     *
     * Exemplo de retorno: "Av. Paulista, Bela Vista - São Paulo/SP"
     */
    public static String obterEnderecoAproximado(Context context,
                                                 double latitude,
                                                 double longitude) {
        if (!Geocoder.isPresent()) return null;

        Geocoder geocoder = new Geocoder(context, new Locale("pt", "BR"));
        try {
            List<Address> addresses =
                    geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses == null || addresses.isEmpty()) return null;

            Address addr = addresses.get(0);
            StringBuilder sb = new StringBuilder();

            if (addr.getThoroughfare() != null)
                sb.append(addr.getThoroughfare());

            if (addr.getSubLocality() != null) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(addr.getSubLocality());
            }

            if (addr.getLocality() != null) {
                if (sb.length() > 0) sb.append(" - ");
                sb.append(addr.getLocality());
            }

            if (addr.getAdminArea() != null) {
                if (sb.length() > 0) sb.append("/");
                sb.append(addr.getAdminArea());
            }

            return sb.length() > 0 ? sb.toString() : "Localização obtida";

        } catch (IOException e) {
            Log.e(TAG, "Erro no Geocoder: " + e.getMessage());
            return null;
        }
    }
}