/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View;

import Controler.controladorCrearPartida;
import Controler.controladorInicio;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;



/**
 *
 * @author javie
 */
/**
 * Pantalla donde el usuario puede crear una nueva sala o unirse a una existente.
 */
public class UnirseJugar extends javax.swing.JFrame {


    // --- Atributos ---
    // Controlador específico para las acciones de ESTA pantalla
    private final controladorCrearPartida controlador;
    // Referencia al controlador principal (útil para desconectar al cerrar)
    private final controladorInicio controladorPrincipal;


    /**
     * Creates new form UnirseJugar.
     * @param ctrlInicio El controlador principal desde el que se navega,
     * necesario para obtener la instancia de ServerComunicacion.
     */
    public UnirseJugar(controladorInicio ctrlInicio) {
        initComponents(); // Inicializa componentes Swing (generado por NetBeans)
        this.setLocationRelativeTo(null); // Centrar ventana
        this.controladorPrincipal = ctrlInicio; // Guardar referencia

        // --- Crear e inicializar el controlador específico ---
        controladorCrearPartida ctrlSecundario = null;
        if (ctrlInicio != null && ctrlInicio.getServerComunicacion() != null) {
            System.out.println("VIEW [UnirseJugar]: Creando controladorCrearPartida...");
            ctrlSecundario = new controladorCrearPartida(ctrlInicio.getServerComunicacion());
            ctrlSecundario.setVista(this); // El nuevo controlador conoce esta vista
            // Informar al controlador principal sobre este controlador secundario activo
            ctrlInicio.setControladorCrearPartidaActual(ctrlSecundario);
            System.out.println("VIEW [UnirseJugar]: Controlador específico creado y asignado.");
        } else {
            // Error grave si no podemos obtener la comunicación
            System.err.println("VIEW [UnirseJugar] ERROR CRÍTICO: No se pudo obtener ServerComunicacion.");
            mostrarError("Error crítico de inicialización. Cierre la aplicación.");
            // Deshabilitar funcionalidad si falla
            btnCrearSala.setEnabled(false);
            btnUnirseSala.setEnabled(false);
        }
        this.controlador = ctrlSecundario; // Asignar el controlador creado a la variable de instancia
    }

    // --- Métodos llamados por el Controlador para actualizar esta UI ---

    // --- Este método va DENTRO de tu clase View.UnirseJugar.java ---

    /**
     * Navega a la pantalla de espera (PartidaEspera) después de crear o unirse exitosamente.
     * Crea la nueva vista y su controlador asociado, y actualiza las referencias en controladorInicio.
     * @param idSala El ID de la sala a la que se entró.
     */
    public void navegarAPantallaEspera(String idSala) {
         // Asegurar que la creación y actualización de UI ocurra en el hilo de eventos de Swing
         SwingUtilities.invokeLater(() -> {
             System.out.println("VIEW [UnirseJugar]: Navegando a PantallaEspera para sala: " + idSala);

             // 1. Verificar que tenemos la referencia al controlador principal
             //    Es necesario para obtener el nombre del usuario y pasar la comunicación
             //    a la nueva pantalla/controlador.
             if (this.controladorPrincipal == null) {
                 System.err.println("VIEW [UnirseJugar] ERROR CRÍTICO: controladorPrincipal es null. No se puede continuar.");
                 // Usar el método mostrarError de esta ventana (UnirseJugar)
                 mostrarError("Error interno grave (Controlador Principal no encontrado). No se puede abrir la sala.");
                 reactivarBotones(); // Reactivar botones aquí ya que no se navega
                 return; // Detener ejecución
             }

             // 2. Obtener el nombre del usuario que se registró exitosamente
             //    Necesitas haber añadido el getter en controladorInicio.java
             String miNombre = this.controladorPrincipal.getNombreUsuarioRegistrado();
             if (miNombre == null || miNombre.isBlank()) {
                 // Si no podemos obtener el nombre, es un problema, pero podemos continuar con un default
                 System.err.println("VIEW [UnirseJugar] WARN: No se pudo obtener el nombre de usuario registrado desde controladorInicio. Usando default.");
                 miNombre = "Jugador ???";
             }
             System.out.println("VIEW [UnirseJugar]: Nombre de usuario para PantallaEspera: " + miNombre);


             // --- Navegación Real ---
             try {
                  // 3. Crear la NUEVA ventana de espera (PartidaEspera)
                  //    Le pasamos el controlador principal, el ID de la sala y el nombre del jugador.
                  //    El constructor de PartidaEspera se encargará de crear su propio controlador
                  //    y de registrarse como el controlador secundario activo en controladorInicio.
                  System.out.println("VIEW [UnirseJugar]: Creando instancia de PartidaEspera...");
                  PartidaEspera pantallaEspera = new PartidaEspera(this.controladorPrincipal, idSala, miNombre);

                  // 4. Limpiar la referencia al controlador de ESTA pantalla (UnirseJugar)
                  //    en el controlador principal, ya que vamos a cerrar esta ventana.
                  System.out.println("VIEW [UnirseJugar]: Limpiando controladorCrearPartidaActual en controladorInicio...");
                  this.controladorPrincipal.clearControladorCrearPartidaActual();

                  // 5. Hacer visible la nueva pantalla de espera
                  System.out.println("VIEW [UnirseJugar]: Haciendo visible PartidaEspera...");
                  pantallaEspera.setVisible(true);

                  // 6. Cerrar ESTA ventana (UnirseJugar)
                  System.out.println("VIEW [UnirseJugar]: Cerrando esta ventana (dispose)...");
                  this.dispose();

             } catch (Exception e) {
                  // Capturar cualquier error inesperado durante la creación/visualización
                  // de la ventana PartidaEspera.
                  System.err.println("VIEW [UnirseJugar] ERROR CRÍTICO: Excepción al crear/mostrar PartidaEspera: " + e.getMessage());
                  e.printStackTrace(); // Imprimir stack trace para depuración
                  // Mostrar error en ESTA ventana, ya que no pudimos navegar
                  mostrarError("No se pudo abrir la pantalla de espera.\nError: " + e.getMessage());
                   // Reactivar botones para permitir reintento si falla la navegación
                   reactivarBotones();
                   // Limpiar referencia por si acaso
                   this.controladorPrincipal.clearControladorCrearPartidaActual();
             }
             // --- Fin Navegación Real ---

         }); // Fin de invokeLater
    } // Fin de navegarAPantallaEspera


