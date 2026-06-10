package com.example.myapplicationaf;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplicationaf.activities.BuscaLivroActivity;
import com.example.myapplicationaf.activities.ListaLivrosActivity;
import com.google.android.material.button.MaterialButton;

/**
 * MainActivity — tela inicial do app.
 *
 * Responsabilidade única: navegação.
 * Dois botões levam às duas funções principais:
 *   - Buscar livros  → BuscaLivroActivity
 *   - Minha lista    → ListaLivrosActivity
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configurarToolbar();
        configurarBotoes();
    }

    private void configurarToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void configurarBotoes() {
        MaterialButton btnBuscar = findViewById(R.id.btn_buscar_livros);
        MaterialButton btnLista  = findViewById(R.id.btn_minha_lista);

        btnBuscar.setOnClickListener(v ->
                startActivity(new Intent(this, BuscaLivroActivity.class)));

        btnLista.setOnClickListener(v ->
                startActivity(new Intent(this, ListaLivrosActivity.class)));
    }
}