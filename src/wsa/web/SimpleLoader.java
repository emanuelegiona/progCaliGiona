package src.wsa.web;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;

import src.wsa.web.html.Parsing;

import java.net.URL;
import java.net.URLConnection;

import java.util.concurrent.CountDownLatch;

/** Scarica una pagina in modo sincrono facendo uso di un oggetto WebEngine; implementa Loader*/
public class SimpleLoader implements Loader{
    private volatile WebEngine engine;
    private volatile Exception ex;
    private volatile Parsing parsed;
    private volatile CountDownLatch latch;

    /** Costruttore di SimpleLoader.
     * Inizializza la WebEngine, che ignora il codice JavaScript, e ne imposta il listener.*/
    public SimpleLoader(){
        Platform.runLater(() -> {
            engine = new WebEngine();
            engine.setJavaScriptEnabled(false);

            engine.getLoadWorker().stateProperty().addListener((o, ov, nv) -> {
                if (nv == Worker.State.SUCCEEDED) {
                    if (engine.getDocument() != null) {
                        try{
                            parsed=new Parsing(engine.getDocument());
                        }catch (Exception e){
                            ex = e;
                        }
                    }
                    latch.countDown();
                    ex = null;

                } else if (nv == Worker.State.FAILED) {
                    ex = new Exception("Errore durante il download");
                    latch.countDown();

                } else if (nv == Worker.State.CANCELLED) {
                    ex = new Exception("Download annullato");
                    latch.countDown();
                }
            });
        });
    }

    /**
     * Ritorna il risultato del tentativo di scaricare la pagina specificata. e'
     * bloccante, finche' l'operazione non e' conclusa non ritorna.
     *
     * @param url l'URL di una pagina web
     * @return il risultato del tentativo di scaricare la pagina
     */
    @Override
    public LoadResult load(URL url) {
        latch=new CountDownLatch(1);
        Platform.runLater(() -> engine.load(""));

        try {
            latch.await();
        } catch (InterruptedException e) {
            ex = new Exception("Interrotto");
        }

        latch=new CountDownLatch(1);
        Platform.runLater(() -> engine.load(url.toString()));
        try {
            latch.await();
        } catch (InterruptedException e) {
            ex = new Exception("Interrotto");
        }

        return new LoadResult(url, parsed, ex);
    }

    /**
     * Ritorna null se l'URL e' scaricabile senza errori, altrimenti ritorna
     * un'eccezione che riporta l'errore.
     *
     * @param url un URL
     * @return null se l'URL e' scaricabile senza errori, altrimenti
     * l'eccezione
     */
    @Override
    public Exception check(URL url) {
        Exception ex=null;
        try{
            URLConnection conn=url.openConnection();
            conn.setRequestProperty("User-Agent","Mozilla/5.0");
            conn.setRequestProperty("Accept", "text/html;q=1.0,*;q=0");
            conn.setRequestProperty("Accept-Encoding", "identity;q=1.0,*;q=0");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            conn.connect();
        }catch(Exception e){
            ex=e;
        }
        return ex;
    }
}