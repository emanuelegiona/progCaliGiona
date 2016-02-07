package src.wsa.gui;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import src.wsa.web.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

public class Main extends Application {
    static volatile Crawler c;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Parent root = createScene();
        Scene scene = new Scene(root, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Parent createScene(){
        c=WebFactory.getCrawler(new HashSet<>(),new HashSet<>(),new HashSet<>(),null);
        Text txt = new Text("This is a text");
        Button loader = new Button("Loader");
        Button async = new Button("AsyncLoader");
        Button crawlerStartbtn=new Button("Crawler start");
        Button crawlerSuspendbtn=new Button("Crawler suspend");
        Button risultatobtn=new Button("estrai");
        Button siteCrawler = new Button("dominio");
        Button test = new Button("test");

        loader.setOnAction(v -> loaderSimple());
        async.setOnAction(v -> asyncLoader());
        crawlerStartbtn.setOnAction(v -> crawlerStart());
        crawlerSuspendbtn.setOnAction(v->crawlerSuspend());
        risultatobtn.setOnAction(v->risultato());
        siteCrawler.setOnAction(v -> {
            try {
                siteCrawler();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        test.setOnAction(e -> {
            System.out.println(Thread.activeCount());
        });

        VBox vb = new VBox(txt, loader, async, crawlerStartbtn, crawlerSuspendbtn, test,risultatobtn, siteCrawler);
        vb.setAlignment(Pos.CENTER);
        vb.setSpacing(30);
        return vb;
    }

    private void siteCrawler() throws URISyntaxException, IOException {
        //SiteCrawler site = WebFactory.getSiteCrawler(new URI("https://www.google.it/"), null);
        System.out.println(SiteCrawler.checkDomain(new URI("https://www.youtube.com/watch?v=vtocSMxzEdM")));
        System.out.println(SiteCrawler.checkSeed(new URI("https://www.google.it/"), new URI("https://www.google.it/doodles")));
    }

    public static void loaderSimple(){
        SimpleLoader simpleLoader = new SimpleLoader();
        URL[] url=new URL[1];
        try {
            url[0]=new URL("file:///C:/Users/User/Desktop/hw3_files/testAsync/pages/object.html");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        new Thread(()-> {
            long t=System.currentTimeMillis();
            LoadResult r = simpleLoader.load(url[0]);
            System.out.println(r.url+"|"+r.parsed.getLinks().size());
            System.out.println("Tempo: "+(System.currentTimeMillis()-t));
        }).start();
    }

    public static void asyncLoader(){



        new Thread(()-> {
            AsyncLoader al = WebFactory.getAsyncLoader();
            List<Future<LoadResult>> list = new ArrayList<>();

            try {

                URL url1 = new URL("file:///C:/Users/User/Desktop/hw3_files/testAsync/pages/di.html");
                URL url2 = new URL("file:///C:/Users/User/Desktop/hw3_files/testAsync/pages/di2.html");
                URL url3 = new URL("file:///C:/Users/User/Desktop/hw3_files/testAsync/pages/lezione01.html");
                URL url4 = new URL("file:///C:/Users/User/Desktop/hw3_files/testAsync/pages/lezione14.html");
                URL url5 = new URL("file:///C:/Users/User/Desktop/hw3_files/testAsync/pages/object.html");


                URL[] urls = {url1,url2,url3,url4,url5};
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
                c.add(new URI("http://www.google.it"));
                c.add(new URI("https://github.com/emanuelegiona/wsa/blob/master/web/SimpleAsyncLoader.java"));
                c.add(new URI("https://www.facebook.com/"));
                c.add(new URI("http://pellacini.di.uniroma1.it/teaching/fondamenti14/"));
                c.add(new URI("http://twiki.di.uniroma1.it/pub/Metod_prog/RS_INFO/info.html"));
                c.add(new URI("https://python.org"));
                c.add(new URI("http://youtube.com"));

                System.out.println("Tento di aggiungere il link");
                c.add(new URI("http://google.it"));
                c.add(new URI("htpp:goodle.o"));
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

                System.out.println("ready");

                //System.out.println("Tento di aggiungere il link");
                //c.add(new URI("http://google.it"));

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void risultato(){
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
}