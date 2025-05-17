/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controler;

import javax.swing.JButton;

/**
 *  Crear una interfaz de comunicación (callback)
 * @author javie
 */
public interface TableroListener {

    /**
     *  Crear una interfaz de comunicación (callback)
     * @param fila
     * @param columna
     * @param celda
     */
    void onCeldaSeleccionada(int fila, int columna, JButton celda);
}
