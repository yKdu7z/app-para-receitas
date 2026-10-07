package br.com.cadernodereceitas.ui.componentes

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.cadernodereceitas.R
import br.com.cadernodereceitas.model.Categoria
import br.com.cadernodereceitas.ui.tema.LocalCoresCaderno
import coil3.compose.AsyncImage

/**
 * Foto da receita ou, quando não houver foto, a inicial do nome sobre a cor da categoria.
 * É decorativa: o nome da receita sempre aparece em texto ao lado.
 */
@Composable
fun CapaReceita(
    foto: String?,
    nome: String,
    categoria: Categoria,
    modifier: Modifier = Modifier,
    tamanho: Dp = 56.dp,
    formato: Shape = RoundedCornerShape(12.dp),
) {
    val cor = LocalCoresCaderno.current.daCategoria(categoria)
    Box(
        modifier = modifier
            .size(tamanho)
            .clip(formato)
            .background(cor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = nome.take(1).uppercase(),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
        )
        if (foto != null) {
            AsyncImage(
                model = foto,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
fun TituloSecao(texto: String, modifier: Modifier = Modifier, complemento: String? = null) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        if (complemento != null) {
            Text(
                text = complemento,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun PassoPreparo(numero: Int, texto: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(vertical = 10.dp)) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = numero.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp, top = 3.dp),
        )
    }
}

@Composable
fun CartaoDica(texto: String, modifier: Modifier = Modifier) {
    val cores = LocalCoresCaderno.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cores.dicaFundo)
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.dica),
            style = MaterialTheme.typography.labelLarge,
            color = cores.dicaTexto,
        )
        Spacer(Modifier.height(4.dp))
        Text(text = texto, style = MaterialTheme.typography.bodyLarge, color = cores.dicaTexto)
    }
}

// ---- Estados de tela: carregando, vazio e erro ----

@Composable
fun EstadoCarregando(mensagem: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text(
            text = mensagem,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun EstadoMensagem(
    @DrawableRes icone: Int,
    titulo: String,
    mensagem: String?,
    modifier: Modifier = Modifier,
    corIcone: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textoAcao: String? = null,
    onAcao: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(icone),
            contentDescription = null,
            tint = corIcone,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        if (mensagem != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = mensagem,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 360.dp),
            )
        }
        if (textoAcao != null) {
            Spacer(Modifier.height(24.dp))
            FilledTonalButton(onClick = onAcao) { Text(textoAcao) }
        }
    }
}

@Composable
fun EstadoErro(mensagem: String, onTentarNovamente: () -> Unit, modifier: Modifier = Modifier) {
    EstadoMensagem(
        icone = R.drawable.ic_erro,
        titulo = mensagem,
        mensagem = null,
        corIcone = MaterialTheme.colorScheme.error,
        textoAcao = stringResource(R.string.tentar_novamente),
        onAcao = onTentarNovamente,
        modifier = modifier,
    )
}
