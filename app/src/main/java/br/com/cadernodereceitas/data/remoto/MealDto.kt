package br.com.cadernodereceitas.data.remoto

import br.com.cadernodereceitas.model.Categoria
import br.com.cadernodereceitas.model.DetalheDescoberta
import br.com.cadernodereceitas.model.Ingrediente
import br.com.cadernodereceitas.model.Receita
import br.com.cadernodereceitas.model.ReceitaDescoberta
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/** Receita como a TheMealDB entrega, já com os 20 pares ingrediente/medida agrupados. */
data class MealDto(
    val id: String,
    val nome: String,
    val categoria: String?,
    val area: String?,
    val instrucoes: String?,
    val fotoUrl: String?,
    val ingredientes: List<Pair<String, String?>>,
) {
    companion object {
        fun de(json: JsonObject): MealDto? {
            val id = json.texto("idMeal") ?: return null
            val nome = json.texto("strMeal") ?: return null
            return MealDto(
                id = id,
                nome = nome,
                categoria = json.texto("strCategory"),
                area = json.texto("strArea"),
                instrucoes = json.texto("strInstructions"),
                fotoUrl = json.texto("strMealThumb"),
                ingredientes = (1..20).mapNotNull { n ->
                    json.texto("strIngredient$n")?.let { it to json.texto("strMeasure$n") }
                },
            )
        }

        private fun JsonObject.texto(chave: String): String? =
            (this[chave] as? JsonPrimitive)?.contentOrNull?.trim()?.takeIf { it.isNotEmpty() }
    }
}

// Linhas como "2", "STEP 3" ou "Step 4:" só numeram os passos e são descartadas
private val linhaSoNumeracao = Regex("""^(step\s*)?\d+[.:)]?$""", RegexOption.IGNORE_CASE)

fun MealDto.paraDescoberta() = ReceitaDescoberta(
    id = id,
    nome = nome,
    fotoUrl = fotoUrl,
    categoria = categoria,
    origem = area,
)

fun MealDto.paraDetalhe() = DetalheDescoberta(
    id = id,
    nome = nome,
    fotoUrl = fotoUrl,
    categoria = categoria,
    origem = area,
    ingredientes = ingredientes.map { (nome, medida) -> Ingrediente(nome, medida) },
    passos = instrucoes.orEmpty()
        .lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !linhaSoNumeracao.matches(it) },
)

/** Converte a receita encontrada em uma receita do caderno, pronta para ser salva. */
fun DetalheDescoberta.paraReceita() = Receita(
    nome = nome,
    categoria = if (categoria.equals("Dessert", ignoreCase = true)) Categoria.DOCES else Categoria.SALGADOS,
    tempoPreparoMin = null,
    rendimento = null,
    ingredientes = ingredientes,
    passos = passos,
    foto = fotoUrl,
    idOrigem = id,
)
