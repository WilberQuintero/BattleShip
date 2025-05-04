/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View;

import Controler.controladorInicio;
import Controler.controladorTablero;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 *
 * @author javie
 */
public class TableroJuego extends javax.swing.JFrame {
// --- Atributos ---
    private final controladorInicio controladorPrincipal;
    private final controladorTablero controlador; // Controlador específico
    private final String idSala;
    private final String miNombreUsuario;
    private final String flotaString; // Flota recibida como String "Nombre:Tamaño,..."
    private List<String> barcosParaColocar; // Flota parseada

    // Variables para tu lógica de colocación (ejemplos)
    private String barcoSeleccionado = null; // Qué barco está colocando el usuario
    private boolean orientacionHorizontal = true; // Orientación actual
    // Necesitarás una representación del tablero lógico del jugador aquí
    // private Model.Tablero miTableroLogico = new Model.Tablero();

    /**
     * Constructor para la pantalla de colocación/juego.
     * @param ctrlPrincipal El controlador principal.
     * @param idSala El ID de la sala.
     * @param miNombre Nombre del jugador local.
     * @param flotaRecibida String describiendo la flota a colocar.
     */
    public TableroJuego(controladorInicio ctrlPrincipal, String idSala, String miNombre, String flotaRecibida) {
        initComponents();
        this.setLocationRelativeTo(null);
        this.setTitle("Battleship - Sala: " + idSala + " - Jugador: " + miNombre);

        this.controladorPrincipal = ctrlPrincipal;
        this.idSala = idSala;
        this.miNombreUsuario = miNombre;
        this.flotaString = flotaRecibida;

        // Crear controlador específico para esta pantalla
        controladorTablero ctrlTablero = null;
        if (ctrlPrincipal != null && ctrlPrincipal.getServerComunicacion() != null) {
             System.out.println("VIEW [TableroJuego]: Creando controladorTablero para sala: " + idSala);
             ctrlTablero = new controladorTablero(ctrlPrincipal.getServerComunicacion(), idSala, miNombre, this);
             // Informar al controlador principal (si es necesario manejar eventos globales aquí)
             // ctrlPrincipal.setControladorTableroActual(ctrlTablero); // Necesitarías esto en controladorInicio
             System.out.println("VIEW [TableroJuego]: Controlador específico creado y asignado.");
        } else {
             System.err.println("VIEW [TableroJuego] ERROR CRÍTICO: No se pudo obtener ServerComunicacion.");
             mostrarError("Error crítico de inicialización.");
             // Deshabilitar botones si falla
             if(readyButton != null) readyButton.setEnabled(false);
             if(cancelButton != null) cancelButton.setEnabled(false);
        }
        this.controlador = ctrlTablero;

        // Configurar UI inicial
        configurarUIInicial();
        parsearYMostrarFlota(); // Parsear y mostrar la lista de barcos
        prepararTableroParaColocacion(); // Configurar listeners del tablero
    }

    /** Configura elementos iniciales de la UI */
    private void configurarUIInicial() {
        if(numberLabel != null) numberLabel.setText("Coloca tus barcos. Haz clic en un barco y luego en el tablero.");
        if(readyButton != null) readyButton.setEnabled(false); // Deshabilitado hasta colocar todos
        if(cancelButton != null) cancelButton.setText("Resetear"); // Cambiar texto
        if(cancelButton != null) cancelButton.setEnabled(true);  // Habilitado para resetear
        // TODO: Limpiar/dibujar la cuadrícula inicial en tableroJPanel
    }

    /** Parsea el string de flota y actualiza la UI (ej. un JList en jPanel2) */
    private void parsearYMostrarFlota() {
        System.out.println("VIEW [TableroJuego]: Parseando flota: " + flotaString);
        if (flotaString != null && !flotaString.isEmpty()) {
            barcosParaColocar = new ArrayList<>(Arrays.asList(flotaString.split(",")));
            // TODO: Llenar el componente UI en jPanel2 (ej. JList) con esta lista 'barcosPorColocar'
            // Por ejemplo:
            // DefaultListModel<String> model = new DefaultListModel<>();
            // barcosPorColocar.forEach(model::addElement);
            // miListaDeBarcosEnPanel2.setModel(model);
            System.out.println("VIEW [TableroJuego]: Flota parseada: " + "barcosPorColocar");
        } else {
             mostrarError("No se recibió la flota para colocar.");
             if(readyButton != null) readyButton.setEnabled(false);
        }
    }

