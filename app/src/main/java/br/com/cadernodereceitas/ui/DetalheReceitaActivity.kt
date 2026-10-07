package br.com.cadernodereceitas.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import br.com.cadernodereceitas.R
import br.com.cadernodereceitas.data.ReceitasMock
import br.com.cadernodereceitas.databinding.ActivityDetalheReceitaBinding
import br.com.cadernodereceitas.databinding.ItemPassoBinding
import br.com.cadernodereceitas.model.Receita

class DetalheReceitaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalheReceitaBinding
    private lateinit var receita: Receita

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetalheReceitaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.aplicarMargensDoSistema()

        val encontrada = ReceitasMock.buscarPorId(intent.getIntExtra(EXTRA_RECEITA_ID, -1))
        if (encontrada == null) {
            Toast.makeText(this, R.string.receita_nao_encontrada, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        receita = encontrada

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        preencherTela()

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.containerIngredientes, IngredientesFragment.novaInstancia(receita.id))
            }
        }
    }

    private fun preencherTela() {
        binding.txtCategoria.setText(receita.categoria.titulo)
        binding.txtCategoria.setTextColor(ContextCompat.getColor(this, receita.categoria.cor))
        binding.txtNome.text = receita.nome
        binding.txtInfo.text = listOfNotNull(
            receita.tempoFormatado(),
            receita.rendimento?.let { getString(R.string.rende, it) },
        ).joinToString(" · ")

        receita.passos.forEachIndexed { indice, passo ->
            val item = ItemPassoBinding.inflate(layoutInflater, binding.listaPassos, true)
            item.txtNumero.text = (indice + 1).toString()
            item.txtPasso.text = passo
        }

        binding.blocoDica.isVisible = receita.dica != null
        binding.txtDica.text = receita.dica
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detalhe, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.acaoFavoritar)?.apply {
            setIcon(if (receita.favorita) R.drawable.ic_estrela_cheia else R.drawable.ic_estrela_vazia)
            setTitle(if (receita.favorita) R.string.desmarcar_favorita else R.string.marcar_favorita)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId != R.id.acaoFavoritar) return super.onOptionsItemSelected(item)

        ReceitasMock.alternarFavorita(receita.id)
        receita = receita.copy(favorita = !receita.favorita)
        invalidateOptionsMenu()
        Toast.makeText(
            this,
            if (receita.favorita) R.string.favorita_adicionada else R.string.favorita_removida,
            Toast.LENGTH_SHORT,
        ).show()
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        private const val EXTRA_RECEITA_ID = "receita_id"

        fun criarIntent(context: Context, receitaId: Int): Intent =
            Intent(context, DetalheReceitaActivity::class.java)
                .putExtra(EXTRA_RECEITA_ID, receitaId)
    }
}
