package src.wsa.web;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import src.wsa.web.html.Parsing;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CountDownLatch;

public class SimpleLoader implements Loader{
    private volatile WebEngine engine;
    private volatile Exception ex;
    private volatile Parsing parsed=null;
    private volatile CountDownLatch latch;

    public SimpleLoader(){
        engine = new WebEngine();

        //listner della WebEngine
        Platform.runLater(() -> {
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
                    //done=true;
                    ex = new Exception("Errore durante il download");

                } else if (nv == Worker.State.CANCELLED) {
                    //done = true;
                    ex = new Exception("Download annullato");
                }
            });
        });
    }

    /**
     * Ritorna il risultato del tentativo di scaricare la pagina specificata. È
     * bloccante, finchè l'operazione non è conclusa non ritorna.
     *
     * @param url l'URL di una pagina web
     * @return il risultato del tentativo di scaricare la pagina
     */
    @Override
    public LoadResult load(URL url) {
        latch=new CountDownLatch(1);
        Platform.runLater(() -> {
            engine.load("");
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
        }

        latch=new CountDownLatch(1);
        Platform.runLater(() -> {
            engine.load(url.toString());
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
        }

        return new LoadResult(url, parsed, ex);
    }

    /**
     * Ritorna null se l'URL è scaricabile senza errori, altrimenti ritorna
     * un'eccezione che riporta l'errore.
     *
     * @param url un URL
     * @return null se l'URL è scaricabile senza errori, altrimenti
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