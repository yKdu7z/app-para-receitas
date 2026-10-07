package br.com.cadernodereceitas.data.local

import br.com.cadernodereceitas.model.Ingrediente
import br.com.cadernodereceitas.model.Receita

fun ReceitaEntity.paraModelo() = Receita(
    id = id,
    nome = nome,
    categoria = categoria,
    tempoPreparoMin = tempoPreparoMin,
    rendimento = rendimento,
    ingredientes = ingredientes.map { Ingrediente(it.nome, it.quantidade) },
    passos = passos,
    dica = dica,
    favorita = favorita,
    foto = foto,
    idOrigem = idOrigem,
)

fun Receita.paraEntidade() = ReceitaEntity(
    id = id,
    nome = nome,
    categoria = categoria,
    tempoPreparoMin = tempoPreparoMin,
    rendimento = rendimento,
    ingredientes = ingredientes.map { IngredienteSalvo(it.nome, it.quantidade) },
    passos = passos,
    dica = dica,
    favorita = favorita,
    foto = foto,
    idOrigem = idOrigem,
)
