package br.com.cadernodereceitas.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import br.com.cadernodereceitas.R
import br.com.cadernodereceitas.data.ReceitasMock
import br.com.cadernodereceitas.databinding.ActivityMainBinding
import br.com.cadernodereceitas.model.Categoria
import com.google.android.material.divider.MaterialDividerItemDecoration

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val adapter = ReceitaAdapter(
        aoClicarReceita = { receita ->
            startActivity(DetalheReceitaActivity.criarIntent(this, receita.id))
        },
        aoClicarFavorita = { receita ->
            ReceitasMock.alternarFavorita(receita.id)
            atualizarLista()
        },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.aplicarMargensDoSistema()

        val total = ReceitasMock.listar().size
        binding.txtQuantidade.text =
            resources.getQuantityString(R.plurals.quantidade_receitas, total, total)

        binding.listaReceitas.adapter = adapter
        binding.listaReceitas.addItemDecoration(
            MaterialDividerItemDecoration(this, MaterialDividerItemDecoration.VERTICAL).apply {
                dividerInsetStart = resources.getDimensionPixelSize(R.dimen.recuo_divisor)
                isLastItemDecorated = false
            }
        )

        binding.grupoFiltros.setOnCheckedStateChangeListener { _, _ -> atualizarLista() }
    }

    override fun onResume() {
        super.onResume()
        // Recarrega ao voltar do detalhe, onde a receita pode ter sido favoritada
        atualizarLista()
    }

    private fun atualizarLista() {
        val filtro = binding.grupoFiltros.checkedChipId
        val categoria = when (filtro) {
            R.id.chipSalgados -> Categoria.SALGADOS
            R.id.chipDoces -> Categoria.DOCES
            R.id.chipBebidas -> Categoria.BEBIDAS
            else -> null
        }
        val somenteFavoritas = filtro == R.id.chipFavoritas

        val receitas = ReceitasMock.listar(categoria, somenteFavoritas)
        adapter.submitList(receitas)

        binding.txtVazio.isVisible = receitas.isEmpty()
        binding.txtVazio.setText(
            if (somenteFavoritas) R.string.vazio_favoritas else R.string.vazio_categoria
        )
    }
}
