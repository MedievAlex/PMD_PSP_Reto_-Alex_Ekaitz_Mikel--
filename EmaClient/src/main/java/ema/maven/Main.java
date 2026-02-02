package ema.maven;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ema.maven.view.ClienteGUI;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
    	SpringApplication app = new SpringApplication(Main.class);
        app.setHeadless(false);
        var context = app.run(args);

        ClienteGUI window = context.getBean(ClienteGUI.class);
        window.mostrarVentana();
    }
}