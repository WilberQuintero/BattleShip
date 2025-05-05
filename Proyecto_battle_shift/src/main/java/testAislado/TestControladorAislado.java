/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package testAislado;

// Importar el controlador REAL
import Controler.controladorCrearPartida;
// Importar los Mocks
import testAislado.MockServerComunicacion;
import testAislado.MockUnirseJugar;
// Importar el controlador de Inicio SÓLO si MockUnirseJugar lo necesita en super()
import Controler.controladorInicio;

import java.util.HashMap;
import java.util.Map;

public class TestControladorAislado {

    public static void main(String[] args) {
        System.out.println("--- INICIANDO PRUEBA AISLADA (con Mocks Heredados) ---");

        // 1. Crear instancias falsas/mocks
        System.out.println("\n1. Creando Mocks...");
        // Usamos el Mock que hereda de ServerComunicacion
        MockServerComunicacion mockComms = new MockServerComunicacion();
        // Usamos el Mock que hereda de UnirseJugar. Pasamos null como controladorInicio.
        // Si esto da error, necesitarás crear un 'new controladorInicio()' o un mock para él.
        MockUnirseJugar mockVista = new MockUnirseJugar("TestVista1", null);

        // 2. Crear la instancia REAL del controlador a probar
        System.out.println("\n2. Creando instancia REAL de controladorCrearPartida...");
        // Ahora el tipo MockServerComunicacion ES compatible con ServerComunicacion
        controladorCrearPartida controlador = new controladorCrearPartida(mockComms);

        // 3. Enlazar la vista falsa con el controlador real
        System.out.println("\n3. Enlazando Mock Vista con Controlador Real...");
        // Ahora el tipo MockUnirseJugar ES compatible con UnirseJugar
        controlador.setVista(mockVista);

        // 4. Simular datos de respuesta UNIDO_OK
        System.out.println("\n4. Preparando datos simulados para UNIDO_OK...");
        Map<String, Object> datosUnidoOk = new HashMap<>();
        datosUnidoOk.put("idSala", "SALA_TEST_AISLADO");
        datosUnidoOk.put("mensaje", "Simulación de unión exitosa.");

        // 5. Llamar DIRECTAMENTE al método problemático (Unirse Sala)
        System.out.println("\n5. Llamando a controlador.procesarRespuestaUnirseSala(true, datosUnidoOk)...");
        try {
            // Asegúrate que procesarRespuestaUnirseSala en el controlador REAL tenga el log de entrada
            controlador.procesarRespuestaUnirseSala(true, datosUnidoOk);
            System.out.println("   >>> Llamada a procesarRespuestaUnirseSala RETORNÓ.");
        } catch (Throwable t) {
             System.err.println("   >>> ERROR INESPERADO DURANTE LLAMADA DIRECTA a procesarRespuestaUnirseSala:");
             t.printStackTrace();
        }


        // 6. Simular datos de respuesta SALA_CREADA_OK
        System.out.println("\n6. Preparando datos simulados para SALA_CREADA_OK...");
        Map<String, Object> datosCreadaOk = new HashMap<>();
        datosCreadaOk.put("idSala", "SALA_TEST_AISLADO_2");
        datosCreadaOk.put("mensaje", "Simulación de creación exitosa.");


        // 7. Llamar DIRECTAMENTE al otro método (Crear Sala)
         System.out.println("\n7. Llamando a controlador.procesarRespuestaCrearSala(true, datosCreadaOk)...");
         try {
            // Asegúrate que procesarRespuestaCrearSala en el controlador REAL tenga el log de entrada
            controlador.procesarRespuestaCrearSala(true, datosCreadaOk);
            System.out.println("   >>> Llamada a procesarRespuestaCrearSala RETORNÓ.");
        } catch (Throwable t) {
             System.err.println("   >>> ERROR INESPERADO DURANTE LLAMADA DIRECTA a procesarRespuestaCrearSala:");
             t.printStackTrace();
        }

        System.out.println("\n--- PRUEBA AISLADA FINALIZADA ---");
        System.out.println("Verifica si aparecieron los logs 'DEBUG [CrearPartida]: Ingresando a...'");
    }
}