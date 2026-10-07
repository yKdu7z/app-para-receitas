package br.com.cadernodereceitas.model

enum class Categoria { SALGADOS, DOCES, BEBIDAS }

data class Ingrediente(
    val nome: String,
    // null quando o ingrediente vai "a gosto"
    val quantidade: String? = null,
)

data class Receita(
    val id: Long = 0,
    val nome: String,
    val categoria: Categoria,
    // Receitas importadas da internet não informam o tempo
    val tempoPreparoMin: Int?,
    val rendimento: String?,
    val ingredientes: List<Ingrediente>,
    val passos: List<String>,
    val dica: String? = null,
    val favorita: Boolean = false,
    // Caminho de arquivo local (foto escolhida pelo usuário) ou URL (receita importada)
    val foto: String? = null,
    // id da receita na TheMealDB quando ela foi salva pela aba Descobrir
    val idOrigem: String? = null,
)

/** Item da lista de resultados da aba Descobrir. */
data class ReceitaDescoberta(
    val id: String,
    val nome: String,
    val fotoUrl: String?,
    val categoria: String?,
    val origem: String?,
)

/** Receita completa vinda da TheMealDB, exibida antes de ser salva no caderno. */
data class DetalheDescoberta(
    val id: String,
    val nome: String,
    val fotoUrl: String?,
    val categoria: String?,
    val origem: String?,
    val ingredientes: List<Ingrediente>,
    val passos: List<String>,
)
