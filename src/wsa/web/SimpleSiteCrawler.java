package src.wsa.web;

import src.wsa.web.gui.WindowsManager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

public class SimpleSiteCrawler implements SiteCrawler{
    private volatile URI dom;
    private final Path dir;
    private volatile Set<URI> succDownload;
    private volatile Set<URI> toDownload;
    private volatile Set<URI> failDownload;
    private final Crawler crawler;
    private final Predicate<URI> pageLink;
    private volatile ConcurrentLinkedQueue<CrawlerResult> results;
    private String savePath;
    private Thread crawlingThread;

    //TODO: aggiungere merda alla tableview con:
    //Platform.runLater(() -> Main.data.add(new UriTableView(uri)));

    public SimpleSiteCrawler(URI dom, Path dir) throws IllegalArgumentException,IOException{
        this.dom=dom;
        this.dir=dir;

        savePath = this.dir + "\\" + dom.getAuthority()+"h"+LocalDateTime.now().toString().replace(":", "m").replace(".", "_") +".cg";
        if(dom!=null && dir==null) {
            succDownload = new HashSet<>();
            toDownload = new HashSet<>();
            failDownload = new HashSet<>();
            results=new ConcurrentLinkedQueue<>();
        }

        if(dir!=null) {
            update();
        }

        if(dom==null && dir!=null){
            String s=dir.toString();
            s=s.substring(0,s.lastIndexOf("h")+1);
            try {
                URI u=new URI(s);
                Object[] array=WindowsManager.apriArchivio(this.dir.toString());

                this.dom=(URI)array[0];
                succDownload=(Set<URI>)array[1];
                toDownload=(Set<URI>)array[2];
                failDownload=(Set<URI>)array[3];
                results=(ConcurrentLinkedQueue<CrawlerResult>)array[4];
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("ERRORE: il file non contiene un archivio.");
            }
        }

        pageLink=(URI u)->SiteCrawler.checkSeed(this.dom,u);
        crawler=WebFactory.getCrawler(succDownload,toDownload,failDownload,pageLink);
    }

    /**
     * Aggiunge un seed URI. Se però è presente tra quelli già scaricati,
     * quelli ancora da scaricare o quelli che sono andati in errore,
     * l'aggiunta non ha nessun effetto. Se invece è un nuovo URI, è aggiunto
     * all'insieme di quelli da scaricare.
     *
     * @param uri un URI
     * @throws IllegalArgumentException se uri non appartiene al dominio di
     *                                  questo SuteCrawlerrawler
     * @throws IllegalStateException    se il SiteCrawler è cancellato
     */
    @Override
    public void addSeed(URI uri) throws IllegalArgumentException,IllegalStateException{
        if(isCancelled())
            throw new IllegalStateException();
        if(!SiteCrawler.checkSeed(dom,uri))
            throw new IllegalArgumentException();
        toDownload.add(uri);
        crawler.add(uri);
    }

