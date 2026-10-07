package br.com.cadernodereceitas.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import br.com.cadernodereceitas.AppContainer
import br.com.cadernodereceitas.CadernoApp

/** Dá acesso às dependências do app dentro das fábricas de ViewModel. */
val CreationExtras.container: AppContainer
    get() = (checkNotNull(this[APPLICATION_KEY]) as CadernoApp).container
