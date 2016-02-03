package src.wsa.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.chart.Chart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import src.wsa.web.CrawlerResult;
import src.wsa.web.SiteCrawler;
import src.wsa.web.WebFactory;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class SchedaSito {

    private static Window primaryWindow;
    private static CrawlerResult risultato;
    private static SiteCrawler siteCrawler;


    public static void showSchedaSito(SplitPane sp, Stage primaryStage, URI uri){
        primaryWindow  = sp.getScene().getWindow();
        siteCrawler = DirectoryWindow.getSiteCrawler();
        risultato = siteCrawler.get(uri);

        //modifica
        BorderPane bp = new BorderPane();
        Text x = new Text("X");


        HBox hb = new HBox(new Text("Statistiche sito                                                                                          "), x);
        hb.setPrefWidth(200);

        bp.setTop(new ToolBar(hb));
        bp.setStyle("-fx-background-color: #FFFFFF");

        StackPane st = (StackPane) sp.getItems().get(1);
        st.getChildren().add(bp);

        Button apriSito = WindowsManager.createButton("Apri sito", 100,50,null,false);
        bp.setCenter(createSchedaWindow(risultato));
        bp.setBottom(apriSito);


        apriSito.setOnAction(e -> {
            WebWindow.showWebWindow(primaryStage, uri);
        });


        x.setOnMouseClicked(e -> {
            SplitPane splitPane = (SplitPane) sp.getItems().get(0);
            TableView table = (TableView) splitPane.getItems().get(0);
            TableColumn tb = (TableColumn) table.getColumns().get(0);
            tb.setPrefWidth(855);
            sp.getItems().remove(sp.getItems().get(1));

        });
    }

    private static Parent createSchedaWindow(CrawlerResult cr){

        String indirizzoUri = ottimizzaUri(cr.uri);

        Exception e = cr.exc;
        boolean rule = cr.linkPage;
        int errSize=0;
        int linksSize=0;
        if(rule){
            if(cr.errRawLinks!=null) errSize = cr.errRawLinks.size();
            if (cr.links!= null) linksSize = cr.links.size();
        }


        Text uriLbl = new Text("Uri: " + indirizzoUri);

        Text linksSizeLbl = new Text("Link scaricati: " + linksSize);
        Text errSizeLbl = new Text("Link falliti: " + errSize);

        String exc = "null";
        if(e != null) exc = e.toString();
        Text excLbl = new Text("Eccezione: " + exc);

        String appartenenza = "No";
        if (rule == true) appartenenza = "Si";
        Text ruleLbl = new Text("Appartiene al dominio: " + appartenenza);

        return new VBox(uriLbl,linksSizeLbl,errSizeLbl, excLbl, ruleLbl, creaGrafico());
    }

    private static PieChart creaGrafico(){
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                        new PieChart.Data("Grapefruit", 13),
                        new PieChart.Data("Oranges", 25),
                        new PieChart.Data("Plums", 10),
                        new PieChart.Data("Pears", 22),
                        new PieChart.Data("Apples", 30));
        final PieChart chart = new PieChart(pieChartData);
        chart.setTitle("Statistiche");
        return chart;
    }

    private static String ottimizzaUri(URI uri){
        String uriFinale = uri.toString();
        if(uriFinale.length()>63){
            uriFinale = uriFinale.substring(0,62) + "...";
        }
        return uriFinale;
    }


}