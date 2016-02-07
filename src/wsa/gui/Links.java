package src.wsa.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import src.wsa.web.CrawlerResult;
import src.wsa.web.SiteCrawler;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 03/02/2016.
 */
public class Links {
    static Stage stage = new Stage();
    private static ObservableList<TableViewLinks> linksTotali = FXCollections.observableArrayList();
    private static SiteCrawler siteCrawler;


    public static void  showLinksWindow(Stage primaryStage, CrawlerResult risultato){
        linksTotali.clear();
        stage.setTitle("Links");
        try{
            stage.initOwner(primaryStage);
            stage.setAlwaysOnTop(false);
        }catch (Exception e){}

        Scene finestra = new Scene(createLinksWindows(risultato), 800, 600);

        stage.setScene(finestra);
        stage.setResizable(true);
        stage.show();
    }



    //seguit o se ha incontrato un errore
    private static Parent createLinksWindows(CrawlerResult risultato ){
        BorderPane bp = new BorderPane();
        siteCrawler = SchedaSito.getSiteCrawler();
        CrawlerResult cr;
        for(URI u: risultato.links){
            try {
                cr = siteCrawler.get(u);
                TableViewLinks tv = new TableViewLinks(cr.uri.toString(), cr.linkPage ? "si" : "no", cr.exc == null ? "null" : "Errore");
                linksTotali.add(tv);
            }catch (IllegalArgumentException e){
                TableViewLinks tv = new TableViewLinks(u.toString(), "no",  "null");
                linksTotali.add(tv);
            }


        }


        TableView tableTotale = new TableView<>();


        TableColumn links = new TableColumn("Links estratti");

        links.setCellValueFactory(
                new PropertyValueFactory<TableViewLinks, String>("uri"));
        links.setResizable(false);

        TableColumn seguito = new TableColumn("Seguito?");

        seguito.setCellValueFactory(
                new PropertyValueFactory<TableViewLinks, String>("seguito"));
        seguito.setResizable(false);

        TableColumn errore = new TableColumn("Errore");

        errore.setCellValueFactory(
                new PropertyValueFactory<TableViewLinks, String>("errore"));
        errore.setResizable(false);

        tableTotale.setItems(linksTotali);

        tableTotale.getColumns().addAll(links, seguito, errore);


        links.prefWidthProperty().bind(tableTotale.widthProperty().multiply(0.8));
        seguito.prefWidthProperty().bind(tableTotale.widthProperty().multiply(0.1));
        errore.prefWidthProperty().bind(tableTotale.widthProperty().multiply(0.1));

        bp.setCenter(tableTotale);

        return bp;

    }


}
