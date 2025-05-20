/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package testDeServercomprobar;



// Importa las clases CONCRETAS que vas a instanciar

import com.mycompany.blackboard.HandlerChain;
import com.mycompany.blackboard.Controller;

// No necesitas importar las interfaces (IServer, IBlackboard) aquí,
// porque las usarás implícitamente al pasar las instancias concretas.
// Evento tampoco es necesario aquí directamente para esta prueba básica.

import java.io.IOException;
import server.Server;

/**
 * Clase de prueba para iniciar el servidor Battleship y probar el flujo
 * de conexión inicial usando la estructura adaptada al estilo Dominó
 * y con interfaces para evitar dependencias cíclicas.
 */
public class ServerTest {

    public static void main(String[] args) {
        int serverPort = 5000; // Puerto para el servidor

        System.out.println("--- INICIANDO PRUEBA BATTLESHIP (con Interfaces) ---");

        // --- 1. Instanciación de Componentes ---
        // Primero creamos los objetos que no dependen de otros en su constructor inmediato.
        System.out.println("[TEST] Creando instancia de Server...");
        Server server = new Server(serverPort); // Server concreto

        System.out.println("[TEST] Creando instancia de HandlerChain (pasando Server como IServer)...");
        // Pasamos la instancia 'server'. Java permite esto porque Server implementa IServer.
        HandlerChain blackboard = new HandlerChain(server); // HandlerChain concreto

        System.out.println("[TEST] Creando instancia de Controller (pasando Server como IServer)...");
        // Pasamos la instancia 'server'. Java permite esto porque Server implementa IServer.
        Controller controller = new Controller(server); // Controller concreto

        System.out.println("[TEST] Componentes instanciados.");

        // --- 2. Inyección de Dependencias / Cableado ---
        // Ahora establecemos las referencias cruzadas usando los setters.
        System.out.println("[TEST] Estableciendo referencias cruzadas...");

        // El Server necesita conocer el Blackboard (como IBlackboard)
        server.setBlackboard(blackboard); // Pasamos 'blackboard', se acepta como IBlackboard

        // El Controller necesita conocer el Blackboard (como IBlackboard)
        controller.setBlackboard(blackboard); // Pasamos 'blackboard', se acepta como IBlackboard

        // El Blackboard necesita conocer el Controller. Este setter también
        // registra las KnowledgeSources dentro del HandlerChain.
        blackboard.setController(controller); // Pasamos 'controller' concreto

        System.out.println("[TEST] Referencias establecidas y Handlers registrados en HandlerChain.");

        // --- 3. Iniciar el Servidor ---
        System.out.println("[TEST] Iniciando el hilo del servidor...");
        Thread serverThread = new Thread(server, "BattleshipServerThread");
        serverThread.start(); // Llama al método run() del Server

        // Pequeña pausa para asegurar que el hilo del servidor arranque
        try {
            Thread.sleep(500); // 500 ms
        } catch (InterruptedException e) {
            System.err.println("[TEST WARN] Hilo principal interrumpido durante pausa inicial.");
            Thread.currentThread().interrupt();
        }

        // --- 4. Probar la Conexión ---
        System.out.println("\n==============================================================");
        System.out.println("[TEST] Servidor listo y escuchando en el puerto " + serverPort);
        System.out.println("[TEST] Abre una terminal/consola y ejecuta:");
        System.out.println("       telnet localhost " + serverPort);
        System.out.println("[TEST] O usa cualquier otro cliente de sockets para conectar.");
        System.out.println("       Observa la salida de la consola para ver el flujo de eventos.");
        System.out.println("==============================================================");

        System.out.println("\n[TEST] Presiona la tecla Enter en ESTA consola para detener el servidor...");
        try {
            // Espera a que el usuario presione Enter
            System.in.read();
        } catch (IOException e) {
            System.err.println("[TEST ERROR] Error esperando la entrada del usuario: " + e.getMessage());
        }

        // --- 5. Detener el Servidor ---
        System.out.println("\n[TEST] Deteniendo el servidor...");
        server.stopServer(); // Llama al método stopServer()

        // --- 6. Esperar a que el Hilo del Servidor Termine ---
        System.out.println("[TEST] Esperando a que el hilo del servidor finalice...");
        try {
            // Espera un tiempo máximo para que el hilo termine limpiamente
            serverThread.join(3000); // Espera hasta 3 segundos
            if (serverThread.isAlive()) {
                 System.out.println("[TEST WARN] El hilo del servidor no terminó después de 3s. Interrumpiendo...");
                 serverThread.interrupt(); // Intenta forzar la detención
            } else {
                 System.out.println("[TEST] Hilo del servidor finalizado correctamente.");
            }
        } catch (InterruptedException e) {
            System.err.println("[TEST ERROR] Hilo principal interrumpido mientras esperaba al servidor.");
            Thread.currentThread().interrupt();
        }

        // --- 7. Inspección Final (Opcional) ---
        System.out.println("\n[TEST] Realizando inspección final del HandlerChain...");
        // Llama al método inspect que añadimos al HandlerChain para ver el estado
        if (blackboard != null) {
            blackboard.inspect();
        }

        System.out.println("\n--- PRUEBA FINALIZADA ---");
    }
}