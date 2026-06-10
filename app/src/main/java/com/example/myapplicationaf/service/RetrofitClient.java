package com.example.myapplicationaf.service;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

/**
 * Singleton que configura e fornece o cliente Retrofit.
 *
 * Uso em qualquer Activity:
 *   GoogleBooksApiService service = RetrofitClient.getInstance().getService();
 *   service.buscarLivros("Dom Casmurro", 20).enqueue(callback);
 *
 * Singleton garante que apenas um cliente HTTP existe durante
 * toda a vida do app — economiza memória e reutiliza conexões.
 */
public class RetrofitClient {

    private static final String BASE_URL        = "https://www.googleapis.com/books/v1/";
    private static final int    TIMEOUT_SEGUNDOS = 15;

    private static RetrofitClient instance;
    private final Retrofit retrofit;

    private RetrofitClient() {

        // 1. Interceptor de log: exibe no Logcat a URL, headers e body de cada requisição e resposta
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // 2. Cliente HTTP com timeouts configurados. connectTimeout: tempo máximo para abrir a conexão TCP - readTimeout: tempo máximo aguardando o servidor responder - writeTimeout: tempo máximo para enviar o corpo da requisição
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();

        // 3. Retrofit com conversor Gson, GsonConverterFactory desserializa o JSON automaticamente para os tipos definidos em GoogleBooksResponse
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
    /**
     * Retorna a instância única — thread-safe com double-checked locking.
     */
    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }
    /**
     * Retorna a implementação gerada pelo Retrofit da interface
     * GoogleBooksApiService, pronta para fazer chamadas HTTP.
     */
    public GoogleBooksApiService getService() {
        return retrofit.create(GoogleBooksApiService.class);
    }
}