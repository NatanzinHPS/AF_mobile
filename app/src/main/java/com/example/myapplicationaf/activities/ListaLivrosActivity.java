package com.example.myapplicationaf.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplicationaf.R;
import com.example.myapplicationaf.adapters.LivroSalvoAdapter;
import com.example.myapplicationaf.model.LivroSalvo;
import com.example.myapplicationaf.repository.FirebaseRepository;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * ListaLivrosActivity — exibe todos os livros salvos no Firestore.
 *
 * Clique curto  → abre DetalheEdicaoActivity (editar status/observação)
 * Clique longo  → AlertDialog de confirmação para excluir
 * SwipeRefresh  → recarrega a lista do Firestore
 */
public class ListaLivrosActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener {

    /** Chave do Intent para passar o LivroSalvo para DetalheEdicaoActivity. */
    public static final String EXTRA_LIVRO_SALVO = "LIVRO_SALVO_JSON";

    // Views
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar        progressBar;
    private LinearLayout       layoutEstado;
    private TextView           tvEstadoMensagem;
    private RecyclerView       recyclerLivros;

    // Adapter e lista
    private LivroSalvoAdapter    adapter;
    private final List<LivroSalvo> listaLivros = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_livros);

        vincularViews();
        configurarToolbar();
        configurarSwipeRefresh();
        configurarRecyclerView();
        carregarLivros();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarrega ao voltar de DetalheEdicaoActivity
        carregarLivros();
    }

    // ─── Configuração inicial ─────────────────────────────────────

    private void vincularViews() {
        swipeRefresh     = findViewById(R.id.swipe_refresh);
        progressBar      = findViewById(R.id.progress_bar);
        layoutEstado     = findViewById(R.id.layout_estado);
        tvEstadoMensagem = findViewById(R.id.tv_estado_mensagem);
        recyclerLivros   = findViewById(R.id.recycler_livros);
    }

    private void configurarToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void configurarSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
    }

    private void configurarRecyclerView() {
        adapter = new LivroSalvoAdapter(
                listaLivros,

                // Clique curto → DetalheEdicaoActivity
                livro -> {
                    Intent intent = new Intent(
                            this, DetalheEdicaoActivity.class);
                    intent.putExtra(
                            EXTRA_LIVRO_SALVO,
                            new Gson().toJson(livro));
                    startActivity(intent);
                },

                // Clique longo → confirmar exclusão
                livro -> {
                    confirmarExclusao(livro);
                    return true;
                }
        );

        recyclerLivros.setLayoutManager(new LinearLayoutManager(this));
        recyclerLivros.setAdapter(adapter);
    }

    // ─── Carregar dados ───────────────────────────────────────────

    private void carregarLivros() {
        exibirCarregando(true);

        FirebaseRepository.getInstance().listarLivros(
                livros -> {
                    exibirCarregando(false);
                    listaLivros.clear();
                    listaLivros.addAll(livros);
                    adapter.notifyDataSetChanged();
                    atualizarEstadoLista();
                },
                e -> {
                    exibirCarregando(false);
                    exibirEstado(getString(R.string.erro_carregar_lista)
                            + ": " + e.getMessage());
                });
    }

    // ─── Exclusão ─────────────────────────────────────────────────

    private void confirmarExclusao(LivroSalvo livro) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirmar_exclusao_titulo)
                .setMessage(getString(
                        R.string.confirmar_exclusao_msg,
                        livro.getTitulo()))
                .setPositiveButton(R.string.excluir,
                        (dialog, which) -> excluirLivro(livro))
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void excluirLivro(LivroSalvo livro) {
        FirebaseRepository.getInstance().excluirLivro(
                livro.getDocumentId(),
                () -> {
                    listaLivros.remove(livro);
                    adapter.notifyDataSetChanged();
                    atualizarEstadoLista();
                    Toast.makeText(this,
                            R.string.livro_excluido_sucesso,
                            Toast.LENGTH_SHORT).show();
                },
                e -> Toast.makeText(this,
                        getString(R.string.erro_excluir)
                                + ": " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
    }

    // ─── Estados da UI ────────────────────────────────────────────

    private void exibirCarregando(boolean carregando) {
        swipeRefresh.setRefreshing(false);
        progressBar.setVisibility(
                carregando ? View.VISIBLE : View.GONE);
        layoutEstado.setVisibility(View.GONE);
    }

    private void atualizarEstadoLista() {
        if (listaLivros.isEmpty()) {
            layoutEstado.setVisibility(View.VISIBLE);
            tvEstadoMensagem.setText(R.string.lista_vazia);
            recyclerLivros.setVisibility(View.GONE);
        } else {
            layoutEstado.setVisibility(View.GONE);
            recyclerLivros.setVisibility(View.VISIBLE);
        }

        // Atualiza subtítulo da toolbar com a contagem
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(
                    listaLivros.size() + " "
                            + getString(R.string.livros_salvos));
        }
    }

    private void exibirEstado(String mensagem) {
        progressBar.setVisibility(View.GONE);
        layoutEstado.setVisibility(View.VISIBLE);
        tvEstadoMensagem.setText(mensagem);
        recyclerLivros.setVisibility(View.GONE);
    }

    @Override
    public void onRefresh() {
        carregarLivros();
    }
}