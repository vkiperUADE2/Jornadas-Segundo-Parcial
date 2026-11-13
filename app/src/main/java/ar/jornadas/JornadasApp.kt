package ar.jornadas

import android.app.Application

/** Vive lo mismo que el proceso: acá se crea una sola vez la base, Retrofit y el servicio de prueba. */
class JornadasApp : Application() {
    val servicios by lazy { Servicios(this) }
}
