package src.wsa.gui;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import src.wsa.web.LoadResult;
import src.wsa.web.Loader;
import src.wsa.web.WebFactory;

import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;

public class Avanzate {
    private static Stage stage=new Stage();
    private static Loader loader=WebFactory.getLoader();

    public static void showAvanzate(Stage primaryStage, URI uri, int ID){
        stage.setTitle("Avanzate");

        try{
            stage.initOwner(primaryStage);
            stage.setAlwaysOnTop(false);
        }catch(Exception e){}

        Scene finestra = new Scene(createAdv(uri,ID), 300, 150);

        stage.setScene(finestra);
        stage.show();
        stage.setMinHeight(150);
        stage.setMinWidth(300);
    }

    private static Parent createAdv(URI uri,int ID){
        Text nImm=new Text("Calcolo in corso...");
        nImm.setFont(Font.font(17));

        VBox vb=WindowsManager.createVBox(25,10,null,null,new TextFlow(nImm));

        Text nNodes=new Text("");
        nNodes.setFont(nImm.getFont());
        final LoadResult[] res = {null};
        Thread t=new Thread(()-> {
            try {
                Integer[] nums=(Integer[])MainGUI.getStats().get(uri);

                if(nums[2]==-1 && nums[3]==-1) {
                    res[0] = loader.load(uri.toURL());
                    while (res[0].exc == null && res[0].parsed == null) ;

                    if (res[0].exc == null) {
                        int numImm = res[0].parsed.getByTag("img").size();
                        final int[] numNodes = {0};
                        res[0].parsed.visit(n -> numNodes[0]++);

                        nImm.setText("Numero immagini: " + numImm);
                        nNodes.setText("Numero nodi nell'albero: " + numNodes[0]);

                        nums[2]=numImm;
                        nums[3]=numNodes[0];
                        Object[] objects=MainGUI.activeCrawlers.get(ID);
                        Map<URI,Integer[]> stats=(Map<URI,Integer[]>)objects[3];
                        stats.put(uri,nums);
                        MainGUI.activeCrawlers.put(ID,objects);
                    } else {
                        nImm.setText("Errore durante il download:\n" + res[0].exc);
                        nNodes.setText("");
                    }
                }
                else{
                    nImm.setText("Numero immagini: " + nums[2]);
                    nNodes.setText("Numero nodi nell'albero: " + nums[3]);
                }

                Platform.runLater(() -> vb.getChildren().addAll(nImm, (nNodes.getText()==""?null:nNodes)));

                Thread.currentThread().interrupt();
            } catch (MalformedURLException e) {
                Alert alert = WindowsManager.creaAlert(Alert.AlertType.ERROR, "Errore", "URL non valida");
                alert.showAndWait();

                Thread.currentThread().interrupt();
                stage.close();
            }
        });
        t.setDaemon(true);
        t.start();

        return vb;
    }
}
