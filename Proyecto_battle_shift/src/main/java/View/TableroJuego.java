/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View;

import Controler.controladorInicio;
import Controler.controladorTablero;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import View.IconTransferHandler;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    
    private Set<String> posicionesOcupadas = new HashSet<>();
    private Point initialClick;
    private JLabel lblArrastrando;
    private List<Map<String, Object>> barcosColocados = new ArrayList<>();
    private Image rotatedImage;
    private boolean isRotado = false;
    private int tamañoBarcoR;

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
        this.flotaString = "Portaaviones:4,Crucero:3,Submarino:2,Barco:1";
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
        prepararTableroParaColocacion(); // Configurar listeners del tablero
    }

    /** Configura elementos iniciales de la UI */
    private void configurarUIInicial() {
        if(numberLabel != null) numberLabel.setText("Coloca tus barcos. Haz clic en un barco y luego en el tablero.");
        if(readyButton != null) readyButton.setEnabled(false); // Deshabilitado hasta colocar todos
        if(cancelButton != null) cancelButton.setText("Resetear"); // Cambiar texto
        if(cancelButton != null) cancelButton.setEnabled(true);  // Habilitado para resetear
        // TODO: Limpiar/dibujar la cuadrícula inicial en tableroJPanel
        dibujarTablero();
    }

    
    /** Parsea el string de flota y actualiza la UI (ej. un JList en jPanel2) */
    private void parsearYMostrarFlota() {
        System.out.println("VIEW [TableroJuego]: Parseando flota: " + flotaString);
        if (flotaString != null && !flotaString.isEmpty()) {
             barcosParaColocar = new ArrayList<>(Arrays.asList(flotaString.split(",")));
            
            
            posicionesOcupadas.clear();
            tableroJPanel.removeAll();
            tableroJPanel.repaint();
            dibujarTablero();
            
            System.out.println("VIEW [TableroJuego]: Flota parseada: " + "barcosPorColocar");
            Map<String, Integer> tiposBarcos = obtenerMapaDesdeFlota(flotaString);
            configurarBarcos(tiposBarcos);
        } else {
             mostrarError("No se recibió la flota para colocar.");
             if(readyButton != null) readyButton.setEnabled(false);
        }
    }
    

    private void configurarBarcos(Map<String, Integer> tiposBarcos) {
        int cellSize = 40;

        // Limpia el panel de selección
        jPanel2.removeAll();
        jPanel2.setLayout(new BoxLayout(jPanel2, BoxLayout.Y_AXIS));

        // Helper para crear botones de barco
        BiConsumer<String, Integer> creaBotones = (tipo, cantidad) -> {
            String ruta;
            int tamaño;
            switch (tipo) {
                case "crucero":
                    ruta = "/Images/Cruise.png";
                    tamaño = 3; 
                    break;
                case "submarino":
                    ruta = "/Images/Submarine.png";
                    tamaño = 1;
                    break;
                case "barco":
                    ruta = "/Images/Destroyer.png";
                    tamaño = 2;
                    break;
                case "artillero":
                default:
                    ruta = "/Images/Battleship.png";
                    tamaño = 4;
                    break;
            }

            ImageIcon iconBase = cargarIcono(ruta);
            tamañoBarcoR=tamaño;
            
            for (int i = 0; i < cantidad; i++) {
                
                final Image img= iconBase.getImage()
                    .getScaledInstance(tamaño * cellSize, cellSize, Image.SCALE_SMOOTH);
                
                final JButton btn = new JButton(new ImageIcon(img));
                btn.setActionCommand(String.valueOf(tamaño));
                btn.setPreferredSize(new Dimension(tamaño * cellSize, cellSize));

                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON3) {
                        isRotado = !isRotado;
                        ImageIcon icon = (ImageIcon) btn.getIcon();
                        Image originalImg = iconBase.getImage().getScaledInstance(tamaño * cellSize, cellSize, Image.SCALE_SMOOTH);

                        Image nuevaImg;
                        if (isRotado) {
                            rotatedImage = rotateImage(originalImg);
                            nuevaImg = rotatedImage;
                            btn.setPreferredSize(new Dimension(cellSize, tamaño * cellSize));
                        } else {
                            rotatedImage = originalImg;
                            nuevaImg = rotatedImage;
                            btn.setPreferredSize(new Dimension(tamaño * cellSize, cellSize));
                        }

                        btn.setIcon(new ImageIcon(nuevaImg));
                        jPanel2.revalidate();
                        jPanel2.repaint();
                        return;
                    }
                        initialClick = e.getPoint(); // Guardar posición inicial
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON3) {
                            return;
                        }
                        if (lblArrastrando != null) {
                            tableroJPanel.remove(lblArrastrando);
                            lblArrastrando = null;

                            Point puntoEnTablero = SwingUtilities.convertPoint(btn, e.getPoint(), tableroJPanel);

                            // Alinea al grid
                            int col = (puntoEnTablero.x - cellSize) / cellSize;
                            int row = (puntoEnTablero.y - cellSize) / cellSize;

                            int tamaño = Integer.parseInt(btn.getActionCommand());

                            // Verificar si el barco cabe en la posición, considerando si está rotado
                            if (row < 0 || col < 0 || (isRotado && (row + tamaño) > 10) || (!isRotado && (col + tamaño) > 10)) {
                                JOptionPane.showMessageDialog(null, "No puedes colocar el barco fuera del tablero.");
                                return;
                            }

                            // Verifica si alguna posición ya está ocupada (horizontal)
                            for (int i = 0; i < tamaño; i++) {
                                String clave = row + "," + (col + i);
                                if (posicionesOcupadas.contains(clave)) {
                                    JOptionPane.showMessageDialog(null, "Ya hay un barco en esa posición.");
                                    return;
                                }
                            }

                            // Verifica si alguna posición ya está ocupada (vertical) si está rotado
                            if (isRotado) {
                                for (int i = 0; i < tamaño; i++) {
                                    String clave = (row + i) + "," + col;
                                    if (posicionesOcupadas.contains(clave)) {
                                        JOptionPane.showMessageDialog(null, "Ya hay un barco en esa posición.");
                                        return;
                                    }
                                }
                            }

                            // Marcar todas las posiciones como ocupadas
                            for (int i = 0; i < tamaño; i++) {
                                if (isRotado) {
                                    System.out.println("Rotado ocupado");
                                    posicionesOcupadas.add((row+i) + "," + col); // Vertical
                                } else {
                                    posicionesOcupadas.add(row + "," + (col + i)); // Horizontal
                                }
                            }

                            // Colocar el barco usando la imagen rotada o no, según el estado
                            JLabel lbl = new JLabel(new ImageIcon(rotatedImage));
                            if (isRotado) {
                                lbl.setBounds((col + 1) * cellSize, (row + 1) * cellSize, rotatedImage.getWidth(null), rotatedImage.getHeight(null));
                            } else {
                                lbl.setBounds((col + 1) * cellSize, (row + 1) * cellSize, img.getWidth(null), img.getHeight(null));
                            }
                            lbl.setOpaque(false);
                            tableroJPanel.add(lbl);
                            tableroJPanel.setComponentZOrder(lbl, 0);
                            tableroJPanel.revalidate();
                            tableroJPanel.repaint();

                            // Guardar información del barco
                            Map<String, Object> barcoInfo = new HashMap<>();
                            barcoInfo.put("fila", row);
                            barcoInfo.put("columna", col);
                            barcoInfo.put("tamaño", tamaño);
                            barcosColocados.add(barcoInfo);

                            btn.setVisible(false); // Ocultar botón
                        }
                    }
                });

                btn.addMouseMotionListener(new MouseMotionAdapter() {
                    @Override
                    public void mouseDragged(MouseEvent e) {
                        if (lblArrastrando != null) {
                            tableroJPanel.remove(lblArrastrando);
                            lblArrastrando = null;
                            tableroJPanel.repaint();
                        }

                        ImageIcon icon = (ImageIcon) btn.getIcon();
                        Image img = icon.getImage();

                        if (isRotado) {
                            rotatedImage = rotateImage(img);
                        } else {
                            rotatedImage = img;
                        }

                        lblArrastrando = new JLabel(new ImageIcon(rotatedImage));
                        lblArrastrando.setSize(rotatedImage.getWidth(null), rotatedImage.getHeight(null));
                        lblArrastrando.setOpaque(false);
                        tableroJPanel.add(lblArrastrando);
                        tableroJPanel.setComponentZOrder(lblArrastrando, 0);

                        Point puntoEnTablero = SwingUtilities.convertPoint(btn, e.getPoint(), tableroJPanel);
                        if (isRotado) {
                            lblArrastrando.setLocation(
                            puntoEnTablero.x - (lblArrastrando.getWidth() / 6),
                            puntoEnTablero.y - (lblArrastrando.getHeight() / 6)
                        );
                        } else {
                            lblArrastrando.setLocation(
                            puntoEnTablero.x - (lblArrastrando.getWidth() / 6),
                            puntoEnTablero.y - (lblArrastrando.getHeight() / 2)
                        );
                        }
                        

                        tableroJPanel.revalidate();
                        tableroJPanel.repaint();
                    }
                });

                jPanel2.add(btn);
                jPanel2.add(Box.createVerticalStrut(5));
            }
        };
        for (Map.Entry<String, Integer> entry : tiposBarcos.entrySet()) {
            creaBotones.accept(entry.getKey(), entry.getValue());
        }
        jPanel2.revalidate();
        jPanel2.repaint();
    }
    
    
    private Map<String, Integer> obtenerMapaDesdeFlota(String flotaString) {
        Map<String, Integer> mapa = new HashMap<>();

        if (flotaString == null || flotaString.isEmpty()) return mapa;

        if (flotaString.startsWith("flota=")) {
            flotaString = flotaString.substring(6);
        }

        String[] partes = flotaString.split(",");
        for (String parte : partes) {
            String[] tipoYValor = parte.split(":");
            if (tipoYValor.length == 2) {
                String tipo = tipoYValor[0].trim().toLowerCase();
                int cantidad;
                try {
                    cantidad = Integer.parseInt(tipoYValor[1].trim());
                } catch (NumberFormatException e) {
                    cantidad = 0;
                }

                switch (tipo) {
                    case "portaaviones":
                        tipo = "artillero";
                        break;
                    case "crucero":
                    case "submarino":
                    case "barco":
                        break;
                    default:
                        System.out.println("Tipo de barco desconocido: " + tipo);
                        continue;
                }

                mapa.put(tipo, cantidad);
            }
        }

        return mapa;
    }

   private Image rotateImage(Image img) {
        int w = img.getWidth(null);
        int h = img.getHeight(null);

        // El nuevo BufferedImage debe ser suficientemente grande para contener la imagen rotada
        BufferedImage rotated = new BufferedImage(h, w, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Mover el punto de origen al centro del nuevo lienzo
        g2d.translate(h / 2.0, w / 2.0);
        g2d.rotate(Math.toRadians(90));
        g2d.translate(-w / 2.0, -h / 2.0);

        // Dibujar la imagen rotada
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return rotated;
    }
 
    private ImageIcon cargarIcono(String ruta) {
        URL url = getClass().getResource(ruta);
        if (url == null) {
            System.err.println("No se encontró la imagen: " + ruta);
            return new ImageIcon();
        }
        return new ImageIcon(url);
    }

    private void dibujarTablero() {
        tableroJPanel.removeAll();
        tableroJPanel.setLayout(null);

        int cellSize = 40;
        int filas = 10;
        int columnas = 10;
        int offset = cellSize; // Espacio para coordenadas (una fila y una columna extra)

        int anchoTablero = (columnas + 1) * cellSize;
        int altoTablero = (filas + 1) * cellSize;

        // Escalar imagen de fondo al tamaño del tablero
        ImageIcon fondoOriginal = cargarIcono("/Images/Board.png");
        Image imagenEscalada = fondoOriginal.getImage().getScaledInstance(anchoTablero, altoTablero, Image.SCALE_SMOOTH);
        ImageIcon fondo = new ImageIcon(imagenEscalada);

        JLabel fondoLabel = new JLabel(fondo);
        fondoLabel.setBounds(0, 0, anchoTablero, altoTablero);
        fondoLabel.setLayout(null); // Permitir agregar componentes manualmente

        // Crear botones y agregarlos al fondoLabel (dejando espacio para coordenadas)
        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                JButton celda = new JButton();
                celda.setBackground(new Color(255, 255, 255, 0)); // Transparente
                celda.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                celda.setBounds((columna + 1) * cellSize, (fila + 1) * cellSize, cellSize, cellSize); // +1 por el offset
                celda.setActionCommand(fila + "," + columna);
                celda.setFocusable(false);
                celda.addActionListener(e -> {
                    String[] pos = e.getActionCommand().split(",");
                    System.out.println("Click en celda: (" + pos[0] + "," + pos[1] + ")");
                });
                fondoLabel.add(celda);
            }
        }

        tableroJPanel.add(fondoLabel);
        tableroJPanel.setPreferredSize(new Dimension(anchoTablero, altoTablero));
        tableroJPanel.revalidate();
        tableroJPanel.repaint();
    }

    /** Añade listeners al tablero para manejar clics de colocación */
    private void prepararTableroParaColocacion() {
         System.out.println("VIEW [TableroJuego]: Preparando tablero para colocación...");
         Map<String, Integer> tiposBarcos = obtenerMapaDesdeFlota(flotaString);
         configurarBarcos(tiposBarcos);
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
                .addComponent(numberLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 422, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        tableroJPanelLayout.setVerticalGroup(
            tableroJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableroJPanelLayout.createSequentialGroup()
                .addGap(179, 179, 179)
                .addComponent(numberLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(203, Short.MAX_VALUE))
        );

        jPanel1.add(tableroJPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 20, 440, 440));

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 190, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 380, Short.MAX_VALUE)
        );

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, 190, 380));

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
        isRotado = false;
        parsearYMostrarFlota(); // Vuelve a mostrar la flota completa
        // miTableroLogico.limpiar(); // Método hipotético
        tableroJPanel.repaint(); // Forzar redibujado del tablero vacío
        readyButton.setEnabled(false);
        numberLabel.setText("Colocación reseteada. Vuelve a colocar.");
        
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void readyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readyButtonActionPerformed
       System.out.println("VIEW [TableroJuego]: Botón Confirmar Colocación presionado.");
       System.out.println("Barcos colocados:");
            for (Map<String, Object> barco : barcosColocados) {
                int fila = (int) barco.get("fila");
                int columna = (int) barco.get("columna");
                int tamaño = (int) barco.get("tamaño");
                System.out.println("Barco tamaño " + tamaño + " en (" + fila + ", " + columna + ")");
        }
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
