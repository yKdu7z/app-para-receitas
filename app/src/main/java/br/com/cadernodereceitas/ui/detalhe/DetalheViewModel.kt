package br.com.cadernodereceitas.ui.detalhe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.cadernodereceitas.data.ReceitaRepository
import br.com.cadernodereceitas.model.Receita
import br.com.cadernodereceitas.ui.container
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DetalheUiState {
    data object Carregando : DetalheUiState
    data object NaoEncontrada : DetalheUiState
    data object Excluida : DetalheUiState
    data class Conteudo(
        val receita: Receita,
        // Posições dos ingredientes que o usuário já marcou como separados
        val separados: Set<Int>,
    ) : DetalheUiState
}

class DetalheViewModel(
    private val id: Long,
    private val repositorio: ReceitaRepository,
    private val estadoSalvo: SavedStateHandle,
) : ViewModel() {

    private val separados = estadoSalvo.getStateFlow(CHAVE_SEPARADOS, arrayListOf<Int>())
    private val excluida = MutableStateFlow(false)

    val uiState: StateFlow<DetalheUiState> = combine(
        repositorio.observarReceita(id),
        separados,
        excluida,
    ) { receita, separados, excluida ->
        when {
            excluida -> DetalheUiState.Excluida
            receita == null -> DetalheUiState.NaoEncontrada
            else -> DetalheUiState.Conteudo(receita, separados.toSet())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetalheUiState.Carregando)

    fun alternarIngrediente(posicao: Int) {
        val atual = separados.value
        estadoSalvo[CHAVE_SEPARADOS] = ArrayList(if (posicao in atual) atual - posicao else atual + posicao)
    }

    fun alternarFavorita() {
        viewModelScope.launch { repositorio.alternarFavorita(id) }
    }

    fun excluir() {
        viewModelScope.launch {
            excluida.value = true
            repositorio.excluir(id)
        }
    }

    companion object {
        private const val CHAVE_SEPARADOS = "separados"

        fun fabrica(id: Long) = viewModelFactory {
            initializer { DetalheViewModel(id, container.receitas, createSavedStateHandle()) }
        }
    }
}
