package src.wsa.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import src.wsa.web.CrawlerResult;
import src.wsa.web.SiteCrawler;
import java.net.URI;
import java.time.LocalDateTime;

public class SchedaSito {
    private static Window primaryWindow;
    private static CrawlerResult risultato;
    private static SiteCrawler siteCrawler;

    public static void showSchedaSito(SplitPane sp, Stage primaryStage, URI uri){
        primaryWindow  = sp.getScene().getWindow();
        siteCrawler = DirectoryWindow.getSiteCrawler();
        if(siteCrawler == null) siteCrawler = MainGUI.getSiteCrawler();
        if (siteCrawler.get(uri) != null){
            risultato = siteCrawler.get(uri);

            //modifica
            BorderPane bp = new BorderPane();
            Text statistiche = new Text("Statistiche sito");
            Text x = new Text("X");
            Pane p = new Pane();
            HBox hb = new HBox(statistiche,p, x);
            HBox.setHgrow(p, Priority.ALWAYS);
            Tooltip.install(hb, new Tooltip("HBOX"));
            hb.setPrefWidth(200);
            bp.setStyle("-fx-background-color: #FFFFFF");

            StackPane st = (StackPane) sp.getItems().get(1);
            st.getChildren().add(bp);

            ToolBar toolBar = new ToolBar(statistiche,p,x);

            bp.setTop(toolBar);
            bp.setCenter(createSchedaWindow(risultato));
            bp.setBottom(pie(primaryStage, risultato));

            x.setOnMouseClicked(e -> {
                SplitPane splitPane = (SplitPane) sp.getItems().get(0);
                TableView table = (TableView) splitPane.getItems().get(0);
                TableColumn links = (TableColumn) table.getColumns().get(0);
                TableColumn tb = (TableColumn) table.getColumns().get(1);
                //links.prefWidthProperty().bind(MainGUI.griglia.widthProperty().multiply(0.875));
                //tb.prefWidthProperty().bind(MainGUI.griglia.widthProperty().multiply(0.125));
                links.prefWidthProperty().bind(table.widthProperty().multiply(0.875));
                tb.prefWidthProperty().bind(table.widthProperty().multiply(0.125));
                sp.getItems().remove(sp.getItems().get(1));
            });
        }
    }

    private static HBox pie(Stage primaryStage, CrawlerResult risultato){
        Button apriSito = WindowsManager.createButton("Apri sito", 100,50,null,false);
        apriSito.setOnAction(e -> WebWindow.showWebWindow(primaryStage, risultato.uri));

        Button apriLinks = WindowsManager.createButton("Mostra Links", 100,50, null, false);
        apriLinks.setOnAction(e -> {
            if (risultato.links==null && risultato.errRawLinks == null){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Informazione");
                alert.setHeaderText(null);
                alert.setContentText("\n       Non ci sono link da visualizzare");
                alert.showAndWait();
            }else{
                Links.showLinksWindow(primaryStage, risultato);
            }
        });

        return WindowsManager.createHBox(10,5,null,null,apriSito,apriLinks);
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
        uriLbl.setFont(Font.font(14));
        final Tooltip tooltip = new Tooltip(cr.uri.toString());
        Tooltip.install(uriLbl, tooltip);

        String appartenenza = "No";
        if (rule == true) appartenenza = "Si";
        Text ruleLbl = new Text("Appartiene al dominio: " + appartenenza);
        ruleLbl.setFont(uriLbl.getFont());

        VBox vb = new VBox(uriLbl,ruleLbl);

        Text linksSizeLbl = new Text("Link scaricati: " + linksSize);
        linksSizeLbl.setFont(uriLbl.getFont());
        Text errSizeLbl = new Text("Link falliti: " + errSize);
        errSizeLbl.setFont(uriLbl.getFont());
        if(appartenenza.equals("Si")){
            vb.getChildren().addAll(linksSizeLbl,errSizeLbl);
        }

        String exc = "null";
        if(e != null) exc = e.getMessage();
        Text excLbl = new Text("Eccezione: " + exc);
        excLbl.setFont(uriLbl.getFont());

        if(!exc.equals("null")) vb.getChildren().add(excLbl);

        if(e==null) {
            Text conta1 = new Text("Numero link verso questa pagina: " + MainGUI.getStats().get(cr.uri)[0]);
            conta1.setFont(uriLbl.getFont());
            vb.getChildren().add(conta1);

            Text conta2 = new Text("Numero link a pagine fuori dal dominio: " + MainGUI.getStats().get(cr.uri)[1]);
            conta2.setFont(uriLbl.getFont());
            vb.getChildren().add(conta2);
        }

        LocalDateTime t=LocalDateTime.now();
        Text data=new Text("Ultimo aggiornamento: "+
                (t.getDayOfMonth()<10?"0"+t.getDayOfMonth():t.getDayOfMonth()) +
                "/"+t.getMonth()+"/"+t.getYear()+
                " alle "+
                (t.getHour()<10?"0"+t.getHour():t.getHour()) +
                ":"+
                (t.getMinute()<10?"0"+t.getMinute():t.getMinute()));
        data.setFont(uriLbl.getFont());
        vb.getChildren().add(data);

        vb.setSpacing(5);
        vb.setPadding(new Insets(5));

        return vb;
    }

    private static String ottimizzaUri(URI uri){
        String uriFinale = uri.toString();
        if(uriFinale.length()>48){
            uriFinale = uriFinale.substring(0,47) + "...";
        }
        return uriFinale;
    }

    public static SiteCrawler getSiteCrawler() {
        return siteCrawler;
    }
}