    /**
     * Muestra un mensaje de error en un diálogo.
     * Llamado por controladorCrearPartida.
     * @param mensaje El mensaje de error.
     */
    public void mostrarError(String mensaje) {
        // Asegurar que se ejecuta en el hilo de Swing
        SwingUtilities.invokeLater(() -> {
             System.out.println("VIEW [UnirseJugar]: Mostrando error: " + mensaje);
             JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
             // Como hubo un error, reactivar botones para que el usuario intente de nuevo
             reactivarBotones();
        });
    }

    /** Método helper para deshabilitar botones y mostrar estado */
    private void deshabilitarBotones(String textoEstado) {
         btnCrearSala.setEnabled(false);
         btnUnirseSala.setEnabled(false);
         btnCrearSala.setText(textoEstado); // Mostrar estado en un botón
         btnUnirseSala.setText("...");
    }

    /** Método helper para reactivar ambos botones */
    public void reactivarBotones() {
        reactivarBotonCrear();
        reactivarBotonUnirse();
    }

    /** Reactiva el botón de Crear Sala */
    public void reactivarBotonCrear() {
        // Asegurar que se ejecuta en el hilo de Swing
        SwingUtilities.invokeLater(() -> {
            btnCrearSala.setEnabled(true);
            btnCrearSala.setText("Crear Sala");
             // Reactivar el otro botón también si no hay acción en curso
             if (btnUnirseSala.getText().equals("...")) {
                 btnUnirseSala.setEnabled(true);
                 btnUnirseSala.setText("Unirse a Sala");
             }
        });
    }

