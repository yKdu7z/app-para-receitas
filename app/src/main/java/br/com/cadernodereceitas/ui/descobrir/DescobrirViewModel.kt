package br.com.cadernodereceitas.ui.descobrir

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.cadernodereceitas.data.DescobertaRepository
import br.com.cadernodereceitas.model.ReceitaDescoberta
import br.com.cadernodereceitas.ui.container
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.io.IOException

enum class TipoErro { SEM_CONEXAO, SERVIDOR }

sealed interface ResultadoBusca {
    data object Carregando : ResultadoBusca
    data class Vazio(val termo: String) : ResultadoBusca
    data class Erro(val tipo: TipoErro) : ResultadoBusca
    data class Conteudo(val termo: String, val receitas: List<ReceitaDescoberta>) : ResultadoBusca
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class DescobrirViewModel(
    private val repositorio: DescobertaRepository,
    private val estadoSalvo: SavedStateHandle,
) : ViewModel() {

    val termo: StateFlow<String> = estadoSalvo.getStateFlow(CHAVE_TERMO, "")
    private val tentativa = MutableStateFlow(0)

    val resultado: StateFlow<ResultadoBusca> = combine(
        // Espera o usuário parar de digitar antes de chamar a API
        termo.debounce(TEMPO_ESPERA_DIGITACAO_MS).map { it.trim() }.distinctUntilChanged(),
        tentativa,
    ) { termo, _ -> termo }
        // flatMapLatest cancela a busca anterior quando chega um termo novo
        .flatMapLatest { termo -> buscar(termo) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResultadoBusca.Carregando)

    private fun buscar(termo: String) = flow {
        emit(ResultadoBusca.Carregando)
        val resultado = try {
            val receitas = repositorio.buscar(termo)
            if (receitas.isEmpty()) ResultadoBusca.Vazio(termo) else ResultadoBusca.Conteudo(termo, receitas)
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            ResultadoBusca.Erro(TipoErro.SEM_CONEXAO)
        } catch (e: Exception) {
            ResultadoBusca.Erro(TipoErro.SERVIDOR)
        }
        emit(resultado)
    }

    fun alterarTermo(novo: String) {
        estadoSalvo[CHAVE_TERMO] = novo.take(50)
    }

    fun tentarNovamente() {
        tentativa.update { it + 1 }
    }

    companion object {
        private const val CHAVE_TERMO = "termo"
        private const val TEMPO_ESPERA_DIGITACAO_MS = 400L

        val Fabrica = viewModelFactory {
            initializer { DescobrirViewModel(container.descobertas, createSavedStateHandle()) }
        }
    }
}
