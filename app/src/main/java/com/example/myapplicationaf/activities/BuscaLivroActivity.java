package com.example.myapplicationaf.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.myapplicationaf.BuildConfig;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplicationaf.R;
import com.example.myapplicationaf.adapters.LivroApiAdapter;
import com.example.myapplicationaf.model.GoogleBooksResponse;
import com.example.myapplicationaf.model.LivroApi;
import com.example.myapplicationaf.service.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * BuscaLivroActivity — tela de pesquisa de livros.
 *
 * Fluxo:
 *   1. Usuário digita query e toca em Buscar
 *   2. Verificamos conexão com internet
 *   3. Chamamos a Google Books API via Retrofit
 *   4. Exibimos os resultados no RecyclerView
 *   5. Clique em um livro → CadastroLivroActivity
 */
public class BuscaLivroActivity extends AppCompatActivity {

    /** Chave do Intent para passar o LivroApi selecionado. */
    public static final String EXTRA_LIVRO_API = "LIVRO_API_JSON";

    private static final int MAX_RESULTADOS = 20;

    // Views
    private TextInputEditText etBusca;
    private MaterialButton    btnBuscar;
    private ProgressBar       progressBar;
    private View              layoutEstado;
    private TextView          tvEstadoMensagem;
    private RecyclerView      recyclerResultados;

    // Adapter e lista
    private LivroApiAdapter   adapter;
    private final List<LivroApi> listaLivros = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busca_livro);

        vincularViews();
        configurarToolbar();
        configurarRecyclerView();
        configurarCampoBusca();
        configurarBotaoBuscar();
    }

    // ─── Configuração inicial ─────────────────────────────────────

    private void vincularViews() {
        etBusca           = findViewById(R.id.et_busca);
        btnBuscar         = findViewById(R.id.btn_buscar);
        progressBar       = findViewById(R.id.progress_bar);
        layoutEstado      = findViewById(R.id.layout_estado);
        tvEstadoMensagem  = findViewById(R.id.tv_estado_mensagem);
        recyclerResultados = findViewById(R.id.recycler_resultados);
    }

    private void configurarToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void configurarRecyclerView() {
        // Ao clicar num livro, serializa para JSON e abre CadastroLivroActivity
        adapter = new LivroApiAdapter(listaLivros, livro -> {
            Intent intent = new Intent(this, CadastroLivroActivity.class);
            intent.putExtra(EXTRA_LIVRO_API, new Gson().toJson(livro));
            startActivity(intent);
        });

        recyclerResultados.setLayoutManager(new LinearLayoutManager(this));
        recyclerResultados.setAdapter(adapter);
    }

    private void configurarCampoBusca() {
        // Permite buscar pressionando "Enter" no teclado
        etBusca.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                realizarBusca();
                return true;
            }
            return false;
        });
    }

    private void configurarBotaoBuscar() {
        btnBuscar.setOnClickListener(v -> realizarBusca());
    }

    // ─── Busca ────────────────────────────────────────────────────

    private void realizarBusca() {
        String query = etBusca.getText() != null
                ? etBusca.getText().toString().trim()
                : "";

        // Validação: campo vazio
        if (query.isEmpty()) {
            etBusca.setError(getString(R.string.erro_campo_vazio));
            return;
        }

        // Validação: sem internet
        if (!isInternetDisponivel()) {
            exibirEstado(getString(R.string.erro_sem_internet));
            return;
        }

        fecharTeclado();
        exibirCarregando(true);
        limparResultados();

        // Chamada HTTP assíncrona via Retrofit
        RetrofitClient.getInstance()
                .getService()
                .buscarLivros(query, MAX_RESULTADOS, BuildConfig.GOOGLE_BOOKS_API_KEY)
                .enqueue(new Callback<GoogleBooksResponse>() {

                    @Override
                    public void onResponse(Call<GoogleBooksResponse> call,
                                           Response<GoogleBooksResponse> response) {
                        exibirCarregando(false);

                        // Resposta com erro HTTP (4xx, 5xx)
                        if (!response.isSuccessful() || response.body() == null) {
                            exibirEstado(getString(R.string.erro_api_indisponivel));
                            return;
                        }

                        List<GoogleBooksResponse.Item> items =
                                response.body().getItems();

                        // Nenhum resultado encontrado
                        if (items == null || items.isEmpty()) {
                            exibirEstado(getString(R.string.nenhum_resultado)
                                    + " \"" + query + "\"");
                            return;
                        }

                        // Converte cada Item da API em LivroApi e exibe
                        for (GoogleBooksResponse.Item item : items) {
                            listaLivros.add(item.toLivroApi());
                        }
                        adapter.notifyDataSetChanged();
                        recyclerResultados.setVisibility(View.VISIBLE);
                        layoutEstado.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(Call<GoogleBooksResponse> call,
                                          Throwable t) {
                        exibirCarregando(false);
                        exibirEstado(getString(R.string.erro_chamada_api)
                                + ": " + t.getMessage());
                    }
                });
    }

    // ─── Estados da UI ────────────────────────────────────────────

    private void exibirCarregando(boolean carregando) {
        progressBar.setVisibility(carregando ? View.VISIBLE : View.GONE);
        btnBuscar.setEnabled(!carregando);
        layoutEstado.setVisibility(View.GONE);
        if (carregando) recyclerResultados.setVisibility(View.GONE);
    }

    private void exibirEstado(String mensagem) {
        layoutEstado.setVisibility(View.VISIBLE);
        tvEstadoMensagem.setText(mensagem);
        recyclerResultados.setVisibility(View.GONE);
    }

    private void limparResultados() {
        listaLivros.clear();
        adapter.notifyDataSetChanged();
    }

    // ─── Utilitários ──────────────────────────────────────────────

    private boolean isInternetDisponivel() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkCapabilities cap =
                cm.getNetworkCapabilities(cm.getActiveNetwork());
        return cap != null && (
                cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        cap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    private void fecharTeclado() {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(
                    getCurrentFocus().getWindowToken(), 0);
        }
    }
}