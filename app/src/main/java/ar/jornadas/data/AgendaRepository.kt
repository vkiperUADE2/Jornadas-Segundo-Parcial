package ar.jornadas.data

class AgendaRepository(private val api: ProgramaApi, private val dao: CarteleraDao) {
    val programa = dao.observar()
    val hayEdicion = dao.hayEdicion()

    // El repositorio es único por proceso (JornadasApp), así que esta marca vale "una vez por sesión".
    private var cargaAutomaticaHecha = false

    /** Devuelve true solo la primera vez que se pide en este proceso. */
    fun tomarCargaAutomatica(): Boolean {
        if (cargaAutomaticaHecha) return false
        cargaAutomaticaHecha = true
        return true
    }

    suspend fun sincronizar() {
        val fichas = api.consultar().map {
            ContenidoCharla(it.id, it.title, it.track, it.day, it.time, it.room, it.speaker, it.summary)
        }
        dao.reemplazarPrograma(fichas)
    }

    suspend fun cambiarFavorita(id: String) = dao.alternar(id)
}
