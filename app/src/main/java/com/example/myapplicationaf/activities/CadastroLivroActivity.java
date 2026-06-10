package com.example.myapplicationaf.activities;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplicationaf.R;
import com.example.myapplicationaf.location.LocationHelper;
import com.example.myapplicationaf.model.LivroApi;
import com.example.myapplicationaf.model.LivroSalvo;
import com.example.myapplicationaf.repository.FirebaseRepository;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

/**
 * CadastroLivroActivity — formulário de cadastro do livro geolocalizado.
 *
 * Recebe o LivroApi via Intent (JSON), exibe seus dados e permite:
 *   1. Capturar localização GPS atual
 *   2. Selecionar situação (Spinner)
 *   3. Selecionar status de leitura (Spinner)
 *   4. Adicionar observação pessoal
 *   5. Salvar tudo no Firebase Firestore
 */
public class CadastroLivroActivity extends AppCompatActivity {

    // Views
    private ImageView         ivCapa;
    private TextView          tvTitulo;
    private TextView          tvAutores;
    private TextView          tvEditoraAno;
    private MaterialButton    btnCapturarLocalizacao;
    private ProgressBar       progressLocalizacao;
    private TextView          tvLocalizacaoStatus;
    private TextView          tvEnderecoAproximado;
    private Spinner           spinnerSituacao;
    private Spinner           spinnerStatus;
    private TextInputEditText etObservacao;
    private MaterialButton    btnSalvar;
    private ProgressBar       progressSalvar;