    /** Añade listeners al tablero para manejar clics de colocación */
    private void prepararTableroParaColocacion() {
         System.out.println("VIEW [TableroJuego]: Preparando tablero para colocación...");
         // TODO: Implementar lógica para colocar barcos
         // 1. Añadir MouseListener a tableroJPanel (o a sus celdas si son botones).
         // 2. En el listener:
         //    a. Calcular la celda (fila, columna) donde se hizo clic.
         //    b. Verificar si hay un 'barcoSeleccionado' de la lista.
         //    c. Verificar si el barco cabe en esa posición con la 'orientacionHorizontal' actual.
         //    d. Verificar si colisiona con otros barcos ya puestos en 'miTableroLogico'.
         //    e. Si es válido:
         //       - Dibujar el barco en la UI (tableroJPanel).
         //       - Añadir el barco a 'miTableroLogico'.
         //       - Eliminar el barco de la lista 'barcosPorColocar' y actualizar la UI de la lista.
         //       - Limpiar 'barcoSeleccionado'.
         //       - Si 'barcosPorColocar' está vacío, habilitar 'readyButton'.
         //    f. Si no es válido: Mostrar mensaje de error.
         //
         // También necesitarás listeners en la lista de barcos (jPanel2) para seleccionar
         // un barco y quizás un botón para cambiar la orientación.
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        tableroJPanel = new javax.swing.JPanel();
        numberLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cancelButton = new javax.swing.JButton();
        readyButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tableroJPanel.setBackground(new java.awt.Color(204, 204, 204));
        tableroJPanel.setPreferredSize(new java.awt.Dimension(450, 450));

        numberLabel.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N

        javax.swing.GroupLayout tableroJPanelLayout = new javax.swing.GroupLayout(tableroJPanel);
        tableroJPanel.setLayout(tableroJPanelLayout);
        tableroJPanelLayout.setHorizontalGroup(
            tableroJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableroJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(numberLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 422, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );
        tableroJPanelLayout.setVerticalGroup(
            tableroJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableroJPanelLayout.createSequentialGroup()
                .addGap(179, 179, 179)
                .addComponent(numberLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(213, Short.MAX_VALUE))
        );

        jPanel1.add(tableroJPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 10, 450, 450));

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 150, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 390, Short.MAX_VALUE)
        );

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, 150, 390));

        jLabel1.setFont(new java.awt.Font("Segoe UI Symbol", 1, 24)); // NOI18N
        jLabel1.setText("NAVES:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 10, -1, -1));

        cancelButton.setBackground(new java.awt.Color(255, 105, 97));
        cancelButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        cancelButton.setText("Cancelar");
        cancelButton.setBorder(null);
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        jPanel1.add(cancelButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 260, 120, 40));

        readyButton.setBackground(new java.awt.Color(189, 236, 182));
        readyButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        readyButton.setText("Listo");
        readyButton.setBorder(null);
        readyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readyButtonActionPerformed(evt);
            }
        });
        jPanel1.add(readyButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 160, 120, 40));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 833, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 470, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
       System.out.println("VIEW [TableroJuego]: Botón Resetear presionado.");
        // TODO: Implementar lógica para:
        // 1. Limpiar la representación lógica del tablero (miTableroLogico).
        // 2. Limpiar la representación visual del tablero (tableroJPanel).
        // 3. Restaurar la lista completa de barcos en 'barcosPorColocar' y en la UI (jPanel2).
        // 4. Deshabilitar el botón 'readyButton'.
        // 5. Limpiar 'barcoSeleccionado'.
        parsearYMostrarFlota(); // Vuelve a mostrar la flota completa
        // miTableroLogico.limpiar(); // Método hipotético
        tableroJPanel.repaint(); // Forzar redibujado del tablero vacío
        readyButton.setEnabled(false);
        numberLabel.setText("Colocación reseteada. Vuelve a colocar.");
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void readyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readyButtonActionPerformed
       System.out.println("VIEW [TableroJuego]: Botón Confirmar Colocación presionado.");

         // 1. VALIDAR que todos los barcos estén colocados (ya debería estar hecho para habilitar el botón)
         if (!todosBarcosColocados()) { // Necesitas implementar esta función
              mostrarError("Aún faltan barcos por colocar.");
              return;
         }

         // 2. Serializar el tablero a JSON
         System.out.println("VIEW [TableroJuego]: Serializando tablero...");
         String tableroJson = serializarTableroAJson(); // Necesitas implementar esta función
         System.out.println("VIEW [TableroJuego]: Tablero JSON: " + tableroJson); // Imprime para depurar

         if (tableroJson == null) {
              mostrarError("Error al generar la configuración del tablero.");
              return;
         }

         // 3. Enviar al controlador
         if (controlador != null) {
              System.out.println("VIEW [TableroJuego]: Llamando a controlador.enviarColocacionLista...");
//              controlador.enviarColocacionLista(tableroJson);
              // 4. Deshabilitar UI y mostrar mensaje de espera
              deshabilitarColocacion("Esperando al oponente...");
         } else {
               mostrarError("Error interno: Controlador no disponible.");
         }
    }//GEN-LAST:event_readyButtonActionPerformed
