package src.wsa.gui;

import javafx.application.Platform;
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
import java.net.URI;
import java.util.Map;

/** Mostra informazioni avanzate riguardo il sito aperto, come numero di immagini e numero di nodi nell'albero di parsing.*/
public class Avanzate {
    private static Stage stage=new Stage();
    private static Loader loader=WebFactory.getLoader();

    /** Crea e mostra la finestra
     * @param primaryStage il Main stage
     * @param uri l'URI alla quale si riferisce
     * @param ID l'identificatore dell'esplorazione alla quale appartiene*/
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

    /** Genera il contenuto della finestra
     * @param uri l'URI alla quale si riferisce
     * @param ID l'identificatore dell'esplorazione alla quale appartiene
     * @return il contenuto della finestra
     */
    private static VBox createAdv(URI uri,int ID){
        Text nImm=new Text("Calcolo in corso...");
        Text nNodes=new Text("");

        nImm.setFont(Font.font(17));
        nNodes.setFont(nImm.getFont());

        VBox vb=WindowsManager.createVBox(25,10,null,null,new TextFlow(nImm));

        final LoadResult[] res = {null};
        Thread t=new Thread(()-> {
            try {
                Integer[] nums=(Integer[]) Main.getStats().get(uri);

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

                        Object[] objects= Main.activeCrawlers.get(ID);
                        Map<URI,Integer[]> stats=(Map<URI,Integer[]>)objects[3];
                        stats.put(uri,nums);
                        Main.activeCrawlers.put(ID,objects);
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
            } catch (Exception e) {
                Platform.runLater(()-> {
                    Alert alert = WindowsManager.creaAlert(Alert.AlertType.ERROR, "Errore", "URL non valida");
                    alert.showAndWait();
                    stage.close();
                });
                Thread.currentThread().interrupt();
            }
        });
        t.setDaemon(true);
        t.start();

        return vb;
    }
}
