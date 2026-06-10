package com.example.myapplicationaf.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * Modelo que representa um livro geolocalizado salvo no Firestore.
 *
 * Estrutura do documento (coleção "livros_salvos"):
 * {
 *   idApi, titulo, autores, editora, dataPublicacao, thumbnailUrl,
 *   situacao, statusLeitura, observacao,
 *   latitude, longitude, enderecoAproximado,
 *   dataCadastro (ServerTimestamp automático)
 * }
 */
public class LivroSalvo {

    public static final String SITUACAO_BIBLIOTECA = "Biblioteca";
    public static final String SITUACAO_LIVRARIA   = "Livraria";
    public static final String SITUACAO_AULA       = "Aula";
    public static final String SITUACAO_INDICACAO  = "Indicação";
    public static final String SITUACAO_CASA       = "Leitura em Casa";
    public static final String SITUACAO_VIAGEM     = "Viagem";
    public static final String SITUACAO_OUTRO      = "Outro";

    public static final String[] SITUACOES = {
            SITUACAO_BIBLIOTECA, SITUACAO_LIVRARIA, SITUACAO_AULA,
            SITUACAO_INDICACAO,  SITUACAO_CASA,     SITUACAO_VIAGEM,
            SITUACAO_OUTRO
    };

    public static final String STATUS_QUERO_LER = "Quero ler";
    public static final String STATUS_LENDO     = "Lendo";
    public static final String STATUS_CONCLUIDO = "Concluído";

    public static final String[] STATUS_LEITURA = {
            STATUS_QUERO_LER, STATUS_LENDO, STATUS_CONCLUIDO
    };

    // Campos do Firestore

    @DocumentId
    private String documentId;

    // Dados vindos da API
    private String idApi;
    private String titulo;
    private String autores;
    private String editora;
    private String dataPublicacao;
    private String thumbnailUrl;

    // Dados preenchidos pelo usuário
    private String situacao;
    private String statusLeitura;
    private String observacao;

    // Localização capturada pelo GPS
    private double latitude;
    private double longitude;
    private String enderecoAproximado;

    // Controle de tempo (preenchidos pelo servidor)
    @ServerTimestamp
    private Timestamp dataCadastro;

    @ServerTimestamp
    private Timestamp dataAtualizacao;

    public LivroSalvo() {}
    /**
     * Cria um LivroSalvo a partir de um LivroApi + dados do formulário.
     * Usado em CadastroLivroActivity antes de chamar o repositório.
     */
    public static LivroSalvo fromLivroApi(LivroApi api,
                                          String situacao,
                                          String statusLeitura,
                                          String observacao,
                                          double latitude,
                                          double longitude) {
        LivroSalvo ls = new LivroSalvo();
        ls.idApi          = api.getIdApi();
        ls.titulo         = api.getTitulo();
        ls.autores        = api.getAutores();
        ls.editora        = api.getEditora();
        ls.dataPublicacao = api.getDataPublicacao();
        ls.thumbnailUrl   = api.getThumbnailUrl();
        ls.situacao       = situacao;
        ls.statusLeitura  = statusLeitura;
        ls.observacao     = observacao;
        ls.latitude       = latitude;
        ls.longitude      = longitude;
        return ls;
    }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getIdApi() { return idApi; }
    public void setIdApi(String idApi) { this.idApi = idApi; }

    public String getTitulo() { return titulo != null ? titulo : "Sem título"; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutores() { return autores != null ? autores : ""; }
    public void setAutores(String autores) { this.autores = autores; }

    public String getEditora() { return editora != null ? editora : ""; }
    public void setEditora(String editora) { this.editora = editora; }

    public String getDataPublicacao() { return dataPublicacao != null ? dataPublicacao : ""; }
    public void setDataPublicacao(String dataPublicacao) { this.dataPublicacao = dataPublicacao; }

    public String getThumbnailUrl() { return thumbnailUrl != null ? thumbnailUrl : ""; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getSituacao() { return situacao != null ? situacao : ""; }
    public void setSituacao(String situacao) { this.situacao = situacao; }

    public String getStatusLeitura() { return statusLeitura != null ? statusLeitura : ""; }
    public void setStatusLeitura(String statusLeitura) { this.statusLeitura = statusLeitura; }

    public String getObservacao() { return observacao != null ? observacao : ""; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getEnderecoAproximado() { return enderecoAproximado != null ? enderecoAproximado : ""; }
    public void setEnderecoAproximado(String e) { this.enderecoAproximado = e; }

    public Timestamp getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(Timestamp dataCadastro) { this.dataCadastro = dataCadastro; }

    public Timestamp getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(Timestamp dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }


    /** Retorna "lat, lng" formatado com 6 casas decimais. */
    public String getCoordenadasFormatadas() {
        return String.format("%.6f, %.6f", latitude, longitude);
    }

    /**
     * Cor ARGB associada ao status — usada no Chip do card.
     * Verde = Concluído | Azul = Lendo | Laranja = Quero ler
     */
    public int getCorStatusLeitura() {
        if (STATUS_CONCLUIDO.equals(statusLeitura)) return 0xFF4CAF50;
        if (STATUS_LENDO.equals(statusLeitura))     return 0xFF2196F3;
        return 0xFFFF9800;
    }
}