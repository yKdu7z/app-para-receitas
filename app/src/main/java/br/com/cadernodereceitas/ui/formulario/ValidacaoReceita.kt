package br.com.cadernodereceitas.ui.formulario

import androidx.annotation.StringRes
import br.com.cadernodereceitas.R

const val TAMANHO_MAXIMO_NOME = 80
const val TEMPO_MAXIMO_MIN = 1440

data class CampoIngrediente(
    // Chave estável para o Compose não confundir as linhas ao remover uma do meio
    val chave: Long,
    val quantidade: String = "",
    val nome: String = "",
)

data class ErrosFormulario(
    @StringRes val nome: Int? = null,
    @StringRes val tempo: Int? = null,
    @StringRes val ingredientes: Int? = null,
    @StringRes val passos: Int? = null,
) {
    val temErro: Boolean get() = listOfNotNull(nome, tempo, ingredientes, passos).isNotEmpty()
}

/** Regras do formulário, separadas da tela para poderem ser testadas sozinhas. */
fun validarReceita(
    nome: String,
    tempo: String,
    ingredientes: List<CampoIngrediente>,
    passos: String,
): ErrosFormulario {
    val tempoNumero = tempo.trim().toIntOrNull()
    return ErrosFormulario(
        nome = when {
            nome.isBlank() -> R.string.erro_nome_obrigatorio
            nome.trim().length > TAMANHO_MAXIMO_NOME -> R.string.erro_nome_longo
            else -> null
        },
        tempo = when {
            tempo.isBlank() -> null
            tempoNumero == null || tempoNumero !in 1..TEMPO_MAXIMO_MIN -> R.string.erro_tempo_invalido
            else -> null
        },
        ingredientes = if (ingredientes.none { it.nome.isNotBlank() }) R.string.erro_ingredientes else null,
        passos = if (separarPassos(passos).isEmpty()) R.string.erro_passos else null,
    )
}

fun separarPassos(texto: String): List<String> =
    texto.lines().map { it.trim() }.filter { it.isNotEmpty() }
