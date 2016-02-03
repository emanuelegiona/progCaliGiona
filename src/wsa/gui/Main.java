package src.wsa.gui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import src.wsa.web.SiteCrawler;

import java.net.URI;


public class Main extends Application {
    Stage stage;
    public static ObservableList<UriTableView> data = FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {


        stage = primaryStage;
        stage.setTitle("Web Crawler");
        Scene sceneMenu = new Scene(finestra(), 950, 650);


        stage.setScene(sceneMenu);
        stage.setResizable(false);

        stage.show();
    }



    /** crea tutta la finestra
     *
     * @return il layout della finestra
     */
    private Parent finestra(){
        VBox vb = new VBox(bottoniPrincipali(), tableView());
        return vb;
    }


    /**
     *  pie di pagina
     * @return il pie di pagina con la firma
     */
    private Parent pie(){
        Label firma = new Label("   Realizzato da Cali Daniele e Giona Emanuele.");

        HBox bottom = new HBox(firma);
        bottom.setPadding(new Insets(8));

        return bottom;


    }
    SplitPane spMain = new SplitPane();



    public static TableView griglia;
    /**
     *  creazione della tableview
     * @return
     */
    private Parent tableView(){
        griglia = new TableView();



        //colonne
        TableColumn links = new TableColumn("Links visitati");
        links.setPrefWidth(855);
        links.setCellValueFactory(
                new PropertyValueFactory<UriTableView, URI>("uri"));
        links.setResizable(false);

        TableColumn stato = new TableColumn("Stato");
        stato.setPrefWidth(90);
        stato.setCellValueFactory(
                new PropertyValueFactory<UriTableView, String>("stato"));

        stato.setResizable(false);
        //modifica

        griglia.setRowFactory(e -> {
            TableRow<UriTableView> row = new TableRow<>();

            row.setOnMouseClicked(g -> {
                if (!row.isEmpty()) {
                    URI uri = (URI)row.getItem().getUri();
                    String statoUri = row.getItem().getStato();
                    if (statoUri.equals("Completato") || statoUri.equals("  Fallito")) {
                        if (spMain.getItems().size() == 1) {
                            aggiungiSchedaSito(uri);

                        } else {
                            spMain.getItems().remove(spMain.getItems().get(1));
                            aggiungiSchedaSito(uri);
                        }
                    }
                }


            });

            return row;
        });


        SplitPane sp = new SplitPane();



        griglia.setItems(data);

        griglia.getColumns().addAll(links, stato);

        griglia.setPrefHeight(570);
        sp.getItems().add(griglia);
        sp.setOrientation(Orientation.VERTICAL);
        spMain.getItems().add(sp);




        return spMain;

    }

    private void aggiungiSchedaSito(URI uri){
        final Pane st = new StackPane();
        st.setMinWidth(400);
        st.setMaxWidth(400);
        spMain.getItems().add(st);
        SchedaSito.showSchedaSito(spMain, stage, uri);
        TableColumn tb = (TableColumn) griglia.getColumns().get(0);
        tb.setPrefWidth(450);
    }

    /**
     *  controlli principali per far partire il crawler
     */
    private Parent bottoniPrincipali(){

        //bottone start
        Image down = new Image(getClass().getResourceAsStream("/rsz_down.png"));
        Button StartBtn = WindowsManager.createButton(null, 40, 40, down, false);


        //bottone pausa
        Image pause = new Image(getClass().getResourceAsStream("/rsz_1pause.png"));
        Button suspendBtn = WindowsManager.createButton(null, 40, 40, pause, true);


        //bottone stop
        Image stop = new Image(getClass().getResourceAsStream("/rsz_1delete.png"));
        Button stopBtn = WindowsManager.createButton(null, 40, 40, stop, true);

        //bottone +
        Image plus = new Image(getClass().getResourceAsStream("/rsz_blu-.png"));
        ToggleButton piu = new ToggleButton();
        piu.setGraphic(new ImageView(plus));
        piu.setDisable(true);

        HBox bottoni = WindowsManager.createHBox(3, 3, null, null, StartBtn, suspendBtn, stopBtn, piu);


        //listner
        StartBtn.setOnAction(e -> DirectoryWindow.showDirectoryWindow(stage));

        suspendBtn.setOnAction(e -> {
            SiteCrawler siteCrawler = DirectoryWindow.getSiteCrawler();
            if(siteCrawler.isRunning()){
                Image play = new Image(getClass().getResourceAsStream("/rsz_play.png"));
                suspendBtn.setGraphic(new ImageView(play));
                siteCrawler.suspend();
            }
            else {
                suspendBtn.setGraphic(new ImageView(pause));
                siteCrawler.start();
            }

        });

        stopBtn.setOnAction(e -> {
            StartBtn.setDisable(false);
            suspendBtn.setDisable(true);
            stopBtn.setDisable(true);
            piu.setDisable(true);
            SiteCrawler siteCrawler = DirectoryWindow.getSiteCrawler();
            siteCrawler.cancel();
            suspendBtn.setGraphic(new ImageView(pause));
        });

        piu.setOnAction(e -> {
            if(piu.isSelected()){
                final Pane st = new StackPane();
                st.setPrefHeight(100);
                SplitPane split = (SplitPane) spMain.getItems().get(0);
                split.getItems().add(st);
                Popup.showSeeds(spMain);
                spMain.setPrefHeight(572);

            }else{
                SplitPane splitPane = (SplitPane) spMain.getItems().get(0);
                splitPane.getItems().remove(splitPane.getItems().get(1));
            }




        });

        return bottoni;

    }
}