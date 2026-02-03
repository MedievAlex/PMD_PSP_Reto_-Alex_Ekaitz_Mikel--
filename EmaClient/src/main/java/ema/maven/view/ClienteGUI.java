package ema.maven.view;

import ema.maven.service.Servicio;
import ema.maven.model.APK;
import ema.maven.model.Usuario;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import java.util.List;

@Component
public class ClienteGUI {

    private JFrame frame;
    private final Servicio servicio;

    private JTextField txtTitulo;
    private JTextArea textoAPK;
    private JLabel lblImagen;

    private JTextField txtNombre;
    private JPasswordField txtPassword;
    private JTextArea textoUsuarios;

    public ClienteGUI(Servicio servicio) {
        this.servicio = servicio;
        initialize();
    }

    public void mostrarVentana() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }

    private void initialize() {
        frame = new JFrame("Cliente APK");
        frame.setBounds(100, 100, 700, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();

        // -------------------- Panel APKs --------------------
        JPanel panelAPK = new JPanel(new BorderLayout());
        JPanel topPanelAPK = new JPanel();
        txtTitulo = new JTextField(20);
        JButton btnBuscar = new JButton("Buscar APK");
        JButton btnListar = new JButton("Listar todas las APKs");
        topPanelAPK.add(new JLabel("Título:"));
        topPanelAPK.add(txtTitulo);
        topPanelAPK.add(btnBuscar);
        topPanelAPK.add(btnListar);

        textoAPK = new JTextArea();
        textoAPK.setEditable(false);
        JScrollPane scrollAPK = new JScrollPane(textoAPK);

        lblImagen = new JLabel();
        lblImagen.setHorizontalAlignment(JLabel.CENTER);

        panelAPK.add(topPanelAPK, BorderLayout.NORTH);
        panelAPK.add(scrollAPK, BorderLayout.CENTER);
        panelAPK.add(lblImagen, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> buscarAPK());
        btnListar.addActionListener(e -> listarTodasAPKs());

        tabbedPane.addTab("APKs", panelAPK);

        // -------------------- Panel Usuarios --------------------
        JPanel panelUsuarios = new JPanel(new BorderLayout());
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

        panelUsuarios.add(formPanel, BorderLayout.NORTH);
        panelUsuarios.add(new JScrollPane(textoUsuarios), BorderLayout.CENTER);

        btnLogin.addActionListener(e -> loginUsuario());
        btnRegistro.addActionListener(e -> registrarUsuario());

        tabbedPane.addTab("Usuarios", panelUsuarios);

        frame.getContentPane().add(tabbedPane);
    }

    // -------------------- Métodos con SwingWorker + Mono/Flux --------------------

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

    // -------------------- Método para mostrar errores --------------------
    private void mostrarError(Exception e, JTextArea area) {
        if (e.getCause() instanceof WebClientResponseException ex) {
            area.setText("Error en la petición: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
        } else if (e.getCause() instanceof WebClientRequestException) {
            area.setText("No se pudo conectar con la API. ¿Está arrancada?");
        } else {
            area.setText("Error inesperado: " + e.getMessage());
        }
    }


    // -------------------- Método común para mostrar APK en la GUI --------------------
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
}
