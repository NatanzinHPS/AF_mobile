package com.example.myapplicationaf.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplicationaf.R;
import com.example.myapplicationaf.model.LivroSalvo;
import com.example.myapplicationaf.repository.FirebaseRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

/**
 * DetalheEdicaoActivity — visualização e edição de um livro salvo.
 *
 * Recebe o LivroSalvo via Intent (JSON serializado pela ListaLivrosActivity).
 *
 * Exibe (somente leitura):
 *   - Capa, título, autores, editora/ano
 *   - Coordenadas GPS e endereço aproximado
 *
 * Permite editar:
 *   - Situação (Spinner)
 *   - Status de leitura (Spinner)
 *   - Observação pessoal (EditText)
 *
 * Ao salvar, chama FirebaseRepository.atualizarLivro() e volta
 * para a ListaLivrosActivity, que recarrega no onResume().
 */
public class DetalheEdicaoActivity extends AppCompatActivity {

    // Views — somente leitura
    private ImageView ivCapa;
    private TextView  tvTitulo;
    private TextView  tvAutores;
    private TextView  tvEditoraAno;
    private TextView  tvCoordenadas;
    private TextView  tvEndereco;

    // Views — editáveis
    private Spinner           spinnerSituacao;
    private Spinner           spinnerStatus;
    private TextInputEditText etObservacao;
    private MaterialButton    btnSalvarAlteracoes;
    private ProgressBar       progressSalvar;

    // Dados
    private LivroSalvo livroSalvo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_edicao);

        vincularViews();
        configurarToolbar();
        carregarLivroSalvo();
        configurarBotaoSalvar();
    }

    // ─── Configuração inicial ─────────────────────────────────────

    private void vincularViews() {
        ivCapa              = findViewById(R.id.iv_capa);
        tvTitulo            = findViewById(R.id.tv_titulo);
        tvAutores           = findViewById(R.id.tv_autores);
        tvEditoraAno        = findViewById(R.id.tv_editora_ano);
        tvCoordenadas       = findViewById(R.id.tv_coordenadas);
        tvEndereco          = findViewById(R.id.tv_endereco);
        spinnerSituacao     = findViewById(R.id.spinner_situacao);
        spinnerStatus       = findViewById(R.id.spinner_status);
        etObservacao        = findViewById(R.id.et_observacao);
        btnSalvarAlteracoes = findViewById(R.id.btn_salvar_alteracoes);
        progressSalvar      = findViewById(R.id.progress_salvar);
    }

    private void configurarToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    // ─── Carregar e exibir dados ──────────────────────────────────

    private void carregarLivroSalvo() {
        String json = getIntent().getStringExtra(
                ListaLivrosActivity.EXTRA_LIVRO_SALVO);

        if (json == null) {
            Toast.makeText(this,
                    R.string.erro_livro_nao_encontrado,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        livroSalvo = new Gson().fromJson(json, LivroSalvo.class);
        preencherDados();
    }

    private void preencherDados() {

        // ── Capa ─────────────────────────────────────────────────
        if (!livroSalvo.getThumbnailUrl().isEmpty()) {
            Glide.with(this)
                    .load(livroSalvo.getThumbnailUrl())
                    .apply(RequestOptions
                            .bitmapTransform(new RoundedCorners(8)))
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(R.drawable.ic_book_placeholder)
                    .into(ivCapa);
        } else {
            ivCapa.setImageResource(R.drawable.ic_book_placeholder);
        }

        // ── Textos básicos ────────────────────────────────────────
        tvTitulo.setText(livroSalvo.getTitulo());
        tvAutores.setText(livroSalvo.getAutores());

        // Monta "Editora · Ano"
        StringBuilder info = new StringBuilder();
        if (!livroSalvo.getEditora().isEmpty())
            info.append(livroSalvo.getEditora());
        if (!livroSalvo.getDataPublicacao().isEmpty()) {
            String ano = livroSalvo.getDataPublicacao().length() >= 4
                    ? livroSalvo.getDataPublicacao().substring(0, 4)
                    : livroSalvo.getDataPublicacao();
            if (info.length() > 0) info.append(" · ");
            info.append(ano);
        }
        tvEditoraAno.setText(info.length() > 0
                ? info.toString()
                : getString(R.string.info_nao_disponivel));

        // ── Localização ───────────────────────────────────────────
        tvCoordenadas.setText(
                "🌐 " + livroSalvo.getCoordenadasFormatadas());

        if (!livroSalvo.getEnderecoAproximado().isEmpty()) {
            tvEndereco.setVisibility(View.VISIBLE);
            tvEndereco.setText(
                    "📍 " + livroSalvo.getEnderecoAproximado());
        }

        // ── Spinners com valor atual pré-selecionado ──────────────
        configurarSpinnerComSelecao(
                spinnerSituacao,
                LivroSalvo.SITUACOES,
                livroSalvo.getSituacao());

        configurarSpinnerComSelecao(
                spinnerStatus,
                LivroSalvo.STATUS_LEITURA,
                livroSalvo.getStatusLeitura());

        // ── Observação ────────────────────────────────────────────
        etObservacao.setText(livroSalvo.getObservacao());
    }

    /**
     * Configura um Spinner com a lista de itens e pré-seleciona
     * o valor que já estava salvo no livro.
     */
    private void configurarSpinnerComSelecao(Spinner spinner,
                                             String[] itens,
                                             String valorAtual) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                itens);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Percorre os itens para encontrar e selecionar o valor atual
        for (int i = 0; i < itens.length; i++) {
            if (itens[i].equals(valorAtual)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    // ─── Salvar alterações ────────────────────────────────────────

    private void configurarBotaoSalvar() {
        btnSalvarAlteracoes.setOnClickListener(v -> salvarAlteracoes());
    }

    private void salvarAlteracoes() {
        String novaSituacao   = spinnerSituacao
                .getSelectedItem().toString();
        String novoStatus     = spinnerStatus
                .getSelectedItem().toString();
        String novaObservacao = etObservacao.getText() != null
                ? etObservacao.getText().toString().trim() : "";

        exibirCarregando(true);

        FirebaseRepository.getInstance().atualizarLivro(
                livroSalvo.getDocumentId(),
                novaSituacao,
                novoStatus,
                novaObservacao,
                () -> {
                    exibirCarregando(false);
                    Toast.makeText(this,
                            R.string.livro_atualizado_sucesso,
                            Toast.LENGTH_SHORT).show();
                    // Volta para ListaLivrosActivity
                    // que recarrega no onResume()
                    finish();
                },
                e -> {
                    exibirCarregando(false);
                    Toast.makeText(this,
                            getString(R.string.erro_atualizar)
                                    + ": " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void exibirCarregando(boolean carregando) {
        progressSalvar.setVisibility(
                carregando ? View.VISIBLE : View.GONE);
        btnSalvarAlteracoes.setEnabled(!carregando);
    }
}