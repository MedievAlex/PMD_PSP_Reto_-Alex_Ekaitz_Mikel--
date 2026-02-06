package ema.maven.view;

import ema.maven.service.Servicio;
import ema.maven.model.APK;
import ema.maven.model.Usuario;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Base64;
import javax.imageio.ImageIO;
import java.util.List;

@Component
public class ClienteGUI {

    private JFrame frame;
    private final Servicio servicio;
    
    // Panel Usuarios
    private JTextField txtNombre;
    private JPasswordField txtPassword;
    private JTextArea textoUsuarios;

    // Panel APKs - Buscar y Listar
    private JTextField txtTitulo;
    private JPanel panelTarjetas;

    // Panel Crear APK
    private JTextField txtNuevoTitulo;
    private JTextField txtNuevoAutor;
    private JTextField txtNuevaDescripcion;
    private JTextField txtNuevaImagenPath;
    private String imagenBase64Nueva = "";

    // Panel Actualizar APK
    private JTextField txtActualizarTitulo;
    private JTextField txtActualizarAutor;
    private JTextField txtActualizarDescripcion;
    private JTextField txtActualizarImagenPath;
    private String imagenBase64Actualizar = "";
    private String tituloOriginalActualizar = "";

    // Panel Eliminar APK
    private JTextField txtEliminarTitulo;
    private JTextArea textoEliminarAPK;
    
    // Panel Descargar
    private JTextField txtDescargarTitulo;
    private JTextArea textoDescargar;

    // Panel Hash
    private JTextField txtHashTitulo;
    private JTextField txtHashArchivoLocal;
    private JComboBox<String> comboAlgoritmo;
    private JTextArea textoHash;

    public ClienteGUI(Servicio servicio) {
        this.servicio = servicio;
        initialize();
    }

