/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model.entidades;

import enums.*;
import java.util.ArrayList;
import java.util.List;

import java.util.Random; // Para seleccionar el primer jugador

public class Partida {
    private Jugador jugador1;
    private Jugador jugador2;
    private Jugador jugadorEnTurno;
    private EstadoPartida estado;
    private String idPartida; // Para identificar la partida

    // Constructor privado para ser usado por un método factory/estático
    private Partida(String idPartida) {
        this.idPartida = idPartida;
        this.estado = EstadoPartida.CONFIGURACION;
    }

    // Método estático para crear la partida, asegura que se configura inicialmente.
    public static Partida crearJuego(String idPartida, String nombreJ1, String nombreJ2, int dimensionTablero) {
        Partida nuevaPartida = new Partida(idPartida);
        nuevaPartida.jugador1 = new Jugador(nombreJ1, dimensionTablero);
        nuevaPartida.jugador2 = new Jugador(nombreJ2, dimensionTablero);
        // Aún no se define jugadorEnTurno ni se inicia la partida.
        return nuevaPartida;
    }
    
    // Método para añadir jugadores si se crean por separado
    public void setJugador1(Jugador jugador1) {
        if (this.estado == EstadoPartida.CONFIGURACION) this.jugador1 = jugador1;
    }
    public void setJugador2(Jugador jugador2) {
        if (this.estado == EstadoPartida.CONFIGURACION) this.jugador2 = jugador2;
    }


    public boolean iniciarPartida() {
        if (jugador1 != null && jugador1.haConfirmadoTablero() &&
            jugador2 != null && jugador2.haConfirmadoTablero() &&
            estado == EstadoPartida.CONFIGURACION) {
            
            this.estado = EstadoPartida.EN_CURSO;
            // Seleccionar aleatoriamente quién empieza
            this.jugadorEnTurno = new Random().nextBoolean() ? jugador1 : jugador2;
            return true;
        }
        return false; // No se cumplen las condiciones para iniciar
    }

    public Jugador obtenerJugadorEnTurno() {
        return jugadorEnTurno;
    }
   
    public Jugador obtenerOponente() {
        if (jugadorEnTurno == null) return null;
        return jugadorEnTurno.equals(jugador1) ? jugador2 : jugador1;
    }


    public void cambiarTurno() {
        if (estado == EstadoPartida.EN_CURSO) {
            if (jugadorEnTurno.equals(jugador1)) {
                jugadorEnTurno = jugador2;
            } else {
                jugadorEnTurno = jugador1;
            }
        }
    }

    public ResultadoDisparo procesarDisparo(Posicion posicionAtaque) {
        if (estado != EstadoPartida.EN_CURSO || jugadorEnTurno == null) {
            // Lanzar excepción o devolver un error, la partida no está en curso o no hay turno.
            // System.err.println("La partida no está en curso o no hay jugador en turno.");
            return null; // O un resultado de error específico
        }

        Jugador oponente = obtenerOponente();
        if (oponente == null) return null; // No debería pasar si la partida está EN_CURSO

        // Validar si el jugador en turno ya disparó en esa posición en su tablero de seguimiento
        if (jugadorEnTurno.getTableroSeguimiento().yaSeDisparoEn(posicionAtaque)) {
            // System.err.println("Ya se disparó en la posición: " + posicionAtaque);
            return jugadorEnTurno.getTableroSeguimiento().obtenerResultadoEn(posicionAtaque); // Devolver resultado previo
        }

        ResultadoDisparo resultado = oponente.getTableroFlota().recibirDisparo(posicionAtaque);
        jugadorEnTurno.getTableroSeguimiento().marcarDisparo(posicionAtaque, resultado);

        if (verificarCondicionVictoria()) {
            // El juego termina, el estado se actualiza en verificarCondicionVictoria
        } else {
            // Si no fue AGUA, y el juego permite turno extra por impacto (según descripción original "si acierta ... tiene derecho a un turno adicional")
            // La descripción original dice "Si un jugador con su disparo impacta una nave, tiene derecho a un nuevo disparo, así hasta que falle."
            if (resultado == ResultadoDisparo.AGUA) {
                cambiarTurno();
            }
            // Si fue IMPACTO o HUNDIDO, el turno NO cambia (sigue el mismo jugador).
        }
        return resultado;
    }

    public boolean verificarCondicionVictoria() {
        if (estado != EstadoPartida.EN_CURSO) return false; // Solo se verifica si está en curso

        boolean j1HaPerdido = jugador1.todosLosBarcosHundidos();
        boolean j2HaPerdido = jugador2.todosLosBarcosHundidos();

        if (j1HaPerdido || j2HaPerdido) {
            this.estado = EstadoPartida.FINALIZADA;
            // jugadorEnTurno podría ser el ganador si el oponente perdió.
            // Si j1HaPerdido, el ganador es jugador2 (asumiendo que fue el último en jugar o el estado es final).
            // Si j2HaPerdido, el ganador es jugador1.
            // Podrías añadir un campo "ganador" a la partida.
            return true;
        }
        return false;
    }
    
   public Jugador getJugadorPorNombre(String nombre) {
        if (jugador1 != null && jugador1.getNombre().equals(nombre)) {
            return jugador1;
        }
        if (jugador2 != null && jugador2.getNombre().equals(nombre)) {
            return jugador2;
        }
        return null;
    }

    public int getDimensionTablero() {
        // Si 'dimensionTablero' es un atributo de Partida y se guarda en crearJuego:
        // return this.dimensionTablero; 
        
        // Alternativa si no lo guardas explícitamente en Partida:
        if (this.jugador1 != null && this.jugador1.getTableroFlota() != null) {
             return this.jugador1.getTableroFlota().getDimension();
        }
        return 10; // O tu DIMENSION_TABLERO_DEFAULT de controladorInicio
    }

    // Getters
    public String getIdPartida() { return idPartida; }
    public Jugador getJugador1() { return jugador1; }
    public Jugador getJugador2() { return jugador2; }
    public EstadoPartida getEstado() { return estado; }


public void setJugadorEnTurno(Jugador jugador) { 
    this.jugadorEnTurno = jugador;
}
    public void setEstado(EstadoPartida estado) { this.estado = estado; }
}