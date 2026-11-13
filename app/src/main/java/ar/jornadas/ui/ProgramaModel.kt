package ar.jornadas.ui

import android.app.Application
import androidx.lifecycle.*
import ar.jornadas.Servicios
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ProgramaModel(app: Application, private val estado: SavedStateHandle) : AndroidViewModel(app) {
    private val servicios = Servicios(app)
    val mesa = servicios.mesa
    private val repo = servicios.agenda
    val charlas = repo.programa.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    /** null mientras Room todavía no respondió; true si hay alguna edición guardada (aunque esté vacía). */
    val hayEdicion = repo.hayEdicion.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    // Filtros en SavedStateHandle: sobreviven a rotación, a salir del catálogo y a la muerte del proceso,
    // pero no se guardan en disco, así que una tarea nueva arranca sin filtros.
    val dia = estado.getStateFlow("dia", 0)
    val tema = estado.getStateFlow("tema", "Todos")
    val fallo = MutableStateFlow(false)
    val cargando = MutableStateFlow(false)

    fun recargar() {
        viewModelScope.launch {
            cargando.value = true
            fallo.value = false
            try {
                repo.sincronizar()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Red caída, HTTP de error o JSON inválido: los datos guardados quedan intactos.
                fallo.value = true
            } finally {
                cargando.value = false
            }
        }
    }

    fun elegirDia(indice: Int) { estado["dia"] = indice }
    fun elegirTema(nombre: String) { estado["tema"] = nombre }

    fun marcar(id: String) { viewModelScope.launch { repo.cambiarFavorita(id) } }
}
