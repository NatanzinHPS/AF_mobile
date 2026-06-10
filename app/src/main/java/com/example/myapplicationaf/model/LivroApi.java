package com.example.myapplicationaf.model;

/**
 * Modelo que representa um livro retornado pela Google Books API.
 *
 * É preenchido pelo GoogleBooksResponse após o parsing do JSON,
 * e passado entre telas via Intent (serializado em JSON com Gson).
 */
public class LivroApi {

    private String idApi;
    private String titulo;
    private String autores;
    private String editora;
    private String dataPublicacao;
    private String descricao;
    private String thumbnailUrl;
    private String isbn;

    public LivroApi() {}

    public LivroApi(String idApi, String titulo, String autores,
                    String editora, String dataPublicacao,
                    String descricao, String thumbnailUrl, String isbn) {
        this.idApi          = idApi;
        this.titulo         = titulo;
        this.autores        = autores;
        this.editora        = editora;
        this.dataPublicacao = dataPublicacao;
        this.descricao      = descricao;
        this.thumbnailUrl   = thumbnailUrl;
        this.isbn           = isbn;
    }

    public String getIdApi() { return idApi; }
    public void setIdApi(String idApi) { this.idApi = idApi; }

    public String getTitulo() { return titulo != null ? titulo : "Sem título"; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutores() { return autores != null ? autores : "Autor desconhecido"; }
    public void setAutores(String autores) { this.autores = autores; }

    public String getEditora() { return editora != null ? editora : "Editora não informada"; }
    public void setEditora(String editora) { this.editora = editora; }

    public String getDataPublicacao() { return dataPublicacao != null ? dataPublicacao : ""; }
    public void setDataPublicacao(String dataPublicacao) { this.dataPublicacao = dataPublicacao; }

    public String getDescricao() { return descricao != null ? descricao : ""; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getThumbnailUrl() { return thumbnailUrl != null ? thumbnailUrl : ""; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getIsbn() { return isbn != null ? isbn : ""; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    /**
     * Monta a linha de subtítulo exibida nos cards:
     * "Autor · Editora · Ano"
     */
    public String getResumoAutoresEditora() {
        StringBuilder sb = new StringBuilder();

        if (autores != null && !autores.isEmpty())
            sb.append(autores);

        if (editora != null && !editora.isEmpty()
                && !editora.equals("Editora não informada")) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(editora);
        }

        if (dataPublicacao != null && !dataPublicacao.isEmpty()) {
            String ano = dataPublicacao.length() >= 4
                    ? dataPublicacao.substring(0, 4)
                    : dataPublicacao;
            if (sb.length() > 0) sb.append(" · ");
            sb.append(ano);
        }

        return sb.length() > 0 ? sb.toString() : "Informações não disponíveis";
    }
}
