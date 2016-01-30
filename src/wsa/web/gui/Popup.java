package src.wsa.web.gui;


import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

/**
 * Created by User on 09/01/2016.
 */
public class Popup  {

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

    }

}