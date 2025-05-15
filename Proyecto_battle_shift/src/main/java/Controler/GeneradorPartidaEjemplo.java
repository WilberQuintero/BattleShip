/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controler;

import Model.entidades.*; // Asumiendo que aquí están tus entidades
import enums.*;          // Asumiendo que aquí están tus enums
import java.util.UUID;   // Para generar un ID de partida único

public class GeneradorPartidaEjemplo {

    public static final int DIMENSION_TABLERO = 10;

    public static Partida crearPartidaDeEjemploListaParaJugar() {
        System.out.println("Creando partida de ejemplo...");

        // 1. Crear la Partida y los Jugadores (usando tu método factory)
        String idPartida = "PARTIDA_" + UUID.randomUUID().toString().substring(0, 8);
        Partida partida = Partida.crearJuego(idPartida, "ValienteCapitan", "PirataBarbanegra", DIMENSION_TABLERO);

        Jugador jugador1 = partida.getJugador1();
        Jugador jugador2 = partida.getJugador2();

        System.out.println("Jugador 1: " + jugador1.getNombre());
        System.out.println("Jugador 2: " + jugador2.getNombre());

        // 2. Colocar barcos para Jugador 1
        System.out.println("Colocando barcos para " + jugador1.getNombre() + "...");
        colocarFlotaEstandar(jugador1, true); // true para colocar en la parte "superior"
        jugador1.setHaConfirmadoTablero(true);
        System.out.println("Barcos de " + jugador1.getNombre() + " colocados y tablero confirmado.");

        // 3. Colocar barcos para Jugador 2
        System.out.println("Colocando barcos para " + jugador2.getNombre() + "...");
        colocarFlotaEstandar(jugador2, false); // false para colocar en la parte "inferior"
        jugador2.setHaConfirmadoTablero(true);
        System.out.println("Barcos de " + jugador2.getNombre() + " colocados y tablero confirmado.");

        // 4. Iniciar la partida (esto debería establecer el estado y el turno)
        boolean partidaIniciada = partida.iniciarPartida();
        if (partidaIniciada) {
            System.out.println("Partida iniciada. Estado: " + partida.getEstado() +
                               ". Turno de: " + (partida.obtenerJugadorEnTurno() != null ? partida.obtenerJugadorEnTurno().getNombre() : "N/A"));
        } else {
            System.err.println("Error: La partida no pudo iniciarse. Verifica la lógica de confirmación de tableros.");
            // Si no se inicia, el estado podría seguir en CONFIGURACION.
            // Para la UI, es probable que quieras que esté EN_CURSO.
            // Forzamos el estado si es necesario para el ejemplo, asumiendo que los tableros están listos.
            if (jugador1.haConfirmadoTablero() && jugador2.haConfirmadoTablero() && partida.getEstado() != EstadoPartida.EN_CURSO) {
                 System.out.println("Forzando inicio de partida para el ejemplo (si iniciarPartida() no lo hizo)...");
                 partida.setEstado(EstadoPartida.EN_CURSO); // Asumiendo que tienes este setter
                 if (partida.obtenerJugadorEnTurno() == null) {
                    partida.cambiarTurno(); // Para asignar un primer turno si no se hizo
                    // O una lógica como: partida.setJugadorEnTurno(jugador1);
                 }
                 System.out.println("Estado forzado a: " + partida.getEstado() +
                                   ". Turno de: " + (partida.obtenerJugadorEnTurno() != null ? partida.obtenerJugadorEnTurno().getNombre() : "N/A"));
            }
        }
        
        System.out.println("Objeto Partida de ejemplo creado y listo.");
        return partida;
    }

    private static void colocarFlotaEstandar(Jugador jugador, boolean esJugador1Layout) {
        // Reglas: 2 Portaaviones (4), 2 Cruceros (3), 4 Submarinos (2), 3 Barcos Patrulla (1)
        int yOffset = esJugador1Layout ? 0 : 5; // Colocar barcos de J2 más abajo para evitar colisiones visuales simples

        // Portaaviones (x2)
        jugador.colocarBarco(TipoNave.PORTAAVIONES, new Posicion(0, yOffset + 0), Orientacion.HORIZONTAL); // A1-D1 o F1-I1
        jugador.colocarBarco(TipoNave.PORTAAVIONES, new Posicion(0, yOffset + 2), Orientacion.HORIZONTAL); // A3-D3 o F3-I3

        // Cruceros (x2)
        jugador.colocarBarco(TipoNave.CRUCERO, new Posicion(5, yOffset + 0), Orientacion.VERTICAL);   // F1-F3 o K1-K3
        jugador.colocarBarco(TipoNave.CRUCERO, new Posicion(7, yOffset + 1), Orientacion.VERTICAL);   // H2-H4 o M2-M4

        // Submarinos (x4)
        jugador.colocarBarco(TipoNave.SUBMARINO, new Posicion(0, yOffset + 4), Orientacion.HORIZONTAL); // A5-B5 o F5-G5
        jugador.colocarBarco(TipoNave.SUBMARINO, new Posicion(3, yOffset + 4), Orientacion.HORIZONTAL); // D5-E5 o I5-J5
        jugador.colocarBarco(TipoNave.SUBMARINO, new Posicion(6, yOffset + 4), Orientacion.HORIZONTAL); // G5-H5 o L5-M5
        jugador.colocarBarco(TipoNave.SUBMARINO, new Posicion(0, yOffset + 1), Orientacion.VERTICAL);   // A2-A3 o F2-F3  (Cuidado con colisiones, ajustar)
                                                                                                  // Ajuste para J1: B2-B3 | Para J2: G7-G8
        if(esJugador1Layout) {
             jugador.colocarBarco(TipoNave.SUBMARINO, new Posicion(1, 1), Orientacion.VERTICAL); // B2,B3
        } else {
            jugador.colocarBarco(TipoNave.SUBMARINO, new Posicion(1, yOffset + 1), Orientacion.VERTICAL); // B7,B8
        }


        // Barcos Patrulla (x3)
        jugador.colocarBarco(TipoNave.BARCO_PATRULLA, new Posicion(9, yOffset + 0), Orientacion.HORIZONTAL); // J1 o O1
        jugador.colocarBarco(TipoNave.BARCO_PATRULLA, new Posicion(9, yOffset + 2), Orientacion.HORIZONTAL); // J3 o O3
        jugador.colocarBarco(TipoNave.BARCO_PATRULLA, new Posicion(9, yOffset + 4), Orientacion.HORIZONTAL); // J5 o O5

        // Verificar que todos los barcos se hayan podido agregar (según la lógica de TableroFlota.agregarBarco)
        // Para este ejemplo, asumimos que las posiciones son válidas y no se superponen con esta distribución simple.
        // En una implementación real, la UI o una lógica de IA se encargaría de la colocación válida.
    }