    /**
     * Inizia l'esecuzione del SiteCrawler se non è già in esecuzione e ci sono
     * URI da scaricare, altrimenti l'invocazione è ignorata. Quando è in
     * esecuzione il metodo isRunning ritorna true.
     *
     * @throws IllegalStateException se il SiteCrawler è cancellato
     */
    @Override
    public void start() throws IllegalStateException{
        if(isCancelled())
            throw new IllegalStateException();

        addSeed(dom);
        if(!isRunning() && !toDownload.isEmpty()){
            crawler.start();

            Timer timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    /*update();*/
                    System.out.println("timer");
                }
            }, 60000, 120000);

            crawlingThread=new Thread(()->{
                //System.out.println("crawlingThread");
                while(crawler.isRunning()){
                    //System.out.println("sto aggiornando i risultati");
                    if(crawlingThread.isInterrupted())
                        break;

                    Optional<CrawlerResult> cr=crawler.get();
                    if(cr.isPresent())
                        results.add(cr.get());

                    //System.out.println("ris: "+results.size());
                }
            });
            crawlingThread.setDaemon(true);
            crawlingThread.start();
        }
    }

    /**
     * Sospende l'esecuzione del SiteCrawler. Se non è in esecuzione, ignora
     * l'invocazione. L'esecuzione può essere ripresa invocando start. Durante
     * la sospensione l'attività dovrebbe essere ridotta al minimo possibile
     * (eventuali thread dovrebbero essere terminati). Se è stata specificata
     * una directory per l'archiviazione, lo stato del crawling è archiviato.
     *
     * @throws IllegalStateException se il SiteCrawler è cancellato
     */
    @Override
    public void suspend() throws IllegalStateException{
        if(isCancelled())
            throw new IllegalStateException();
        if(isRunning()){
            crawler.suspend();
            crawlingThread.interrupt();

            if(dir!=null){
                update();
            }
        }
    }

    /**
     * Cancella il SiteCrawler per sempre. Dopo questa invocazione il
     * SiteCrawler non può più essere usato. Tutte le risorse sono
     * rilasciate.
     */
    @Override
    public void cancel() {
        suspend();
        crawler.cancel();
    }

    /**
     * Ritorna il risultato relativo al prossimo URI. Se il SiteCrawler non è
     * in esecuzione, ritorna un Optional vuoto. Non è bloccante, ritorna
     * immediatamente anche se il prossimo risultato non è ancora pronto.
     *
     * @return il risultato relativo al prossimo URI scaricato
     * @throws IllegalStateException se il SiteCrawler è cancellato
     */
    @Override
    public Optional<CrawlerResult> get() throws IllegalStateException{
        if(isCancelled())
            throw new IllegalStateException();
        /*
        if(isRunning() && !results.isEmpty() && results.keySet().contains(resIndex))
            return Optional.of((CrawlerResult)results.get(resIndex++)[1]);
        */
        if(isRunning() && !results.isEmpty())
            return Optional.of(results.poll());
        return Optional.empty();
    }

    /**
     * Ritorna il risultato del tentativo di scaricare la pagina che
     * corrisponde all'URI dato.
     *
     * @param uri un URI
     * @return il risultato del tentativo di scaricare la pagina
     * @throws IllegalArgumentException se uri non è nell'insieme degli URI
     *                                  scaricati né nell'insieme degli URI che hanno prodotto errori.
     * @throws IllegalStateException    se il SiteCrawler è cancellato
     */
    @Override
    public CrawlerResult get(URI uri) throws IllegalArgumentException,IllegalStateException{
        if(isCancelled())
            throw new IllegalStateException();
        if(!succDownload.contains(uri) && !failDownload.contains(uri))
            throw new IllegalArgumentException();

        CrawlerResult res=null;
        /*
        for(int i:results.keySet()){
            res=(CrawlerResult)results.get(i)[1];
            if(res.uri==uri)
                return res;
        }
        */
        boolean found=false;
        while(!results.isEmpty() && !found){
            CrawlerResult cr=results.poll();
            if(cr.uri==uri) {
                res = cr;
                found=true;
            }
            results.add(cr);
        }
        return res;
    }

    /**
     * Ritorna l'insieme di tutti gli URI scaricati, possibilmente vuoto.
     *
     * @return l'insieme di tutti gli URI scaricati (mai null)
     * @throws IllegalStateException se il SiteCrawler è cancellato
     */
    @Override
    public Set<URI> getLoaded() throws IllegalStateException {
        if(isCancelled())
            throw new IllegalStateException();
        return succDownload;
    }

    /**
     * Ritorna l'insieme, possibilmente vuoto, degli URI che devono essere
     * ancora scaricati. Quando l'esecuzione del crawler termina normalmente
     * l'insieme è vuoto.
     *
     * @return l'insieme degli URI ancora da scaricare (mai null)
     * @throws IllegalStateException se il SiteCrawler è cancellato
     */
    @Override
    public Set<URI> getToLoad() throws IllegalStateException{
        if(isCancelled())
            throw new IllegalStateException();
        return toDownload;
    }

    /**
     * Ritorna l'insieme, possibilmente vuoto, degli URI che non è stato
     * possibile scaricare a causa di errori.
     *
     * @return l'insieme degli URI che hanno prodotto errori (mai null)
     * @throws IllegalStateException se il SiteCrawler è cancellato
     */
    @Override
    public Set<URI> getErrors() throws IllegalStateException{
        if(isCancelled())
            throw new IllegalStateException();
        return failDownload;
    }

    /**
     * Ritorna true se il SiteCrawler è in esecuzione.
     *
     * @return true se il SiteCrawler è in esecuzione
     */
    @Override
    public boolean isRunning() {
        return crawler.isRunning();
    }

    /**
     * Ritorna true se il SiteCrawler è stato cancellato. In tal caso non può
     * più essere usato.
     *
     * @return true se il SiteCrawler è stato cancellato
     */
    @Override
    public boolean isCancelled() {
        return crawler.isCancelled();
    }

    private void update(){
        succDownload=crawler.getLoaded();
        toDownload=crawler.getToLoad();
        failDownload=crawler.getErrors();
        Object[] array = {this.dom, succDownload, toDownload, failDownload, results};
        WindowsManager.salvaArchivio(this.dom, savePath, array);
    }
}