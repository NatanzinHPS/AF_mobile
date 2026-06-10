package com.example.myapplicationaf.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplicationaf.R;
import com.example.myapplicationaf.model.LivroApi;

import java.util.List;

/**
 * Adapter do RecyclerView para exibir resultados da busca na API.
 *
 * Cada item exibe:
 *   - Capa do livro (thumbnail via Glide)
 *   - Título
 *   - Autores
 *   - Editora · Ano de publicação
 *
 * O clique em um item é tratado via interface OnLivroClickListener,
 * mantendo o adapter desacoplado da Activity.
 */
public class LivroApiAdapter
        extends RecyclerView.Adapter<LivroApiAdapter.ViewHolder> {

    // ─── Interface de clique ──────────────────────────────────────

    public interface OnLivroClickListener {
        void onLivroClick(LivroApi livro);
    }

    // ─── Campos ───────────────────────────────────────────────────

    private final List<LivroApi>        livros;
    private final OnLivroClickListener  listener;

    public LivroApiAdapter(List<LivroApi> livros,
                           OnLivroClickListener listener) {
        this.livros   = livros;
        this.listener = listener;
    }

    // ─── Ciclo do RecyclerView ────────────────────────────────────

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        // Infla o layout item_livro_api.xml para cada linha da lista
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_livro_api, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(livros.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return livros.size();
    }

    // ─── ViewHolder ───────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivCapa;
        private final TextView  tvTitulo;
        private final TextView  tvAutores;
        private final TextView  tvEditoraAno;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCapa       = itemView.findViewById(R.id.iv_capa);
            tvTitulo     = itemView.findViewById(R.id.tv_titulo);
            tvAutores    = itemView.findViewById(R.id.tv_autores);
            tvEditoraAno = itemView.findViewById(R.id.tv_editora_ano);
        }

        void bind(LivroApi livro, OnLivroClickListener listener) {

            // Textos
            tvTitulo.setText(livro.getTitulo());
            tvAutores.setText(livro.getAutores());
            tvEditoraAno.setText(livro.getResumoAutoresEditora());

            // Capa via Glide
            // - placeholder: ícone padrão enquanto carrega
            // - error: mesmo ícone se a URL falhar
            // - RoundedCorners: bordas arredondadas na imagem
            if (!livro.getThumbnailUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(livro.getThumbnailUrl())
                        .apply(RequestOptions
                                .bitmapTransform(new RoundedCorners(8)))
                        .placeholder(R.drawable.ic_book_placeholder)
                        .error(R.drawable.ic_book_placeholder)
                        .into(ivCapa);
            } else {
                ivCapa.setImageResource(R.drawable.ic_book_placeholder);
            }

            // Clique no item inteiro
            itemView.setOnClickListener(v -> listener.onLivroClick(livro));
        }
    }
}