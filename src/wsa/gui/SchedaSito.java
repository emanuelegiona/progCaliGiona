package src.wsa.gui;


import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by User on 07/01/2016.
 */
public class SchedaSito {

    private static Window primaryWindow;


    public static void showSchedaSito(SplitPane sp, Stage primaryStage){
        primaryWindow  = sp.getScene().getWindow();


        BorderPane bp = new BorderPane();
        Text x = new Text("X");


        HBox hb = new HBox(new Text("Statistiche sito                                                                                          "), x);
        hb.setPrefWidth(200);

        bp.setTop(new ToolBar(hb));
        bp.setStyle("-fx-background-color: #FFFFFF");

        StackPane st = (StackPane) sp.getItems().get(1);
        st.getChildren().add(bp);

        Button apriWebView = WindowsManager.createButton("Apri sito", 100,50,null,false);
        bp.setCenter(apriWebView);

        apriWebView.setOnAction(e -> {
            URL url = null;
            try {
                url = new URL("http://www.google.it");
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            }
            WebWindow.showWebWindow(primaryStage, url);
        });


        x.setOnMouseClicked(e -> {
            SplitPane splitPane = (SplitPane) sp.getItems().get(0);
            TableView table = (TableView) splitPane.getItems().get(0);
            TableColumn tb = (TableColumn) table.getColumns().get(0);
            tb.setPrefWidth(855);
            sp.getItems().remove(sp.getItems().get(1));

        });


    }

    private static Parent createSchedaWindow(Stage primaryStage){
        return new VBox();
    }


}