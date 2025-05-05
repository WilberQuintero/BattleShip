/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package testAislado; // O tu paquete de prueba

// Importar la clase REAL para heredar de ella

// Importar otras clases necesarias si las usa la clase padre


import com.mycompany.servercomunicacion.ServerComunicacion;
import java.net.Socket; // Ejemplo

/**
 * Versión Falsa (Stub/Mock) de ServerComunicacion para pruebas aisladas.
 * HEREDA de la clase real para ser compatible, pero sobrescribe métodos
 * para evitar la comunicación real.
 */
public class MockServerComunicacion extends ServerComunicacion { 

    /**
     * Constructor que llama al constructor de la clase padre con datos falsos.
     */
    public MockServerComunicacion() {
        // Llama al constructor de ServerComunicacion(host, port)
        super("mock_host", 0); // Host y puerto no se usarán
        System.out.println("MOCK COMMS: Instancia (heredada) creada.");
    }

    // --- Sobrescribir métodos clave para evitar acciones reales ---

    @Override
    public synchronized void conectar() {
        System.out.println("MOCK COMMS: conectar() llamado, pero NO conecta realmente.");
        // No llamar a super.conectar()
        // Podríamos simular una conexión exitosa llamando al listener si lo tuviéramos
        // if (listener != null) listener.onConectado(); // Necesitaríamos acceso al listener
    }

    @Override
    public synchronized void desconectar() {
         System.out.println("MOCK COMMS: desconectar() llamado, pero NO hace nada real.");
         // No llamar a super.desconectar()
    }

    @Override
    public boolean isConectado() {
         System.out.println("MOCK COMMS: isConectado() llamado (siempre devuelve true para la prueba).");
        return true; // Simular que siempre está conectado para las pruebas del controlador
    }

    // Sobrescribir los métodos de envío para que solo impriman un log
    @Override
    public void enviarRegistroUsuario(String nombreUsuario) {
         System.out.println("MOCK COMMS: enviarRegistroUsuario('" + nombreUsuario + "') llamado (NO SE ENVÍA NADA).");
    }

    @Override
    public void crearSala(String idSala) {
         System.out.println("MOCK COMMS: crearSala('" + idSala + "') llamado (NO SE ENVÍA NADA).");
    }

    @Override
    public void unirseASala(String idSala) {
         System.out.println("MOCK COMMS: unirseASala('" + idSala + "') llamado (NO SE ENVÍA NADA).");
    }
//
//     @Override
//     public void enviarEventoListo(String idSala) {
//          System.out.println("MOCK COMMS: enviarEventoListo('" + idSala + "') llamado (NO SE ENVÍA NADA).");
//     }
//
//     @Override
//     public void salirDeSala(String idSala) {
//          System.out.println("MOCK COMMS: salirDeSala('" + idSala + "') llamado (NO SE ENVÍA NADA).");
//     }
//
//     @Override
//     public void enviarColocacionLista(String idSala, String tableroJson) {
//           System.out.println("MOCK COMMS: enviarColocacionLista('" + idSala + "', '"+ tableroJson +"') llamado (NO SE ENVÍA NADA).");
//     }
//
//     @Override
//     public void enviarDisparo(String idSala, int x, int y) {
//           System.out.println("MOCK COMMS: enviarDisparo('" + idSala + "', "+x+", "+y+") llamado (NO SE ENVÍA NADA).");
//     }
//
//     @Override
//      public void abandonarPartida(String idSala) {
//           System.out.println("MOCK COMMS: abandonarPartida('" + idSala + "') llamado (NO SE ENVÍA NADA).");
//      }

    // NO necesitamos sobreescribir el método privado enviarMensaje
    // porque estamos sobreescribiendo los métodos públicos que lo llaman.

    // NO necesitamos la lógica del listener thread aquí.
}