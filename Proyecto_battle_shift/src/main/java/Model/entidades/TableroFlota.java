/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model.entidades;
import enums.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;


/**
 *
 * @author javie
 */
public class TableroFlota {
    private List<Barco> barcos;
    private final int dimension; // Ej. 10 para un tablero de 10x10

    public TableroFlota(int dimension) {
        this.dimension = dimension;
        this.barcos = new ArrayList<>();
    }

    public boolean agregarBarco(Barco barco) {
        if (esPosicionValidaParaBarco(barco)) {
            return barcos.add(barco);
        }
        return false;
    }

    public ResultadoDisparo recibirDisparo(Posicion pos) {
        for (Barco barco : barcos) {
            if (barco.ocupaPosicion(pos)) {
                boolean fueImpactoNuevo = barco.registrarImpacto(pos);
                if (fueImpactoNuevo) {
                    if (barco.estaHundido()) {
                        return ResultadoDisparo.HUNDIDO;
                    } else {
                        return ResultadoDisparo.IMPACTO;
                    }
                } else {
                    // Ya había sido impactado en esta posición, podría considerarse IMPACTO repetido o una lógica específica.
                    // Por simplicidad, si ya estaba impactada y el barco no se hundió con este disparo, es IMPACTO.
                    // Si el barco ya estaba hundido, es un disparo a una casilla de un barco hundido.
                    // El diagrama de ResultadoDisparo es AGUA, IMPACTO, HUNDIDO.
                    // Asumamos que disparar a una casilla ya impactada de un barco no hundido sigue siendo "IMPACTO".
                    // Si el barco ya estaba hundido, y se dispara a una de sus casillas, ¿qué resultado es?
                    // Podría ser HUNDIDO (para indicar que esa casilla pertenece a un barco ya hundido) o AGUA (si no hay más lógica).
                    // Vamos a asumir que si la casilla pertenece a un barco, siempre se devuelve IMPACTO o HUNDIDO (si se hunde con este o ya estaba hundido).
                    return barco.estaHundido() ? ResultadoDisparo.HUNDIDO : ResultadoDisparo.IMPACTO;
                }
            }
        }
        return ResultadoDisparo.AGUA;
    }
    
    /**
     * NUEVO MÉTODO: Limpia todos los barcos del tablero.
     * Necesario si quieres reemplazar la flota actual con una nueva desde la UI.
     */
    public void limpiarBarcos() {
        if (this.barcos != null) {
            this.barcos.clear();
        }
    }

    public boolean estanTodosBarcosHundidos() {
        if (barcos.isEmpty()) return false; // No hay barcos que hundir
        for (Barco barco : barcos) {
            if (!barco.estaHundido()) {
                return false;
            }
        }
        return true;
    }

    public boolean esPosicionValidaParaBarco(Barco barco) {
        // 1. Verificar que todas las posiciones del barco están dentro del tablero
        for (Posicion p : barco.getPosicionesOcupadas()) {
            if (p.getX() < 0 || p.getX() >= dimension || p.getY() < 0 || p.getY() >= dimension) {
                return false; // Fuera de los límites
            }
        }
        // 2. Verificar que no se superponga con otros barcos existentes
        for (Barco existente : barcos) {
            for (Posicion pBarcoNuevo : barco.getPosicionesOcupadas()) {
                if (existente.ocupaPosicion(pBarcoNuevo)) {
                    return false; // Superposición
                }
            }
        }
        // (Opcional) Podrías añadir lógica para verificar que los barcos no estén pegados,
        // si la regla del juego "sin colocar las naves pegadas entre sí" se implementa estrictamente.
        // Esto implicaría revisar las casillas adyacentes a cada barco.
        return true;
    }
    
    public Barco getBarcoEn(Posicion pos) {
        for (Barco barco : barcos) {
            if (barco.ocupaPosicion(pos)) {
                return barco;
            }
        }
        return null;
    }

    // Getters
    public List<Barco> getBarcos() { return new ArrayList<>(barcos); } // Devuelve copia
    public int getDimension() { return dimension; }
}