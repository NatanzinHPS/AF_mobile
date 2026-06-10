package com.example.myapplicationaf.service;

import com.example.myapplicationaf.model.GoogleBooksResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Interface Retrofit que define os endpoints da Google Books API v1.
 *
 * URL base: https://www.googleapis.com/books/v1/
 *
 * Endpoint usado:
 *   GET volumes?q={query}&maxResults={n}
 *
 * Exemplos de query:
 *   "Dom Casmurro"          → busca geral por título
 *   "intitle:Clean Code"    → força busca no título
 *   "inauthor:Machado"      → força busca no autor
 *   "isbn:9788535914849"    → busca por ISBN
 *
 * Não é necessária API Key para buscas simples (quota gratuita do Google).
 */
public interface GoogleBooksApiService {

    /**
     * Busca volumes pela query informada.
     *
     * @param query      Termo de busca (título, autor, palavra-chave)
     * @param maxResults Número máximo de resultados — entre 1 e 40
     */
    @GET("volumes")
    Call<GoogleBooksResponse> buscarLivros(
            @Query("q") String query,
            @Query("maxResults") int maxResults,
            @Query("key") String apiKey
    );

    /**
     * Busca com paginação — usado para carregar mais resultados.
     *
     * @param query      Termo de busca
     * @param maxResults Número máximo de resultados
     * @param startIndex Índice inicial (0 = primeira página)
     */
    @GET("volumes")
    Call<GoogleBooksResponse> buscarLivrosPaginado(
            @Query("q") String query,
            @Query("maxResults") int maxResults,
            @Query("startIndex") int startIndex
    );
}