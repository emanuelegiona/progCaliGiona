package src.wsa.gui;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import src.wsa.web.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

public class MainOLD extends Application {
    static volatile Crawler c;
    static volatile SiteCrawler sc;
    static Parent root;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        /*Parent*/ root = createScene();
        Scene scene = new Scene(root, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Parent createScene() {
        c=WebFactory.getCrawler(new HashSet<>(),new HashSet<>(),new HashSet<>(),null);
        try {
            //sc=WebFactory.getSiteCrawler(new URI("http://www.google.it"),null);
            sc=WebFactory.getSiteCrawler(new URI("http://twiki.di.uniroma1.it/pub/Metod_prog/RS_L14/lezione14.html"),null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Text txt = new Text("This is a text");
        Button loader = new Button("stringhe");
        Button async = new Button("AsyncLoader");
        Button crawlerStartbtn=new Button("Crawler start");
        Button crawlerSuspendbtn=new Button("Crawler suspend");
        Button risultatobtn=new Button("estrai");
        Button sitecr=new Button("sitecrawler");
        Button salva=new Button("salva");
        Button apri=new Button("apri");

        loader.setOnAction(v -> loaderSimple());
        async.setOnAction(v -> asyncLoader());
        crawlerStartbtn.setOnAction(v -> crawlerStart());
        crawlerSuspendbtn.setOnAction(v->crawlerSuspend());
        risultatobtn.setOnAction(v->risultato());
        sitecr.setOnAction(v -> scr());
        salva.setOnAction(v->salva());
        apri.setOnAction(v->apri());

        VBox vb = new VBox(sitecr,salva,apri);
        vb.setAlignment(Pos.CENTER);
        vb.setSpacing(30);
        return vb;
    }

    public static void loaderSimple(){
        /*
        SimpleLoader simpleLoader = new SimpleLoader();
        URL[] url=new URL[1];
        try {
            url[0]=new URL("http://google.it");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        new Thread(()-> {
            long t=System.currentTimeMillis();
            LoadResult r = simpleLoader.load(url[0]);
            System.out.println(r.url+"|"+r.parsed.getLinks().size());
            System.out.println("Tempo: "+(System.currentTimeMillis()-t));
        }).start();
        */
        //String s="https://www.google.it/search?q=java 8 string";
        String s="http://www.google.it/intl/it/business/?gmbsrc=it-ww-et-gs-z-gmb-s-z-h~ser|sitemapb|u&ppsrc=GMBES&utm_campaign=it-ww-et-gs-z-gmb-s-z-h~ser|sitemapb|u&utm_source=gmb&utm_medium=et";

        String reserved="!*'();:@&=+$,/?#[] ";
        String s1=s.substring(0,s.indexOf("//")+2);
        s1=s1.replace(" ","");
        String s2=s.substring(s.indexOf("//")+2);
        System.out.println(s1+" | "+s2);
        String[] split=s2.split("/");
        String finale=s1;

        for(int i=0;i<split.length;i++) {
            String x=split[i];
            ArrayList<String> xParts=new ArrayList<>();

            int last=0;
            for(int j=0;j<x.length();j++){
                if(reserved.contains(Character.toString(x.charAt(j)))) {
                    String x1 = x.substring(last, j);
                    xParts.add(x1);
                    String x2 = "" + x.charAt(j);
                    xParts.add(x2);
                    last=j+1;
                }
            }
            x = x.substring(last);
            xParts.add(x);

            String[] xEncoded={};
            xEncoded=xParts.toArray(xEncoded);

            for(int j=0;j<xEncoded.length;j++){
                String xP=xEncoded[j];
                if(!reserved.contains(xP))
                    try {
                        xP=URLEncoder.encode(xP, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                if(xP.equals(" "))
                    xP="%20";
                xEncoded[j]=xP;
            }

            x=String.join("",xEncoded);
            split[i]=x;
        }

        finale+=String.join("/",split);
        System.out.println(finale);
    }

    public static void asyncLoader(){
        AsyncLoader al = WebFactory.getAsyncLoader();
        List<Future<LoadResult>> list = new ArrayList<>();

        new Thread(()-> {
            try {
                URL url1 = new URL("http://www.google.it");
                URL url2 = new URL("https://github.com/emanuelegiona/src.wsa/blob/master/web/SimpleAsyncLoader.java");
                URL url3 = new URL("https://www.facebook.com/");
                URL url4 = new URL("http://pellacini.di.uniroma1.it/teaching/fondamenti14/");
                URL url5 = new URL("http://twiki.di.uniroma1.it/pub/Metod_prog/RS_INFO/info.html");
                URL url6 = new URL("https://python.org");
                URL url7 = new URL("http://youtube.com");
                URL[] urls = {url1,url2,url3,url4,url5,url6,url7};
                //URL[] urls = {url1, url3};
                for (int i = 0; i < urls.length; i++) {
                    list.add(al.submit(urls[i]));
                    System.out.println("Aggiunto: " + urls[i]);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                for (Future<LoadResult> l : list) {
                    System.out.println("output: " + l.get().url + "|" + l.get().parsed.getLinks().size());
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }).start();
    }

    public static void crawlerStart(){
        new Thread(()-> {
            try {
                //c.add(new URI("http://www.google.it"));
                //c.add(new URI("https://github.com/emanuelegiona/src.wsa/blob/master/web/SimpleAsyncLoader.java"));
                //c.add(new URI("https://www.facebook.com/"));
                //c.add(new URI("http://pellacini.di.uniroma1.it/teaching/fondamenti14/"));
                //c.add(new URI("http://twiki.di.uniroma1.it/pub/Metod_prog/RS_INFO/info.html"));
                //c.add(new URI("https://python.org"));
                //c.add(new URI("http://youtube.com"));
                c.add(new URI("http://4chan.org"));

                //System.out.println("Tento di aggiungere il link");
                //c.add(new URI("http://google.it"));
                //c.add(new URI("htpp:goodle.o"));
                c.start();

                /*
                System.out.println("------------------------");
                System.out.println("Scaricati:");
                c.getLoaded().forEach(System.out::println);
                System.out.println("------------------------");
                System.out.println("Da scaricare:");
                c.getToLoad().forEach(System.out::println);
                System.out.println("------------------------");
                System.out.println("Errori:");
                c.getErrors().forEach(System.out::println);
                System.out.println("------------------------");
                */

                //System.out.println("ready");

                //System.out.println("Tento di aggiungere il link");
                //c.add(new URI("http://google.it"));

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }).start();

        /*
        new Thread(()->{
            while(true){
                risultato();
            }
        }).start();
        */
    }

    public static void risultato(){
        Optional<CrawlerResult> res;
        res = c.get();
        if(res.isPresent()) {
            CrawlerResult cr = res.get();
            System.out.println(cr.uri + " || " + (cr.links==null?null:cr.links.size()) + " || " + cr.exc);
        }
    }

    public void crawlerSuspend(){
        c.suspend();
    }

    public void scr(){
        sc.start();
        new Thread(()->{
            while(true) {
                Optional<CrawlerResult> t = sc.get();
                if (t.isPresent()) {
                    CrawlerResult cr = t.get();
                    System.out.println(cr.uri + " || " + cr.linkPage + "||" + (cr.links == null ? null : cr.links.size()) + " || " + cr.exc);
                }
            }
        }).start();
    }

    public void salva(){
        URI dom=null;
        try {
            dom = new URI("http://www.google.it");
        }catch(URISyntaxException e){}

        Stage stage=new Stage();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        directoryChooser.setTitle("Scegli Directory");

        String dir="";
        if (selectedDirectory != null) {
            dir = selectedDirectory.toString();
        }
        String savePath = dir + "\\" + dom.getAuthority()+"h"+ LocalDateTime.now().toString().replace(":", "m").replace(".", "_") +".cg";
        System.out.println(savePath);
        Object[] o={"ciao",true,5};
        try {
            WindowsManager.salvaArchivio(dom, savePath, o);
        }catch(Exception e){}
    }

    public void apri(){
        Stage stage=new Stage();
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(stage);
        fileChooser.setTitle("Scegli Archivio");

        String dir="";
        String fileName="";
        if (selectedFile != null) {
            dir = selectedFile.toString();
            fileName=selectedFile.getName();
        }

        String s=null;
        boolean b=false;
        int val=0;
        try {
            fileName=fileName.substring(0,fileName.lastIndexOf("h"));
            URI u=new URI(fileName);
            Object[] array=WindowsManager.apriArchivio(dir);
            s=(String)array[0];
            b=(boolean)array[1];
            val=(int)array[2];
        } catch (Exception e) {
            System.out.println(e.getClass()==IOException.class);
        }

        System.out.println(s+"|"+b+"|"+val);
    }
}