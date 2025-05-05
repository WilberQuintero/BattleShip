/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package testAislado; // O tu paquete de prueba

// Importar la vista REAL para heredar
import View.UnirseJugar;
// Importar el controlador que necesita el constructor de la vista REAL
import Controler.controladorInicio;
import java.util.Map;

/**
 * Una versión Falsa (Mock) de la vista UnirseJugar, solo para pruebas.
 * Hereda de la real para compatibilidad de tipos.
 * Sobrescribe métodos para imprimir en consola en lugar de actuar en UI.
 */
public class MockUnirseJugar extends UnirseJugar { // <-- Añadir "extends UnirseJugar"

    private String testIdentifier;

    /**
     * Constructor del Mock. Llama al constructor de la clase padre (UnirseJugar)
     * pasándole un controladorInicio (puede ser null o un mock si fuera necesario).
     */
    public MockUnirseJugar(String id, controladorInicio ctrlInicioStub) {
        // Llama al constructor de UnirseJugar. ctrlInicioStub puede ser null si
        // los métodos que vamos a probar no dependen de él directamente aquí.
        // Si UnirseJugar requiere un controladorInicio NO NULO, tendrás que crear un mock simple para él.
        // Por ahora, intentamos con null. Si da NullPointerException, creamos un mock básico de controladorInicio.
        super(ctrlInicioStub); // <-- LLAMADA AL CONSTRUCTOR PADRE
        // initComponents(); // El constructor padre ya llama a esto

        this.testIdentifier = id;
        System.out.println("MOCK VIEW [" + testIdentifier + "]: Instancia (heredada) creada.");
        // Deshabilitar la visibilidad real de la ventana JFrame heredada
        // setVisible(false); // Podrías necesitar esto si super() la hace visible
    }

    // --- Sobrescribir métodos llamados por el controlador para LOGUEAR ---

    @Override
    public void navegarAPantallaEspera(String idSala) {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("MOCK VIEW [" + testIdentifier + "]: MÉTODO navegarAPantallaEspera FUE LLAMADO! idSala=" + idSala);
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    @Override
    public void mostrarError(String mensaje) {
         System.out.println("MOCK VIEW [" + testIdentifier + "]: MÉTODO mostrarError FUE LLAMADO! Mensaje=" + mensaje);
    }

    @Override
    public void reactivarBotonCrear() {
         System.out.println("MOCK VIEW [" + testIdentifier + "]: MÉTODO reactivarBotonCrear FUE LLAMADO!");
    }

    @Override
     public void reactivarBotonUnirse() {
          System.out.println("MOCK VIEW [" + testIdentifier + "]: MÉTODO reactivarBotonUnirse FUE LLAMADO!");
     }

    @Override
     public void reactivarBotones() {
          System.out.println("MOCK VIEW [" + testIdentifier + "]: MÉTODO reactivarBotones FUE LLAMADO!");
     }

     // Sobrescribir otros métodos si es necesario...
}