package br.com.cadernodereceitas.ui.descobrir

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.cadernodereceitas.data.DescobertaRepository
import br.com.cadernodereceitas.data.ReceitaRepository
import br.com.cadernodereceitas.data.remoto.paraReceita
import br.com.cadernodereceitas.model.DetalheDescoberta
import br.com.cadernodereceitas.ui.container
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface DetalheDescobertaUiState {
    data object Carregando : DetalheDescobertaUiState
    data object NaoEncontrada : DetalheDescobertaUiState
    data class Erro(val tipo: TipoErro) : DetalheDescobertaUiState
    data class Conteudo(
        val receita: DetalheDescoberta,
        // id da receita no caderno, quando ela já foi salva
        val idNoCaderno: Long?,
        val salvando: Boolean,
    ) : DetalheDescobertaUiState
}

class DetalheDescobertaViewModel(
    private val id: String,
    private val descobertas: DescobertaRepository,
    private val receitas: ReceitaRepository,
) : ViewModel() {

    private sealed interface Carga {
        data object Carregando : Carga
        data class Pronta(val receita: DetalheDescoberta?) : Carga
        data class Falhou(val tipo: TipoErro) : Carga
    }

    private val carga = MutableStateFlow<Carga>(Carga.Carregando)
    private val salvando = MutableStateFlow(false)
    private var trabalhoCarga: Job? = null

    val uiState: StateFlow<DetalheDescobertaUiState> = combine(
        carga,
        receitas.observarIdNoCaderno(id),
        salvando,
    ) { carga, idNoCaderno, salvando ->
        when (carga) {
            Carga.Carregando -> DetalheDescobertaUiState.Carregando
            is Carga.Falhou -> DetalheDescobertaUiState.Erro(carga.tipo)
            is Carga.Pronta -> carga.receita
                ?.let { DetalheDescobertaUiState.Conteudo(it, idNoCaderno, salvando) }
                ?: DetalheDescobertaUiState.NaoEncontrada
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetalheDescobertaUiState.Carregando)

    init {
        carregar()
    }

    fun carregar() {
        trabalhoCarga?.cancel()
        trabalhoCarga = viewModelScope.launch {
            carga.value = Carga.Carregando
            carga.value = try {
                Carga.Pronta(descobertas.detalhar(id))
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                Carga.Falhou(TipoErro.SEM_CONEXAO)
            } catch (e: Exception) {
                Carga.Falhou(TipoErro.SERVIDOR)
            }
        }
    }

    fun salvarNoCaderno() {
        val receita = (carga.value as? Carga.Pronta)?.receita ?: return
        if (salvando.value) return
        viewModelScope.launch {
            salvando.value = true
            try {
                // O banco ignora a inserção se a receita já estiver salva, então tocar duas vezes não duplica
                receitas.salvar(receita.paraReceita())
            } finally {
                salvando.value = false
            }
        }
    }

    companion object {
        fun fabrica(id: String) = viewModelFactory {
            initializer { DetalheDescobertaViewModel(id, container.descobertas, container.receitas) }
        }
    }
}
