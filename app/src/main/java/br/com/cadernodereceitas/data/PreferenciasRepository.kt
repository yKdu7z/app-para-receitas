package br.com.cadernodereceitas.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class Tema { SISTEMA, CLARO, ESCURO }

class PreferenciasRepository(private val dataStore: DataStore<Preferences>) {

    private val dados: Flow<Preferences> = dataStore.data.catch { erro ->
        if (erro is IOException) emit(emptyPreferences()) else throw erro
    }

    val tema: Flow<Tema> = dados.map { preferencias ->
        Tema.entries.find { it.name == preferencias[CHAVE_TEMA] } ?: Tema.SISTEMA
    }

    suspend fun definirTema(tema: Tema) {
        dataStore.edit { it[CHAVE_TEMA] = tema.name }
    }

    suspend fun receitasIniciaisGravadas(): Boolean = dados.first()[CHAVE_RECEITAS_INICIAIS] ?: false

    suspend fun marcarReceitasIniciaisGravadas() {
        dataStore.edit { it[CHAVE_RECEITAS_INICIAIS] = true }
    }

    private companion object {
        val CHAVE_TEMA = stringPreferencesKey("tema")
        val CHAVE_RECEITAS_INICIAIS = booleanPreferencesKey("receitas_iniciais_gravadas")
    }
}
