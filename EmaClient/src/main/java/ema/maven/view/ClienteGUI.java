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
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import javax.imageio.ImageIO;
import java.util.List;

@Component
public class ClienteGUI {

    private JFrame frame;
    private final Servicio servicio;

    // Panel APKs - Buscar y Listar
    private JTextField txtTitulo;
    private JTextArea textoAPK;
    private JLabel lblImagen;

    // Panel Usuarios
    private JTextField txtNombre;
    private JPasswordField txtPassword;
    private JTextArea textoUsuarios;

    // Panel Crear APK
    private JTextField txtNuevoTitulo;
    private JTextField txtNuevaDescripcion;
    private JTextField txtNuevaVersion;
    private JTextField txtNuevoSize;
    private JTextField txtNuevoCompany;
    private JTextField txtNuevaImage;
    private JTextArea textoCrearAPK;

    // Panel Actualizar APK
    private JTextField txtActualizarTitulo;
    private JTextField txtActualizarDescripcion;
    private JTextField txtActualizarVersion;
    private JTextField txtActualizarSize;
    private JTextField txtActualizarCompany;
    private JTextField txtActualizarImage;
    private JTextArea textoActualizarAPK;

    // Panel Eliminar APK
    private JTextField txtEliminarTitulo;
    private JTextArea textoEliminarAPK;

    // Panel Hash
    private JTextField txtHashTitulo;
    private JComboBox<String> comboAlgoritmo;
    private JTextArea textoHash;

    // Panel Descargar
    private JTextField txtDescargarTitulo;
    private JTextArea textoDescargar;

    public ClienteGUI(Servicio servicio) {
        this.servicio = servicio;
        initialize();
    }

