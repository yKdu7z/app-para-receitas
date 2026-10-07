@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.cadernodereceitas.ui.descobrir

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.cadernodereceitas.R
import br.com.cadernodereceitas.model.ReceitaDescoberta
import br.com.cadernodereceitas.ui.componentes.EstadoCarregando
import br.com.cadernodereceitas.ui.componentes.EstadoErro
import br.com.cadernodereceitas.ui.componentes.EstadoMensagem
import coil3.compose.AsyncImage

@Composable
fun DescobrirRota(
    onAbrirDescoberta: (String) -> Unit,
    viewModel: DescobrirViewModel = viewModel(factory = DescobrirViewModel.Fabrica),
) {
    val termo by viewModel.termo.collectAsStateWithLifecycle()
    val resultado by viewModel.resultado.collectAsStateWithLifecycle()
    DescobrirTela(
        termo = termo,
        resultado = resultado,
        onTermoChange = viewModel::alterarTermo,
        onAbrirDescoberta = onAbrirDescoberta,
        onTentarNovamente = viewModel::tentarNovamente,
    )
}

@Composable
fun DescobrirTela(
    termo: String,
    resultado: ResultadoBusca,
    onTermoChange: (String) -> Unit,
    onAbrirDescoberta: (String) -> Unit,
    onTentarNovamente: () -> Unit,
) {
    val foco = LocalFocusManager.current

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.titulo_descobrir)) }) },
    ) { espacamento ->
        Column(Modifier.padding(espacamento)) {
            OutlinedTextField(
                value = termo,
                onValueChange = onTermoChange,
                label = { Text(stringResource(R.string.buscar_receitas)) },
                placeholder = { Text(stringResource(R.string.buscar_dica)) },
                leadingIcon = { Icon(painterResource(R.drawable.ic_buscar), contentDescription = null) },
                trailingIcon = {
                    if (termo.isNotEmpty()) {
                        IconButton(onClick = { onTermoChange("") }) {
                            Icon(painterResource(R.drawable.ic_fechar), stringResource(R.string.limpar_busca))
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { foco.clearFocus() }),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
            Text(
                text = stringResource(R.string.aviso_ingles),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )

            AnimatedContent(
                targetState = resultado,
                contentKey = { it::class },
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "resultado da busca",
            ) { estado ->
                when (estado) {
                    ResultadoBusca.Carregando -> EstadoCarregando(stringResource(R.string.buscando))
                    is ResultadoBusca.Vazio -> EstadoMensagem(
                        icone = R.drawable.ic_buscar,
                        titulo = stringResource(R.string.vazio_busca_titulo),
                        mensagem = stringResource(R.string.vazio_busca_mensagem),
                    )
                    is ResultadoBusca.Erro -> EstadoErro(
                        mensagem = stringResource(
                            if (estado.tipo == TipoErro.SEM_CONEXAO) R.string.erro_sem_conexao else R.string.erro_servidor
                        ),
                        onTentarNovamente = onTentarNovamente,
                    )
                    is ResultadoBusca.Conteudo -> GradeResultados(estado, onAbrirDescoberta)
                }
            }
        }
    }
}

@Composable
private fun GradeResultados(estado: ResultadoBusca.Conteudo, onAbrir: (String) -> Unit) {
    // Adaptive: 2 colunas no celular, mais colunas em telas largas
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = if (estado.termo.isEmpty()) {
                    stringResource(R.string.sugestoes)
                } else {
                    stringResource(R.string.resultados_para, estado.termo)
                },
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() },
            )
        }
        items(estado.receitas, key = { it.id }) { receita ->
            CartaoDescoberta(receita, onClick = { onAbrir(receita.id) })
        }
    }
}

@Composable
private fun CartaoDescoberta(receita: ReceitaDescoberta, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(bottom = 8.dp),
    ) {
        AsyncImage(
            model = receita.fotoUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = receita.nome,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        val detalhes = listOfNotNull(receita.categoria, receita.origem).joinToString(" · ")
        if (detalhes.isNotEmpty()) {
            Text(
                text = detalhes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}
