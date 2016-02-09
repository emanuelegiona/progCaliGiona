package src.wsa.gui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
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

/** Sezione relativa alle statistiche di un sito selezionato*/
public class SchedaSito {
    private static Window primaryWindow;
    private static CrawlerResult risultato;
    private static SiteCrawler siteCrawler;

    /** Crea la finestra delle statistiche del sito selezionato
     * @param sp lo SplitPane corrente
     * @param primaryStage il Main stage
     * @param uri il sito selezionato*/
    public static void showSchedaSito(SplitPane sp, Stage primaryStage, URI uri){
        primaryWindow  = sp.getScene().getWindow();
        siteCrawler = Main.getSiteCrawler();

        if (siteCrawler.get(uri) != null){
            risultato = siteCrawler.get(uri);

            Text statistiche = new Text("Statistiche sito");
            Text x = new Text("X");
            Pane p = new Pane();
            HBox.setHgrow(p, Priority.ALWAYS);

            BorderPane bp = new BorderPane();
            StackPane st = (StackPane) sp.getItems().get(1);
            st.getChildren().add(bp);

            ToolBar toolBar = new ToolBar(statistiche,p,x);
            bp.setTop(toolBar);
            bp.setCenter(createSchedaWindow(risultato));
            bp.setBottom(pie(primaryStage, risultato));
            bp.setStyle("-fx-background-color: #FFFFFF");

            x.setOnMouseClicked(e -> {
                SplitPane splitPane = (SplitPane) sp.getItems().get(0);
                TableView table = (TableView) splitPane.getItems().get(0);
                TableColumn links = (TableColumn) table.getColumns().get(0);
                TableColumn tb = (TableColumn) table.getColumns().get(1);

                links.prefWidthProperty().bind(table.widthProperty().multiply(0.875));
                tb.prefWidthProperty().bind(table.widthProperty().multiply(0.125));
                sp.getItems().remove(sp.getItems().get(1));
            });
        }
    }

    /** Crea un layout orizzontale per i bottoni in fondo all scheda
     * @param primaryStage il Main stage
     * @param risultato il risultato relativo al sito selezionato
     * @return il layout*/
    private static HBox pie(Stage primaryStage, CrawlerResult risultato){
        Button apriSito = WindowsManager.createButton("Apri sito", 100,50,null,false);
        apriSito.setOnAction(e -> WebWindow.showWebWindow(primaryStage, risultato.uri));

        Button apriLinks = WindowsManager.createButton("Mostra Link", 100,50, null, false);
        apriLinks.setOnAction(e -> {
            if (risultato.links==null && risultato.errRawLinks == null){
                Alert alert=WindowsManager.creaAlert(Alert.AlertType.INFORMATION,"Informazione","Non ci sono link da visualizzare");
                alert.showAndWait();
            }
            else
                Links.showLinksWindow(primaryStage, risultato);
        });

        Button avanzate=WindowsManager.createButton("Avanzate",100,50,null,false);
        int ID= Main.tabCrawlers.get(Main.tabPane.getSelectionModel().getSelectedItem());
        avanzate.setOnAction(e->Avanzate.showAvanzate(primaryStage,risultato.uri,ID));

        return WindowsManager.createHBox(10,5,null,null,apriSito,apriLinks,avanzate);
    }

    /** Crea il contenuto della scheda del sito selezionato
     * @param cr il risultato del sito selezionato
     * @return il contenuto della scheda*/
    private static Parent createSchedaWindow(CrawlerResult cr){
        String indirizzoUri = ottimizzaUri(cr.uri);
        Exception e = cr.exc;
        boolean rule = cr.linkPage;
        int errSize=0;
        int linksSize=0;

        if(rule){
            if(cr.errRawLinks!=null)
                errSize = cr.errRawLinks.size();
            if (cr.links!= null)
                linksSize = cr.links.size();
        }

        Text uriLbl = new Text("Uri: " + indirizzoUri);
        uriLbl.setFont(Font.font(14));
        final Tooltip tooltip = new Tooltip(cr.uri.toString());
        Tooltip.install(uriLbl, tooltip);

        String appartenenza = "No";
        if (rule)
            appartenenza = "Si";
        Text ruleLbl = new Text("Appartiene al dominio: " + appartenenza);
        ruleLbl.setFont(uriLbl.getFont());

        VBox vb = new VBox(uriLbl,ruleLbl);

        Text linksSizeLbl = new Text("Link scaricati: " + linksSize);
        linksSizeLbl.setFont(uriLbl.getFont());

        Text errSizeLbl = new Text("Link falliti: " + errSize);
        errSizeLbl.setFont(uriLbl.getFont());

        if(appartenenza.equals("Si"))
            vb.getChildren().addAll(linksSizeLbl,errSizeLbl);

        String exc = "null";
        if(e != null)
            exc = e.getMessage();
        Text excLbl = new Text("Eccezione: " + exc);
        excLbl.setFont(uriLbl.getFont());

        if(!exc.equals("null"))
            vb.getChildren().add(excLbl);

        if(e==null) {
            Text conta1 = new Text("Numero link verso questa pagina: " + Main.getStats().get(cr.uri)[0]);
            conta1.setFont(uriLbl.getFont());
            vb.getChildren().add(conta1);

            Text conta2 = new Text("Numero link a pagine fuori dal dominio: " + Main.getStats().get(cr.uri)[1]);
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

    /** Tronca l'URI per evitare una visualizzazione non ottimale
     * @param uri l'URI da controllare
     * @return l'URI troncata, eventualmente*/
    private static String ottimizzaUri(URI uri){
        String uriFinale = uri.toString();
        if(uriFinale.length()>48)
            uriFinale = uriFinale.substring(0,47) + "...";

        return uriFinale;
    }
}