/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package testDeServercomprobar;

/**
 *
 * @author caarl
 */ 

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Simula el flujo de dos clientes para probar:
 * 1. Cliente 1 crea una sala.
 * 2. Cliente 2 se une a esa sala.
 * Ejecutar ServerTest primero para tener el servidor corriendo.
 * Observar la consola del ServerTest para ver los logs detallados,
 * incluyendo el número de jugadores antes y después de unirse.
 */
public class TestFlujoUnirseSala {

    private static final String HOST = "localhost";
    // Asegúrate que este puerto coincida con el de ServerTest
    private static final int PUERTO = 5000;
    // Nombre de la sala para la prueba
    private static final String ID_SALA_PRUEBA = "SalaBatallaAlfa";

    public static void main(String[] args) {
        System.out.println("--- INICIANDO TEST DE FLUJO: UNIRSE A SALA ---");
        System.out.println("Asegúrate de que ServerTest esté corriendo en el puerto " + PUERTO);

        // --- Simulación Cliente 1: Crear Sala ---
        System.out.println("\n[TEST] Simulando Cliente 1 (Host) creando sala...");
        simularClienteCreaSala(ID_SALA_PRUEBA);

        // Pequeña pausa para dar tiempo al servidor a procesar
        try {
            System.out.println("\n[TEST] Pausa breve (1 segundo)...");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // --- Simulación Cliente 2: Unirse a Sala ---
        System.out.println("\n[TEST] Simulando Cliente 2 uniéndose a la sala...");
        simularClienteSeUneASala(ID_SALA_PRUEBA);

        System.out.println("\n--- TEST DE FLUJO FINALIZADO ---");
        System.out.println("Revisa la consola donde corre ServerTest para ver los logs detallados.");
    }

    /**
     * Simula un cliente que se conecta y envía el comando para crear una sala.
     * @param idSala El ID de la sala a crear.
     */
    private static void simularClienteCreaSala(String idSala) {
        String mensaje = "EVENTO;TIPO=CREAR_SALA;idSala=" + idSala;

        try (
            Socket socket = new Socket(HOST, PUERTO);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            // Opcional: BufferedReader para leer respuesta si la hubiera
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        ) {
            System.out.println("[CLIENTE 1] Conectado al servidor.");
            System.out.println("[CLIENTE 1] Enviando: " + mensaje);
            out.println(mensaje);

            // Opcional: Leer y mostrar la respuesta del servidor
            try {
                 socket.setSoTimeout(2000); // Espera max 2 segundos por una respuesta
                 String respuesta = in.readLine();
                 System.out.println("[CLIENTE 1] Respuesta recibida: " + (respuesta != null ? respuesta : "(sin respuesta/timeout)"));
            } catch (java.net.SocketTimeoutException e) {
                 System.out.println("[CLIENTE 1] No se recibió respuesta del servidor (timeout).");
            }

            System.out.println("[CLIENTE 1] Desconectando.");

        } catch (IOException e) {
            System.err.println("[CLIENTE 1 ERROR] No se pudo conectar o comunicar: " + e.getMessage());
        }
    }

     /**
     * Simula un cliente que se conecta y envía el comando para unirse a una sala.
     * @param idSala El ID de la sala a la que unirse.
     */
    private static void simularClienteSeUneASala(String idSala) {
        String mensaje = "EVENTO;TIPO=UNIRSE_SALA;idSala=" + idSala;

        try (
            Socket socket = new Socket(HOST, PUERTO);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            // Opcional: BufferedReader para leer respuesta
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        ) {
            System.out.println("[CLIENTE 2] Conectado al servidor.");
            System.out.println("[CLIENTE 2] Enviando: " + mensaje);
            out.println(mensaje);

            // Opcional: Leer y mostrar la respuesta del servidor
             try {
                 socket.setSoTimeout(2000); // Espera max 2 segundos por una respuesta
                 String respuesta = in.readLine();
                 System.out.println("[CLIENTE 2] Respuesta recibida: " + (respuesta != null ? respuesta : "(sin respuesta/timeout)"));
            } catch (java.net.SocketTimeoutException e) {
                 System.out.println("[CLIENTE 2] No se recibió respuesta del servidor (timeout).");
            }

            System.out.println("[CLIENTE 2] Desconectando.");

        } catch (IOException e) {
            System.err.println("[CLIENTE 2 ERROR] No se pudo conectar o comunicar: " + e.getMessage());
        }
    }
}