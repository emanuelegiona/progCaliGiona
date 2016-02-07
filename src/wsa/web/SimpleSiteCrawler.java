package src.wsa.web;

import src.wsa.gui.MainGUI;
import src.wsa.gui.UriTableView;
import src.wsa.gui.WindowsManager;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;

public class SimpleSiteCrawler implements SiteCrawler{
    private volatile URI dom;
    private final Path dir;
    private volatile Set<URI> succDownload;
    private volatile Set<URI> toDownload;
    private volatile Set<URI> failDownload;
    private final Crawler crawler;
    private final Predicate<URI> pageLink;
    private String savePath;
    private Thread crawlingThread;
    private volatile Map<URI, CrawlerResult> results;

    private int ID;

    public SimpleSiteCrawler(URI dom, Path dir) throws IllegalArgumentException,IOException{
        this.dom=dom;
        this.dir=dir;


        if(dom!=null) {
            savePath = this.dir + "\\" + dom.getAuthority()+"h"+LocalDateTime.now().toString().replace(":", "m").replace(".", "_") +".cg";
            succDownload = new HashSet<>();
            toDownload = new HashSet<>();
            failDownload = new HashSet<>();
            results=new HashMap<>();
        }

        if(dom==null && dir!=null){
            try{
                String fileName=dir.getFileName().toString();
                open(fileName);
                savePath = this.dir.toString();
            } catch (Exception e) {
                if(e.getClass().equals(IOException.class))
                    throw new IOException(e.getMessage());
                else
                    throw new IllegalArgumentException("ERRORE: il file non contiene un archivio.");
            }
        }

        pageLink=(URI u)->SiteCrawler.checkSeed(this.dom,u);
        crawler=WebFactory.getCrawler(succDownload,toDownload,failDownload,pageLink);

        ID=MainGUI.ID;
        MainGUI.crID.put(crawler,ID);
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

        if(!isRunning() && !toDownload.isEmpty()){
            crawler.start();

            Timer timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    update();
                    try {
                        save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 5000 /**//*60000/**/, /*5000*/ /**/60000/**/);

            crawlingThread=new Thread(()->{
                while(true){
                    if(crawlingThread.isInterrupted())
                        break;

                    try {
                        Optional<CrawlerResult> cr = get();
                        if (cr.isPresent()) {
                            URI u = cr.get().uri;
                            CrawlerResult res = cr.get();
                            results.put(u, res);
                        }
                    }catch(IllegalStateException e){
                        crawlingThread.interrupt();
                        break;
                    }

                   /****************************** if(Math.abs(this.toDownload.size()+this.succDownload.size()+this.failDownload.size())-
                            (crawler.getToLoad().size()+crawler.getLoaded().size()+crawler.getErrors().size())>30)
                        update();*****************************************/
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

        return crawler.get();
    }

    //modifica

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

        update();

        if(!succDownload.contains(uri) && !failDownload.contains(uri))
            throw new IllegalArgumentException();



        return results.get(uri);

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

    }

    private void save() throws Exception{
        if(dir!=null) {
            Map<URI,CRSerializable> serial=new HashMap<>();
            results.forEach((u,cr)->serial.put(u,new CRSerializable(cr)));
            Map<URI,Integer[]> stats=MainGUI.getStats(identify());
            Object[] array = {this.dom, succDownload,toDownload, failDownload, serial, stats};
            WindowsManager.salvaArchivio(this.dom, savePath, array);
        }
    }

    private void open(String fileName) throws Exception{
        fileName=fileName.substring(0,fileName.lastIndexOf("h"));
        URI u=new URI(fileName);
        Object[] array=WindowsManager.apriArchivio(dir.toString());

        this.dom=(URI)array[0];
        Object[] objects=MainGUI.activeCrawlers.get(identify());
        objects[4]=this.dom;
        MainGUI.activeCrawlers.put(identify(),objects);

        this.succDownload=(Set<URI>)array[1];
        this.toDownload=(Set<URI>)array[2];
        this.failDownload=(Set<URI>)array[3];
        this.results = new HashMap<>();
        ((HashMap<URI,CRSerializable>)array[4]).forEach((uri,cr)-> this.results.put(uri,new CrawlerResult(cr.u,cr.lp,cr.links,cr.err,cr.e)));
        results.forEach((uri, cr) -> MainGUI.getData(identify()).add(new UriTableView(uri, (cr.exc==null?"Completato":"  Fallito"))));
        /*Map<URI,Integer[]> stats=(HashMap<URI,Integer[]>)array[5];
        Object[] objects=new Object[4];
        objects[3]=stats;
        MainGUI.activeCrawlers.put(identify(),objects);*/
    }

    private int identify(){
        for(int i:MainGUI.activeCrawlers.keySet())
            if(MainGUI.getSiteCrawler(i).equals(this))
                return i;
        return 0;
    }
}