package src.wsa.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import src.wsa.web.SiteCrawler;
import src.wsa.web.WebFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DirectoryWindow {
    static Stage stage; //stage principale
    static ObservableList<String> listItems = FXCollections.observableArrayList(); //lista degli items nella listview
    static private SiteCrawler siteCrawler;

    /** Crea e mostra la finestra della directory
     * @param primaryStage lo stage della finestra principale*/

    public static void  showDirectoryWindow(Stage primaryStage){
        stage = new Stage();
        listItems.clear();
        stage.setTitle("Directory");
        stage.initModality(Modality.WINDOW_MODAL);  //setta la finestra in modo tale da
        stage.initOwner(primaryStage);              // non poter cliccare nella finestra principale

        Scene finestra = new Scene(createDirectoryWindow(primaryStage), 570, 400);

        stage.setScene(finestra);
        stage.setResizable(false);
        stage.show();
    }


    /** Crea la parte centrale della finestra della directoryWindow
     * @return la parte centrale della finestra
     */
    private static Parent list(){
        //parte sinistra per aggiungere seeds
        Text seedLbl = new Text(" Aggiungi seed:");
        Text info = new Text("Lasciare vuoto se si vuole far partire \nl'esplorazione direttamente dal dominio.");
        TextField insertSeed = WindowsManager.createTextField(0, "es: https://www.google.com/doodles", true);
        VBox seeds = WindowsManager.createVBox(10, 10, Pos.TOP_LEFT, seedLbl, insertSeed);
        seeds.getChildren().add(info);

        //parte destra con la lsitView
        ListView list = new ListView();
        list.setItems(listItems);


        //parte centrale con i bottoni e le relative immagini
        Image image= new Image(WindowsManager.class.getResourceAsStream("/rsz_blu.png"));
        Button add = WindowsManager.createButton(null, 30, 43, image, false);

        image = new Image(WindowsManager.class.getResourceAsStream("/rsz_bluX.png"));
        Button remove = WindowsManager.createButton(null, 43, 43, image, false);

        VBox buttons = WindowsManager.createVBox(50, 0, Pos.CENTER, null, add, remove);
        HBox center = WindowsManager.createHBox(10, 0, null, null, seeds, buttons, list);

        seeds.prefWidthProperty().bind(center.widthProperty().multiply(0.4));
        buttons.prefWidthProperty().bind(center.widthProperty().multiply(0.1));


        //listner
        listListner(list, insertSeed, add, remove);

        return center;

    }

    /** Comprende tutti i listner dei controlli nella parte centrale della finestra
     * @param list la listView principale
     * @param insertSeed textField per inserire seeds
     * @param add bottone per aggiungere seeds
     * @param remove bottone per rimuovere seeds
     */
    private static void listListner(ListView list, TextField insertSeed, Button add, Button remove){

        insertSeed.setOnAction(e -> {
            if (!insertSeed.getText().equals("") && !listItems.contains(insertSeed.getText())){
                listItems.add(insertSeed.getText());
                insertSeed.setText("");
            }
        });


        add.setOnAction(e -> {
            if (!insertSeed.getText().equals("") && !listItems.contains(insertSeed.getText())){
                listItems.add(insertSeed.getText());
                insertSeed.setText("");
            }

        });


        remove.setOnAction(e -> {
            int i = list.getSelectionModel().getSelectedIndex();
            if (i != -1) {
                listItems.remove(i);
            }
        });

    }



    /** Crea la finestra alla pressione del pulsante start*/

    private static Parent createDirectoryWindow(Stage primaryStage){
        Separator separator1 = new Separator();
        Separator separator2 = new Separator();

        //crea la parte sopra della finestra con l'inserimento del dominio
        Text dominionLbl = new Text("Dominio:");
        TextField dominionTxt = WindowsManager.createTextField(0, "es: https://www.google.it", true);

        HBox dominio = WindowsManager.createHBox(10, 15, Pos.CENTER, dominionLbl, dominionTxt);
        HBox.setHgrow(dominionTxt, Priority.ALWAYS);

        //crea la parte sotto con l'inserimento del percorso della directory di archiviazione
        CheckBox spunta = new CheckBox("Utilizza una directory per l'archiviazione.");
        spunta.setSelected(true);

        Text directoryLbl = new Text("Scegli directory: ");
        TextField directoryTxt = WindowsManager.createTextField(0, "es: C:\\Users\\User\\Documents", true);
        Button puntiniBtn = WindowsManager.createButton("...", 35, 10, null, false);

        HBox directory = WindowsManager.createHBox(10, 15, Pos.CENTER, directoryLbl, directoryTxt, puntiniBtn);

        HBox.setHgrow(directoryTxt, Priority.ALWAYS);
        VBox vb = WindowsManager.createVBox(10, 0, Pos.CENTER, null, separator1, list(), separator2, spunta, directory);


        //crea la parte finale della finestra con i bottoni "ok" e "chiudi"
        Image image= new Image(WindowsManager.class.getResourceAsStream("/rsz_tick.png"));
        Button ok = WindowsManager.createButton("OK", 80, 30, image, false);

        image = new Image(WindowsManager.class.getResourceAsStream("/rsz_del.png"));
        Button chiudi = WindowsManager.createButton("Chiudi", 100, 30, image, false);
        HBox bottom = WindowsManager.createHBox(10, 7, Pos.BOTTOM_RIGHT, null, ok, chiudi);

        //posiziona il tutto
        BorderPane pane = new BorderPane();
        pane.setTop(dominio);
        pane.setCenter(vb);
        pane.setBottom(bottom);

        //listner
        directoryListner(spunta,directory,directoryLbl,puntiniBtn,directoryTxt,dominionTxt,primaryStage,ok,chiudi);

        return pane;
    }

    /** tutti i listner della finestra tranne quelli della parte dentrale
     * @param spunta il checkbox che fa disabilitare l'inserimento della directory se disattivato
     * @param directory il box dei controlli per l0inserimento della directory
     * @param directoryLbl la label dell'inserimento della directory
     * @param puntiniBtn  bottone per scegliere la directory
     * @param directoryTxt textField dove inserire il percorso della directory
     * @param dominionTxt textField dove inserire il dominio
     * @param primaryStage lo stage della finestra principale
     * @param ok bottone per confermare il tutto
     * @param chiudi bottone per chiudere la fienstra senza confermare
     */
    private static void directoryListner(CheckBox spunta, HBox directory, Text directoryLbl, Button puntiniBtn, TextField directoryTxt, TextField dominionTxt, Stage primaryStage,
                                            Button ok, Button chiudi){

        spunta.setOnAction(e -> {
            if (!spunta.isSelected()) {
                directory.setDisable(true);
                directoryLbl.setFill(Color.web("#B2B2B2"));
            } else {
                directory.setDisable(false);
                directoryLbl.setFill(Color.web("#000000"));


            }
        });

        puntiniBtn.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(stage);
            directoryChooser.setTitle("Scegli Directory");

            if (selectedDirectory != null) {
                directoryTxt.setText(selectedDirectory.toString());
            }


        });

        dominionTxt.setOnAction(e -> {
            HBox a = (HBox) primaryStage.getScene().getRoot().getChildrenUnmodifiable().get(0);
            a.getChildren().get(0).setDisable(true);
            for(int i=1; i<=3; i++){
                a.getChildren().get(i).setDisable(false);
            }
            stage.close();

        });


        ok.setOnAction(e -> new Thread(() -> siteCrawlerStart(primaryStage, dominionTxt, directoryTxt)).start());

        chiudi.setOnAction(e -> stage.close());

    }


    private static void siteCrawlerStart(Stage primaryStage, TextField dominioTxt, TextField directoryTxt){
        String dominio = null;
        String directory = null;
        List seeds;
        Path path = null;
        URI dom = null;

        if(dominioTxt.getText().equals("")) {
            Platform.runLater( () -> {
                Alert alert = WindowsManager.creaAlert(Alert.AlertType.ERROR,"Errore","Immettere un dominio");
                alert.showAndWait();
            });
            return;
        }
            dominio = dominioTxt.getText();


        if (!directoryTxt.isDisabled()){
            directory = directoryTxt.getText();
            if(Files.exists(Paths.get(directory))  &&  !directory.equals("")){
                path = Paths.get(directory);
            }
            else{
                Platform.runLater(() -> {
                    Alert alertt = WindowsManager.creaAlert(Alert.AlertType.ERROR, "Errore", "Percorso inesistente");
                    alertt.showAndWait();
                });
                return;
            }
        }


        VBox centerBox = (VBox) stage.getScene().getRoot().getChildrenUnmodifiable().get(1);
        HBox listaBox = (HBox) centerBox.getChildren().get(1);
        ListView listSeeds = (ListView) listaBox.getChildren().get(2);
        seeds = new ArrayList<>(listSeeds.getItems());

        try {
            MainGUI.ID++;
            dom = new URI(dominio);
            SiteCrawler siteCrawler = WebFactory.getSiteCrawler(dom, path);
            final URI finalDom = dom;
            Platform.runLater(() -> {
                MainGUI.tabPane.getTabs().remove(MainGUI.guidaTab);

                Object[] objects = new Object[7];
                Tab tab = new Tab("In Download");
                tab.setTooltip(new Tooltip(finalDom.toString()));
                ObservableList<UriTableView> fx = FXCollections.observableArrayList();
                objects[0] = siteCrawler;
                objects[1] = fx;
                objects[3] = new HashMap<URI,Integer[]>();
                objects[4]=finalDom;
                objects[5]=0; //max link in una pagina
                objects[6]=0; //uri interni al dominio

                MainGUI.tabCrawlers.put(tab,MainGUI.ID);
                MainGUI.activeCrawlers.put(MainGUI.ID,objects);

                MainGUI.tabPane.getTabs().add(tab);
                MainGUI.tabPane.getSelectionModel().select(tab);

                SplitPane sp = MainGUI.tableView();
                tab.setContent(sp);
                objects[2] = sp;

                MainGUI.activeCrawlers.put(MainGUI.ID,objects);

                if (seeds.isEmpty()) siteCrawler.addSeed(finalDom);
                else {
                    URI uriSeed = null;
                    for (Object seed : seeds) {
                        String stringaSeed = (String) seed;
                        try {
                            uriSeed = new URI(stringaSeed);
                        } catch (URISyntaxException e) {}
                        siteCrawler.addSeed(uriSeed);
                    }
                }
                siteCrawler.start();

                Image pause = new Image(MainGUI.class.getResourceAsStream("/rsz_1pause.png"));
                MainGUI.suspendBtn.setGraphic(new ImageView(pause));
                MainGUI.suspendBtn.setDisable(false);
                MainGUI.stopBtn.setDisable(false);
                MainGUI.piu.setDisable(false);
                MainGUI.grafico.setDisable(false);
            });

        } catch (IOException | URISyntaxException | IllegalArgumentException e) {
            if(MainGUI.tabPane.getTabs().isEmpty())
                MainGUI.tabPane.getTabs().add(MainGUI.guidaTab);
            Platform.runLater(() -> {
                Alert alert = WindowsManager.creaAlert(Alert.AlertType.ERROR,"Errore", e.getMessage());
                alert.showAndWait();
            });
            return;
        }


        Platform.runLater(() -> {
            stage.close();
            HBox a = (HBox)primaryStage.getScene().getRoot().getChildrenUnmodifiable().get(0);

            for(int i=2; i<=5; i++){
                a.getChildren().get(i).setDisable(false);
            }
        });


    }

    public static SiteCrawler getSiteCrawler(){
        return siteCrawler;
    }
}
