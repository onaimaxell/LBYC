package com.dasalla.pos.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneUtil {

    public static void switchScene(Stage stage, String fxmlPath) {
        try {
            double width  = stage.getWidth();
            double height = stage.getHeight();

            FXMLLoader loader = new FXMLLoader(
                SceneUtil.class.getResource(fxmlPath)
            );
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                SceneUtil.class.getResource("/css/styles.css").toExternalForm()
            );

            stage.setScene(scene);
            stage.setWidth(width);
            stage.setHeight(height);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}