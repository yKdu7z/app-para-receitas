package br.com.cadernodereceitas.data.remoto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import retrofit2.http.GET
import retrofit2.http.Query

/** API pública da TheMealDB (https://www.themealdb.com/api.php). */
interface MealDbApi {

    @GET("search.php")
    suspend fun buscar(@Query("s") termo: String): RespostaMealDb

    @GET("lookup.php")
    suspend fun detalhar(@Query("i") id: String): RespostaMealDb
}

// A API devolve "meals": null quando não encontra nada.
// Cada item é lido como JsonObject porque os ingredientes vêm em campos numerados.
@Serializable
data class RespostaMealDb(val meals: List<JsonObject>? = null)
