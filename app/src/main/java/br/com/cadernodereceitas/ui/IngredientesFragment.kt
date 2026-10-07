package br.com.cadernodereceitas.ui

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import br.com.cadernodereceitas.R
import br.com.cadernodereceitas.data.ReceitasMock
import br.com.cadernodereceitas.databinding.FragmentIngredientesBinding
import br.com.cadernodereceitas.databinding.ItemIngredienteBinding

/**
 * Lista de ingredientes com checkbox, para ir marcando o que já foi separado
 * antes de começar o preparo.
 */
class IngredientesFragment : Fragment() {

    private var _binding: FragmentIngredientesBinding? = null
    private val binding get() = _binding!!

    // Posições dos ingredientes já marcados; sobrevive à rotação via onSaveInstanceState
    private val separados = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.getIntArray(ESTADO_SEPARADOS)?.let { separados.addAll(it.toList()) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentIngredientesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val receita = ReceitasMock.buscarPorId(requireArguments().getInt(ARG_RECEITA_ID)) ?: return
        val total = receita.ingredientes.size

        receita.ingredientes.forEachIndexed { indice, ingrediente ->
            val item = ItemIngredienteBinding.inflate(layoutInflater, binding.listaIngredientes, true)
            item.checkIngrediente.text = ingrediente.nome
            item.txtQuantidade.text = ingrediente.quantidade ?: getString(R.string.a_gosto)

            val marcado = indice in separados
            item.checkIngrediente.isChecked = marcado
            riscar(item, marcado)

            item.checkIngrediente.setOnCheckedChangeListener { _, agoraMarcado ->
                if (agoraMarcado) separados.add(indice) else separados.remove(indice)
                riscar(item, agoraMarcado)
                atualizarContador(total)
            }
            item.root.setOnClickListener { item.checkIngrediente.toggle() }
        }

        atualizarContador(total)
    }

    private fun riscar(item: ItemIngredienteBinding, marcado: Boolean) {
        listOf(item.checkIngrediente, item.txtQuantidade).forEach { texto ->
            texto.paintFlags = if (marcado) {
                texto.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                texto.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            texto.alpha = if (marcado) 0.55f else 1f
        }
    }

    private fun atualizarContador(total: Int) {
        binding.txtContador.text = if (separados.size == total) {
            getString(R.string.ingredientes_completos)
        } else {
            getString(R.string.contador_ingredientes, separados.size, total)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray(ESTADO_SEPARADOS, separados.toIntArray())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_RECEITA_ID = "receita_id"
        private const val ESTADO_SEPARADOS = "separados"

        fun novaInstancia(receitaId: Int) = IngredientesFragment().apply {
            arguments = bundleOf(ARG_RECEITA_ID to receitaId)
        }
    }
}
