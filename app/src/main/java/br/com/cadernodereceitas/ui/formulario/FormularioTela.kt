@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.cadernodereceitas.ui.formulario

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.cadernodereceitas.R
import br.com.cadernodereceitas.model.Categoria
import br.com.cadernodereceitas.ui.componentes.EstadoCarregando
import br.com.cadernodereceitas.ui.componentes.TituloSecao
import br.com.cadernodereceitas.ui.componentes.rotulo
import coil3.compose.AsyncImage

@Composable
fun FormularioRota(
    idReceita: Long?,
    onFechar: () -> Unit,
    onSalvo: (Long) -> Unit,
    viewModel: FormularioViewModel = viewModel(factory = FormularioViewModel.fabrica(idReceita)),
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(estado.idSalvo) {
        estado.idSalvo?.let(onSalvo)
    }

    val seletorFoto = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) viewModel.escolherFoto(uri)
    }

    FormularioTela(
        estado = estado,
        acoes = AcoesFormulario(
            onFechar = onFechar,
            onSalvar = viewModel::salvar,
            onNome = viewModel::alterarNome,
            onCategoria = viewModel::alterarCategoria,
            onTempo = viewModel::alterarTempo,
            onRendimento = viewModel::alterarRendimento,
            onIngrediente = viewModel::alterarIngrediente,
            onAdicionarIngrediente = viewModel::adicionarIngrediente,
            onRemoverIngrediente = viewModel::removerIngrediente,
            onPassos = viewModel::alterarPassos,
            onDica = viewModel::alterarDica,
            onEscolherFoto = {
                seletorFoto.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            },
            onRemoverFoto = viewModel::removerFoto,
        ),
    )
}

class AcoesFormulario(
    val onFechar: () -> Unit,
    val onSalvar: () -> Unit,
    val onNome: (String) -> Unit,
    val onCategoria: (Categoria) -> Unit,
    val onTempo: (String) -> Unit,
    val onRendimento: (String) -> Unit,
    val onIngrediente: (chave: Long, quantidade: String, nome: String) -> Unit,
    val onAdicionarIngrediente: () -> Unit,
    val onRemoverIngrediente: (Long) -> Unit,
    val onPassos: (String) -> Unit,
    val onDica: (String) -> Unit,
    val onEscolherFoto: () -> Unit,
    val onRemoverFoto: () -> Unit,
)

@Composable
fun FormularioTela(estado: FormularioUiState, acoes: AcoesFormulario) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (estado.editando) R.string.editar_receita else R.string.nova_receita))
                },
                navigationIcon = {
                    IconButton(onClick = acoes.onFechar) {
                        Icon(painterResource(R.drawable.ic_fechar), stringResource(R.string.fechar))
                    }
                },
                actions = {
                    TextButton(onClick = acoes.onSalvar, enabled = !estado.salvando && !estado.carregando) {
                        Text(stringResource(R.string.salvar))
                    }
                },
            )
        },
    ) { espacamento ->
        if (estado.carregando) {
            EstadoCarregando(stringResource(R.string.carregando_receita), Modifier.padding(espacamento))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacamento)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CampoFoto(estado.foto, acoes.onEscolherFoto, acoes.onRemoverFoto)

                OutlinedTextField(
                    value = estado.nome,
                    onValueChange = acoes.onNome,
                    label = { Text(stringResource(R.string.campo_nome)) },
                    isError = estado.erros.nome != null,
                    supportingText = estado.erros.nome?.let { { Text(stringResource(it)) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Column {
                    Text(
                        stringResource(R.string.campo_categoria),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        Categoria.entries.forEachIndexed { indice, categoria ->
                            SegmentedButton(
                                selected = estado.categoria == categoria,
                                onClick = { acoes.onCategoria(categoria) },
                                shape = SegmentedButtonDefaults.itemShape(indice, Categoria.entries.size),
                                label = { Text(stringResource(categoria.rotulo), maxLines = 1) },
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = estado.tempo,
                        onValueChange = acoes.onTempo,
                        label = { Text(stringResource(R.string.campo_tempo)) },
                        isError = estado.erros.tempo != null,
                        supportingText = {
                            Text(stringResource(estado.erros.tempo ?: R.string.opcional))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = estado.rendimento,
                        onValueChange = acoes.onRendimento,
                        label = { Text(stringResource(R.string.campo_rendimento)) },
                        placeholder = { Text(stringResource(R.string.campo_rendimento_exemplo)) },
                        supportingText = { Text(stringResource(R.string.opcional)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f),
                    )
                }

                CamposIngredientes(estado, acoes)

                OutlinedTextField(
                    value = estado.passos,
                    onValueChange = acoes.onPassos,
                    label = { Text(stringResource(R.string.campo_passos)) },
                    isError = estado.erros.passos != null,
                    supportingText = {
                        Text(stringResource(estado.erros.passos ?: R.string.campo_passos_ajuda))
                    },
                    minLines = 5,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = estado.dica,
                    onValueChange = acoes.onDica,
                    label = { Text(stringResource(R.string.campo_dica)) },
                    minLines = 2,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )

                if (estado.aviso != null) {
                    Text(
                        text = stringResource(estado.aviso),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        // Leitores de tela anunciam o aviso assim que ele aparece
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                }

                Button(
                    onClick = acoes.onSalvar,
                    enabled = !estado.salvando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    if (estado.salvando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(stringResource(R.string.salvar_receita))
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CampoFoto(foto: String?, onEscolher: () -> Unit, onRemover: () -> Unit) {
    if (foto == null) {
        OutlinedButton(onClick = onEscolher, modifier = Modifier.fillMaxWidth()) {
            Icon(painterResource(R.drawable.ic_foto), contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.adicionar_foto))
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AsyncImage(
            model = foto,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .heightIn(max = 280.dp)
                .aspectRatio(16f / 10f)
                .clip(RoundedCornerShape(16.dp)),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onEscolher) { Text(stringResource(R.string.trocar_foto)) }
            TextButton(onClick = onRemover) { Text(stringResource(R.string.remover_foto)) }
        }
    }
}

@Composable
private fun CamposIngredientes(estado: FormularioUiState, acoes: AcoesFormulario) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TituloSecao(stringResource(R.string.ingredientes))
        estado.ingredientes.forEachIndexed { indice, campo ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = campo.quantidade,
                    onValueChange = { acoes.onIngrediente(campo.chave, it, campo.nome) },
                    label = { Text(stringResource(R.string.campo_quantidade)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.weight(0.35f),
                )
                OutlinedTextField(
                    value = campo.nome,
                    onValueChange = { acoes.onIngrediente(campo.chave, campo.quantidade, it) },
                    label = { Text(stringResource(R.string.campo_ingrediente)) },
                    isError = estado.erros.ingredientes != null && indice == 0,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.weight(0.65f),
                )
                if (estado.ingredientes.size > 1) {
                    IconButton(onClick = { acoes.onRemoverIngrediente(campo.chave) }) {
                        Icon(
                            painterResource(R.drawable.ic_fechar),
                            contentDescription = stringResource(R.string.remover_ingrediente, indice + 1),
                        )
                    }
                }
            }
        }
        if (estado.erros.ingredientes != null) {
            Text(
                text = stringResource(estado.erros.ingredientes),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        TextButton(onClick = acoes.onAdicionarIngrediente) {
            Icon(painterResource(R.drawable.ic_adicionar), contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.adicionar_ingrediente))
        }
    }
}
