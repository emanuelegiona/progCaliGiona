package src.wsa.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Grafico {
    private static Stage stage = new Stage();

    public static void  showGraphic(Stage primaryStage, int ID){
        stage.setTitle("Grafico");

        try{
            stage.initOwner(primaryStage);
            stage.setAlwaysOnTop(false);
        }catch (Exception e){}

        Scene finestra = new Scene(createGraphic(ID), 500, 500);

        stage.setScene(finestra);
        stage.show();
        stage.setMinHeight(500);
        stage.setMinWidth(500);
    }

    private static Parent createGraphic(int ID){
        Object[] objects=MainGUI.activeCrawlers.get(ID);

        ConcurrentHashMap<URI,Integer[]> stats=new ConcurrentHashMap((HashMap < URI, Integer[]>)objects[3]);
        HashMap<String,Integer> inData=new HashMap<>();
        HashMap<String,Integer> outData=new HashMap<>();

        for(int i=1;i<=10;i++){
            inData.put(("<"+(5*i)),0);
            outData.put(("<"+(5*i)),0);
        }
        inData.put("50+",0);
        outData.put("50+",0);

        stats.forEach((u,i)->{
            int val=i[0];
            for(int j=1;j<=10;j++){
                if(val<(5*j)){
                    inData.put(("<"+(5*j)),inData.get(("<"+(5*j)))+1);
                    break;
                }
            }
            if(val>=50)
                inData.put("50+",inData.get("50+")+1);

            val=i[1];
            for(int j=1;j<=10;j++){
                if(val<(5*j)){
                    outData.put(("<"+(5*j)),outData.get(("<"+(5*j)))+1);
                    break;
                }
            }
            if(val>=50)
                outData.put("50+",outData.get("50+")+1);
        });

        Text titolo = new Text("Dominio: " + objects[4]);
        titolo.setFont(Font.font(14));

        HBox grafici = WindowsManager.createHBox(20,0,
                Pos.CENTER,
                null,
                creaGrafico("Link Entranti",inData),
                creaGrafico("Link Uscenti",outData));

        LocalDateTime t=LocalDateTime.now();
        Text data=new Text("Ultimo aggiornamento: "+
                (t.getDayOfMonth()<10?"0"+t.getDayOfMonth():t.getDayOfMonth()) +
                "/"+t.getMonth()+"/"+t.getYear()+
                " alle "+
                (t.getHour()<10?"0"+t.getHour():t.getHour()) +
                ":"+
                (t.getMinute()<10?"0"+t.getMinute():t.getMinute()));
        data.setFont(titolo.getFont());

        VBox vb=WindowsManager.createVBox(25,10,null,titolo,grafici);
        vb.getChildren().add(data);
        return vb;
    }


    private static PieChart creaGrafico(String title,Map<String,Integer> vals){
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        vals.forEach((k,v)->{
            pieChartData.add(new PieChart.Data(k,v));
        });
        final PieChart chart = new PieChart(pieChartData);
        chart.setTitle(title);
        chart.setLabelLineLength(10);
        chart.setLegendVisible(true);
        chart.setLabelsVisible(false);
        return chart;
    }

}
