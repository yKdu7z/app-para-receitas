package br.com.cadernodereceitas.ui.caderno

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import br.com.cadernodereceitas.data.ReceitaRepository
import br.com.cadernodereceitas.model.Categoria
import br.com.cadernodereceitas.model.Receita
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** Repositório em memória usado no lugar do Room nos testes. */
private class ReceitaRepositoryFake(iniciais: List<Receita>) : ReceitaRepository {
    val receitas = MutableStateFlow(iniciais)
    var falhar = false

    override fun observarReceitas(): Flow<List<Receita>> =
        if (falhar) flow { throw IllegalStateException("banco indisponível") } else receitas

    override fun observarReceita(id: Long) = receitas.map { lista -> lista.find { it.id == id } }
    override fun observarIdNoCaderno(idOrigem: String) =
        receitas.map { lista -> lista.find { it.idOrigem == idOrigem }?.id }
    override suspend fun buscarReceita(id: Long) = receitas.value.find { it.id == id }
    override suspend fun salvar(receita: Receita) = receita.id
    override suspend fun alternarFavorita(id: Long) = receitas.update { lista ->
        lista.map { if (it.id == id) it.copy(favorita = !it.favorita) else it }
    }
    override suspend fun excluir(id: Long) = receitas.update { lista -> lista.filterNot { it.id == id } }
    override suspend fun importarFoto(uri: Uri) = error("não usado")
}

private fun receita(id: Long, categoria: Categoria, favorita: Boolean = false) = Receita(
    id = id,
    nome = "Receita $id",
    categoria = categoria,
    tempoPreparoMin = 10,
    rendimento = null,
    ingredientes = emptyList(),
    passos = listOf("Passo"),
    favorita = favorita,
)

@OptIn(ExperimentalCoroutinesApi::class)
class CadernoViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun preparar() = Dispatchers.setMain(dispatcher)

    @After
    fun limpar() = Dispatchers.resetMain()

    private val repositorio = ReceitaRepositoryFake(
        listOf(
            receita(1, Categoria.DOCES, favorita = true),
            receita(2, Categoria.SALGADOS),
            receita(3, Categoria.DOCES),
        )
    )

    @Test
    fun `mostra todas as receitas e o total`() = runTest(dispatcher) {
        val viewModel = CadernoViewModel(repositorio, SavedStateHandle())
        backgroundScope.launch { viewModel.uiState.collect {} }

        val estado = viewModel.uiState.value
        assertEquals(3, estado.totalReceitas)
        assertEquals(listOf(1L, 2L, 3L), (estado.lista as ListaCaderno.Conteudo).receitas.map { it.id })
    }

    @Test
    fun `filtro por categoria e favoritas`() = runTest(dispatcher) {
        val viewModel = CadernoViewModel(repositorio, SavedStateHandle())
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.selecionarFiltro(FiltroCaderno.DOCES)
        assertEquals(listOf(1L, 3L), (viewModel.uiState.value.lista as ListaCaderno.Conteudo).receitas.map { it.id })

        viewModel.selecionarFiltro(FiltroCaderno.FAVORITAS)
        assertEquals(listOf(1L), (viewModel.uiState.value.lista as ListaCaderno.Conteudo).receitas.map { it.id })
    }

    @Test
    fun `lista fica vazia quando nenhuma receita combina com o filtro`() = runTest(dispatcher) {
        val viewModel = CadernoViewModel(repositorio, SavedStateHandle())
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.selecionarFiltro(FiltroCaderno.BEBIDAS)
        assertEquals(ListaCaderno.Vazia, viewModel.uiState.value.lista)
    }

    @Test
    fun `favoritar atualiza a lista de forma reativa`() = runTest(dispatcher) {
        val viewModel = CadernoViewModel(repositorio, SavedStateHandle())
        backgroundScope.launch { viewModel.uiState.collect {} }
        viewModel.selecionarFiltro(FiltroCaderno.FAVORITAS)

        viewModel.alternarFavorita(2)

        assertEquals(listOf(1L, 2L), (viewModel.uiState.value.lista as ListaCaderno.Conteudo).receitas.map { it.id })
    }

    @Test
    fun `erro no banco vira estado de erro`() = runTest(dispatcher) {
        repositorio.falhar = true
        val viewModel = CadernoViewModel(repositorio, SavedStateHandle())
        backgroundScope.launch { viewModel.uiState.collect {} }

        assertEquals(ListaCaderno.Erro, viewModel.uiState.value.lista)
    }

    @Test
    fun `filtro escolhido e restaurado do SavedStateHandle`() = runTest(dispatcher) {
        val viewModel = CadernoViewModel(repositorio, SavedStateHandle(mapOf("filtro" to FiltroCaderno.SALGADOS)))
        backgroundScope.launch { viewModel.uiState.collect {} }

        assertEquals(FiltroCaderno.SALGADOS, viewModel.uiState.value.filtro)
        assertEquals(listOf(2L), (viewModel.uiState.value.lista as ListaCaderno.Conteudo).receitas.map { it.id })
    }
}
