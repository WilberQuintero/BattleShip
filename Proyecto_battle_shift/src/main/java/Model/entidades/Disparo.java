/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model.entidades;
import enums.*;
import java.util.List;

// Este es un Value Object, representa un evento de disparo con su resultado.
// Podría ser usado para logging, historial, o si la Partida devuelve un objeto Disparo más rico.
public class Disparo {
    private final Posicion posicionAtaque;
    private final ResultadoDisparo resultado;
    private final Jugador autor; // Referencia al jugador que realizó el disparo

    public Disparo(Posicion posicionAtaque, ResultadoDisparo resultado, Jugador autor) {
        this.posicionAtaque = posicionAtaque;
        this.resultado = resultado;
        this.autor = autor;
    }

    public Posicion getPosicionAtaque() { return posicionAtaque; }
    public ResultadoDisparo getResultado() { return resultado; }
    public Jugador getAutor() { return autor; }
}