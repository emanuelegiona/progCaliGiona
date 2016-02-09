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

/** Classe di utilita'*/
public class WindowsManager {

    /** Crea un VBox con le caratteristiche in input
     * @param spacing lo spacing oppure 0
     * @param padding il padding oppure 0
     * @param alignment l'allineamento oppure null
     * @param elementi gli elemnti che vanno all'interno del box
     * @return Il VBox con le caratteristiche desiderate*/
    public static VBox createVBox(int spacing, int padding, Pos alignment, Text text, Parent... elementi){
        VBox vb = new VBox();
        if (text != null)
            vb.getChildren().add(text);

        if (elementi!=null)
            vb.getChildren().addAll(elementi);

        vb.setSpacing(spacing);
        vb.setPadding(new Insets(padding));
        if (alignment != null)
            vb.setAlignment(alignment);

        return vb;
    }

    /** Crea un HBox con le caratteristiche in input
     * @param spacing lo sapacing oppure 0
     * @param padding il padding oppure 0
     * @param alignment l'allineamento oppure null
     * @param text eventuale testo oppure null
     *@param elementi gli elemnti che vanno all'interno del box  @return Il HBox con le caratteristiche desiderate*/
    public static HBox createHBox(int spacing, int padding, Pos alignment, Text text, Parent... elementi){
        HBox hb = new HBox();
        if (text != null)
            hb.getChildren().add(text);

        if (elementi!=null)
            hb.getChildren().addAll(elementi);

        hb.setSpacing(spacing);
        hb.setPadding(new Insets(padding));
        if (alignment != null)
            hb.setAlignment(alignment);

        return hb;
    }

    /** Crea un button con le caratteristiche in input
     * @param text testo da aggiungere al button oppure null
     * @param width larghezza del button oppure 0
     * @param height altezza del button oppure 0
     * @param image immagine del button oppure null
     * @param disabled se il button e' disabilitato oppure no
     * @return il button completo*/
    public static Button createButton(String text, double width, double height, Image image, boolean disabled){
        Button button = new Button();
        button.setDisable(disabled);
        if (text != null)
            button.setText(text);

        if (width != 0 && height != 0)
            button.setPrefSize(width, height);

        if (image != null)
            button.setGraphic(new ImageView(image));

        return button;
    }

    /** Crea una TextField con le caratteristiche in input
     * @param columns numero di colonne oppure 0
     * @param prompt promptTest oppure null
     * @param style se true, il testo viene impostato a grigio, altrimenti nero
     * @return la TextField completa*/
    public static TextField createTextField(int columns, String prompt, boolean style){
        TextField field = new TextField();
        if (columns != 0)
            field.setPrefColumnCount(columns);

        if (prompt != null)
            field.setPromptText(prompt);

        if (style == true)
            field.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%); }");

        return field;
    }

    /** Crea o sovrascrive un file nella directory specificata contenente un array di Object
     * @param dir file path completo di file name in String
     * @param array l'array di Object da scrivere nel file
     * @throws IOException se avvengono errori di scrittura*/
    public static void salvaArchivio(String dir, Object[] array) throws IOException{
        FileOutputStream f;
        f = new FileOutputStream(dir);
        ObjectOutputStream fOUT = new ObjectOutputStream(f);

        fOUT.writeObject(array);

        f.flush();
        f.close();
    }

    /** Apre un file dalla directory specificata
     * @param dir file path completo di file name in String
     * @return array di Object contenuto nel file
     * @throws IOException se avvengono errori di lettura
     * @throws ClassNotFoundException se il file non contiene un array di Object*/
    public static Object[] apriArchivio(String dir) throws IOException,ClassNotFoundException{
        Object[] res=null;

        FileInputStream f=new FileInputStream(dir);
        ObjectInputStream fIN=new ObjectInputStream(f);

        res=(Object[])fIN.readObject();
        return res;
    }

    /** Crea un messaggio con le caratteristiche specificate
     * @param at tipo di Alert
     * @param titolo il titolo del messaggio
     * @param testo il testo del messaggio
     * @return un Alert con le caratteristiche specificate*/
    public static Alert creaAlert(Alert.AlertType at,String titolo, String testo ){
        Alert alert = new Alert(at);
        alert.setTitle(titolo);
        alert.setContentText(testo);
        alert.setHeaderText(null);
        return alert;
    }
}