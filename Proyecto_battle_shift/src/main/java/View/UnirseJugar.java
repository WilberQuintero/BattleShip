/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View;

import Controler.controladorCrearPartida;
import Controler.controladorInicio;
import Controler.controladorPartidaEspera;
import Controler.controladorTablero;
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
    // Referencia al controlador principal (útil para desconectar al cerrar o navegar)
    private final controladorInicio controladorPrincipal;

    /**
     * Creates new form UnirseJugar.
     * @param ctrlInicio El controlador principal desde el que se navega,
     * necesario para obtener la instancia de ServerComunicacion.
     */
    public UnirseJugar(controladorInicio ctrlInicio) {
        initComponents(); // Inicializa componentes Swing (generado por NetBeans)
        this.setLocationRelativeTo(null); // Centrar
        this.controladorPrincipal = ctrlInicio; // Guardar referencia
       

        // --- Crear e inicializar el controlador específico ---
        controladorCrearPartida ctrlSecundario = null;
        if (ctrlInicio != null && ctrlInicio.getServerComunicacion() != null) {
            System.out.println("VIEW [UnirseJugar]: Creando controladorCrearPartida...");
            // Le pasamos la instancia de ServerComunicacion obtenida del controlador principal
            ctrlSecundario = new controladorCrearPartida(ctrlInicio.getServerComunicacion());
            // Le decimos al controlador específico cuál es su vista
            ctrlSecundario.setVista(this);
             // Le decimos al controlador principal cuál es el controlador secundario activo ahora
            ctrlInicio.setControladorCrearPartidaActual(ctrlSecundario);
            System.out.println("VIEW [UnirseJugar]: Controlador específico creado y asignado.");
        } else {
            // Error grave si no podemos obtener la comunicación
             System.err.println("VIEW [UnirseJugar] ERROR CRÍTICO: No se pudo obtener ServerComunicacion del controlador principal.");
             mostrarError("Error crítico de inicialización. Cierre la aplicación.");
             // Deshabilitar funcionalidad si falla
             if(btnCrearSala != null) btnCrearSala.setEnabled(false); // Verificar null por si initComponents falla
             if(btnUnirseSala != null) btnUnirseSala.setEnabled(false);
        }
        // Asignar el controlador creado (puede ser null si falló arriba) a la variable de instancia
        this.controlador = ctrlSecundario;
    }
    
    
    
    /**
 * Navega a la pantalla de tablero de juego.
 * Se asume que este método es llamado desde el EDT.
 * @param idSala El identificador de la sala a la que se va a unir el tablero.
 */
