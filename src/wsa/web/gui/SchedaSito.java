package src.wsa.web.gui;


import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;


/**
 * Created by User on 07/01/2016.
 */
public class SchedaSito {


    public static void showSchedaSito(SplitPane sp){

        BorderPane bp = new BorderPane();
        Text x = new Text("X");


        HBox hb = new HBox(new Text("Statistiche sito                                                                                          "), x);
        hb.setPrefWidth(200);

        bp.setTop(new ToolBar(hb));
        bp.setStyle("-fx-background-color: #FFFFFF");

        StackPane st = (StackPane) sp.getItems().get(1);
        st.getChildren().add(bp);


        x.setOnMouseClicked(e -> {
            SplitPane splitPane = (SplitPane) sp.getItems().get(0);
            TableView table = (TableView) splitPane.getItems().get(0);
            TableColumn tb = (TableColumn) table.getColumns().get(0);
            tb.setPrefWidth(848);
            sp.getItems().remove(sp.getItems().get(1));

        });


    }

    private static Parent createSchedaWindow(Stage primaryStage){
        return new VBox();
    }


}