    public void mostrarVentana() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }

    private void initialize() {
        frame = new JFrame("Cliente APK - Todas las funcionalidades");
        frame.setBounds(100, 100, 900, 600);
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

        textoAPK = new JTextArea();
        textoAPK.setEditable(false);
        JScrollPane scrollAPK = new JScrollPane(textoAPK);

        lblImagen = new JLabel();
        lblImagen.setHorizontalAlignment(JLabel.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollAPK, BorderLayout.CENTER);
        panel.add(lblImagen, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> buscarAPK());
        btnListar.addActionListener(e -> listarTodasAPKs());

        return panel;
    }

    // ==================== PANEL 3: Crear APK ====================
    private JPanel crearPanelCrearAPK() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        txtNuevoTitulo = new JTextField(20);
        txtNuevaDescripcion = new JTextField(20);
        txtNuevaVersion = new JTextField(20);
        txtNuevoSize = new JTextField(20);
        txtNuevoCompany = new JTextField(20);
        txtNuevaImage = new JTextField(20);
        
        formPanel.add(new JLabel("Título:"));
        formPanel.add(txtNuevoTitulo);
        formPanel.add(new JLabel("Descripción:"));
        formPanel.add(txtNuevaDescripcion);
        //formPanel.add(new JLabel("Imagen (Base64):"));
        //formPanel.add(txtNuevaImage);
        
        JButton btnCrear = new JButton("Crear APK");
        formPanel.add(new JLabel(""));
        formPanel.add(btnCrear);

        textoCrearAPK = new JTextArea();
        textoCrearAPK.setEditable(false);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(textoCrearAPK), BorderLayout.CENTER);

        btnCrear.addActionListener(e -> crearAPK());

        return panel;
    }

    // ==================== PANEL 4: Actualizar APK ====================
    private JPanel crearPanelActualizarAPK() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        txtActualizarTitulo = new JTextField(20);
        txtActualizarDescripcion = new JTextField(20);
        txtActualizarVersion = new JTextField(20);
        txtActualizarSize = new JTextField(20);
        txtActualizarCompany = new JTextField(20);
        txtActualizarImage = new JTextField(20);
        
        formPanel.add(new JLabel("Título actual (buscar):"));
        formPanel.add(txtActualizarTitulo);
        formPanel.add(new JLabel("Nueva Descripción:"));
        formPanel.add(txtActualizarDescripcion);
        //formPanel.add(new JLabel("Nueva Imagen (Base64):"));
        //formPanel.add(txtActualizarImage);
        
        JButton btnActualizar = new JButton("Actualizar APK");
        JButton btnCargar = new JButton("Cargar APK existente");
        formPanel.add(btnCargar);
        formPanel.add(btnActualizar);

        textoActualizarAPK = new JTextArea();
        textoActualizarAPK.setEditable(false);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(textoActualizarAPK), BorderLayout.CENTER);

        btnCargar.addActionListener(e -> cargarAPKParaActualizar());
        btnActualizar.addActionListener(e -> actualizarAPK());

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
        
        JPanel formPanel = new JPanel();
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        txtHashTitulo = new JTextField(20);
        comboAlgoritmo = new JComboBox<>(new String[]{"MD5", "SHA-1", "SHA-256"});
        JButton btnCalcularHash = new JButton("Calcular Hash");
        
        formPanel.add(new JLabel("Título de la APK:"));
        formPanel.add(txtHashTitulo);
        formPanel.add(new JLabel("Algoritmo:"));
        formPanel.add(comboAlgoritmo);
        formPanel.add(btnCalcularHash);

        textoHash = new JTextArea();
        textoHash.setEditable(false);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(textoHash), BorderLayout.CENTER);

        btnCalcularHash.addActionListener(e -> calcularHash());

        return panel;
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

        textoAPK.setText("Buscando APK...");
        lblImagen.setIcon(null);

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
                    mostrarAPK(apk);
                } catch (Exception e) {
                    mostrarError(e, textoAPK);
                }
            }
        }.execute();
    }

    private void listarTodasAPKs() {
        textoAPK.setText("Cargando todas las APKs...");
        lblImagen.setIcon(null);

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
                    StringBuilder sb = new StringBuilder();
                    for (APK apk : lista) {
                        sb.append(apk.toString()).append("\n\n");
                    }
                    textoAPK.setText(sb.toString());
                } catch (Exception e) {
                    mostrarError(e, textoAPK);
                }
            }
        }.execute();
    }

    private void crearAPK() {
        String titulo = txtNuevoTitulo.getText().trim();
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "El título es obligatorio");
            return;
        }

        textoCrearAPK.setText("Creando APK...");

        new SwingWorker<APK, Void>() {
            @Override
            protected APK doInBackground() {
                APK apk = new APK();
                apk.setTitulo(titulo);
                apk.setDescripcion(txtNuevaDescripcion.getText().trim());
                apk.setImage(txtNuevaImage.getText().trim());

                Mono<APK> mono = servicio.addAPK(apk);
                return mono.block();
            }

            @Override
            protected void done() {
                try {
                    APK apk = get();
                    textoCrearAPK.setText("APK creada correctamente:\n" + apk.toString());
                    limpiarCamposCrear();
                } catch (Exception e) {
                    mostrarError(e, textoCrearAPK);
                }
            }
        }.execute();
    }

    private void cargarAPKParaActualizar() {
        String titulo = txtActualizarTitulo.getText().trim();
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Introduce el título de la APK a actualizar");
            return;
        }

        textoActualizarAPK.setText("Cargando APK...");

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
                        txtActualizarDescripcion.setText(apk.getDescripcion() != null ? apk.getDescripcion() : "");
                        txtActualizarImage.setText(apk.getImage() != null ? apk.getImage() : "");
                        textoActualizarAPK.setText("APK cargada. Modifica los campos y pulsa 'Actualizar APK'");
                    }
                } catch (Exception e) {
                    mostrarError(e, textoActualizarAPK);
                }
            }
        }.execute();
    }

    private void actualizarAPK() {
        String titulo = txtActualizarTitulo.getText().trim();
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "El título es obligatorio");
            return;
        }

        textoActualizarAPK.setText("Actualizando APK...");

        new SwingWorker<APK, Void>() {
            @Override
            protected APK doInBackground() {
                APK apk = new APK();
                apk.setTitulo(titulo);
                apk.setDescripcion(txtActualizarDescripcion.getText().trim());
                apk.setImage(txtActualizarImage.getText().trim());

                Mono<APK> mono = servicio.updateAPK(titulo, apk);
                return mono.block();
            }

            @Override
            protected void done() {
                try {
                    APK apk = get();
                    textoActualizarAPK.setText("APK actualizada correctamente:\n" + apk.toString());
                    limpiarCamposActualizar();
                } catch (Exception e) {
                    mostrarError(e, textoActualizarAPK);
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

    private void calcularHash() {
        String titulo = txtHashTitulo.getText().trim();
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Introduce el título de la APK");
            return;
        }

        String algoritmo = (String) comboAlgoritmo.getSelectedItem();
        textoHash.setText("Calculando hash " + algoritmo + "...");

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
                    textoHash.setText("Hash " + algoritmo + " de '" + titulo + "':\n\n" + hash);
                } catch (Exception e) {
                    mostrarError(e, textoHash);
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

    // ==================== MÉTODOS AUXILIARES ====================

    private void mostrarAPK(APK apk) throws Exception {
        if (apk != null) {
            textoAPK.setText(apk.toString());

            if (apk.getImage() != null && apk.getImage().startsWith("data:image")) {
                String base64 = apk.getImage().split(",")[1];
                byte[] bytes = java.util.Base64.getDecoder().decode(base64);
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));

                int maxWidth = 400;
                int maxHeight = 200;
                int width = img.getWidth();
                int height = img.getHeight();
                if (width > maxWidth || height > maxHeight) {
                    double scale = Math.min((double) maxWidth / width, (double) maxHeight / height);
                    width = (int) (width * scale);
                    height = (int) (height * scale);
                    Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    lblImagen.setIcon(new ImageIcon(scaled));
                } else {
                    lblImagen.setIcon(new ImageIcon(img));
                }
            } else {
                lblImagen.setIcon(null);
            }
        } else {
            textoAPK.setText("No se encontró la APK");
            lblImagen.setIcon(null);
        }
    }

    private void mostrarError(Exception e, JTextArea area) {
        if (e.getCause() instanceof WebClientResponseException ex) {
            area.setText("Error en la petición: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
        } else if (e.getCause() instanceof WebClientRequestException) {
            area.setText("No se pudo conectar con la API. ¿Está arrancada?");
        } else {
            area.setText("Error inesperado: " + e.getMessage());
        }
    }

    private void limpiarCamposCrear() {
        txtNuevoTitulo.setText("");
        txtNuevaDescripcion.setText("");
        txtNuevaVersion.setText("");
        txtNuevoSize.setText("");
        txtNuevoCompany.setText("");
        txtNuevaImage.setText("");
    }

    private void limpiarCamposActualizar() {
        txtActualizarTitulo.setText("");
        txtActualizarDescripcion.setText("");
        txtActualizarVersion.setText("");
        txtActualizarSize.setText("");
        txtActualizarCompany.setText("");
        txtActualizarImage.setText("");
    }
}