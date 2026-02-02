package ema.maven.view;

import ema.maven.service.Servicio;
import ema.maven.model.APK;
import ema.maven.model.Usuario;

import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;

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
        topPanelAPK.add(new JLabel("Título:"));
        topPanelAPK.add(txtTitulo);
        topPanelAPK.add(btnBuscar);

        textoAPK = new JTextArea();
        textoAPK.setEditable(false);
        JScrollPane scrollAPK = new JScrollPane(textoAPK);

        lblImagen = new JLabel();
        lblImagen.setHorizontalAlignment(JLabel.CENTER);

        panelAPK.add(topPanelAPK, BorderLayout.NORTH);
        panelAPK.add(scrollAPK, BorderLayout.CENTER);
        panelAPK.add(lblImagen, BorderLayout.SOUTH);

        btnBuscar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buscarAPK();
            }
        });

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

        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loginUsuario();
            }
        });

        btnRegistro.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                registrarUsuario();
            }
        });

        tabbedPane.addTab("Usuarios", panelUsuarios);

        frame.getContentPane().add(tabbedPane);
    }

    // -------------------- Métodos con SwingWorker --------------------
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
                return servicio.getAPK(titulo); // bloquea en hilo de fondo
            }

            @Override
            protected void done() {
                try {
                    APK apk = get();
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
                        textoAPK.setText("No se encontró la APK: " + titulo);
                        lblImagen.setIcon(null);
                    }
                } catch (Exception e) {
                    textoAPK.setText("Error al consultar la API: " + e.getMessage());
                    lblImagen.setIcon(null);
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
        new SwingWorker<Usuario, Void>() {
            @Override
            protected Usuario doInBackground() {
                return servicio.login(new Usuario(nombre, pass));
            }

            @Override
            protected void done() {
                try {
                    Usuario u = get();
                    textoUsuarios.setText(u != null ? "Login correcto: " + u.getNombre() : "Login fallido");
                } catch (Exception e) {
                    textoUsuarios.setText("Error al conectar con la API: " + e.getMessage());
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
        new SwingWorker<Usuario, Void>() {
            @Override
            protected Usuario doInBackground() {
                return servicio.signUp(new Usuario(nombre, pass));
            }

            @Override
            protected void done() {
                try {
                    Usuario u = get();
                    textoUsuarios.setText(u != null ? "Registro correcto: " + u.getNombre() : "Registro fallido");
                } catch (Exception e) {
                    textoUsuarios.setText("Error al conectar con la API: " + e.getMessage());
                }
            }
        }.execute();
    }
}
