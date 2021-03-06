package src.wsa.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import src.wsa.web.SiteCrawler;
import java.net.URI;
import java.net.URISyntaxException;

/** Sezione relativa all'inserimento di seed*/
public class Popup  {
    private static SiteCrawler siteCrawler = Main.getSiteCrawler();

    /** Crea e mostra la sezione per aggiungere i seed
     * @param sp lo SplitPane corrente*/
    public static void showSeeds(SplitPane sp){
        Text addSeedLbl = new Text("Aggiungi seed");
        TextField addSeedTxt = WindowsManager.createTextField(31,"es: www.google.it/doodles",true);
        Button addSeedBtn = WindowsManager.createButton("Aggiungi", 0, 0, null, false);

        HBox hb = WindowsManager.createHBox(10, 5, Pos.CENTER, addSeedLbl, addSeedTxt, addSeedBtn);

        SplitPane split = (SplitPane) sp.getItems().get(0);
        StackPane st = (StackPane) split.getItems().get(1);
        st.setMaxHeight(40);
        st.setMinHeight(40);
        st.setPrefHeight(40);
        st.getChildren().add(hb);

        addSeedBtn.setOnAction( e -> {
            String uriAggiunto = "";
            if(addSeedTxt.getText() != null)
                uriAggiunto = addSeedTxt.getText();

            URI uri = null;
            try {
                uri = new URI(uriAggiunto);
            } catch (URISyntaxException e1) {
                Alert alert=WindowsManager.creaAlert(Alert.AlertType.ERROR,"Errore","URI non valido");
                alert.showAndWait();
            }

            if(siteCrawler!= null){
                try {
                    siteCrawler.addSeed(uri);
                }catch (IllegalArgumentException el){
                    Alert alert = WindowsManager.creaAlert(Alert.AlertType.ERROR,"Errore","Il seed non e' nel dominio");
                    alert.showAndWait();
                }
            }
            addSeedTxt.clear();
        });
    }
}