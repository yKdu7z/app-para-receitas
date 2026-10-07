package br.com.cadernodereceitas.data

import br.com.cadernodereceitas.data.remoto.MealDbApi
import br.com.cadernodereceitas.data.remoto.MealDto
import br.com.cadernodereceitas.data.remoto.paraDescoberta
import br.com.cadernodereceitas.data.remoto.paraDetalhe
import br.com.cadernodereceitas.model.DetalheDescoberta
import br.com.cadernodereceitas.model.ReceitaDescoberta

/** Receitas da internet, usadas na aba Descobrir. */
interface DescobertaRepository {
    suspend fun buscar(termo: String): List<ReceitaDescoberta>
    suspend fun detalhar(id: String): DetalheDescoberta?
}

class DescobertaRepositoryMealDb(private val api: MealDbApi) : DescobertaRepository {

    override suspend fun buscar(termo: String): List<ReceitaDescoberta> =
        api.buscar(termo).meals.orEmpty()
            .mapNotNull { MealDto.de(it)?.paraDescoberta() }

    override suspend fun detalhar(id: String): DetalheDescoberta? =
        api.detalhar(id).meals?.firstOrNull()
            ?.let { MealDto.de(it)?.paraDetalhe() }
}
