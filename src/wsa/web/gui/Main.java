package src.wsa.web.gui;

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
import javafx.scene.text.Text;
import javafx.stage.Stage;



public class Main extends Application {
    Stage stage;
    private final ObservableList<UriTableView> data = FXCollections.observableArrayList();


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Text t = new Text("http://www.google.it/doodles");
        ProgressBar pi = new ProgressBar(.6);


        data.add(new UriTableView(t, pi));


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



    TableView griglia;
    /**
     *  creazione della tableview
     * @return
     */
    private Parent tableView(){
        griglia = new TableView();



        //colonne
        TableColumn links = new TableColumn("Links visitati");
        links.setPrefWidth(848);
        links.setCellValueFactory(
                new PropertyValueFactory<UriTableView, Object>("uri"));
        TableColumn progress = new TableColumn("Progresso");
        progress.setPrefWidth(110);
        progress.setCellValueFactory(
                new PropertyValueFactory<UriTableView, Object>("p"));
        progress.setResizable(false);
        links.setResizable(false);


        griglia.setRowFactory(e -> {
            TableRow<UriTableView> row = new TableRow<>();
            row.setOnMouseClicked(g -> {
                if (spMain.getItems().size() < 2) {
                    final Pane st = new StackPane();
                    st.setMinWidth(400);
                    st.setMaxWidth(400);
                    spMain.getItems().add(st);
                    SchedaSito.showSchedaSito(spMain);
                    TableColumn tb = (TableColumn) griglia.getColumns().get(0);
                    tb.setPrefWidth(442);

                }


                if (row.getIndex() > 10)
                    row.setStyle("-fx-background-color: #FFEBEB;" +
                            "-fx-border-style: solid line-join round;" +
                            "-fx-border-width: 0.1px");
                else row.setStyle("-fx-background-color: #F2FFF2;" +
                        "-fx-border-style: solid line-join round;" +
                        "-fx-border-width: 0.1px");


            });

            return row;
        });


        SplitPane sp = new SplitPane();



        griglia.setItems(data);

        griglia.getColumns().addAll(links, progress);

        griglia.setPrefHeight(570);
        sp.getItems().add(griglia);
        sp.setOrientation(Orientation.VERTICAL);
        spMain.getItems().add(sp);

        return spMain;

    }

    /**
     *  controlli principali per far partire il crawler
     */
    private Parent bottoniPrincipali(){

        //bottone start
        Image play = new Image(getClass().getResourceAsStream("/rsz_down.png"));
        Button StartBtn = WindowsManager.createButton(null, 40, 40, play, false);


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
            StartBtn.setDisable(false);
            suspendBtn.setDisable(true);
            stopBtn.setDisable(true);
            piu.setDisable(true);
        });

        stopBtn.setOnAction(e -> {
            StartBtn.setDisable(false);
            suspendBtn.setDisable(true);
            stopBtn.setDisable(true);
            piu.setDisable(true);
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