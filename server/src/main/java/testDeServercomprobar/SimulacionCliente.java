/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package testDeServercomprobar;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
/**
 *
 * @author Hector
 */
public class SimulacionCliente {
    public static void main(String[] args) {
        String host = "localhost";
        int puerto = 5000;

        try (
            Socket socket = new Socket(host, puerto);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        ) {
            // Crear una sala
            String mensaje = "EVENTO;TIPO=CREAR_SALA;idSala=SALA_1;jugador=Juan";
            System.out.println("[CLIENTE] Enviando mensaje: " + mensaje);
            out.println(mensaje);

            // Esperar y leer la respuesta del servidor
            String respuesta = in.readLine();
            System.out.println("[CLIENTE] Respuesta recibida: " + respuesta);

        } catch (IOException e) {
            System.err.println("[CLIENTE ERROR] No se pudo conectar o comunicar con el servidor: " + e.getMessage());
        }
    }
}
