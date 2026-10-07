@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.cadernodereceitas.ui.caderno

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.cadernodereceitas.R
import br.com.cadernodereceitas.data.Tema
import br.com.cadernodereceitas.model.Receita
import br.com.cadernodereceitas.ui.componentes.CapaReceita
import br.com.cadernodereceitas.ui.componentes.EstadoCarregando
import br.com.cadernodereceitas.ui.componentes.EstadoErro
import br.com.cadernodereceitas.ui.componentes.EstadoMensagem
import br.com.cadernodereceitas.ui.componentes.resumoDaReceita

@Composable
fun CadernoRota(
    tema: Tema,
    onTemaChange: (Tema) -> Unit,
    onAbrirReceita: (Long) -> Unit,
    onNovaReceita: () -> Unit,
    viewModel: CadernoViewModel = viewModel(factory = CadernoViewModel.Fabrica),
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    CadernoTela(
        estado = estado,
        tema = tema,
        onTemaChange = onTemaChange,
        onFiltroChange = viewModel::selecionarFiltro,
        onAbrirReceita = onAbrirReceita,
        onAlternarFavorita = viewModel::alternarFavorita,
        onNovaReceita = onNovaReceita,
        onTentarNovamente = viewModel::tentarNovamente,
    )
}

@Composable
fun CadernoTela(
    estado: CadernoUiState,
    tema: Tema,
    onTemaChange: (Tema) -> Unit,
    onFiltroChange: (FiltroCaderno) -> Unit,
    onAbrirReceita: (Long) -> Unit,
    onAlternarFavorita: (Long) -> Unit,
    onNovaReceita: () -> Unit,
    onTentarNovamente: () -> Unit,
) {
    val rolagem = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(rolagem.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.titulo_caderno),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (estado.totalReceitas > 0 && rolagem.state.collapsedFraction < 0.5f) {
                            Text(
                                text = pluralStringResource(
                                    R.plurals.quantidade_receitas,
                                    estado.totalReceitas,
                                    estado.totalReceitas,
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = { MenuTema(tema, onTemaChange) },
                scrollBehavior = rolagem,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNovaReceita,
                icon = { Icon(painterResource(R.drawable.ic_adicionar), contentDescription = null) },
                text = { Text(stringResource(R.string.nova_receita)) },
            )
        },
    ) { espacamento ->
        Column(Modifier.padding(espacamento)) {
            FiltrosCaderno(selecionado = estado.filtro, onSelecionar = onFiltroChange)

            AnimatedContent(
                targetState = estado.lista,
                contentKey = { it::class },
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "lista do caderno",
            ) { lista ->
                when (lista) {
                    ListaCaderno.Carregando -> EstadoCarregando(stringResource(R.string.carregando_caderno))
                    ListaCaderno.Erro -> EstadoErro(stringResource(R.string.erro_caderno), onTentarNovamente)
                    ListaCaderno.Vazia -> ListaVazia(estado.filtro, onNovaReceita)
                    is ListaCaderno.Conteudo -> ListaReceitas(lista.receitas, onAbrirReceita, onAlternarFavorita)
                }
            }
        }
    }
}

@Composable
private fun FiltrosCaderno(selecionado: FiltroCaderno, onSelecionar: (FiltroCaderno) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(FiltroCaderno.entries) { filtro ->
            FilterChip(
                selected = filtro == selecionado,
                onClick = { onSelecionar(filtro) },
                label = { Text(stringResource(filtro.rotulo)) },
            )
        }
    }
}

private val FiltroCaderno.rotulo: Int
    get() = when (this) {
        FiltroCaderno.TODAS -> R.string.filtro_todas
        FiltroCaderno.FAVORITAS -> R.string.filtro_favoritas
        FiltroCaderno.SALGADOS -> R.string.categoria_salgados
        FiltroCaderno.DOCES -> R.string.categoria_doces
        FiltroCaderno.BEBIDAS -> R.string.categoria_bebidas
    }

@Composable
private fun ListaReceitas(
    receitas: List<Receita>,
    onAbrirReceita: (Long) -> Unit,
    onAlternarFavorita: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        // Espaço extra no fim para o último item não ficar escondido atrás do botão "Nova receita"
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
    ) {
        items(receitas, key = { it.id }) { receita ->
            Column(Modifier.animateItem()) {
                ItemReceita(
                    receita = receita,
                    onClick = { onAbrirReceita(receita.id) },
                    onAlternarFavorita = { onAlternarFavorita(receita.id) },
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 88.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}

@Composable
private fun ItemReceita(receita: Receita, onClick: () -> Unit, onAlternarFavorita: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        leadingContent = { CapaReceita(receita.foto, receita.nome, receita.categoria) },
        headlineContent = {
            Text(receita.nome, maxLines = 2, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = { Text(resumoDaReceita(receita)) },
        trailingContent = {
            BotaoFavorita(favorita = receita.favorita, onAlternar = onAlternarFavorita)
        },
    )
}

@Composable
fun BotaoFavorita(favorita: Boolean, onAlternar: () -> Unit) {
    IconToggleButton(checked = favorita, onCheckedChange = { onAlternar() }) {
        Icon(
            painter = painterResource(if (favorita) R.drawable.ic_estrela_cheia else R.drawable.ic_estrela_vazia),
            contentDescription = stringResource(
                if (favorita) R.string.desmarcar_favorita else R.string.marcar_favorita
            ),
            tint = if (favorita) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ListaVazia(filtro: FiltroCaderno, onNovaReceita: () -> Unit) {
    when (filtro) {
        FiltroCaderno.TODAS -> EstadoMensagem(
            icone = R.drawable.ic_caderno,
            titulo = stringResource(R.string.vazio_caderno_titulo),
            mensagem = stringResource(R.string.vazio_caderno_mensagem),
            textoAcao = stringResource(R.string.nova_receita),
            onAcao = onNovaReceita,
        )
        FiltroCaderno.FAVORITAS -> EstadoMensagem(
            icone = R.drawable.ic_estrela_vazia,
            titulo = stringResource(R.string.vazio_favoritas_titulo),
            mensagem = stringResource(R.string.vazio_favoritas_mensagem),
        )
        else -> EstadoMensagem(
            icone = R.drawable.ic_caderno,
            titulo = stringResource(R.string.vazio_categoria_titulo),
            mensagem = stringResource(R.string.vazio_categoria_mensagem),
        )
    }
}

@Composable
private fun MenuTema(tema: Tema, onTemaChange: (Tema) -> Unit) {
    var aberto by remember { mutableStateOf(false) }
    IconButton(onClick = { aberto = true }) {
        Icon(painterResource(R.drawable.ic_tema), contentDescription = stringResource(R.string.tema))
    }
    DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
        Tema.entries.forEach { opcao ->
            DropdownMenuItem(
                text = { Text(stringResource(opcao.rotulo)) },
                leadingIcon = { RadioButton(selected = opcao == tema, onClick = null) },
                onClick = {
                    onTemaChange(opcao)
                    aberto = false
                },
            )
        }
    }
}

private val Tema.rotulo: Int
    get() = when (this) {
        Tema.SISTEMA -> R.string.tema_sistema
        Tema.CLARO -> R.string.tema_claro
        Tema.ESCURO -> R.string.tema_escuro
    }
