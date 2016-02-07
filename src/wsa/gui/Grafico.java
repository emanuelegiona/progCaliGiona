package src.wsa.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import src.wsa.web.SiteCrawler;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by User on 06/02/2016.
 */
public class Grafico {


    private static Stage stage = new Stage();


    public static void  showGraphic(Stage primaryStage, int ID){
        stage.setTitle("Grafico");

        try{
            stage.initOwner(primaryStage);
            stage.setAlwaysOnTop(false);
        }catch (Exception e){}

        Scene finestra = new Scene(createGraphic(ID, 0), 500, 500);

        stage.setScene(finestra);
        stage.show();
    }

    private static Parent createGraphic(int ID, int numeroSeeds){
        Object[] objects=MainGUI.activeCrawlers.get(ID);

        HashMap<URI,Integer[]> stats=(HashMap<URI,Integer[]>)objects[3];
        final int[] in={0,0,0,0,0};
        final int[] out={0,0,0,0,0};
        stats.forEach((u,i)->{
            int val=i[0];
            if(val<5)
                in[0]++;
            else if(val<10)
                in[1]++;
            else if(val<25)
                in[2]++;
            else if(val<50)
                in[3]++;
            else
                in[4]++;

            val=i[1];
            if(val<5)
                out[0]++;
            else if(val<10)
                out[1]++;
            else if(val<25)
                out[2]++;
            else if(val<50)
                out[3]++;
            else
                out[4]++;
        });

        for(int i=0;i<in.length;i++){
            System.out.println(i+": "+in[i]);
        }

        HashMap<String,Integer> inData=new HashMap<>();
        for(int i=0;i<in.length;i++){
            String s="";
            switch (i){
                case 0: s="<5";
                    break;
                case 1: s="<10";
                    break;
                case 2: s="<25";
                    break;
                case 3: s="<50";
                    break;
                case 4: s=">=50";
                    break;
            }
            inData.put(s,i);
        }
        HashMap<String,Integer> outData=new HashMap<>();
        for(int i=0;i<out.length;i++){
            String s="";
            switch (i){
                case 0: s="<5";
                    break;
                case 1: s="<10";
                    break;
                case 2: s="<25";
                    break;
                case 3: s="<50";
                    break;
                case 4: s=">=50";
                    break;
            }
            outData.put(s,i);
        }


        Text titolo = new Text("Dominio: " + objects[4]);
        Text nSeeds = new Text("Numero seeds iniziali: " + numeroSeeds);

        HBox grafici = WindowsManager.createHBox(20,0,
                Pos.CENTER,
                null,
                creaGrafico("Link Entranti",inData),
                creaGrafico("Link Uscenti",outData));

        LocalDateTime t=LocalDateTime.now();
        Text data=new Text("Ultimo aggiornamento: "+t.getDayOfMonth()+"/"+t.getMonth()+"/"+t.getYear()+" alle "+t.getHour()+":"+t.getMinute());
        return new VBox(titolo, nSeeds, grafici,data);
    }


    private static PieChart creaGrafico(String title,Map<String,Integer> vals){
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        vals.forEach((k,v)->{
            //System.out.println(k+"|"+v);
            pieChartData.add(new PieChart.Data(k,v));});
        final PieChart chart = new PieChart(pieChartData);
        chart.setTitle(title);
        chart.setLabelLineLength(10);
        chart.setLegendVisible(false);
        return chart;
    }

}