    // Dados
    private LivroApi     livroApi;
    private LocationHelper locationHelper;
    private double       latitudeCapturada  = 0;
    private double       longitudeCapturada = 0;
    private boolean      localizacaoObtida  = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_livro);

        vincularViews();
        configurarToolbar();
        carregarLivroApi();
        configurarSpinners();
        configurarBotaoLocalizacao();
        configurarBotaoSalvar();

        locationHelper = new LocationHelper();
    }

    // ─── Configuração inicial ─────────────────────────────────────

    private void vincularViews() {
        ivCapa                 = findViewById(R.id.iv_capa);
        tvTitulo               = findViewById(R.id.tv_titulo);
        tvAutores              = findViewById(R.id.tv_autores);
        tvEditoraAno           = findViewById(R.id.tv_editora_ano);
        btnCapturarLocalizacao = findViewById(R.id.btn_capturar_localizacao);
        progressLocalizacao    = findViewById(R.id.progress_localizacao);
        tvLocalizacaoStatus    = findViewById(R.id.tv_localizacao_status);
        tvEnderecoAproximado   = findViewById(R.id.tv_endereco_aproximado);
        spinnerSituacao        = findViewById(R.id.spinner_situacao);
        spinnerStatus          = findViewById(R.id.spinner_status);
        etObservacao           = findViewById(R.id.et_observacao);
        btnSalvar              = findViewById(R.id.btn_salvar);
        progressSalvar         = findViewById(R.id.progress_salvar);
    }

    private void configurarToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    // ─── Dados do livro da API ────────────────────────────────────

    private void carregarLivroApi() {
        String json = getIntent().getStringExtra(
                BuscaLivroActivity.EXTRA_LIVRO_API);

        if (json == null) {
            Toast.makeText(this,
                    R.string.erro_livro_nao_encontrado,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        livroApi = new Gson().fromJson(json, LivroApi.class);

        tvTitulo.setText(livroApi.getTitulo());
        tvAutores.setText(livroApi.getAutores());

        // Monta "Editora · Ano"
        String editora = livroApi.getEditora();
        String data    = livroApi.getDataPublicacao();
        StringBuilder info = new StringBuilder();
        if (!editora.equals("Editora não informada")) info.append(editora);
        if (!data.isEmpty()) {
            String ano = data.length() >= 4 ? data.substring(0, 4) : data;
            if (info.length() > 0) info.append(" · ");
            info.append(ano);
        }
        tvEditoraAno.setText(info.length() > 0
                ? info.toString()
                : getString(R.string.info_nao_disponivel));

        // Capa via Glide
        if (!livroApi.getThumbnailUrl().isEmpty()) {
            Glide.with(this)
                    .load(livroApi.getThumbnailUrl())
                    .apply(RequestOptions
                            .bitmapTransform(new RoundedCorners(8)))
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(R.drawable.ic_book_placeholder)
                    .into(ivCapa);
        } else {
            ivCapa.setImageResource(R.drawable.ic_book_placeholder);
        }
    }

    // ─── Spinners ─────────────────────────────────────────────────

    private void configurarSpinners() {
        ArrayAdapter<String> adapterSituacao = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                LivroSalvo.SITUACOES);
        adapterSituacao.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerSituacao.setAdapter(adapterSituacao);

        ArrayAdapter<String> adapterStatus = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                LivroSalvo.STATUS_LEITURA);
        adapterStatus.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapterStatus);
    }

    // ─── Localização GPS ──────────────────────────────────────────

    private void configurarBotaoLocalizacao() {
        btnCapturarLocalizacao.setOnClickListener(v -> capturarLocalizacao());
    }

    private void capturarLocalizacao() {
        // Verifica permissão — se não tiver, solicita ao sistema
        if (!LocationHelper.hasPermissao(this)) {
            LocationHelper.solicitarPermissao(this);
            return;
        }
        iniciarCapturaGPS();
    }

    @SuppressLint("MissingPermission")
    private void iniciarCapturaGPS() {
        btnCapturarLocalizacao.setEnabled(false);
        progressLocalizacao.setVisibility(View.VISIBLE);
        tvLocalizacaoStatus.setText(R.string.obtendo_localizacao);

        locationHelper.obterLocalizacao(this, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                locationHelper.pararAtualizacoes();
                progressLocalizacao.setVisibility(View.GONE);
                btnCapturarLocalizacao.setEnabled(true);

                Location loc = result.getLastLocation();
                if (loc != null) {
                    latitudeCapturada  = loc.getLatitude();
                    longitudeCapturada = loc.getLongitude();
                    localizacaoObtida  = true;

                    String coords = String.format(
                            "%.6f, %.6f",
                            latitudeCapturada,
                            longitudeCapturada);

                    tvLocalizacaoStatus.setText(
                            getString(R.string.localizacao_obtida)
                                    + "\n" + coords);
                    tvLocalizacaoStatus.setTextColor(
                            getColor(R.color.verde_sucesso));

                    // Reverse geocoding em background
                    buscarEnderecoBackground(
                            latitudeCapturada, longitudeCapturada);
                } else {
                    tvLocalizacaoStatus.setText(R.string.erro_localizacao);
                    tvLocalizacaoStatus.setTextColor(
                            getColor(R.color.vermelho_erro));
                }
            }
        });
    }

    /** Busca o endereço legível em background para não travar a UI. */
    @SuppressLint("StaticFieldLeak")
    private void buscarEnderecoBackground(double lat, double lng) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                return LocationHelper.obterEnderecoAproximado(
                        CadastroLivroActivity.this, lat, lng);
            }

            @Override
            protected void onPostExecute(String endereco) {
                if (endereco != null && !endereco.isEmpty()) {
                    tvEnderecoAproximado.setVisibility(View.VISIBLE);
                    tvEnderecoAproximado.setText("📍 " + endereco);
                }
            }
        }.execute();
    }

    /** Resultado do dialog de permissão do sistema. */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        if (requestCode == LocationHelper.REQUEST_CODE_LOCALIZACAO) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão concedida: inicia o GPS
                iniciarCapturaGPS();
            } else {
                // Permissão negada: avisa e permite salvar sem coordenadas
                tvLocalizacaoStatus.setText(R.string.permissao_negada_aviso);
                tvLocalizacaoStatus.setTextColor(
                        getColor(R.color.laranja_aviso));
            }
        }
    }

    // ─── Salvar no Firebase ───────────────────────────────────────

    private void configurarBotaoSalvar() {
        btnSalvar.setOnClickListener(v -> salvarLivro());
    }

    private void salvarLivro() {
        String situacao      = spinnerSituacao.getSelectedItem().toString();
        String statusLeitura = spinnerStatus.getSelectedItem().toString();
        String observacao    = etObservacao.getText() != null
                ? etObservacao.getText().toString().trim() : "";

        // Avisa se não capturou localização, mas não bloqueia
        if (!localizacaoObtida) {
            Toast.makeText(this,
                    R.string.aviso_sem_localizacao,
                    Toast.LENGTH_SHORT).show();
        }

        // Monta o objeto com factory method
        LivroSalvo livroSalvo = LivroSalvo.fromLivroApi(
                livroApi, situacao, statusLeitura, observacao,
                latitudeCapturada, longitudeCapturada);

        // Adiciona endereço se o reverse geocoding terminou
        String enderecoTv = tvEnderecoAproximado
                .getText().toString().replace("📍 ", "").trim();
        if (!enderecoTv.isEmpty()) {
            livroSalvo.setEnderecoAproximado(enderecoTv);
        }

        exibirCarregandoSalvar(true);

        FirebaseRepository.getInstance().salvarLivro(
                livroSalvo,
                docId -> {
                    exibirCarregandoSalvar(false);
                    Toast.makeText(this,
                            R.string.livro_salvo_sucesso,
                            Toast.LENGTH_SHORT).show();
                    finish();
                },
                e -> {
                    exibirCarregandoSalvar(false);
                    Toast.makeText(this,
                            getString(R.string.erro_salvar)
                                    + ": " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void exibirCarregandoSalvar(boolean carregando) {
        progressSalvar.setVisibility(
                carregando ? View.VISIBLE : View.GONE);
        btnSalvar.setEnabled(!carregando);
    }

    // ─── Ciclo de vida ────────────────────────────────────────────

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Evita leak de memória parando o GPS ao fechar a tela
        if (locationHelper != null) locationHelper.pararAtualizacoes();
    }
}