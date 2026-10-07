package br.com.cadernodereceitas.ui.componentes

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import br.com.cadernodereceitas.R
import br.com.cadernodereceitas.model.Categoria
import br.com.cadernodereceitas.model.Receita

@get:StringRes
val Categoria.rotulo: Int
    get() = when (this) {
        Categoria.SALGADOS -> R.string.categoria_salgados
        Categoria.DOCES -> R.string.categoria_doces
        Categoria.BEBIDAS -> R.string.categoria_bebidas
    }

fun formatarTempo(minutosTotais: Int): String {
    val horas = minutosTotais / 60
    val minutos = minutosTotais % 60
    return when {
        horas == 0 -> "$minutos min"
        minutos == 0 -> "${horas}h"
        else -> "${horas}h${minutos.toString().padStart(2, '0')}"
    }
}

/** Ex.: "Doces · 50 min · 12 fatias". Partes sem valor são omitidas. */
@Composable
fun resumoDaReceita(receita: Receita): String =
    listOfNotNull(
        stringResource(receita.categoria.rotulo),
        receita.tempoPreparoMin?.let(::formatarTempo),
        receita.rendimento,
    ).joinToString(" · ")
