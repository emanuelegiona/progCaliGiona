package src.wsa.gui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import src.wsa.web.Crawler;
import src.wsa.web.SiteCrawler;
import src.wsa.web.WebFactory;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Classe Main
 * Mantiene tutte le strutture dati per la sincronizzazione tra GUI e backend*/
public class Main extends Application {
    private static Stage stage;

    public static HashMap<URI,Integer[]> stats;
    public static volatile Integer ID=0;
    public static volatile Map<Tab, Integer> tabCrawlers=new HashMap();
    public static volatile Map<Integer, Object[]> activeCrawlers =new HashMap<>();
    public static volatile Map<Crawler, Integer> crID=new HashMap<>();

    public static TabPane tabPane;
    public static Button apriBtn;
    public static Button startBtn;
    public static Button suspendBtn;
    public static Button stopBtn;
    public static ToggleButton piu;
    public static Button grafico;
    public static Tab guidaTab=new Tab("Guida");

    public static void main(String[] args) {
        launch(args);
    }

    /** Crea e mostra il Main stage
     * @param primaryStage JavaFX primaryStage*/
    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("Web Crawler");
        stats =new HashMap<>();

        Scene sceneMenu = new Scene(finestra(), 950, 650);

        stage.setScene(sceneMenu);
        stage.setResizable(true);
        stage.setOnCloseRequest(event -> {
            if (!(tabPane.getTabs().size() == 1 && tabPane.getTabs().contains(guidaTab))) {
                Alert alert = WindowsManager.creaAlert(Alert.AlertType.CONFIRMATION, "Avviso", "Chiudure l'applicazione?\n" +
                        "Le esplorazioni in corso saranno terminate ed eventualmente archiviate.");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK)
                    tabCrawlers.forEach((t, ID) -> getSiteCrawler(ID).cancel());
            }
        });

        stage.show();
        stage.setMinWidth(980);
        stage.setMinHeight(650);
        Guida.creaGuida(guidaTab);
    }

    /** Genera la GUI principale e gestisce il listener del TabPane
     * @return il layout della finestra*/
    private Parent finestra(){
        tabPane = new TabPane();

        tabPane.getTabs().add(guidaTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getSelectionModel().selectedItemProperty().addListener((ov, t, t1) -> {
            if(t1!=null && !t1.equals(guidaTab)) {
                if (getSiteCrawler() != null) {
                    SiteCrawler siteCrawler = getSiteCrawler();

                    if (siteCrawler.isCancelled()) {
                        suspendBtn.setDisable(true);
                        stopBtn.setDisable(true);
                        piu.setDisable(true);
                        grafico.setDisable(true);
                    }
                    else {
                        if (siteCrawler.isRunning()) {
                            Image pause = new Image(getClass().getResourceAsStream("/rsz_1pause.png"));
                            suspendBtn.setGraphic(new ImageView(pause));
                            suspendBtn.setDisable(false);
                            stopBtn.setDisable(false);
                            piu.setDisable(false);
                            grafico.setDisable(false);
                        }
                        else {
                            Image play = new Image(getClass().getResourceAsStream("/rsz_play.png"));
                            suspendBtn.setGraphic(new ImageView(play));
                            suspendBtn.setDisable(false);
                            stopBtn.setDisable(false);
                            piu.setDisable(false);
                            grafico.setDisable(true);
                        }
                    }
                }
            }
            else{
                suspendBtn.setDisable(true);
                stopBtn.setDisable(true);
                piu.setDisable(true);
                grafico.setDisable(true);
            }
        });
        VBox vb = new VBox(bottoniPrincipali(), tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        return vb;
    }

    /** Creazione della TableView per ogni esplorazione
     * @return lo SplitPane contenente la TableView*/
    public static SplitPane tableView(){
        SplitPane spMain = new SplitPane();
        TableView griglia = new TableView();

        //colonne
        TableColumn links = new TableColumn("Link visitati");
        links.setCellValueFactory(
                new PropertyValueFactory<UriTableView, URI>("uri"));
        links.setResizable(false);

        TableColumn stato = new TableColumn("Stato");
        stato.setCellValueFactory(
                new PropertyValueFactory<UriTableView, String>("stato"));
        stato.setResizable(false);

        links.prefWidthProperty().bind(griglia.widthProperty().multiply(0.875));
        stato.prefWidthProperty().bind(griglia.widthProperty().multiply(0.125));
        griglia.prefHeightProperty().bind(stage.widthProperty());

        griglia.setRowFactory(e -> {
            TableRow<UriTableView> row = new TableRow<>();

            row.setOnMouseClicked(g -> {
                if (!row.isEmpty()) {
                    URI uri = row.getItem().getUri();
                    String statoUri = row.getItem().getStato();

                    try {
                        if (statoUri.equals("Completato") || statoUri.equals("  Fallito")) {
                            if (spMain.getItems().size() == 1)
                                aggiungiSchedaSito(uri);
                            else {
                                spMain.getItems().remove(spMain.getItems().get(1));
                                aggiungiSchedaSito(uri);
                            }
                        }
                    }catch (Exception el){
                        if(spMain.getItems().size()==2)
                            spMain.getItems().remove(spMain.getItems().get(1));
                    }
                }
            });

            return row;
        });

        griglia.setItems(getData());
        griglia.getColumns().addAll(links, stato);

        SplitPane sp = new SplitPane();
        sp.getItems().add(griglia);
        sp.setOrientation(Orientation.VERTICAL);
        spMain.getItems().add(sp);

        return spMain;
    }

    /** Mostra la scheda delle statistiche del sito selezionato
     * @param uri il sito selezionato*/
    private static void aggiungiSchedaSito(URI uri){
        SplitPane spMain = getSpMain();

        try{
            final Pane st = new StackPane();
            st.setMinWidth(400);
            st.setMaxWidth(400);
            spMain.getItems().add(st);

            SplitPane split = (SplitPane) getSpMain().getItems().get(0);
            TableView griglia = (TableView) split.getItems().get(0);

            TableColumn tb = (TableColumn) griglia.getColumns().get(0);
            TableColumn tb2 = (TableColumn) griglia.getColumns().get(1);
            tb.prefWidthProperty().bind(griglia.widthProperty().multiply(0.8));
            tb2.prefWidthProperty().bind(griglia.widthProperty().multiply(0.2));

            SchedaSito.showSchedaSito(spMain, stage, uri);
        }catch (IllegalStateException e){
            if(spMain.getItems().size()==2)
                spMain.getItems().remove(spMain.getItems().get(1));

            Alert alert = WindowsManager.creaAlert(Alert.AlertType.ERROR, "Errore", "Azione non consentita: nessuna esplorazione in corso");
            alert.showAndWait();
        }catch(ArrayIndexOutOfBoundsException ae) {}
    }


    /** Bottoni della GUI che gestiscono l'esplorazione
     * @return HBox che contiene i bottoni della GUI*/
    private Parent bottoniPrincipali(){
        //bottone apri
        Image apriImg = new Image(getClass().getResourceAsStream("/rsz_cartella_win.png"));
        apriBtn = WindowsManager.createButton(null, 40, 40, apriImg, false);
        apriBtn.setTooltip(new Tooltip("Apri archivio"));

        //bottone nuovo
        Image down = new Image(getClass().getResourceAsStream("/rsz_down.png"));
        startBtn = WindowsManager.createButton(null, 40, 40, down, false);
        startBtn.setTooltip(new Tooltip("Nuova esplorazione"));

        //bottone pausa
        Image pause = new Image(getClass().getResourceAsStream("/rsz_1pause.png"));
        suspendBtn = WindowsManager.createButton(null, 40, 40, pause, true);
        suspendBtn.setTooltip(new Tooltip("Sospendi"));

        //bottone stop
        Image stop = new Image(getClass().getResourceAsStream("/rsz_1delete.png"));
        stopBtn = WindowsManager.createButton(null, 40, 40, stop, true);
        stopBtn.setTooltip(new Tooltip("Termina"));

        //bottone +
        Image plus = new Image(getClass().getResourceAsStream("/rsz_blu-.png"));
        piu = new ToggleButton();
        piu.setGraphic(new ImageView(plus));
        piu.setTooltip(new Tooltip("Aggiungi seed"));
        piu.setDisable(true);

        //bottone grafico
        Image graphic = new Image(getClass().getResourceAsStream("/rsz_pic.png"));
        grafico = WindowsManager.createButton(null, 40,40, graphic, true);
        grafico.setTooltip(new Tooltip("Statistiche esplorazione"));

        //bottone help
        Image help=new Image(getClass().getResourceAsStream("/rsz_help.png"));
        Button helpBtn = WindowsManager.createButton(null, 40,40, help, false);
        helpBtn.setTooltip(new Tooltip("Mostra guida"));

        Pane p=new Pane();
        HBox.setHgrow(p,Priority.ALWAYS);
        HBox bottoni = WindowsManager.createHBox(3, 3, null, null, apriBtn,startBtn, suspendBtn, stopBtn, piu, grafico, p, helpBtn);

        //listner
        startBtn.setOnAction(e -> DirectoryWindow.showDirectoryWindow(stage));

        apriBtn.setOnAction(e -> {
            try {
                Main.tabPane.getTabs().remove(Main.guidaTab);

                ID++;
                Object[] objects = new Object[7];
                Tab tab = new Tab("In Download");

                ObservableList<UriTableView> fx = FXCollections.observableArrayList();
                objects[1] = fx;
                objects[3] = new HashMap<URI,Integer[]>();
                objects[4] = null; //dominio
                objects[5]=0; //max link in una pagina
                objects[6]=0; //uri interni al dominio
                activeCrawlers.put(ID, objects);

                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extensionCG = new FileChooser.ExtensionFilter("Archivio Esplorazione (*.cg)", "*.cg");
                fileChooser.getExtensionFilters().add(extensionCG);
                File selectedFile = fileChooser.showOpenDialog(stage);
                Path path = selectedFile.toPath();
                fileChooser.setTitle("Seleziona file");
                SiteCrawler siteCrawler = WebFactory.getSiteCrawler(null, path);

                objects[0] = siteCrawler;
                activeCrawlers.put(ID, objects);

                tabCrawlers.put(tab,ID);
                activeCrawlers.put(ID, objects);

                tab.setTooltip(new Tooltip(((URI)objects[4]).toString()));
                tabPane.getTabs().add(tab);
                tabPane.getSelectionModel().select(tab);

                SplitPane sp = Main.tableView();
                tab.setContent(sp);
                objects[2] = sp;

                activeCrawlers.put(ID, objects);
                siteCrawler.start();

                suspendBtn.setGraphic(new ImageView(pause));
                suspendBtn.setDisable(false);
                stopBtn.setDisable(false);
                piu.setDisable(false);
                grafico.setDisable(false);
            } catch (IOException e1) {
                Alert alert=WindowsManager.creaAlert(Alert.AlertType.ERROR,"Errore","Errore di I/O");
                alert.showAndWait();
            } catch (Exception e2){
                if(tabPane.getTabs().isEmpty())
                    tabPane.getTabs().add(guidaTab);
            }
        });

        suspendBtn.setOnAction(e -> {
            SiteCrawler siteCrawler = getSiteCrawler();

            if(siteCrawler.isRunning()){
                Image play = new Image(getClass().getResourceAsStream("/rsz_play.png"));
                suspendBtn.setGraphic(new ImageView(play));
                siteCrawler.suspend();

                tabPane.getSelectionModel().getSelectedItem().setText("In Pausa");
                grafico.setDisable(true);
            }
            else {
                suspendBtn.setGraphic(new ImageView(pause));
                siteCrawler.start();

                tabPane.getSelectionModel().getSelectedItem().setText("In Download");
                grafico.setDisable(false);
            }
        });

        stopBtn.setOnAction(e -> {
            Alert alert = WindowsManager.creaAlert(Alert.AlertType.CONFIRMATION, "Conferma", "Sei sicuro di voler terminare l'esplorazione?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                suspendBtn.setDisable(true);
                stopBtn.setDisable(true);
                piu.setDisable(true);
                grafico.setDisable(true);

                SiteCrawler siteCrawler = getSiteCrawler();
                siteCrawler.cancel();

                suspendBtn.setGraphic(new ImageView(pause));
                tabPane.getSelectionModel().getSelectedItem().setText("Terminato");

                Tab t=tabPane.getSelectionModel().getSelectedItem();
                tabPane.getTabs().remove(t);

                int index=tabCrawlers.get(t); //indice del sitecrawler della tabella corrente
                tabCrawlers.remove(t);        //rimuovo tabella e ID
                activeCrawlers.remove(index); //rimuovo ID e Object[] (con sitecrawler, dominio ecc)

                Crawler target=null;          //cerco il crawler per ID in crID
                for(Crawler c:crID.keySet()){
                    if(crID.get(c).equals(index)) {
                        target=c;
                        break;
                    }
                }
                crID.remove(target);          //rimuovo crawler da crID

                if(tabPane.getTabs().size()==0)
                    tabPane.getTabs().add(guidaTab);
            }
        });

        piu.setOnAction(e -> {
            SiteCrawler siteCrawler=getSiteCrawler();
            if(!siteCrawler.isCancelled()) {
                SplitPane spMain = getSpMain();

                if (piu.isSelected()) {
                    final Pane st = new StackPane();
                    SplitPane split = (SplitPane) spMain.getItems().get(0);
                    split.getItems().add(st);
                    Popup.showSeeds(spMain);
                }
                else {
                    SplitPane splitPane = (SplitPane) spMain.getItems().get(0);
                    splitPane.getItems().remove(splitPane.getItems().get(1));
                }
            }
        });

        grafico.setOnAction(e -> {
            Tab tab=tabPane.getSelectionModel().getSelectedItem();
            SiteCrawler siteCrawler=getSiteCrawler();

            if(siteCrawler.isRunning())
                Grafico.showGraphic(stage,tabCrawlers.get(tab));
        });

        helpBtn.setOnAction(e->{
            if(tabPane.getTabs().contains(guidaTab))
                tabPane.getSelectionModel().select(guidaTab);
            else {
                tabPane.getTabs().add(guidaTab);
                tabPane.getSelectionModel().select(guidaTab);
            }
        });

        return bottoni;
    }

    /** Ritorna il SiteCrawler dell'esplorazione corrente
     * @return il SiteCrawler corrente*/
    public static SiteCrawler getSiteCrawler() {
        Tab tab=tabPane.getSelectionModel().getSelectedItem();
        Object[] objects = activeCrawlers.get(tabCrawlers.get(tab));
        return (SiteCrawler) objects[0];
    }

    /** Ritorna il SiteCrawler relativo all'identificatore i
     * @param i l'identificatore di cui si vuole il SiteCrawler
     * @return il SiteCrawler richiesto*/
    public static SiteCrawler getSiteCrawler(int i){
        Object[] objects= activeCrawlers.get(i);
        return (SiteCrawler)objects[0];
    }

    /** Ritorna i dati della TableView dell'esplorazione corrente
     * @return i dati correntemente in uso*/
    public static ObservableList<UriTableView> getData(){
        Tab tab=tabPane.getSelectionModel().getSelectedItem();
        Object[] objects = activeCrawlers.get(tabCrawlers.get(tab));
        return (ObservableList<UriTableView>) objects[1];
    }

    /** Ritorna i dati della TableView relativa all'identificatore i
     * @param i l'identificatore di cui si vogliono i dati della TableView
     * @return i dati richiesti*/
    public static ObservableList<UriTableView> getData(int i){
        Object[] objects = activeCrawlers.get(i);
        return (ObservableList<UriTableView>) objects[1];
    }

    /** Ritorna lo SplitPane dell'esplorazione corrente
     * @return lo SplitPane richiesto*/
    public static SplitPane getSpMain(){
        Tab tab=tabPane.getSelectionModel().getSelectedItem();
        Object[] objects = activeCrawlers.get(tabCrawlers.get(tab));
        return (SplitPane) objects[2];
    }

    /** Ritorna lo SplitPane relativo all'identificatore i
     * @param i l'identificatore di cui si vuole lo SplitPane
     * @return lo SplitPane richiesto*/
    public static SplitPane getSpMain(int i){
        Object[] objects = activeCrawlers.get(i);
        return (SplitPane) objects[2];
    }

    /** Ritorna i dati relativi agli uri dell'esplorazione corrente
     * @return i dati dell'esplorazione corrente*/
    public static Map<URI, Integer[]> getStats(){
        Tab tab=tabPane.getSelectionModel().getSelectedItem();
        Object[] objects = activeCrawlers.get(tabCrawlers.get(tab));
        return (Map<URI, Integer[]>) objects[3];
    }

    /** Ritorna i dati relativi agli uri relativi all'identificatore i
     * @param i l'identificatore di cui si vogliono i dati
     * @return i dati richiesti*/
    public static Map<URI, Integer[]> getStats(int i){
        Object[] objects = activeCrawlers.get(i);
        return (Map<URI,Integer[]>) objects[3];
    }
}
