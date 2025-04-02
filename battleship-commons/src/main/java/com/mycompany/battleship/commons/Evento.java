/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.battleship.commons;

/**
 *
 * @author caarl
 */

import java.util.HashMap;
import java.util.Map;

/**
 * Clase genérica para representar eventos dentro del sistema Blackboard.
 * Similar al ejemplo de Dominó.
 */
public class Evento {
    private final String tipo;
    private final Map<String, Object> datos;

    public Evento(String tipo) {
        this.tipo = tipo;
        this.datos = new HashMap<>();
    }

    public String getTipo() {
        return tipo;
    }

    public void agregarDato(String clave, Object valor) {
        this.datos.put(clave, valor);
    }

    public Object obtenerDato(String clave) {
        return this.datos.get(clave);
    }

    public Map<String, Object> getDatos() {
        return new HashMap<>(datos); // Devuelve copia para evitar modificación externa
    }

    @Override
    public String toString() {
        return "Evento{" +
               "tipo='" + tipo + '\'' +
               ", datos=" + datos +
               '}';
    }
}