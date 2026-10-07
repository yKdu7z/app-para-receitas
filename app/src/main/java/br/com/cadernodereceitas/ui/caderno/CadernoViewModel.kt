package br.com.cadernodereceitas.ui.caderno

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.cadernodereceitas.data.ReceitaRepository
import br.com.cadernodereceitas.model.Categoria
import br.com.cadernodereceitas.model.Receita
import br.com.cadernodereceitas.ui.container
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FiltroCaderno { TODAS, FAVORITAS, SALGADOS, DOCES, BEBIDAS }

sealed interface ListaCaderno {
    data object Carregando : ListaCaderno
    data object Vazia : ListaCaderno
    data object Erro : ListaCaderno
    data class Conteudo(val receitas: List<Receita>) : ListaCaderno
}

data class CadernoUiState(
    val filtro: FiltroCaderno = FiltroCaderno.TODAS,
    val totalReceitas: Int = 0,
    val lista: ListaCaderno = ListaCaderno.Carregando,
)

@OptIn(ExperimentalCoroutinesApi::class)
class CadernoViewModel(
    private val repositorio: ReceitaRepository,
    private val estadoSalvo: SavedStateHandle,
) : ViewModel() {

    // Fica no SavedStateHandle para o filtro sobreviver até à morte do processo
    private val filtro = estadoSalvo.getStateFlow(CHAVE_FILTRO, FiltroCaderno.TODAS)
    private val tentativa = MutableStateFlow(0)

    private val receitas = tentativa.flatMapLatest {
        repositorio.observarReceitas()
            .map<List<Receita>, Result<List<Receita>>> { Result.success(it) }
            .catch { emit(Result.failure(it)) }
    }

    val uiState: StateFlow<CadernoUiState> = combine(receitas, filtro) { resultado, filtro ->
        val todas = resultado.getOrNull()
        CadernoUiState(
            filtro = filtro,
            totalReceitas = todas?.size ?: 0,
            lista = when {
                todas == null -> ListaCaderno.Erro
                else -> todas.filtradas(filtro).let {
                    if (it.isEmpty()) ListaCaderno.Vazia else ListaCaderno.Conteudo(it)
                }
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CadernoUiState(filtro = filtro.value),
    )

    fun selecionarFiltro(novo: FiltroCaderno) {
        estadoSalvo[CHAVE_FILTRO] = novo
    }

    fun alternarFavorita(id: Long) {
        viewModelScope.launch { repositorio.alternarFavorita(id) }
    }

    fun tentarNovamente() {
        tentativa.update { it + 1 }
    }

    companion object {
        private const val CHAVE_FILTRO = "filtro"

        val Fabrica = viewModelFactory {
            initializer { CadernoViewModel(container.receitas, createSavedStateHandle()) }
        }
    }
}

fun List<Receita>.filtradas(filtro: FiltroCaderno): List<Receita> = when (filtro) {
    FiltroCaderno.TODAS -> this
    FiltroCaderno.FAVORITAS -> filter { it.favorita }
    FiltroCaderno.SALGADOS -> filter { it.categoria == Categoria.SALGADOS }
    FiltroCaderno.DOCES -> filter { it.categoria == Categoria.DOCES }
    FiltroCaderno.BEBIDAS -> filter { it.categoria == Categoria.BEBIDAS }
}
