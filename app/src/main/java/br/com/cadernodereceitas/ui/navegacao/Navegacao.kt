package br.com.cadernodereceitas.ui.navegacao

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import br.com.cadernodereceitas.R
import br.com.cadernodereceitas.data.Tema
import br.com.cadernodereceitas.ui.caderno.CadernoRota
import br.com.cadernodereceitas.ui.descobrir.DescobrirRota
import br.com.cadernodereceitas.ui.descobrir.DetalheDescobertaRota
import br.com.cadernodereceitas.ui.detalhe.DetalheRota
import br.com.cadernodereceitas.ui.formulario.FormularioRota
import kotlinx.serialization.Serializable

// Rotas do app. São @Serializable para a pilha de navegação sobreviver à rotação e à morte do processo.
@Serializable data object RotaCaderno : NavKey
@Serializable data object RotaDescobrir : NavKey
@Serializable data class RotaDetalhe(val id: Long) : NavKey
@Serializable data class RotaFormulario(val id: Long? = null) : NavKey
@Serializable data class RotaDetalheDescoberta(val id: String) : NavKey

private enum class Aba(val rota: NavKey, @StringRes val titulo: Int, @DrawableRes val icone: Int) {
    CADERNO(RotaCaderno, R.string.aba_caderno, R.drawable.ic_caderno),
    DESCOBRIR(RotaDescobrir, R.string.aba_descobrir, R.drawable.ic_descobrir),
}

@Composable
fun AppNavegacao(tema: Tema, onTemaChange: (Tema) -> Unit) {
    // O Caderno fica sempre na base da pilha; voltar a partir de Descobrir leva a ele
    val pilha = rememberNavBackStack(RotaCaderno)
    val topo = pilha.last()
    val mostrarAbas = topo == RotaCaderno || topo == RotaDescobrir

    Scaffold(
        // Cada tela cuida das próprias margens do sistema; aqui só entra a barra de abas
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (mostrarAbas) {
                NavigationBar {
                    Aba.entries.forEach { aba ->
                        NavigationBarItem(
                            selected = topo == aba.rota,
                            onClick = { pilha.irParaAba(aba.rota) },
                            icon = { Icon(painterResource(aba.icone), contentDescription = null) },
                            label = { Text(stringResource(aba.titulo)) },
                        )
                    }
                }
            }
        },
    ) { espacamento ->
        NavDisplay(
            backStack = pilha,
            onBack = { pilha.removeLastOrNull() },
            modifier = Modifier
                .padding(espacamento)
                .consumeWindowInsets(espacamento),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                // Cada tela da pilha ganha seus próprios ViewModels, descartados quando ela sai da pilha
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<RotaCaderno> {
                    CadernoRota(
                        tema = tema,
                        onTemaChange = onTemaChange,
                        onAbrirReceita = { pilha.add(RotaDetalhe(it)) },
                        onNovaReceita = { pilha.add(RotaFormulario()) },
                    )
                }
                entry<RotaDetalhe> { rota ->
                    DetalheRota(
                        id = rota.id,
                        onVoltar = { pilha.removeLastOrNull() },
                        onEditar = { pilha.add(RotaFormulario(it)) },
                    )
                }
                entry<RotaFormulario> { rota ->
                    FormularioRota(
                        idReceita = rota.id,
                        onFechar = { pilha.removeLastOrNull() },
                        onSalvo = { id ->
                            pilha.removeLastOrNull()
                            // Receita nova: abre o detalhe dela. Edição: volta para o detalhe que já estava aberto.
                            if (rota.id == null) pilha.add(RotaDetalhe(id))
                        },
                    )
                }
                entry<RotaDescobrir> {
                    DescobrirRota(onAbrirDescoberta = { pilha.add(RotaDetalheDescoberta(it)) })
                }
                entry<RotaDetalheDescoberta> { rota ->
                    DetalheDescobertaRota(
                        id = rota.id,
                        onVoltar = { pilha.removeLastOrNull() },
                        onAbrirNoCaderno = { pilha.add(RotaDetalhe(it)) },
                    )
                }
            },
        )
    }
}

private fun NavBackStack<NavKey>.irParaAba(aba: NavKey) {
    if (last() == aba) return
    // Mantém só a base (Caderno) e empilha a aba escolhida por cima
    while (size > 1) removeAt(lastIndex)
    if (aba != RotaCaderno) add(aba)
}
