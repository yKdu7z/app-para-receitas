@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.cadernodereceitas.ui.detalhe

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.cadernodereceitas.R
import br.com.cadernodereceitas.model.Ingrediente
import br.com.cadernodereceitas.model.Receita
import br.com.cadernodereceitas.ui.caderno.BotaoFavorita
import br.com.cadernodereceitas.ui.componentes.CartaoDica
import br.com.cadernodereceitas.ui.componentes.EstadoCarregando
import br.com.cadernodereceitas.ui.componentes.EstadoMensagem
import br.com.cadernodereceitas.ui.componentes.PassoPreparo
import br.com.cadernodereceitas.ui.componentes.TituloSecao
import br.com.cadernodereceitas.ui.componentes.formatarTempo
import br.com.cadernodereceitas.ui.componentes.rotulo
import br.com.cadernodereceitas.ui.tema.LocalCoresCaderno
import coil3.compose.AsyncImage

@Composable
fun DetalheRota(
    id: Long,
    onVoltar: () -> Unit,
    onEditar: (Long) -> Unit,
    viewModel: DetalheViewModel = viewModel(factory = DetalheViewModel.fabrica(id)),
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(estado) {
        if (estado is DetalheUiState.Excluida) onVoltar()
    }

    DetalheTela(
        estado = estado,
        onVoltar = onVoltar,
        onEditar = { onEditar(id) },
        onExcluir = viewModel::excluir,
        onAlternarFavorita = viewModel::alternarFavorita,
        onAlternarIngrediente = viewModel::alternarIngrediente,
    )
}

@Composable
fun DetalheTela(
    estado: DetalheUiState,
    onVoltar: () -> Unit,
    onEditar: () -> Unit,
    onExcluir: () -> Unit,
    onAlternarFavorita: () -> Unit,
    onAlternarIngrediente: (Int) -> Unit,
) {
    val context = LocalContext.current
    var confirmandoExclusao by rememberSaveable { mutableStateOf(false) }
    val conteudo = estado as? DetalheUiState.Conteudo

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(painterResource(R.drawable.ic_voltar), stringResource(R.string.voltar))
                    }
                },
                actions = {
                    if (conteudo != null) {
                        BotaoFavorita(conteudo.receita.favorita, onAlternarFavorita)
                        IconButton(onClick = { compartilhar(context, conteudo.receita) }) {
                            Icon(painterResource(R.drawable.ic_compartilhar), stringResource(R.string.compartilhar))
                        }
                        MenuMaisOpcoes(onEditar = onEditar, onExcluir = { confirmandoExclusao = true })
                    }
                },
            )
        },
    ) { espacamento ->
        when (estado) {
            DetalheUiState.Carregando, DetalheUiState.Excluida ->
                EstadoCarregando(stringResource(R.string.carregando_receita), Modifier.padding(espacamento))
            DetalheUiState.NaoEncontrada -> EstadoMensagem(
                icone = R.drawable.ic_caderno,
                titulo = stringResource(R.string.receita_nao_encontrada),
                mensagem = null,
                textoAcao = stringResource(R.string.voltar),
                onAcao = onVoltar,
                modifier = Modifier.padding(espacamento),
            )
            is DetalheUiState.Conteudo -> ConteudoReceita(
                receita = estado.receita,
                separados = estado.separados,
                onAlternarIngrediente = onAlternarIngrediente,
                espacamento = espacamento,
            )
        }
    }

    if (confirmandoExclusao && conteudo != null) {
        AlertDialog(
            onDismissRequest = { confirmandoExclusao = false },
            title = { Text(stringResource(R.string.excluir_titulo)) },
            text = { Text(stringResource(R.string.excluir_mensagem, conteudo.receita.nome)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmandoExclusao = false
                    onExcluir()
                }) { Text(stringResource(R.string.excluir)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmandoExclusao = false }) { Text(stringResource(R.string.cancelar)) }
            },
        )
    }
}

@Composable
private fun MenuMaisOpcoes(onEditar: () -> Unit, onExcluir: () -> Unit) {
    var aberto by rememberSaveable { mutableStateOf(false) }
    IconButton(onClick = { aberto = true }) {
        Icon(painterResource(R.drawable.ic_mais_opcoes), stringResource(R.string.mais_opcoes))
    }
    DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.editar)) },
            leadingIcon = { Icon(painterResource(R.drawable.ic_editar), null) },
            onClick = {
                aberto = false
                onEditar()
            },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.excluir)) },
            leadingIcon = { Icon(painterResource(R.drawable.ic_excluir), null) },
            onClick = {
                aberto = false
                onExcluir()
            },
        )
    }
}

