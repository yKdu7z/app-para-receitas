package br.com.cadernodereceitas.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.json.Json

@Database(entities = [ReceitaEntity::class], version = 1, exportSchema = false)
@TypeConverters(Conversores::class)
abstract class BancoDeDados : RoomDatabase() {

    abstract fun receitaDao(): ReceitaDao

    companion object {
        fun criar(context: Context): BancoDeDados =
            Room.databaseBuilder(context, BancoDeDados::class.java, "caderno.db").build()
    }
}

/** Listas são gravadas como JSON em uma coluna de texto. */
class Conversores {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun ingredientesParaTexto(lista: List<IngredienteSalvo>): String = json.encodeToString(lista)

    @TypeConverter
    fun textoParaIngredientes(texto: String): List<IngredienteSalvo> = json.decodeFromString(texto)

    @TypeConverter
    fun passosParaTexto(lista: List<String>): String = json.encodeToString(lista)

    @TypeConverter
    fun textoParaPassos(texto: String): List<String> = json.decodeFromString(texto)
}
