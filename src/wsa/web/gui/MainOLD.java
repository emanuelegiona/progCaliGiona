package src.wsa.web.gui;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import src.wsa.web.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

public class MainOLD extends Application {
    static volatile Crawler c;
    static volatile SiteCrawler sc;

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

    private Parent createScene() {
        c=WebFactory.getCrawler(new HashSet<>(),new HashSet<>(),new HashSet<>(),null);
        Text txt = new Text("This is a text");
        Button loader = new Button("Loader");
        Button async = new Button("AsyncLoader");
        Button crawlerStartbtn=new Button("Crawler start");
        Button crawlerSuspendbtn=new Button("Crawler suspend");
        Button risultatobtn=new Button("estrai");
        Button checkdmn=new Button("dominio");

        loader.setOnAction(v -> loaderSimple());
        async.setOnAction(v -> asyncLoader());
        crawlerStartbtn.setOnAction(v -> crawlerStart());
        crawlerSuspendbtn.setOnAction(v->crawlerSuspend());
        risultatobtn.setOnAction(v->risultato());
        checkdmn.setOnAction(v->dominio());

        VBox vb = new VBox(txt, loader, async, crawlerStartbtn, crawlerSuspendbtn, risultatobtn, checkdmn);
        vb.setAlignment(Pos.CENTER);
        vb.setSpacing(30);
        return vb;
    }

    public static void loaderSimple(){
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

    public void dominio(){
        try {
            System.out.println(LocalDateTime.now().toString().replace(":", "h").replace(".", "_"));

            System.out.println(SiteCrawler.checkDomain(new URI("http://www8.hp.com/it/it/home.html")));
            System.out.println(SiteCrawler.checkSeed(new URI("http://google.it/"),new URI("http://google.it/doodles")));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}