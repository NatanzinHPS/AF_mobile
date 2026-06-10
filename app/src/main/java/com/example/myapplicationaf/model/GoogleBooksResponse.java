package com.example.myapplicationaf.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Mapeia a resposta JSON da Google Books API.
 *
 * JSON recebido:
 * {
 *   "totalItems": 42,
 *   "items": [
 *     {
 *       "id": "xyz123",
 *       "volumeInfo": {
 *         "title": "Clean Code",
 *         "authors": ["Robert C. Martin"],
 *         "publisher": "Prentice Hall",
 *         "publishedDate": "2008-08-01",
 *         "description": "...",
 *         "imageLinks": { "thumbnail": "https://..." },
 *         "industryIdentifiers": [
 *           { "type": "ISBN_13", "identifier": "9780132350884" }
 *         ]
 *       }
 *     }
 *   ]
 * }
 *
 * O Gson preenche os campos automaticamente via @SerializedName.
 */
public class GoogleBooksResponse {

    @SerializedName("totalItems")
    private int totalItems;

    @SerializedName("items")
    private List<Item> items;

    public int getTotalItems() { return totalItems; }
    public List<Item> getItems() { return items; }

    public static class Item {

        @SerializedName("id")
        private String id;

        @SerializedName("volumeInfo")
        private VolumeInfo volumeInfo;

        public String getId() { return id; }
        public VolumeInfo getVolumeInfo() { return volumeInfo; }

        /**
         * Converte este Item da API em um LivroApi (modelo do app).
         * Chamado em BuscaLivroActivity após receber a lista de resultados.
         */
        public LivroApi toLivroApi() {
            LivroApi livro = new LivroApi();
            livro.setIdApi(id != null ? id : "");

            if (volumeInfo == null) return livro;

            livro.setTitulo(volumeInfo.getTitle());
            livro.setEditora(volumeInfo.getPublisher());
            livro.setDataPublicacao(volumeInfo.getPublishedDate());
            livro.setDescricao(volumeInfo.getDescription());

            if (volumeInfo.getAuthors() != null
                    && !volumeInfo.getAuthors().isEmpty()) {
                livro.setAutores(String.join(", ", volumeInfo.getAuthors()));
            }

            if (volumeInfo.getImageLinks() != null) {
                String thumb = volumeInfo.getImageLinks().getThumbnail();
                if (thumb != null) {
                    livro.setThumbnailUrl(thumb.replace("http://", "https://"));
                }
            }

            // Prefere ISBN-13; cai para ISBN-10 se não encontrar
            if (volumeInfo.getIndustryIdentifiers() != null) {
                for (IndustryIdentifier ii : volumeInfo.getIndustryIdentifiers()) {
                    if ("ISBN_13".equals(ii.getType())) {
                        livro.setIsbn(ii.getIdentifier());
                        break;
                    }
                }
            }

            return livro;
        }
    }

    public static class VolumeInfo {

        @SerializedName("title")
        private String title;

        @SerializedName("authors")
        private List<String> authors;

        @SerializedName("publisher")
        private String publisher;

        @SerializedName("publishedDate")
        private String publishedDate;

        @SerializedName("description")
        private String description;

        @SerializedName("imageLinks")
        private ImageLinks imageLinks;

        @SerializedName("industryIdentifiers")
        private List<IndustryIdentifier> industryIdentifiers;

        public String getTitle() { return title; }
        public List<String> getAuthors() { return authors; }
        public String getPublisher() { return publisher; }
        public String getPublishedDate() { return publishedDate; }
        public String getDescription() { return description; }
        public ImageLinks getImageLinks() { return imageLinks; }
        public List<IndustryIdentifier> getIndustryIdentifiers() { return industryIdentifiers; }
    }

    public static class ImageLinks {

        @SerializedName("smallThumbnail")
        private String smallThumbnail;

        @SerializedName("thumbnail")
        private String thumbnail;

        public String getSmallThumbnail() { return smallThumbnail; }
        public String getThumbnail() { return thumbnail; }
    }

    public static class IndustryIdentifier {

        @SerializedName("type")
        private String type;

        @SerializedName("identifier")
        private String identifier;

        public String getType() { return type; }
        public String getIdentifier() { return identifier; }
    }
}