    public void mostrarVentana() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }

    private void initialize() {
        frame = new JFrame("Cliente APK - Todas las funcionalidades");
        frame.setBounds(100, 100, 1000, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();
        
        // ==================== PESTAÑA 1: Usuarios ====================
        tabbedPane.addTab("Usuarios", crearPanelUsuarios());

        // ==================== PESTAÑA 2: Buscar y Listar APKs ====================
        tabbedPane.addTab("Buscar APKs", crearPanelBuscarAPKs());

        // ==================== PESTAÑA 3: Crear APK ====================
        tabbedPane.addTab("Crear APK", crearPanelCrearAPK());

        // ==================== PESTAÑA 4: Actualizar APK ====================
        tabbedPane.addTab("Actualizar APK", crearPanelActualizarAPK());

        // ==================== PESTAÑA 5: Eliminar APK ====================
        tabbedPane.addTab("Eliminar APK", crearPanelEliminarAPK());
        
        // ==================== PESTAÑA 6: Descargar ====================
        tabbedPane.addTab("Descargar APK", crearPanelDescargar());

        // ==================== PESTAÑA 7: Hash ====================
        tabbedPane.addTab("Calcular Hash", crearPanelHash());

        frame.getContentPane().add(tabbedPane);
    }
    
    // ==================== PANEL 1: Usuarios ====================
    private JPanel crearPanelUsuarios() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel formPanel = new JPanel();
        txtNombre = new JTextField(15);
        txtPassword = new JPasswordField(15);
        JButton btnLogin = new JButton("Login");
        JButton btnRegistro = new JButton("Registro");

        formPanel.add(new JLabel("Nombre:"));
        formPanel.add(txtNombre);
        formPanel.add(new JLabel("Contraseña:"));
        formPanel.add(txtPassword);
        formPanel.add(btnLogin);
        formPanel.add(btnRegistro);

        textoUsuarios = new JTextArea(5, 30);
        textoUsuarios.setEditable(false);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(textoUsuarios), BorderLayout.CENTER);

        btnLogin.addActionListener(e -> loginUsuario());
        btnRegistro.addActionListener(e -> registrarUsuario());

        return panel;
    }

    // ==================== PANEL 2: Buscar y Listar APKs ====================
    private JPanel crearPanelBuscarAPKs() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel topPanel = new JPanel();
        txtTitulo = new JTextField(20);
        JButton btnBuscar = new JButton("Buscar APK");
        JButton btnListar = new JButton("Listar todas las APKs");
        
        topPanel.add(new JLabel("Título:"));
        topPanel.add(txtTitulo);
        topPanel.add(btnBuscar);
        topPanel.add(btnListar);

        panelTarjetas = new JPanel();
        panelTarjetas.setLayout(new BoxLayout(panelTarjetas, BoxLayout.Y_AXIS));
        
        JScrollPane scrollTarjetas = new JScrollPane(panelTarjetas);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollTarjetas, BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> buscarAPK());
        btnListar.addActionListener(e -> listarTodasAPKs());

        return panel;
    }

    // ==================== CREAR TARJETA DE APK ====================
    private JPanel crearTarjetaAPK(APK apk) {
        JPanel tarjeta = new JPanel(new BorderLayout(10, 10));
        tarjeta.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            apk.getTitulo() != null ? apk.getTitulo() : "Sin título",
            TitledBorder.LEFT,
            TitledBorder.TOP
        ));
        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        // Imagen
        JLabel lblImagenTarjeta = new JLabel();
        lblImagenTarjeta.setPreferredSize(new Dimension(100, 100));
        lblImagenTarjeta.setHorizontalAlignment(JLabel.CENTER);
        lblImagenTarjeta.setBorder(BorderFactory.createEtchedBorder());
        
        if (apk.getImage() != null && apk.getImage().startsWith("data:image")) {
            try {
                String base64 = apk.getImage().split(",")[1];
                byte[] bytes = Base64.getDecoder().decode(base64);
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
                Image scaled = img.getScaledInstance(90, 90, Image.SCALE_SMOOTH);
                lblImagenTarjeta.setIcon(new ImageIcon(scaled));
            } catch (Exception e) {
                lblImagenTarjeta.setText("[Imagen]");
            }
        } else {
            lblImagenTarjeta.setText("[Sin imagen]");
        }
        
        // Información
        JPanel panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        
        JLabel lblAutor = new JLabel("Autor: " + (apk.getAutor() != null && !apk.getAutor().isEmpty() ? apk.getAutor() : "Desconocido"));
        
        JTextArea txtDescripcion = new JTextArea(3, 40);
        txtDescripcion.setText(apk.getDescripcion() != null ? apk.getDescripcion() : "Sin descripción");
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setEditable(false);
        txtDescripcion.setOpaque(false);
        
        panelInfo.add(lblAutor);
        panelInfo.add(Box.createVerticalStrut(5));
        panelInfo.add(txtDescripcion);

        tarjeta.add(lblImagenTarjeta, BorderLayout.WEST);
        tarjeta.add(panelInfo, BorderLayout.CENTER);

        return tarjeta;
    }

    // ==================== PANEL 3: Crear APK ====================
    private JPanel crearPanelCrearAPK() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        txtNuevoTitulo = new JTextField(20);
        txtNuevoAutor = new JTextField(20);
        txtNuevaDescripcion = new JTextField(20);
        txtNuevaImagenPath = new JTextField(20);
        txtNuevaImagenPath.setEditable(false);
        txtNuevaImagenPath.setFocusable(false);
        
        JButton btnSeleccionarImagen = new JButton("Seleccionar imagen");
        JButton btnCrear = new JButton("Crear APK");
        
        formPanel.add(new JLabel("Título:"));
        formPanel.add(txtNuevoTitulo);
        formPanel.add(new JLabel("Autor:"));
        formPanel.add(txtNuevoAutor);
        formPanel.add(new JLabel("Descripción:"));
        formPanel.add(txtNuevaDescripcion);
        formPanel.add(new JLabel("Imagen:"));
        formPanel.add(txtNuevaImagenPath);
        formPanel.add(btnSeleccionarImagen);
        formPanel.add(btnCrear);

        JPanel panelResultado = new JPanel();
        panelResultado.setLayout(new BoxLayout(panelResultado, BoxLayout.Y_AXIS));
        JScrollPane scrollResultado = new JScrollPane(panelResultado);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(scrollResultado, BorderLayout.CENTER);

        btnSeleccionarImagen.addActionListener(e -> {
            String base64 = seleccionarImagen("Seleccionar imagen para crear");
            if (base64 != null) {
                imagenBase64Nueva = base64;
                txtNuevaImagenPath.setText("Imagen PNG seleccionada");
            }
        });
        btnCrear.addActionListener(e -> crearAPK(panelResultado));

        return panel;
    }

    // ==================== PANEL 4: Actualizar APK ====================
    private JPanel crearPanelActualizarAPK() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField txtBuscarAPK = new JTextField(20);
        txtActualizarTitulo = new JTextField(20);
        txtActualizarAutor = new JTextField(20);
        txtActualizarDescripcion = new JTextField(20);
        txtActualizarImagenPath = new JTextField(20);
        txtActualizarImagenPath.setEditable(false);
        txtActualizarImagenPath.setFocusable(false);
        
        JButton btnCargar = new JButton("Cargar APK");
        JButton btnSeleccionarImagen = new JButton("Cambiar imagen");
        JButton btnActualizar = new JButton("Actualizar APK");
        
        formPanel.add(new JLabel("Buscar APK (título actual):"));
        formPanel.add(txtBuscarAPK);
        formPanel.add(new JLabel(""));
        formPanel.add(btnCargar);
        formPanel.add(new JLabel("Nuevo Título:"));
        formPanel.add(txtActualizarTitulo);
        formPanel.add(new JLabel("Nuevo Autor:"));
        formPanel.add(txtActualizarAutor);
        formPanel.add(new JLabel("Nueva Descripción:"));
        formPanel.add(txtActualizarDescripcion);
        formPanel.add(new JLabel("Nueva Imagen:"));
        formPanel.add(txtActualizarImagenPath);
        formPanel.add(btnSeleccionarImagen);
        formPanel.add(btnActualizar);

        JPanel panelResultado = new JPanel();
        panelResultado.setLayout(new BoxLayout(panelResultado, BoxLayout.Y_AXIS));
        JScrollPane scrollResultado = new JScrollPane(panelResultado);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(scrollResultado, BorderLayout.CENTER);

        btnCargar.addActionListener(e -> cargarAPKParaActualizar(txtBuscarAPK.getText().trim(), panelResultado));
        btnSeleccionarImagen.addActionListener(e -> {
            String base64 = seleccionarImagen("Seleccionar imagen para actualizar");
            if (base64 != null) {
                imagenBase64Actualizar = base64;
                txtActualizarImagenPath.setText("Imagen PNG seleccionada");
            }
        });
        btnActualizar.addActionListener(e -> actualizarAPK(panelResultado));

        return panel;
    }

    // ==================== PANEL 5: Eliminar APK ====================
    private JPanel crearPanelEliminarAPK() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel formPanel = new JPanel();
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        txtEliminarTitulo = new JTextField(20);
        JButton btnEliminar = new JButton("Eliminar APK");
        
        formPanel.add(new JLabel("Título de la APK:"));
        formPanel.add(txtEliminarTitulo);
        formPanel.add(btnEliminar);

        textoEliminarAPK = new JTextArea();
        textoEliminarAPK.setEditable(false);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(textoEliminarAPK), BorderLayout.CENTER);

        btnEliminar.addActionListener(e -> eliminarAPK());

        return panel;
    }
    
    // ==================== PANEL 6: Descargar ====================
    private JPanel crearPanelDescargar() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel formPanel = new JPanel();
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        txtDescargarTitulo = new JTextField(20);
        JButton btnDescargar = new JButton("Descargar APK");
        
        formPanel.add(new JLabel("Título de la APK:"));
        formPanel.add(txtDescargarTitulo);
        formPanel.add(btnDescargar);

        textoDescargar = new JTextArea();
        textoDescargar.setEditable(false);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(textoDescargar), BorderLayout.CENTER);

        btnDescargar.addActionListener(e -> descargarAPK());

        return panel;
    }

    // ==================== PANEL 7: Hash ====================
    private JPanel crearPanelHash() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        txtHashTitulo = new JTextField(20);
        txtHashArchivoLocal = new JTextField(20);
        txtHashArchivoLocal.setEditable(false);
        txtHashArchivoLocal.setFocusable(false);
        comboAlgoritmo = new JComboBox<>(new String[]{"MD5", "SHA-1", "SHA-256"});
        
        JButton btnCalcularHashServidor = new JButton("Hash del servidor");
        JButton btnSeleccionarArchivo = new JButton("Seleccionar archivo local");
        JButton btnCalcularHashLocal = new JButton("Hash del archivo local");
        JButton btnComparar = new JButton("Comparar ambos");
        
        formPanel.add(new JLabel("Título APK (servidor):"));
        formPanel.add(txtHashTitulo);
        formPanel.add(new JLabel("Archivo local:"));
        formPanel.add(txtHashArchivoLocal);
        formPanel.add(new JLabel("Algoritmo:"));
        formPanel.add(comboAlgoritmo);
        formPanel.add(btnCalcularHashServidor);
        formPanel.add(btnSeleccionarArchivo);
        formPanel.add(btnCalcularHashLocal);
        formPanel.add(btnComparar);

        textoHash = new JTextArea();
        textoHash.setEditable(false);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(textoHash), BorderLayout.CENTER);

        btnCalcularHashServidor.addActionListener(e -> calcularHashServidor());
        btnSeleccionarArchivo.addActionListener(e -> seleccionarArchivoHash());
        btnCalcularHashLocal.addActionListener(e -> calcularHashLocal());
        btnComparar.addActionListener(e -> compararHashes());

        return panel;
    }

    // ==================== MÉTODO PARA IMÁGENES ====================
    
    private String seleccionarImagen(String title) {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Imágenes PNG (*.png)", "png");
        fileChooser.setFileFilter(filter);
        fileChooser.setDialogTitle(title);
        
        int result = fileChooser.showOpenDialog(frame);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            String extension = getFileExtension(selectedFile.getName()).toLowerCase();
            
            if (!extension.equals("png")) {
                JOptionPane.showMessageDialog(frame, "Solo se permiten archivos PNG. Formato recibido: ." + extension, "Formato inválido", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            
            try {
                byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
                return "data:image/png;base64," + Base64.getEncoder().encodeToString(fileContent);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, 
                    "Error al cargar la imagen: " + ex.getMessage());
                return null;
            }
        }
        
        return null;
    }
    
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return (lastDot == -1) ? "" : filename.substring(lastDot + 1);
    }

    // ==================== MÉTODOS PARA HASH ====================
    
    private void seleccionarArchivoHash() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos APK", "apk");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(frame);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            txtHashArchivoLocal.setText(selectedFile.getAbsolutePath());
            textoHash.setText("Archivo seleccionado: " + selectedFile.getName());
        }
    }
    
    private void calcularHashLocal() {
        String rutaArchivo = txtHashArchivoLocal.getText().trim();
        
        if (rutaArchivo.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Selecciona un archivo local primero");
            return;
        }
        
        String algoritmo = (String) comboAlgoritmo.getSelectedItem();
        textoHash.setText("Calculando hash " + algoritmo + " del archivo local...");
        
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                File archivo = new File(rutaArchivo);
                MessageDigest digest = MessageDigest.getInstance(algoritmo);
                
                try (FileInputStream fis = new FileInputStream(archivo)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        digest.update(buffer, 0, bytesRead);
                    }
                }
                
                byte[] hashBytes = digest.digest();
                StringBuilder hexString = new StringBuilder();
                for (byte b : hashBytes) {
                    hexString.append(String.format("%02X", b));
                }
                
                return hexString.toString();
            }
            
            @Override
            protected void done() {
                try {
                    String hash = get();
                    textoHash.setText("Hash " + algoritmo + " (archivo local):\n\n" + hash);
                } catch (Exception e) {
                    textoHash.setText("Error calculando hash local: " + e.getMessage());
                }
            }
        }.execute();
    }
    
    private void compararHashes() {
        String titulo = txtHashTitulo.getText().trim();
        String rutaArchivo = txtHashArchivoLocal.getText().trim();
        
        if (titulo.isEmpty() || rutaArchivo.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Introduce el título y selecciona el archivo local");
            return;
        }
        
        String algoritmo = (String) comboAlgoritmo.getSelectedItem();
        textoHash.setText("Comparando hashes...");
        
        new SwingWorker<String, Void>() {
            private String hashServidor;
            private String hashLocal;
            
            @Override
            protected String doInBackground() throws Exception {
                Mono<String> mono = servicio.getHash(titulo, algoritmo);
                hashServidor = mono.block();
                
                File archivo = new File(rutaArchivo);
                MessageDigest digest = MessageDigest.getInstance(algoritmo);
                
                try (FileInputStream fis = new FileInputStream(archivo)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        digest.update(buffer, 0, bytesRead);
                    }
                }
                
                byte[] hashBytes = digest.digest();
                StringBuilder hexString = new StringBuilder();
                for (byte b : hashBytes) {
                    hexString.append(String.format("%02X", b));
                }
                hashLocal = hexString.toString();
                
                return hashServidor.equals(hashLocal) ? "IGUALES" : "DIFERENTES";
            }
            
            @Override
            protected void done() {
                try {
                    String resultado = get();
                    StringBuilder sb = new StringBuilder();
                    sb.append("=== COMPARACIÓN DE HASHES (" + algoritmo + ") ===\n\n");
                    sb.append("Hash del servidor:\n" + hashServidor + "\n\n");
                    sb.append("Hash del archivo local:\n" + hashLocal + "\n\n");
                    sb.append("Resultado: " + resultado + "\n\n");
                    
                    if (resultado.equals("IGUALES")) {
                        sb.append("Los archivos son idénticos. La descarga fue exitosa.");
                    } else {
                        sb.append("Los archivos son diferentes. Puede haber un error en la descarga.");
                    }
                    
                    textoHash.setText(sb.toString());
                } catch (Exception e) {
                    mostrarError(e, textoHash);
                }
            }
        }.execute();
    }

    // ==================== MÉTODOS DE ACCIÓN ====================
    
    private void loginUsuario() {
        String nombre = txtNombre.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (nombre.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Introduce nombre y contraseña");
            return;
        }

        textoUsuarios.setText("Conectando...");

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                Mono<String> mono = servicio.login(new Usuario(nombre, pass));
                return mono.block();
            }

            @Override
            protected void done() {
                try {
                    String nombreUsuario = get();
                    textoUsuarios.setText("Login correcto: " + nombreUsuario);
                } catch (Exception e) {
                    mostrarError(e, textoUsuarios);
                }
            }
        }.execute();
    }

    private void registrarUsuario() {
        String nombre = txtNombre.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (nombre.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Introduce nombre y contraseña");
            return;
        }

        textoUsuarios.setText("Registrando usuario...");

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                Mono<String> mono = servicio.signUp(new Usuario(nombre, pass));
                return mono.block();
            }

            @Override
            protected void done() {
                try {
                    String mensaje = get();
                    textoUsuarios.setText(mensaje);
                } catch (Exception e) {
                    mostrarError(e, textoUsuarios);
                }
            }
        }.execute();
    }

    private void buscarAPK() {
        String titulo = txtTitulo.getText().trim();
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Introduce un título de APK");
            return;
        }

        panelTarjetas.removeAll();
        JLabel lblCargando = new JLabel("Buscando APK...", JLabel.CENTER);
        panelTarjetas.add(lblCargando);
        panelTarjetas.revalidate();
        panelTarjetas.repaint();

        new SwingWorker<APK, Void>() {
            @Override
            protected APK doInBackground() {
                Mono<APK> mono = servicio.getAPK(titulo);
                return mono.block();
            }

            @Override
            protected void done() {
                try {
                    APK apk = get();
                    mostrarTarjetas(apk != null ? List.of(apk) : List.of());
                } catch (Exception e) {
                    panelTarjetas.removeAll();
                    JTextArea error = new JTextArea(obtenerMensajeError(e));
                    error.setEditable(false);
                    panelTarjetas.add(error);
                    panelTarjetas.revalidate();
                    panelTarjetas.repaint();
                }
            }
        }.execute();
    }

    private void listarTodasAPKs() {
        panelTarjetas.removeAll();
        JLabel lblCargando = new JLabel("Cargando todas las APKs...", JLabel.CENTER);
        panelTarjetas.add(lblCargando);
        panelTarjetas.revalidate();
        panelTarjetas.repaint();

        new SwingWorker<List<APK>, Void>() {
            @Override
            protected List<APK> doInBackground() {
                Flux<APK> flux = servicio.getAPKs();
                return flux.collectList().block();
            }

            @Override
            protected void done() {
                try {
                    List<APK> lista = get();
                    mostrarTarjetas(lista);
                } catch (Exception e) {
                    panelTarjetas.removeAll();
                    JTextArea error = new JTextArea(obtenerMensajeError(e));
                    error.setEditable(false);
                    panelTarjetas.add(error);
                    panelTarjetas.revalidate();
                    panelTarjetas.repaint();
                }
            }
        }.execute();
    }

    private void mostrarTarjetas(List<APK> listaAPKs) {
        panelTarjetas.removeAll();
        
        if (listaAPKs == null || listaAPKs.isEmpty()) {
            JLabel lblVacio = new JLabel("No se encontraron APKs", JLabel.CENTER);
            panelTarjetas.add(lblVacio);
        } else {
            for (APK apk : listaAPKs) {
                panelTarjetas.add(crearTarjetaAPK(apk));
                panelTarjetas.add(Box.createVerticalStrut(10));
            }
        }
        
        panelTarjetas.revalidate();
        panelTarjetas.repaint();
    }

    private void crearAPK(JPanel panelResultado) {
        String titulo = txtNuevoTitulo.getText().trim();
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "El título es obligatorio");
            return;
        }

        panelResultado.removeAll();
        JLabel lblCargando = new JLabel("Creando APK...", JLabel.CENTER);
        panelResultado.add(lblCargando);
        panelResultado.revalidate();
        panelResultado.repaint();

        new SwingWorker<APK, Void>() {
            @Override
            protected APK doInBackground() {
                APK apk = new APK();
                apk.setTitulo(titulo);
                apk.setAutor(txtNuevoAutor.getText().trim());
                apk.setDescripcion(txtNuevaDescripcion.getText().trim());
                apk.setImage(imagenBase64Nueva);

                Mono<APK> mono = servicio.addAPK(apk);
                return mono.block();
            }

            @Override
            protected void done() {
                try {
                    APK apk = get();
                    panelResultado.removeAll();
                    
                    JLabel lblExito = new JLabel("APK creada correctamente:", JLabel.CENTER);
                    lblExito.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    panelResultado.add(lblExito);
                    panelResultado.add(crearTarjetaAPK(apk));
                    
                    panelResultado.revalidate();
                    panelResultado.repaint();
                    limpiarCamposCrear();
                } catch (Exception e) {
                    panelResultado.removeAll();
                    JTextArea error = new JTextArea(obtenerMensajeError(e));
                    error.setEditable(false);
                    panelResultado.add(error);
                    panelResultado.revalidate();
                    panelResultado.repaint();
                }
            }
        }.execute();
    }

    private void cargarAPKParaActualizar(String titulo, JPanel panelResultado) {
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Introduce el título de la APK a actualizar");
            return;
        }

        panelResultado.removeAll();
        JLabel lblCargando = new JLabel("Cargando APK...", JLabel.CENTER);
        panelResultado.add(lblCargando);
        panelResultado.revalidate();
        panelResultado.repaint();

        new SwingWorker<APK, Void>() {
            @Override
            protected APK doInBackground() {
                Mono<APK> mono = servicio.getAPK(titulo);
                return mono.block();
            }

            @Override
            protected void done() {
                try {
                    APK apk = get();
                    if (apk != null) {
                        tituloOriginalActualizar = apk.getTitulo();
                        
                        txtActualizarTitulo.setText(apk.getTitulo() != null ? apk.getTitulo() : "");
                        txtActualizarAutor.setText(apk.getAutor() != null ? apk.getAutor() : "");
                        txtActualizarDescripcion.setText(apk.getDescripcion() != null ? apk.getDescripcion() : "");
                        imagenBase64Actualizar = apk.getImage() != null ? apk.getImage() : "";
                        txtActualizarImagenPath.setText(imagenBase64Actualizar.isEmpty() ? "" : "Imagen actual");
                        
                        panelResultado.removeAll();
                        JLabel lblInfo = new JLabel("APK cargada. Modifica los campos y pulsa 'Actualizar APK':", JLabel.CENTER);
                        lblInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                        panelResultado.add(lblInfo);
                        panelResultado.add(crearTarjetaAPK(apk));
                        panelResultado.revalidate();
                        panelResultado.repaint();
                    }
                } catch (Exception e) {
                    panelResultado.removeAll();
                    JTextArea error = new JTextArea(obtenerMensajeError(e));
                    error.setEditable(false);
                    panelResultado.add(error);
                    panelResultado.revalidate();
                    panelResultado.repaint();
                }
            }
        }.execute();
    }

    private void actualizarAPK(JPanel panelResultado) {
        if (tituloOriginalActualizar.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Primero debes cargar una APK existente");
            return;
        }

        String nuevoTitulo = txtActualizarTitulo.getText().trim();
        if (nuevoTitulo.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "El título no puede estar vacío");
            return;
        }

        panelResultado.removeAll();
        JLabel lblCargando = new JLabel("Actualizando APK...", JLabel.CENTER);
        panelResultado.add(lblCargando);
        panelResultado.revalidate();
        panelResultado.repaint();

        new SwingWorker<APK, Void>() {
            @Override
            protected APK doInBackground() {
                APK apk = new APK();
                apk.setTitulo(nuevoTitulo);
                apk.setAutor(txtActualizarAutor.getText().trim());
                apk.setDescripcion(txtActualizarDescripcion.getText().trim());
                apk.setImage(imagenBase64Actualizar);

                Mono<APK> mono = servicio.updateAPK(tituloOriginalActualizar, apk);
                return mono.block();
            }

            @Override
            protected void done() {
                try {
                    APK apk = get();
                    panelResultado.removeAll();
                    
                    JLabel lblExito = new JLabel("APK actualizada correctamente:", JLabel.CENTER);
                    lblExito.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    panelResultado.add(lblExito);
                    panelResultado.add(crearTarjetaAPK(apk));
                    
                    panelResultado.revalidate();
                    panelResultado.repaint();
                    limpiarCamposActualizar();
                } catch (Exception e) {
                    panelResultado.removeAll();
                    JTextArea error = new JTextArea(obtenerMensajeError(e));
                    error.setEditable(false);
                    panelResultado.add(error);
                    panelResultado.revalidate();
                    panelResultado.repaint();
                }
            }
        }.execute();
    }

    private void eliminarAPK() {
        String titulo = txtEliminarTitulo.getText().trim();
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Introduce el título de la APK a eliminar");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
            frame,
            "¿Estás seguro de que quieres eliminar la APK '" + titulo + "'?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        textoEliminarAPK.setText("Eliminando APK...");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                Mono<Void> mono = servicio.deleteAPK(titulo);
                mono.block();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    textoEliminarAPK.setText("APK '" + titulo + "' eliminada correctamente");
                    txtEliminarTitulo.setText("");
                } catch (Exception e) {
                    mostrarError(e, textoEliminarAPK);
                }
            }
        }.execute();
    }
    
    private void descargarAPK() {
        String titulo = txtDescargarTitulo.getText().trim();
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Introduce el título de la APK");
            return;
        }

        textoDescargar.setText("Descargando APK...");

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                Mono<Resource> mono = servicio.downloadAPK(titulo);
                Resource resource = mono.block();

                if (resource != null) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setSelectedFile(new File(titulo + ".apk"));
                    int result = fileChooser.showSaveDialog(frame);

                    if (result == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        try (FileOutputStream fos = new FileOutputStream(selectedFile)) {
                            fos.write(resource.getInputStream().readAllBytes());
                        }
                        return true;
                    }
                }
                return false;
            }

            @Override
            protected void done() {
                try {
                    Boolean success = get();
                    if (success) {
                        textoDescargar.setText("APK descargada correctamente");
                    } else {
                        textoDescargar.setText("Descarga cancelada");
                    }
                } catch (Exception e) {
                    mostrarError(e, textoDescargar);
                }
            }
        }.execute();
    }

    private void calcularHashServidor() {
        String titulo = txtHashTitulo.getText().trim();
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Introduce el título de la APK");
            return;
        }

        String algoritmo = (String) comboAlgoritmo.getSelectedItem();
        textoHash.setText("Calculando hash " + algoritmo + " del servidor...");

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                Mono<String> mono = servicio.getHash(titulo, algoritmo);
                return mono.block();
            }

            @Override
            protected void done() {
                try {
                    String hash = get();
                    textoHash.setText("Hash " + algoritmo + " de '" + titulo + "' (servidor):\n\n" + hash);
                } catch (Exception e) {
                    mostrarError(e, textoHash);
                }
            }
        }.execute();
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private void mostrarError(Exception e, JTextArea area) {
        area.setText(obtenerMensajeError(e));
    }

    private String obtenerMensajeError(Exception e) {
        if (e.getCause() instanceof WebClientResponseException ex) {
            return "Error en la petición: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString();
        } else if (e.getCause() instanceof WebClientRequestException) {
            return "No se pudo conectar con la API.";
        } else {
            return "Error inesperado: " + e.getMessage();
        }
    }

    private void limpiarCamposCrear() {
        txtNuevoTitulo.setText("");
        txtNuevoAutor.setText("");
        txtNuevaDescripcion.setText("");
        txtNuevaImagenPath.setText("");
        imagenBase64Nueva = "";
    }

    private void limpiarCamposActualizar() {
        txtActualizarTitulo.setText("");
        txtActualizarAutor.setText("");
        txtActualizarDescripcion.setText("");
        txtActualizarImagenPath.setText("");
        imagenBase64Actualizar = "";
        tituloOriginalActualizar = "";
    }
}