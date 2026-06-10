package com.example.myapplicationaf.adapters;

import android.graphics.drawable.GradientDrawable;
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
import com.example.myapplicationaf.model.LivroSalvo;

import java.util.List;

/**
 * Adapter do RecyclerView para a lista de livros salvos.
 *
 * Cada card exibe:
 *   - Capa, título, autores
 *   - Badge colorido com status de leitura
 *   - Situação e coordenadas GPS
 *   - Observação (quando preenchida)
 *
 * Clique curto  → onLivroClick    (abre DetalheEdicaoActivity)
 * Clique longo  → onLivroLongClick (abre confirmação de exclusão)
 */
public class LivroSalvoAdapter
        extends RecyclerView.Adapter<LivroSalvoAdapter.ViewHolder> {

    // ─── Interfaces de clique ─────────────────────────────────────

    public interface OnLivroClickListener {
        void onLivroClick(LivroSalvo livro);
    }

    public interface OnLivroLongClickListener {
        boolean onLivroLongClick(LivroSalvo livro);
    }

    // ─── Campos ───────────────────────────────────────────────────

    private final List<LivroSalvo>          livros;
    private final OnLivroClickListener      clickListener;
    private final OnLivroLongClickListener  longClickListener;

    public LivroSalvoAdapter(List<LivroSalvo> livros,
                             OnLivroClickListener clickListener,
                             OnLivroLongClickListener longClickListener) {
        this.livros            = livros;
        this.clickListener     = clickListener;
        this.longClickListener = longClickListener;
    }

    // ─── Ciclo do RecyclerView ────────────────────────────────────

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_livro_salvo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(livros.get(position), clickListener, longClickListener);
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
        private final TextView  tvStatus;
        private final TextView  tvSituacao;
        private final TextView  tvCoordenadas;
        private final TextView  tvObservacao;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCapa        = itemView.findViewById(R.id.iv_capa);
            tvTitulo      = itemView.findViewById(R.id.tv_titulo);
            tvAutores     = itemView.findViewById(R.id.tv_autores);
            tvStatus      = itemView.findViewById(R.id.tv_status);
            tvSituacao    = itemView.findViewById(R.id.tv_situacao);
            tvCoordenadas = itemView.findViewById(R.id.tv_coordenadas);
            tvObservacao  = itemView.findViewById(R.id.tv_observacao);
        }

        void bind(LivroSalvo livro,
                  OnLivroClickListener clickListener,
                  OnLivroLongClickListener longClickListener) {

            // ── Textos básicos ────────────────────────────────────
            tvTitulo.setText(livro.getTitulo());
            tvAutores.setText(livro.getAutores());
            tvSituacao.setText("📌 " + livro.getSituacao());
            tvCoordenadas.setText("🌐 " + livro.getCoordenadasFormatadas());

            // ── Badge de status (cor dinâmica) ────────────────────
            // Pega o drawable bg_badge_status.xml e troca a cor
            // de acordo com o status do livro (verde/azul/laranja)
            tvStatus.setText(livro.getStatusLeitura());
            GradientDrawable badge =
                    (GradientDrawable) tvStatus.getBackground().mutate();
            badge.setColor(livro.getCorStatusLeitura());

            // ── Observação (oculta se vazia) ──────────────────────
            if (!livro.getObservacao().isEmpty()) {
                tvObservacao.setVisibility(View.VISIBLE);
                tvObservacao.setText("💬 " + livro.getObservacao());
            } else {
                tvObservacao.setVisibility(View.GONE);
            }

            // ── Capa via Glide ────────────────────────────────────
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

            // ── Clique curto ──────────────────────────────────────
            itemView.setOnClickListener(
                    v -> clickListener.onLivroClick(livro));

            // ── Clique longo ──────────────────────────────────────
            itemView.setOnLongClickListener(
                    v -> longClickListener.onLivroLongClick(livro));
        }
    }
}