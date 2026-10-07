package br.com.cadernodereceitas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.cadernodereceitas.data.PreferenciasRepository
import br.com.cadernodereceitas.data.Tema
import br.com.cadernodereceitas.ui.container
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Aparência escolhida pelo usuário (claro, escuro ou igual ao sistema), salva no DataStore. */
class TemaViewModel(private val preferencias: PreferenciasRepository) : ViewModel() {

    val tema: StateFlow<Tema> = preferencias.tema
        .stateIn(viewModelScope, SharingStarted.Eagerly, Tema.SISTEMA)

    fun definirTema(tema: Tema) {
        viewModelScope.launch { preferencias.definirTema(tema) }
    }

    companion object {
        val Fabrica = viewModelFactory {
            initializer { TemaViewModel(container.preferencias) }
        }
    }
}
