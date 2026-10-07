package br.com.cadernodereceitas.ui.formulario

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.cadernodereceitas.R
import br.com.cadernodereceitas.data.ReceitaRepository
import br.com.cadernodereceitas.model.Categoria
import br.com.cadernodereceitas.model.Ingrediente
import br.com.cadernodereceitas.model.Receita
import br.com.cadernodereceitas.ui.container
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormularioUiState(
    val carregando: Boolean = false,
    val editando: Boolean = false,
    val nome: String = "",
    val categoria: Categoria = Categoria.SALGADOS,
    val tempo: String = "",
    val rendimento: String = "",
    val ingredientes: List<CampoIngrediente> = listOf(CampoIngrediente(chave = 0)),
    val passos: String = "",
    val dica: String = "",
    val foto: String? = null,
    val erros: ErrosFormulario = ErrosFormulario(),
    // Mensagem geral mostrada perto do botão salvar
    @StringRes val aviso: Int? = null,
    val salvando: Boolean = false,
    // Preenchido quando a receita foi gravada; a tela usa para sair do formulário
    val idSalvo: Long? = null,
)

class FormularioViewModel(
    private val idReceita: Long?,
    private val repositorio: ReceitaRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormularioUiState(carregando = idReceita != null, editando = idReceita != null))
    val uiState: StateFlow<FormularioUiState> = _uiState.asStateFlow()

    // Mantém favorita e idOrigem da receita original ao editar
    private var original: Receita? = null
    private var proximaChave = 1L

    init {
        if (idReceita != null) carregar(idReceita)
    }

    private fun carregar(id: Long) {
        viewModelScope.launch {
            val receita = repositorio.buscarReceita(id)
            original = receita
            _uiState.update { estado ->
                if (receita == null) return@update estado.copy(carregando = false)
                estado.copy(
                    carregando = false,
                    nome = receita.nome,
                    categoria = receita.categoria,
                    tempo = receita.tempoPreparoMin?.toString().orEmpty(),
                    rendimento = receita.rendimento.orEmpty(),
                    ingredientes = receita.ingredientes.map {
                        CampoIngrediente(proximaChave++, it.quantidade.orEmpty(), it.nome)
                    }.ifEmpty { listOf(CampoIngrediente(proximaChave++)) },
                    passos = receita.passos.joinToString("\n"),
                    dica = receita.dica.orEmpty(),
                    foto = receita.foto,
                )
            }
        }
    }

    // Ao editar um campo, o erro dele some; os outros continuam visíveis até salvar de novo
    fun alterarNome(valor: String) = _uiState.update {
        it.copy(nome = valor.take(TAMANHO_MAXIMO_NOME + 20), erros = it.erros.copy(nome = null))
    }

    fun alterarCategoria(valor: Categoria) = _uiState.update { it.copy(categoria = valor) }

    fun alterarTempo(valor: String) = _uiState.update {
        it.copy(tempo = valor.filter(Char::isDigit).take(4), erros = it.erros.copy(tempo = null))
    }

    fun alterarRendimento(valor: String) = _uiState.update { it.copy(rendimento = valor.take(40)) }

    fun alterarPassos(valor: String) = _uiState.update {
        it.copy(passos = valor, erros = it.erros.copy(passos = null))
    }

    fun alterarDica(valor: String) = _uiState.update { it.copy(dica = valor.take(300)) }

    fun alterarIngrediente(chave: Long, quantidade: String, nome: String) = _uiState.update { estado ->
        estado.copy(
            ingredientes = estado.ingredientes.map {
                if (it.chave == chave) it.copy(quantidade = quantidade.take(30), nome = nome.take(60)) else it
            },
            erros = estado.erros.copy(ingredientes = null),
        )
    }

    fun adicionarIngrediente() = _uiState.update {
        it.copy(ingredientes = it.ingredientes + CampoIngrediente(proximaChave++))
    }

    fun removerIngrediente(chave: Long) = _uiState.update { estado ->
        estado.copy(ingredientes = estado.ingredientes.filterNot { it.chave == chave })
    }

    fun escolherFoto(uri: Uri) {
        viewModelScope.launch {
            try {
                val foto = repositorio.importarFoto(uri)
                _uiState.update { it.copy(foto = foto, aviso = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(aviso = R.string.erro_foto) }
            }
        }
    }

    fun removerFoto() = _uiState.update { it.copy(foto = null) }

    fun salvar() {
        val estado = _uiState.value
        if (estado.salvando || estado.carregando) return

        val erros = validarReceita(estado.nome, estado.tempo, estado.ingredientes, estado.passos)
        if (erros.temErro) {
            _uiState.update { it.copy(erros = erros, aviso = R.string.erro_corrigir_campos) }
            return
        }

        val receita = Receita(
            id = original?.id ?: 0,
            nome = estado.nome.trim(),
            categoria = estado.categoria,
            tempoPreparoMin = estado.tempo.toIntOrNull(),
            rendimento = estado.rendimento.trim().ifEmpty { null },
            ingredientes = estado.ingredientes
                .filter { it.nome.isNotBlank() }
                .map { Ingrediente(it.nome.trim(), it.quantidade.trim().ifEmpty { null }) },
            passos = separarPassos(estado.passos),
            dica = estado.dica.trim().ifEmpty { null },
            favorita = original?.favorita ?: false,
            foto = estado.foto,
            idOrigem = original?.idOrigem,
        )

        _uiState.update { it.copy(salvando = true, erros = ErrosFormulario(), aviso = null) }
        viewModelScope.launch {
            try {
                val id = repositorio.salvar(receita)
                _uiState.update { it.copy(salvando = false, idSalvo = id) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(salvando = false, aviso = R.string.erro_salvar) }
            }
        }
    }

    companion object {
        fun fabrica(idReceita: Long?) = viewModelFactory {
            initializer { FormularioViewModel(idReceita, container.receitas) }
        }
    }
}
