package src.wsa.gui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Created by User on 11/01/2016.
 */
public class WebWindow {
    private static Stage stage = new Stage();


    public static void  showWebWindow(Stage primaryStage, URL url){

        stage.setTitle("Browser");
        try{
            stage.initOwner(primaryStage);
            stage.setAlwaysOnTop(false);
        }catch (Exception e){}

        Scene finestra = new Scene(createWebView(url), 1400, 800);

        stage.setScene(finestra);
        stage.show();

    }

    private static Parent  createWebView(URL url){
        WebView wView = new WebView();
        WebEngine we = wView.getEngine();
        we.load("");
        we.load(url.toString());
        wView.setDisable(true);
        TextField urlTxt = new TextField(url.toString());    // Per immettere l'URL delle pagine
        urlTxt.setPrefHeight(38);
        Image image= new Image(WindowsManager.class.getResourceAsStream("/rsz_refresh.png"));
        Button aggiorna = WindowsManager.createButton(null,30,30,image,false);
        HBox hb = new HBox(aggiorna,urlTxt);
        VBox vb = new VBox(hb, wView);
        HBox.setHgrow(urlTxt, Priority.ALWAYS);       // Si estende in orizzontale
        VBox.setVgrow(wView, Priority.ALWAYS);     // Si estende in verticale


        aggiorna.setOnAction(e -> we.reload());
        return vb;

    }
}