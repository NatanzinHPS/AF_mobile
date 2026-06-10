package com.example.myapplicationaf.repository;

import android.util.Log;

import com.example.myapplicationaf.model.LivroSalvo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Repositório responsável por toda a comunicação com o Firestore.
 *
 * Coleção: "livros_salvos"
 *
 * Operações disponíveis:
 *   salvarLivro()    → CREATE  (add)
 *   listarLivros()   → READ    (get + orderBy)
 *   atualizarLivro() → UPDATE  (update)
 *   excluirLivro()   → DELETE  (delete)
 *
 * Todas as operações são assíncronas e retornam o resultado
 * via callbacks OnSucesso / OnErro, mantendo a UI responsiva.
 *
 * Uso:
 *   FirebaseRepository.getInstance().salvarLivro(livro, onSucesso, onErro);
 */
public class FirebaseRepository {

    private static final String TAG     = "FirebaseRepository";
    private static final String COLECAO = "livros_salvos";

    private static FirebaseRepository instance;
    private final FirebaseFirestore    db;
    private final CollectionReference  colecaoRef;

    /** Callback para operações que não retornam dado (update, delete). */
    public interface OnSucessoSimples {
        void onSucesso();
    }

    /** Callback para operações que retornam um dado (save, list). */
    public interface OnSucessoComDado<T> {
        void onSucesso(T dado);
    }

    /** Callback de erro — recebe a exceção lançada pelo Firestore. */
    public interface OnErro {
        void onErro(Exception e);
    }

    private FirebaseRepository() {
        db         = FirebaseFirestore.getInstance();
        colecaoRef = db.collection(COLECAO);
    }

    public static synchronized FirebaseRepository getInstance() {
        if (instance == null) {
            instance = new FirebaseRepository();
        }
        return instance;
    }

    /**
     * Salva um novo livro geolocalizado no Firestore.
     * O ID do documento é gerado automaticamente pelo Firebase.
     * O callback retorna o ID gerado para confirmar o salvamento.
     *
     * Chamado em: CadastroLivroActivity.salvarLivro()
     */
    public void salvarLivro(LivroSalvo livro,
                            OnSucessoComDado<String> onSucesso,
                            OnErro onErro) {
        colecaoRef.add(livro)
                .addOnSuccessListener(documentReference -> {
                    String docId = documentReference.getId();
                    livro.setDocumentId(docId);
                    Log.d(TAG, "Livro salvo — ID: " + docId);
                    onSucesso.onSucesso(docId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao salvar: " + e.getMessage());
                    onErro.onErro(e);
                });
    }

    /**
     * Retorna todos os livros salvos, do mais recente para o mais antigo.
     * Chamado em: ListaLivrosActivity.carregarLivros()
     */
    public void listarLivros(OnSucessoComDado<List<LivroSalvo>> onSucesso,
                             OnErro onErro) {
        colecaoRef
                .orderBy("dataCadastro", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<LivroSalvo> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        LivroSalvo livro = doc.toObject(LivroSalvo.class);
                        if (livro != null) {
                            livro.setDocumentId(doc.getId());
                            lista.add(livro);
                        }
                    }
                    Log.d(TAG, "Livros carregados: " + lista.size());
                    onSucesso.onSucesso(lista);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao listar: " + e.getMessage());
                    onErro.onErro(e);
                });
    }

    /**
     * Atualiza apenas os campos editáveis pelo usuário.
     * Não sobrescreve coordenadas, título, autor nem dataCadastro.
     *
     * Chamado em: DetalheEdicaoActivity.salvarAlteracoes()
     */
    public void atualizarLivro(String documentId,
                               String situacao,
                               String statusLeitura,
                               String observacao,
                               OnSucessoSimples onSucesso,
                               OnErro onErro) {
        colecaoRef.document(documentId)
                .update(
                        "situacao",        situacao,
                        "statusLeitura",   statusLeitura,
                        "observacao",      observacao,
                        "dataAtualizacao", FieldValue.serverTimestamp()
                )
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Livro atualizado: " + documentId);
                    onSucesso.onSucesso();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao atualizar: " + e.getMessage());
                    onErro.onErro(e);
                });
    }

    /**
     * Exclui permanentemente um livro pelo ID do documento.
     * Chamado em: ListaLivrosActivity.excluirLivro()
     */
    public void excluirLivro(String documentId,
                             OnSucessoSimples onSucesso,
                             OnErro onErro) {
        colecaoRef.document(documentId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Livro excluído: " + documentId);
                    onSucesso.onSucesso();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao excluir: " + e.getMessage());
                    onErro.onErro(e);
                });
    }
}