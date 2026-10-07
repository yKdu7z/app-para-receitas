package br.com.cadernodereceitas.data

import android.content.ContentResolver
import android.net.Uri
import br.com.cadernodereceitas.data.local.ReceitaDao
import br.com.cadernodereceitas.data.local.ReceitasIniciais
import br.com.cadernodereceitas.data.local.paraEntidade
import br.com.cadernodereceitas.data.local.paraModelo
import br.com.cadernodereceitas.model.Receita
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

/** Receitas do caderno do usuário, guardadas no aparelho. */
interface ReceitaRepository {
    fun observarReceitas(): Flow<List<Receita>>
    fun observarReceita(id: Long): Flow<Receita?>
    fun observarIdNoCaderno(idOrigem: String): Flow<Long?>
    suspend fun buscarReceita(id: Long): Receita?

    /** Insere (id = 0) ou atualiza a receita e devolve o id dela no caderno. */
    suspend fun salvar(receita: Receita): Long
    suspend fun alternarFavorita(id: Long)
    suspend fun excluir(id: Long)

    /** Copia a imagem escolhida para a pasta do app e devolve o endereço da cópia. */
    suspend fun importarFoto(uri: Uri): String
}

class ReceitaRepositoryRoom(
    private val dao: ReceitaDao,
    private val preferencias: PreferenciasRepository,
    private val contentResolver: ContentResolver,
    private val pastaFotos: File,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) : ReceitaRepository {

    private val travaReceitasIniciais = Mutex()

    override fun observarReceitas(): Flow<List<Receita>> = flow {
        gravarReceitasIniciaisSeNecessario()
        emitAll(dao.observarTodas().map { lista -> lista.map { it.paraModelo() } })
    }

    override fun observarReceita(id: Long): Flow<Receita?> =
        dao.observar(id).map { it?.paraModelo() }

    override fun observarIdNoCaderno(idOrigem: String): Flow<Long?> =
        dao.observarIdPorOrigem(idOrigem)

    override suspend fun buscarReceita(id: Long): Receita? = dao.buscar(id)?.paraModelo()

    override suspend fun salvar(receita: Receita): Long {
        if (receita.id == 0L) {
            val id = dao.inserir(receita.paraEntidade())
            // Já estava no caderno: devolve o id existente em vez de duplicar
            if (id == -1L && receita.idOrigem != null) {
                return dao.buscarIdPorOrigem(receita.idOrigem) ?: id
            }
            return id
        }

        val anterior = dao.buscar(receita.id)
        dao.atualizar(receita.paraEntidade())
        if (anterior?.foto != receita.foto) apagarFotoLocal(anterior?.foto)
        return receita.id
    }

    override suspend fun alternarFavorita(id: Long) = dao.alternarFavorita(id)

    override suspend fun excluir(id: Long) {
        val receita = dao.buscar(id)
        dao.excluir(id)
        apagarFotoLocal(receita?.foto)
    }

    override suspend fun importarFoto(uri: Uri): String = withContext(io) {
        pastaFotos.mkdirs()
        val destino = File(pastaFotos, "${UUID.randomUUID()}.jpg")
        val entrada = contentResolver.openInputStream(uri)
            ?: throw IOException("Não foi possível abrir a imagem")
        entrada.use { origem -> destino.outputStream().use { origem.copyTo(it) } }
        Uri.fromFile(destino).toString()
    }

    private suspend fun gravarReceitasIniciaisSeNecessario() = travaReceitasIniciais.withLock {
        if (!preferencias.receitasIniciaisGravadas()) {
            dao.inserirTodas(ReceitasIniciais.lista.map { it.paraEntidade() })
            preferencias.marcarReceitasIniciaisGravadas()
        }
    }

    // Só apaga fotos copiadas para a pasta do app; URLs da internet não são arquivos nossos
    private suspend fun apagarFotoLocal(foto: String?) {
        if (foto == null || !foto.startsWith("file:")) return
        withContext(io) { Uri.parse(foto).path?.let { File(it).delete() } }
    }
}