    public static void main(String[] args) {
        Partida partidaEjemplo = crearPartidaDeEjemploListaParaJugar();

        // Aquí tus compañeros pueden tomar 'partidaEjemplo' y pasarlo a sus vistas.
        // Por ejemplo, podrían querer inspeccionar los tableros:
        System.out.println("\n--- Inspección de Tableros ---");
        if (partidaEjemplo.getJugador1() != null && partidaEjemplo.getJugador1().getTableroFlota() != null) {
            System.out.println("Tablero Flota de " + partidaEjemplo.getJugador1().getNombre() + ":");
            imprimirTableroFlota(partidaEjemplo.getJugador1().getTableroFlota());
        }
        if (partidaEjemplo.getJugador2() != null && partidaEjemplo.getJugador2().getTableroFlota() != null) {
            System.out.println("\nTablero Flota de " + partidaEjemplo.getJugador2().getNombre() + ":");
            imprimirTableroFlota(partidaEjemplo.getJugador2().getTableroFlota());
        }
         if (partidaEjemplo.getJugador1() != null && partidaEjemplo.getJugador1().getTableroSeguimiento() != null) {
            System.out.println("\nTablero Seguimiento de " + partidaEjemplo.getJugador1().getNombre() + " (debería estar vacío):");
            imprimirTableroSeguimiento(partidaEjemplo.getJugador1().getTableroSeguimiento());
        }
    }

    // Método helper para imprimir un TableroFlota (simplificado)
    public static void imprimirTableroFlota(TableroFlota tablero) {
        char[][] display = new char[DIMENSION_TABLERO][DIMENSION_TABLERO];
        for (int i = 0; i < DIMENSION_TABLERO; i++) {
            for (int j = 0; j < DIMENSION_TABLERO; j++) {
                display[i][j] = '~'; // Agua
            }
        }

        for (Barco barco : tablero.getBarcos()) {
            char inicialBarco = barco.getTipo().name().charAt(0);
            for (Posicion pos : barco.getPosicionesOcupadas()) {
                if (pos.getY() < DIMENSION_TABLERO && pos.getX() < DIMENSION_TABLERO) { // Asegurar dentro de límites
                    boolean impactada = barco.getPosicionesImpactadas().contains(pos);
                    display[pos.getY()][pos.getX()] = impactada ? 'X' : inicialBarco;
                }
            }
        }

        System.out.print("  ");
        for (int i = 0; i < DIMENSION_TABLERO; i++) System.out.print(i + " ");
        System.out.println();
        for (int i = 0; i < DIMENSION_TABLERO; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < DIMENSION_TABLERO; j++) {
                System.out.print(display[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("Total barcos en tablero: " + tablero.getBarcos().size());
    }
    
    public static void imprimirTableroSeguimiento(TableroSeguimiento tablero) {
        char[][] display = new char[DIMENSION_TABLERO][DIMENSION_TABLERO];
         for (int i = 0; i < DIMENSION_TABLERO; i++) {
            for (int j = 0; j < DIMENSION_TABLERO; j++) {
                display[i][j] = '.'; // Desconocido
            }
        }
        // En una partida no iniciada, este mapa estaría vacío.
        // Si hubiera disparos, los marcarías aquí.
        // Ejemplo: tablero.getRegistrosDisparos().forEach((pos, res) -> display[pos.getY()][pos.getX()] = res == ResultadoDisparo.AGUA ? 'A' : 'I');
        
        System.out.print("  ");
        for (int i = 0; i < DIMENSION_TABLERO; i++) System.out.print(i + " ");
        System.out.println();
        for (int i = 0; i < DIMENSION_TABLERO; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < DIMENSION_TABLERO; j++) {
                System.out.print(display[i][j] + " ");
            }
            System.out.println();
        }
    }
}