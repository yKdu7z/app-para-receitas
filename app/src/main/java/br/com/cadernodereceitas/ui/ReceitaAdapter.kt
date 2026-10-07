package br.com.cadernodereceitas.ui

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.cadernodereceitas.R
import br.com.cadernodereceitas.databinding.ItemReceitaBinding
import br.com.cadernodereceitas.model.Receita

class ReceitaAdapter(
    private val aoClicarReceita: (Receita) -> Unit,
    private val aoClicarFavorita: (Receita) -> Unit,
) : ListAdapter<Receita, ReceitaAdapter.ViewHolder>(Diferenca) {

    class ViewHolder(val binding: ItemReceitaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReceitaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val receita = getItem(position)
        val context = holder.itemView.context

        with(holder.binding) {
            txtInicial.text = receita.nome.first().toString()
            txtInicial.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, receita.categoria.cor))
            txtNome.text = receita.nome
            txtResumo.text = receita.resumo(context)

            btnFavorita.setImageResource(
                if (receita.favorita) R.drawable.ic_estrela_cheia else R.drawable.ic_estrela_vazia
            )
            btnFavorita.contentDescription = context.getString(
                if (receita.favorita) R.string.desmarcar_favorita else R.string.marcar_favorita
            )

            root.setOnClickListener { aoClicarReceita(receita) }
            btnFavorita.setOnClickListener { aoClicarFavorita(receita) }
        }
    }

    private object Diferenca : DiffUtil.ItemCallback<Receita>() {
        override fun areItemsTheSame(antiga: Receita, nova: Receita) = antiga.id == nova.id
        override fun areContentsTheSame(antiga: Receita, nova: Receita) = antiga == nova
    }
}
