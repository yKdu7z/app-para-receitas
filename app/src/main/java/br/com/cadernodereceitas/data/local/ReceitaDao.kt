package br.com.cadernodereceitas.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceitaDao {

    @Query("SELECT * FROM receitas ORDER BY nome COLLATE NOCASE")
    fun observarTodas(): Flow<List<ReceitaEntity>>

    @Query("SELECT * FROM receitas WHERE id = :id")
    fun observar(id: Long): Flow<ReceitaEntity?>

    @Query("SELECT * FROM receitas WHERE id = :id")
    suspend fun buscar(id: Long): ReceitaEntity?

    @Query("SELECT id FROM receitas WHERE idOrigem = :idOrigem")
    fun observarIdPorOrigem(idOrigem: String): Flow<Long?>

    @Query("SELECT id FROM receitas WHERE idOrigem = :idOrigem")
    suspend fun buscarIdPorOrigem(idOrigem: String): Long?

    /** Retorna -1 se a receita já existir (mesmo idOrigem). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserir(receita: ReceitaEntity): Long

    @Insert
    suspend fun inserirTodas(receitas: List<ReceitaEntity>)

    @Update
    suspend fun atualizar(receita: ReceitaEntity)

    @Query("UPDATE receitas SET favorita = NOT favorita WHERE id = :id")
    suspend fun alternarFavorita(id: Long)

    @Query("DELETE FROM receitas WHERE id = :id")
    suspend fun excluir(id: Long)
}
