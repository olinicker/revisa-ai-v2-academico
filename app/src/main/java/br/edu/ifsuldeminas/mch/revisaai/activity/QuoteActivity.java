package br.edu.ifsuldeminas.mch.revisaai.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import br.edu.ifsuldeminas.mch.revisaai.R;
import br.edu.ifsuldeminas.mch.revisaai.api.QuoteResponse;
import br.edu.ifsuldeminas.mch.revisaai.api.QuoteService;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class QuoteActivity extends AppCompatActivity {

    private static final String TAG = "QuoteActivity";
    private TextView textViewQuote;
    private TextView textViewAuthor;
    private Button buttonNewQuote;
    private ProgressBar progressBar;

    // Base URL da API de citações - AGORA TERMINA COM UMA BARRA (/)
    private static final String BASE_URL = "https://random-quotes-freeapi.vercel.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote);

        // Configura a Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarQuote);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Citação do Dia");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializa as views
        textViewQuote = findViewById(R.id.textViewQuote);
        textViewAuthor = findViewById(R.id.textViewAuthor);
        buttonNewQuote = findViewById(R.id.buttonNewQuote);
        progressBar = findViewById(R.id.progressBar);

        // Define o listener para o botão
        buttonNewQuote.setOnClickListener(v -> fetchRandomQuote());

        // Carrega uma citação ao iniciar a Activity
        fetchRandomQuote();
    }

    private void fetchRandomQuote() {
        // Mostra o ProgressBar e desabilita o botão
        progressBar.setVisibility(View.VISIBLE);
        buttonNewQuote.setEnabled(false);
        textViewQuote.setText(""); // Limpa o texto enquanto carrega
        textViewAuthor.setText("");

        // Configuração do OkHttpClient com Logging Interceptor
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        // Cria a instância do Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        // Cria a implementação da interface do serviço
        QuoteService service = retrofit.create(QuoteService.class);

        // Faz a chamada assíncrona à API
        Call<QuoteResponse> call = service.getRandomQuote();
        call.enqueue(new Callback<QuoteResponse>() {
            @Override
            public void onResponse(Call<QuoteResponse> call, Response<QuoteResponse> response) {
                // Esconde o ProgressBar e habilita o botão
                progressBar.setVisibility(View.GONE);
                buttonNewQuote.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    // Se a resposta for bem-sucedida, exibe a citação
                    QuoteResponse quote = response.body();
                    textViewQuote.setText(quote.getQuote());
                    textViewAuthor.setText("- " + quote.getAuthor());
                } else {
                    // Se a resposta não for bem-sucedida, exibe uma mensagem de erro
                    String errorMessage = "Erro na resposta da API: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao ler errorBody", e);
                    }
                    Log.e(TAG, errorMessage);
                    Toast.makeText(QuoteActivity.this, "Erro ao carregar citação. Tente novamente.", Toast.LENGTH_SHORT).show();
                    textViewQuote.setText("Não foi possível carregar a citação.");
                    textViewAuthor.setText("");
                }
            }

            @Override
            public void onFailure(Call<QuoteResponse> call, Throwable t) {
                // Esconde o ProgressBar e habilita o botão
                progressBar.setVisibility(View.GONE);
                buttonNewQuote.setEnabled(true);

                // Em caso de falha na requisição (ex: sem internet), exibe uma mensagem de erro
                Log.e(TAG, "Falha na requisição da API: ", t);
                Toast.makeText(QuoteActivity.this, "Erro de conexão. Verifique sua internet.", Toast.LENGTH_SHORT).show();
                textViewQuote.setText("Não foi possível carregar a citação.");
                textViewAuthor.setText("");
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
