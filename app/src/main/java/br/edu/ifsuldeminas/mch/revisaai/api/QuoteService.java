package br.edu.ifsuldeminas.mch.revisaai.api;

import retrofit2.Call;
import retrofit2.http.GET;

public interface QuoteService {

    // Define o endpoint para obter uma citação aleatória
    // O caminho "api/random" é adicionado à Base URL que será configurada no Retrofit
    @GET("api/random")
    Call<QuoteResponse> getRandomQuote();
}