public void navegarAPantallaTablero(String idSala) {
    System.out.println("VIEW [UnirseJugar]: Navegando a TableroJuego para sala: " + idSala);

    if (this.controladorPrincipal == null) {
        System.err.println("VIEW [UnirseJugar] ERROR CRÍTICO: controladorPrincipal es null.");
        mostrarError("Error interno grave al intentar abrir el tablero.");
        reactivarBotones();
        return;
    }

    String miNombre = this.controladorPrincipal.getNombreUsuarioRegistrado();
    if (miNombre == null || miNombre.isBlank()) {
        System.err.println("VIEW [UnirseJugar] WARN: No se pudo obtener nombre. Usando default.");
        miNombre = "Jugador ???";
    }
    System.out.println("VIEW [UnirseJugar]: Nombre de usuario para TableroJuego: " + miNombre);

    try {
        System.out.println("VIEW [UnirseJugar]: Creando instancia de TableroJuego...");
        TableroJuego pantallaTablero = new TableroJuego(this.controladorPrincipal, idSala, miNombre,"");

        System.out.println("VIEW [UnirseJugar]: Limpiando controladorCrearPartidaActual en controladorInicio...");
        this.controladorPrincipal.clearControladorCrearPartidaActual();

        System.out.println("VIEW [UnirseJugar]: Haciendo visible TableroJuego...");
        pantallaTablero.setVisible(true);

        System.out.println("VIEW [UnirseJugar]: Cerrando esta ventana (dispose)...");
        this.dispose();

    } catch (Exception e) {
        System.err.println("VIEW [UnirseJugar] ERROR CRÍTICO: Excepción al crear/mostrar TableroJuego: " + e.getMessage());
        e.printStackTrace();
        mostrarError("No se pudo abrir la pantalla de juego.\nError: " + e.getMessage());
        reactivarBotones();
        this.controladorPrincipal.clearControladorCrearPartidaActual(); // Limpiar por si acaso
    }
}


    /**
     * Navega a la pantalla de espera (SIN invokeLater interno).
     * Se asume que este método es llamado desde el EDT (ej. acción de botón).
     * @param idSala
     */
    public void navegarAPantallaEspera(String idSala) {

        System.out.println("VIEW [UnirseJugar]: Navegando a PantallaEspera para sala: " + idSala);

        if (this.controladorPrincipal == null) {
            System.err.println("VIEW [UnirseJugar] ERROR CRÍTICO: controladorPrincipal es null.");
            mostrarError("Error interno grave al intentar abrir la sala.");
            reactivarBotones();
            return;
        }

        String miNombre = this.controladorPrincipal.getNombreUsuarioRegistrado();
        if (miNombre == null || miNombre.isBlank()) {
            System.err.println("VIEW [UnirseJugar] WARN: No se pudo obtener nombre. Usando default.");
            miNombre = "Jugador ???";
        }
        System.out.println("VIEW [UnirseJugar]: Nombre de usuario para PantallaEspera: " + miNombre);

        try {
            System.out.println("VIEW [UnirseJugar]: Creando instancia de PartidaEspera...");
            // Crear la pantalla de espera. Su constructor asigna el controladorEsperaActual.
            PartidaEspera pantallaEspera = new PartidaEspera(this.controladorPrincipal, idSala, miNombre);

            System.out.println("VIEW [UnirseJugar]: Limpiando controladorCrearPartidaActual en controladorInicio...");
            this.controladorPrincipal.clearControladorCrearPartidaActual();

            System.out.println("VIEW [UnirseJugar]: Haciendo visible PartidaEspera...");
            pantallaEspera.setVisible(true);

            System.out.println("VIEW [UnirseJugar]: Cerrando esta ventana (dispose)...");
            this.dispose();

        } catch (Exception e) {
            System.err.println("VIEW [UnirseJugar] ERROR CRÍTICO: Excepción al crear/mostrar PartidaEspera: " + e.getMessage());
            e.printStackTrace();
            mostrarError("No se pudo abrir la pantalla de espera.\nError: " + e.getMessage());
            reactivarBotones();
            this.controladorPrincipal.clearControladorCrearPartidaActual(); // Limpiar por si acaso
        }
       
    }


    /**
     * Muestra un mensaje de error en un diálogo.
     * Llamado por controladorCrearPartida.
     * @param mensaje El mensaje de error.
     */
    public void mostrarError(String mensaje) {
        // Asegurar ejecución en EDT
        SwingUtilities.invokeLater(() -> {
             System.out.println("VIEW [UnirseJugar]: Mostrando error: " + mensaje);
             JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
             // Siempre reactivar botones después de un error mostrado en esta pantalla
             reactivarBotones();
        });
    }

    /** Método helper para deshabilitar botones y mostrar estado */
    private void deshabilitarBotones(String textoEstado) {
        if (btnCrearSala != null) {
             btnCrearSala.setEnabled(false);
             btnCrearSala.setText(textoEstado);
        }
         if (btnUnirseSala != null) {
             btnUnirseSala.setEnabled(false);
              // Solo poner texto de estado en el botón que se presionó
             if (!"Creando...".equals(textoEstado)) {
                 btnUnirseSala.setText("...");
             } else {
                 btnUnirseSala.setText(textoEstado); // Si era crear, el otro también muestra creando? o solo puntos?
             }
             if (!"Uniendo...".equals(textoEstado)) {
                  btnCrearSala.setText("...");
             } else {
                  btnCrearSala.setText(textoEstado);
             }

         }
    }


    /** Método helper para reactivar ambos botones */
    public void reactivarBotones() {
        reactivarBotonCrear();
        reactivarBotonUnirse();
    }

    /** Reactiva el botón de Crear Sala */
    public void reactivarBotonCrear() {
        // Asegurar ejecución en EDT
        SwingUtilities.invokeLater(() -> {
            if (btnCrearSala != null) {
                 btnCrearSala.setEnabled(true);
                 btnCrearSala.setText("Crear Sala");
            }
        });
    }

     /** Reactiva el botón de Unirse a Sala */
    public void reactivarBotonUnirse() {
         // Asegurar ejecución en EDT
         SwingUtilities.invokeLater(() -> {
             if (btnUnirseSala != null) {
                 btnUnirseSala.setEnabled(true);
                 btnUnirseSala.setText("Unirse a Sala");
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
         System.out.println("VIEW [UnirseJugar]: Botón Unirse presionado. Nombre: " + nombreSala);
         if (nombreSala.isBlank()) {
             mostrarError("Ingresa el nombre de la sala para unirte.");
             return;
         }
         deshabilitarBotones("Uniendo..."); // Llama a helper
         if (controlador != null) {
             System.out.println("VIEW [UnirseJugar]: Llamando a controladorCrearPartida.solicitarCrearSala...");
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