// Acción al cerrar ventana
     private void formWindowClosing(java.awt.event.WindowEvent evt) {
         // Preguntar si quiere abandonar la partida
         int res = JOptionPane.showConfirmDialog(this, "¿Seguro que quieres abandonar la partida?", "Salir", JOptionPane.YES_NO_OPTION);
         if (res == JOptionPane.YES_OPTION) {
              if (controlador != null) {
//                   controlador.salirDePartida(); // Notificar al servidor
              }
               if (controladorPrincipal != null) {
                   // Limpiar referencias si las hubiera para este controlador
                   // controladorPrincipal.clearControladorTableroActual();
               }
               this.dispose(); // Cierra esta ventana
               // Volver a UnirseJugar?
               // new UnirseJugar(controladorPrincipal).setVisible(true);
         } else {
              // Si cancela, no hacer nada (mantener la ventana abierta)
               setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE); // Asegura que no cierre
         }
     }

     // --- Métodos de Lógica Interna y UI ---

     /** Verifica si todos los barcos de la lista han sido colocados */
     private boolean todosBarcosColocados() {
          // TODO: Implementar esta lógica
          // Debe verificar si la lista 'barcosPorColocar' está vacía.
        return false;
          // TODO: Implementar esta lógica
          // Debe verificar si la lista 'barcosPorColocar' está vacía.
         
     }

     /** Convierte el estado lógico del tablero a JSON String */
     private String serializarTableroAJson() {
          // TODO: Implementar la serialización usando Gson, Jackson u otra librería.
          // Debe tomar la información de 'miTableroLogico' (posiciones de los barcos)
          // y convertirla a un formato JSON estándar.
          // Ejemplo simple (¡DEBES MEJORAR ESTO!):
          // StringBuilder json = new StringBuilder("{ \"barcos\": [");
          // for(Barco b : miTableroLogico.getBarcos()) { json.append(b.toJson()).append(","); }
          // if(miTableroLogico.getBarcos().size() > 0) json.deleteCharAt(json.length()-1); // Quita última coma
          // json.append("] }");
          // return json.toString();
          return "{\"barcos\":[{\"nombre\":\"Ejemplo\",\"pos\":[[0,0],[0,1]]}]}"; // Placeholder
     }

     /** Deshabilita la UI de colocación y muestra un mensaje */
     private void deshabilitarColocacion(String mensaje) {
          // TODO: Deshabilitar la interacción con el tablero (quitar listeners?)
          // TODO: Deshabilitar la lista de selección de barcos
          if(readyButton != null) readyButton.setEnabled(false);
          if(cancelButton != null) cancelButton.setEnabled(false);
          if(numberLabel != null) numberLabel.setText(mensaje);
           System.out.println("VIEW [TableroJuego]: UI de colocación deshabilitada. Mensaje: " + mensaje);
     }

     /** Muestra un mensaje de error */
     public void mostrarError(String mensaje) {
         SwingUtilities.invokeLater(() -> {
              System.out.println("VIEW [TableroJuego]: Mostrando error: " + mensaje);
              JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
         });
     }

     /** Muestra un mensaje informativo */
      public void mostrarMensaje(String mensaje) {
         SwingUtilities.invokeLater(() -> {
              System.out.println("VIEW [TableroJuego]: Mostrando mensaje: " + mensaje);
              if(numberLabel != null) { // Usar el label superior para mensajes
                   numberLabel.setText(mensaje);
              } else { // Fallback
                   JOptionPane.showMessageDialog(this, mensaje, "Info", JOptionPane.INFORMATION_MESSAGE);
              }
         });
     }

     // TODO: Añadir métodos para actualizar el tablero propio y el enemigo
     // public void actualizarMiTablero(int x, int y, String resultado) { ... }
     // public void actualizarTableroEnemigo(int x, int y, String resultado) { ... }
     // public void establecerTurno(boolean miTurno) { ... }
     // public void mostrarFinDeJuego(boolean ganaste, String motivo) { ... }


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(TableroJuego.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                // ESTE CONSTRUCTOR YA NO ES VÁLIDO - Se debe crear desde PartidaEspera
                // new TableroJuego().setVisible(true);
                 System.err.println("TableroJuego no se puede ejecutar directamente. Inicia desde PantallaInicio.");
                 // Mostrar un mensaje al usuario si intenta ejecutar este main
                  JOptionPane.showMessageDialog(null, "Ejecuta la aplicación desde PantallaInicio.", "Error de Ejecución", JOptionPane.ERROR_MESSAGE);

            }
        });
    }
    /**
     * @param args the command line arguments
     */
   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel numberLabel;
    private javax.swing.JButton readyButton;
    private javax.swing.JPanel tableroJPanel;
    // End of variables declaration//GEN-END:variables
}
