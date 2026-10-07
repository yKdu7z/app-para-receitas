package br.com.cadernodereceitas

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import br.com.cadernodereceitas.data.DescobertaRepository
import br.com.cadernodereceitas.data.DescobertaRepositoryMealDb
import br.com.cadernodereceitas.data.PreferenciasRepository
import br.com.cadernodereceitas.data.ReceitaRepository
import br.com.cadernodereceitas.data.ReceitaRepositoryRoom
import br.com.cadernodereceitas.data.local.BancoDeDados
import br.com.cadernodereceitas.data.remoto.MealDbApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

private val Context.dataStore by preferencesDataStore(name = "preferencias")

/** Injeção de dependências manual: cria cada dependência uma única vez para o app todo. */
class AppContainer(context: Context) {

    private val banco = BancoDeDados.criar(context)

    val preferencias = PreferenciasRepository(context.dataStore)

    val receitas: ReceitaRepository = ReceitaRepositoryRoom(
        dao = banco.receitaDao(),
        preferencias = preferencias,
        contentResolver = context.contentResolver,
        pastaFotos = File(context.filesDir, "fotos"),
    )

    private val api: MealDbApi by lazy {
        val json = Json { ignoreUnknownKeys = true }
        val cliente = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/api/json/v1/${BuildConfig.MEALDB_API_KEY}/")
            .client(cliente)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(MealDbApi::class.java)
    }

    val descobertas: DescobertaRepository by lazy { DescobertaRepositoryMealDb(api) }
}
