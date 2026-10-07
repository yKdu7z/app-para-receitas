package br.com.cadernodereceitas

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.cadernodereceitas.data.Tema
import br.com.cadernodereceitas.ui.navegacao.AppNavegacao
import br.com.cadernodereceitas.ui.tema.CadernoTheme

class MainActivity : ComponentActivity() {

    private val temaViewModel: TemaViewModel by viewModels { TemaViewModel.Fabrica }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val tema by temaViewModel.tema.collectAsStateWithLifecycle()
            val escuro = when (tema) {
                Tema.SISTEMA -> isSystemInDarkTheme()
                Tema.CLARO -> false
                Tema.ESCURO -> true
            }

            // Ícones da barra de status acompanham o tema escolhido no app, não só o do sistema
            DisposableEffect(escuro) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { escuro },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { escuro },
                )
                onDispose {}
            }

            CadernoTheme(escuro = escuro) {
                AppNavegacao(tema = tema, onTemaChange = temaViewModel::definirTema)
            }
        }
    }
}
