package br.com.cadernodereceitas.ui.formulario

import br.com.cadernodereceitas.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacaoReceitaTest {

    private val umIngrediente = listOf(CampoIngrediente(chave = 0, quantidade = "2", nome = "Ovos"))

    @Test
    fun `receita completa nao tem erros`() {
        val erros = validarReceita("Omelete", "10", umIngrediente, "Bata os ovos\nFrite")
        assertFalse(erros.temErro)
    }

    @Test
    fun `nome em branco e obrigatorio`() {
        val erros = validarReceita("   ", "", umIngrediente, "Passo")
        assertEquals(R.string.erro_nome_obrigatorio, erros.nome)
    }

    @Test
    fun `nome muito longo e recusado`() {
        val erros = validarReceita("a".repeat(TAMANHO_MAXIMO_NOME + 1), "", umIngrediente, "Passo")
        assertEquals(R.string.erro_nome_longo, erros.nome)
    }

    @Test
    fun `tempo e opcional mas precisa estar no intervalo`() {
        assertNull(validarReceita("Bolo", "", umIngrediente, "Passo").tempo)
        assertEquals(R.string.erro_tempo_invalido, validarReceita("Bolo", "0", umIngrediente, "Passo").tempo)
        assertEquals(R.string.erro_tempo_invalido, validarReceita("Bolo", "1441", umIngrediente, "Passo").tempo)
        assertNull(validarReceita("Bolo", "1440", umIngrediente, "Passo").tempo)
    }

    @Test
    fun `precisa de pelo menos um ingrediente com nome`() {
        val soQuantidade = listOf(CampoIngrediente(chave = 0, quantidade = "1 xícara", nome = " "))
        val erros = validarReceita("Bolo", "", soQuantidade, "Passo")
        assertEquals(R.string.erro_ingredientes, erros.ingredientes)
    }

    @Test
    fun `linhas vazias nao contam como passo`() {
        val erros = validarReceita("Bolo", "", umIngrediente, "\n   \n")
        assertTrue(erros.temErro)
        assertEquals(R.string.erro_passos, erros.passos)
        assertEquals(listOf("Misture", "Asse"), separarPassos("  Misture \n\n Asse"))
    }
}
