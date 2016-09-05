package org.abimon.mods.danganronpa.launcher.windows;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.abimon.mods.danganronpa.launcher.DanganLauncher;
import org.abimon.omnis.io.Data;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URI;

public class GitHubLogin extends JFrame {
    private JPanel panel1;
    private JButton goToSiteButton;
    private JTextField textField1;
    private JButton closeButton;

    public GitHubLogin() {
        super("GitHubLogin");
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();

        closeButton.addActionListener(e -> {
            DanganLauncher.githubOauth = textField1.getText();
            try {
                Data jsonData = new Data(new File(".spiral_settings"));
                JsonElement element = new JsonParser().parse(jsonData.getAsString());
                JsonObject json;
                if (element.isJsonObject())
                    json = element.getAsJsonObject();
                else
                    json = new JsonObject();
                json.addProperty("github", textField1.getText());
                new Data(json.toString()).write(new File(".spiral_settings"));
            } catch (Throwable th) {
                th.printStackTrace();
            }
            setVisible(false);
        });

        goToSiteButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(URI.create("http://130.211.174.176/github?5cfb6d017d07a965b712"));
            } catch (Throwable th) {
                th.printStackTrace();
            }
        });

        setVisible(true);
    }
}