     /** Reactiva el botón de Unirse a Sala */
    public void reactivarBotonUnirse() {
         // Asegurar que se ejecuta en el hilo de Swing
         SwingUtilities.invokeLater(() -> {
             btnUnirseSala.setEnabled(true);
             btnUnirseSala.setText("Unirse a Sala");
              // Reactivar el otro botón también si no hay acción en curso
             if (btnCrearSala.getText().equals("...")) {
                 btnCrearSala.setEnabled(true);
                 btnCrearSala.setText("Crear Sala");
             }
         });
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
        jPanel2 = new javax.swing.JPanel();
        btnCrearSala = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        btnUnirseSala = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtCrearSala = new javax.swing.JTextField();
        txtNombreSalaUnirse = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(32, 51, 75));
        jPanel2.setForeground(new java.awt.Color(32, 51, 75));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnCrearSala.setBackground(new java.awt.Color(67, 68, 84));
        btnCrearSala.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnCrearSala.setForeground(new java.awt.Color(255, 255, 255));
        btnCrearSala.setText("CREAR PARTIDA");
        btnCrearSala.setAutoscrolls(true);
        btnCrearSala.setBorderPainted(false);
        btnCrearSala.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCrearSalaActionPerformed(evt);
            }
        });
        jPanel2.add(btnCrearSala, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 310, -1, -1));

        jPanel3.setBackground(new java.awt.Color(36, 37, 56));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("Únete a una partida o creala tu mismo");

        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("<");
        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.setFocusPainted(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1)
                .addGap(84, 84, 84)
                .addComponent(jLabel1)
                .addContainerGap(163, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(41, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(27, 27, 27))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 730, 100));

        btnUnirseSala.setBackground(new java.awt.Color(67, 68, 84));
        btnUnirseSala.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnUnirseSala.setForeground(new java.awt.Color(255, 255, 255));
        btnUnirseSala.setText("UNIRSE");
        btnUnirseSala.setAutoscrolls(true);
        btnUnirseSala.setBorderPainted(false);
        btnUnirseSala.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUnirseSalaActionPerformed(evt);
            }
        });
        jPanel2.add(btnUnirseSala, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 320, -1, -1));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setText("Unirse a una partida existente");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 200, -1, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setText("Crea una partida");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 200, -1, -1));

        txtCrearSala.setBackground(new java.awt.Color(67, 68, 84));
        txtCrearSala.setForeground(new java.awt.Color(255, 255, 255));
        txtCrearSala.setBorder(null);
        txtCrearSala.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCrearSalaActionPerformed(evt);
            }
        });
        jPanel2.add(txtCrearSala, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 250, 230, 30));

        txtNombreSalaUnirse.setBackground(new java.awt.Color(67, 68, 84));
        txtNombreSalaUnirse.setForeground(new java.awt.Color(255, 255, 255));
        txtNombreSalaUnirse.setBorder(null);
        txtNombreSalaUnirse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNombreSalaUnirseActionPerformed(evt);
            }
        });
        jPanel2.add(txtNombreSalaUnirse, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 260, 230, 30));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 730, 490));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 730, 490));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCrearSalaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCrearSalaActionPerformed
       String nombreSala = txtCrearSala.getText().trim();
        System.out.println("VIEW [UnirseJugar]: Botón Crear presionado. Nombre: " + nombreSala);
        if (nombreSala.isBlank()) {
            mostrarError("Ingresa un nombre para crear la sala.");
            return;
        }
        // Deshabilitar botones mientras se procesa
        deshabilitarBotones("Creando...");
        if (controlador != null) {
             System.out.println("VIEW [UnirseJugar]: Llamando a controladorCrearPartida.solicitarCreacionSala...");
             controlador.solicitarCreacionSala(nombreSala);
        } else {
             mostrarError("Error interno: Controlador no disponible.");
             reactivarBotones();
        }
    }//GEN-LAST:event_btnCrearSalaActionPerformed

    private void btnUnirseSalaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUnirseSalaActionPerformed
        String nombreSala = txtNombreSalaUnirse.getText().trim();
         System.out.println("VIEW [UnirseJugar]: Botón Unirse presionado. Nombre: " + nombreSala);
         if (nombreSala.isBlank()) {
             mostrarError("Ingresa el nombre de la sala para unirte.");
             return;
         }
          // Deshabilitar botones mientras se procesa
         deshabilitarBotones("Uniendo...");
         if (controlador != null) {
             System.out.println("VIEW [UnirseJugar]: Llamando a controladorCrearPartida.solicitarUnirseSala...");
              controlador.solicitarUnirseSala(nombreSala);
         } else {
             mostrarError("Error interno: Controlador no disponible.");
             reactivarBotones();
         }
    }//GEN-LAST:event_btnUnirseSalaActionPerformed

    private void txtCrearSalaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCrearSalaActionPerformed
        // TODO add your handling code here:
        
        
        
    }//GEN-LAST:event_txtCrearSalaActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        PantallaInicio pI=new PantallaInicio();
        pI.show();
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void txtNombreSalaUnirseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNombreSalaUnirseActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNombreSalaUnirseActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCrearSala;
    private javax.swing.JButton btnUnirseSala;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JTextField txtCrearSala;
    private javax.swing.JTextField txtNombreSalaUnirse;
    // End of variables declaration//GEN-END:variables
}
