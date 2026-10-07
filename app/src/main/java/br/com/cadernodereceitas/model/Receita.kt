package br.com.cadernodereceitas.model

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import br.com.cadernodereceitas.R

enum class Categoria(@StringRes val titulo: Int, @ColorRes val cor: Int) {
    SALGADOS(R.string.categoria_salgados, R.color.categoria_salgados),
    DOCES(R.string.categoria_doces, R.color.categoria_doces),
    BEBIDAS(R.string.categoria_bebidas, R.color.categoria_bebidas),
}

data class Ingrediente(
    val nome: String,
    // null quando o ingrediente vai "a gosto"
    val quantidade: String? = null,
)

data class Receita(
    val id: Int,
    val nome: String,
    val categoria: Categoria,
    val tempoPreparoMin: Int,
    val rendimento: String?,
    val ingredientes: List<Ingrediente>,
    val passos: List<String>,
    val dica: String? = null,
    val favorita: Boolean = false,
)