@Composable
private fun ConteudoReceita(
    receita: Receita,
    separados: Set<Int>,
    onAlternarIngrediente: (Int) -> Unit,
    espacamento: PaddingValues,
) {
    val corCategoria = LocalCoresCaderno.current.daCategoria(receita.categoria)
    val total = receita.ingredientes.size

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = espacamento.calculateTopPadding(),
            bottom = espacamento.calculateBottomPadding() + 32.dp,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Em telas largas o texto fica centralizado com largura confortável de leitura
        val larguraMaxima = Modifier.widthIn(max = 640.dp).fillMaxWidth()

        if (receita.foto != null) {
            item {
                AsyncImage(
                    model = receita.foto,
                    contentDescription = stringResource(R.string.foto_da_receita, receita.nome),
                    contentScale = ContentScale.Crop,
                    // Altura limitada para a foto não ocupar a tela inteira com o celular deitado
                    modifier = Modifier
                        .widthIn(max = 640.dp)
                        .heightIn(max = 280.dp)
                        .aspectRatio(16f / 10f)
                        .clip(RoundedCornerShape(16.dp)),
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        item {
            Column(larguraMaxima) {
                Text(
                    text = stringResource(receita.categoria.rotulo).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (receita.foto == null) corCategoria else MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(4.dp))
                Text(receita.nome, style = MaterialTheme.typography.headlineMedium)
                val informacoes = listOfNotNull(
                    receita.tempoPreparoMin?.let(::formatarTempo),
                    receita.rendimento?.let { stringResource(R.string.rende, it) },
                )
                if (informacoes.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = informacoes.joinToString(" · "),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(28.dp))
                TituloSecao(
                    texto = stringResource(R.string.ingredientes),
                    complemento = if (separados.size == total) {
                        stringResource(R.string.ingredientes_completos)
                    } else {
                        stringResource(R.string.contador_ingredientes, separados.size, total)
                    },
                )
                Spacer(Modifier.height(4.dp))
            }
        }

        itemsIndexed(receita.ingredientes) { posicao, ingrediente ->
            LinhaIngrediente(
                ingrediente = ingrediente,
                marcado = posicao in separados,
                onAlternar = { onAlternarIngrediente(posicao) },
                modifier = larguraMaxima,
            )
        }

        item {
            Column(larguraMaxima) {
                Spacer(Modifier.height(28.dp))
                TituloSecao(stringResource(R.string.modo_preparo))
                Spacer(Modifier.height(4.dp))
            }
        }

        itemsIndexed(receita.passos) { posicao, passo ->
            PassoPreparo(numero = posicao + 1, texto = passo, modifier = larguraMaxima)
        }

        if (receita.dica != null) {
            item {
                Spacer(Modifier.height(20.dp))
                CartaoDica(receita.dica, larguraMaxima)
            }
        }

        if (receita.idOrigem != null) {
            item {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.importada_da_internet),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = larguraMaxima,
                )
            }
        }
    }
}

@Composable
private fun LinhaIngrediente(
    ingrediente: Ingrediente,
    marcado: Boolean,
    onAlternar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val decoracao = if (marcado) TextDecoration.LineThrough else null
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .toggleable(value = marcado, role = Role.Checkbox, onValueChange = { onAlternar() })
            .alpha(if (marcado) 0.6f else 1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // onCheckedChange nulo: o clique é tratado pela linha inteira, que é uma área de toque maior
        Checkbox(checked = marcado, onCheckedChange = null)
        Text(
            text = ingrediente.nome,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = decoracao,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = ingrediente.quantidade ?: stringResource(R.string.a_gosto),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textDecoration = decoracao,
        )
    }
}

private fun compartilhar(context: Context, receita: Receita) {
    val texto = buildString {
        appendLine(receita.nome)
        appendLine()
        appendLine(context.getString(R.string.ingredientes))
        receita.ingredientes.forEach { ingrediente ->
            val quantidade = ingrediente.quantidade ?: context.getString(R.string.a_gosto)
            appendLine("• ${ingrediente.nome} — $quantidade")
        }
        appendLine()
        appendLine(context.getString(R.string.modo_preparo))
        receita.passos.forEachIndexed { indice, passo -> appendLine("${indice + 1}. $passo") }
        receita.dica?.let {
            appendLine()
            appendLine("${context.getString(R.string.dica)}: $it")
        }
    }
    val envio = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, receita.nome)
        putExtra(Intent.EXTRA_TEXT, texto.trim())
    }
    context.startActivity(Intent.createChooser(envio, context.getString(R.string.compartilhar_com)))
}
