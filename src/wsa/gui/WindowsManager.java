package src.wsa.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;


import java.io.*;
import java.net.URI;

/**
 * Created by User on 05/01/2016.
 */
public class WindowsManager {


    /** Crea un VBox con le caratteristiche in input
     *
     * @param spacing lo spacing o 0
     * @param padding il padding o 0
     * @param alignment l'allineamento o null
     * @param elementi gli elemnti che vanno all'interno del box
     * @return Il VBox con le caratteristiche desiderate
     */
    public static VBox createVBox(int spacing, int padding, Pos alignment, Text text, Parent... elementi){
        VBox vb = new VBox();
        if (text != null) vb.getChildren().add(text);
        vb.getChildren().addAll(elementi);
        vb.setSpacing(spacing);
        vb.setPadding(new Insets(padding));
        if (alignment != null) vb.setAlignment(alignment);
        return vb;

    }

    /** Crea un HBox con le caratteristiche in input
     *
     * @param spacing lo sapacing o 0
     * @param padding il padding o 0
     * @param alignment l'allineamento o null
     * @param text
     *@param elementi gli elemnti che vanno all'interno del box  @return Il HBox con le caratteristiche desiderate
     */
    public static HBox createHBox(int spacing, int padding, Pos alignment, Text text, Parent... elementi){
        HBox hb = new HBox();
        if (text != null) hb.getChildren().add(text);
        hb.getChildren().addAll(elementi);
        hb.setSpacing(spacing);
        hb.setPadding(new Insets(padding));
        if (alignment != null) hb.setAlignment(alignment);
        return hb;

    }


    /** Crea un button con le caratteristiche in input
     * @param text testo da aggiungere al button o null
     * @param width larghezza del button o 0
     * @param height altezza del button o 0
     * @param image immagine del button o null
     * @param disabled se il button � disabilitato o no
     * @return il button completo
     */
    public static Button createButton(String text, double width, double height, Image image, boolean disabled){
        Button button = new Button();
        button.setDisable(disabled);

        if (text != null) button.setText(text);
        if (width != 0 && height != 0) button.setPrefSize(width, height);
        if (image != null) button.setGraphic(new ImageView(image));

        return button;
    }

    /** Crea una TextField con le caratteristiche in input
     * @param columns numero di colonne o 0
     * @param prompt promptTest o null
     * @param style true o false a seconda se si vuole un certo tipo di stile
     * @return la TextField completa
     */
    public static TextField createTextField(int columns, String prompt, boolean style){
        TextField field = new TextField();

        if (columns != 0) field.setPrefColumnCount(columns);
        if (prompt != null) field.setPromptText(prompt);
        if (style == true) field.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");

        return field;
    }

    public static void salvaArchivio(URI dom, String dir, Object[] array){
        FileOutputStream f;
        try {
            f = new FileOutputStream(dir);
            ObjectOutputStream fOUT = new ObjectOutputStream(f);

            fOUT.writeObject(array);

            f.flush();
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object[] apriArchivio(String dir) throws IOException{
        Object[] res=null;

        FileInputStream f=new FileInputStream(dir);
        ObjectInputStream fIN=new ObjectInputStream(f);

        try {
            res=(Object[])fIN.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return res;
    }
}