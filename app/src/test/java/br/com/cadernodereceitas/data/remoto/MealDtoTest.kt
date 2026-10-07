package br.com.cadernodereceitas.data.remoto

import br.com.cadernodereceitas.model.Categoria
import br.com.cadernodereceitas.model.Ingrediente
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MealDtoTest {

    private fun json(texto: String): JsonObject = Json.parseToJsonElement(texto).jsonObject

    private val flan = json(
        """
        {
          "idMeal": "53322", "strMeal": "Flan", "strCategory": "Dessert", "strArea": "Uruguayan",
          "strMealThumb": "https://exemplo/flan.jpg",
          "strInstructions": "STEP 1\r\nMelt the sugar.\r\n\r\n2\r\nHeat the milk.\r\nStep 3:\r\nBake.",
          "strIngredient1": "Sugar", "strMeasure1": "100g",
          "strIngredient2": "Dulce de leche", "strMeasure2": " ",
          "strIngredient3": "", "strMeasure3": "",
          "strIngredient4": null, "strMeasure4": null
        }
        """
    )

    @Test
    fun `agrupa ingredientes e ignora campos vazios ou nulos`() {
        val dto = MealDto.de(flan)!!
        assertEquals(listOf("Sugar" to "100g", "Dulce de leche" to null), dto.ingredientes)
    }

    @Test
    fun `passos descartam linhas que so numeram`() {
        val detalhe = MealDto.de(flan)!!.paraDetalhe()
        assertEquals(listOf("Melt the sugar.", "Heat the milk.", "Bake."), detalhe.passos)
    }

    @Test
    fun `sobremesa vira doce e o id de origem e guardado`() {
        val receita = MealDto.de(flan)!!.paraDetalhe().paraReceita()
        assertEquals(Categoria.DOCES, receita.categoria)
        assertEquals("53322", receita.idOrigem)
        assertNull(receita.tempoPreparoMin)
        assertEquals(Ingrediente("Sugar", "100g"), receita.ingredientes.first())
    }

    @Test
    fun `item sem id e descartado`() {
        assertNull(MealDto.de(json("""{ "strMeal": "Sem id" }""")))
    }
}
