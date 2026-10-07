package br.com.cadernodereceitas.ui

import android.content.Context
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.com.cadernodereceitas.model.Receita

fun Receita.tempoFormatado(): String {
    val horas = tempoPreparoMin / 60
    val minutos = tempoPreparoMin % 60
    return when {
        horas == 0 -> "$minutos min"
        minutos == 0 -> "${horas}h"
        else -> "${horas}h${minutos.toString().padStart(2, '0')}"
    }
}

/** Linha de resumo usada na lista, ex.: "Doces · 50 min · 12 fatias". */
fun Receita.resumo(context: Context): String =
    listOfNotNull(context.getString(categoria.titulo), tempoFormatado(), rendimento)
        .joinToString(" · ")

/** Evita que o conteúdo fique atrás da barra de status e da barra de navegação. */
fun View.aplicarMargensDoSistema() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.setPadding(barras.left, barras.top, barras.right, barras.bottom)
        insets
    }
}
