package br.com.cadernodereceitas.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.cadernodereceitas.model.Categoria
import kotlinx.serialization.Serializable

@Serializable
data class IngredienteSalvo(
    val nome: String,
    val quantidade: String? = null,
)

// O índice único em idOrigem impede salvar a mesma receita da internet duas vezes
@Entity(tableName = "receitas", indices = [Index(value = ["idOrigem"], unique = true)])
data class ReceitaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val categoria: Categoria,
    val tempoPreparoMin: Int?,
    val rendimento: String?,
    val ingredientes: List<IngredienteSalvo>,
    val passos: List<String>,
    val dica: String?,
    val favorita: Boolean,
    val foto: String?,
    val idOrigem: String?,
)
