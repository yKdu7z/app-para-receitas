@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.cadernodereceitas.ui.descobrir

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.cadernodereceitas.R
import br.com.cadernodereceitas.model.DetalheDescoberta
import br.com.cadernodereceitas.ui.componentes.EstadoCarregando
import br.com.cadernodereceitas.ui.componentes.EstadoErro
import br.com.cadernodereceitas.ui.componentes.EstadoMensagem
import br.com.cadernodereceitas.ui.componentes.PassoPreparo
import br.com.cadernodereceitas.ui.componentes.TituloSecao
import coil3.compose.AsyncImage

@Composable
fun DetalheDescobertaRota(
    id: String,
    onVoltar: () -> Unit,
    onAbrirNoCaderno: (Long) -> Unit,
    viewModel: DetalheDescobertaViewModel = viewModel(factory = DetalheDescobertaViewModel.fabrica(id)),
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    DetalheDescobertaTela(
        estado = estado,
        onVoltar = onVoltar,
        onTentarNovamente = viewModel::carregar,
        onSalvar = viewModel::salvarNoCaderno,
        onAbrirNoCaderno = onAbrirNoCaderno,
    )
}

@Composable
fun DetalheDescobertaTela(
    estado: DetalheDescobertaUiState,
    onVoltar: () -> Unit,
    onTentarNovamente: () -> Unit,
    onSalvar: () -> Unit,
    onAbrirNoCaderno: (Long) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(painterResource(R.drawable.ic_voltar), stringResource(R.string.voltar))
                    }
                },
            )
        },
        bottomBar = {
            if (estado is DetalheDescobertaUiState.Conteudo) {
                BarraSalvar(estado, onSalvar, onAbrirNoCaderno)
            }
        },
    ) { espacamento ->
        when (estado) {
            DetalheDescobertaUiState.Carregando ->
                EstadoCarregando(stringResource(R.string.carregando_receita), Modifier.padding(espacamento))
            DetalheDescobertaUiState.NaoEncontrada -> EstadoMensagem(
                icone = R.drawable.ic_buscar,
                titulo = stringResource(R.string.receita_indisponivel),
                mensagem = null,
                modifier = Modifier.padding(espacamento),
            )
            is DetalheDescobertaUiState.Erro -> EstadoErro(
                mensagem = stringResource(
                    if (estado.tipo == TipoErro.SEM_CONEXAO) R.string.erro_sem_conexao else R.string.erro_servidor
                ),
                onTentarNovamente = onTentarNovamente,
                modifier = Modifier.padding(espacamento),
            )
            is DetalheDescobertaUiState.Conteudo -> ConteudoDescoberta(estado.receita, espacamento)
        }
    }
}

@Composable
private fun ConteudoDescoberta(receita: DetalheDescoberta, espacamento: PaddingValues) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = espacamento.calculateTopPadding(),
            bottom = espacamento.calculateBottomPadding() + 24.dp,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val larguraMaxima = Modifier.widthIn(max = 640.dp).fillMaxWidth()

        item {
            Column(larguraMaxima) {
                AsyncImage(
                    model = receita.fotoUrl,
                    contentDescription = stringResource(R.string.foto_da_receita, receita.nome),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .heightIn(max = 280.dp)
                        .aspectRatio(16f / 10f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                Spacer(Modifier.height(20.dp))
                val detalhes = listOfNotNull(receita.categoria, receita.origem).joinToString(" · ")
                if (detalhes.isNotEmpty()) {
                    Text(
                        text = detalhes.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Text(receita.nome, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.aviso_texto_original),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(28.dp))
                TituloSecao(stringResource(R.string.ingredientes))
                Spacer(Modifier.height(8.dp))
            }
        }

        items(receita.ingredientes) { ingrediente ->
            Row(
                modifier = larguraMaxima.heightIn(min = 40.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = ingrediente.nome,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                ingrediente.quantidade?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
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
    }
}

@Composable
private fun BarraSalvar(
    estado: DetalheDescobertaUiState.Conteudo,
    onSalvar: () -> Unit,
    onAbrirNoCaderno: (Long) -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val largura = Modifier.widthIn(max = 640.dp).fillMaxWidth().heightIn(min = 52.dp)
            val idNoCaderno = estado.idNoCaderno
            if (idNoCaderno != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(stringResource(R.string.salva_no_caderno), style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(onClick = { onAbrirNoCaderno(idNoCaderno) }, modifier = largura) {
                    Text(stringResource(R.string.abrir_no_caderno))
                }
            } else {
                Button(onClick = onSalvar, enabled = !estado.salvando, modifier = largura) {
                    if (estado.salvando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Icon(painterResource(R.drawable.ic_salvar), contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text(stringResource(R.string.salvar_no_caderno))
                    }
                }
            }
        }
    }
}
