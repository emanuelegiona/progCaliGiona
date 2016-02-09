package src.wsa.web;

import javafx.scene.control.Alert;
import src.wsa.gui.Main;
import src.wsa.gui.UriTableView;
import src.wsa.gui.WindowsManager;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;

/** Effettua il crawling di un sito, partendo da un dominio e da seeds.
 * Consente l'archiviazione dell'esplorazione specificando un percorso.
 * Implementa SiteCrawler.*/
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
    private Timer timer;

    /** Costruttore di SimpleSiteCrawler.
     * @param dom URI del dominio del crawling; puo' essere null solo se viene ripresa un'esplorazione precedentemente archiviata.
     * @param dir Path del percorso di archiviazione; puo' essere null, non avverra' alcuna archiviazione.
     * @throws IllegalArgumentException se dir non contiene un archivio valido.
     * @throws IOException se avvengono errori in fasi di lettura o scrittura dell'archivio.
     */
    public SimpleSiteCrawler(URI dom, Path dir) throws IllegalArgumentException,IOException{
        this.dom=dom;
        this.dir=dir;

        if(dom!=null) {
            savePath = this.dir + "/" + dom.getAuthority()+"h"+LocalDateTime.now().toString().replace(":", "m").replace(".", "_") +".cg";
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
    }

    /**
     * Aggiunge un seed URI. Se pero' e' presente tra quelli gia' scaricati,
     * quelli ancora da scaricare o quelli che sono andati in errore,
     * l'aggiunta non ha nessun effetto. Se invece e' un nuovo URI, e' aggiunto
     * all'insieme di quelli da scaricare.
     *
     * @param uri un URI
     * @throws IllegalArgumentException se uri non appartiene al dominio di
     *                                  questo SuteCrawlerrawler
     * @throws IllegalStateException    se il SiteCrawler e' cancellato
     */
    @Override
    public void addSeed(URI uri) throws IllegalArgumentException,IllegalStateException{
        if(isCancelled())
            throw new IllegalStateException();

        if(!SiteCrawler.checkSeed(dom,uri))
            throw new IllegalArgumentException();

        crawler.add(uri);
    }

    /**
     * Inizia l'esecuzione del SiteCrawler se non e' gia' in esecuzione e ci sono
     * URI da scaricare, altrimenti l'invocazione e' ignorata. Quando e' in
     * esecuzione il metodo isRunning ritorna true.
     *
     * @throws IllegalStateException se il SiteCrawler e' cancellato
     */
    @Override
    public void start() throws IllegalStateException{
        if(isCancelled())
            throw new IllegalStateException();

        if(!isRunning()){
            crawler.start();

            timer = new Timer(true);
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
            }, 5000, 30000);

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
                }
            });
            crawlingThread.setDaemon(true);
            crawlingThread.start();
        }
    }

    /**
     * Sospende l'esecuzione del SiteCrawler. Se non e' in esecuzione, ignora
     * l'invocazione. L'esecuzione puo' essere ripresa invocando start. Durante
     * la sospensione l'attivita' dovrebbe essere ridotta al minimo possibile
     * (eventuali thread dovrebbero essere terminati). Se e' stata specificata
     * una directory per l'archiviazione, lo stato del crawling e' archiviato.
     *
     * @throws IllegalStateException se il SiteCrawler e' cancellato
     */
    @Override
    public void suspend() throws IllegalStateException{
        if(isCancelled())
            throw new IllegalStateException();

        if(isRunning()){
            crawler.suspend();
            crawlingThread.interrupt();
            timer.cancel();
            update();

            try{
                save();
            }catch(Exception e){
                Alert alert=WindowsManager.creaAlert(Alert.AlertType.ERROR,"Errore","Errore durante il salvataggio");
                alert.show();
            }
        }
    }

    /**
     * Cancella il SiteCrawler per sempre. Dopo questa invocazione il
     * SiteCrawler non puo' piu' essere usato. Tutte le risorse sono
     * rilasciate.
     */
    @Override
    public void cancel() {
        suspend();
        crawler.cancel();

        succDownload=null;
        toDownload=null;
        failDownload=null;
        results=null;
    }

    /**
     * Ritorna il risultato relativo al prossimo URI. Se il SiteCrawler non e'
     * in esecuzione, ritorna un Optional vuoto. Non e' bloccante, ritorna
     * immediatamente anche se il prossimo risultato non e' ancora pronto.
     *
     * @return il risultato relativo al prossimo URI scaricato
     * @throws IllegalStateException se il SiteCrawler e' cancellato
     */
    @Override
    public Optional<CrawlerResult> get() throws IllegalStateException{
        if(isCancelled())
            throw new IllegalStateException();

        return crawler.get();
    }

    /**
     * Ritorna il risultato del tentativo di scaricare la pagina che
     * corrisponde all'URI dato.
     *
     * @param uri un URI
     * @return il risultato del tentativo di scaricare la pagina
     * @throws IllegalArgumentException se uri non e' nell'insieme degli URI
     *                                  scaricati né nell'insieme degli URI che hanno prodotto errori.
     * @throws IllegalStateException    se il SiteCrawler e' cancellato
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
     * @throws IllegalStateException se il SiteCrawler e' cancellato
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
     * l'insieme e' vuoto.
     *
     * @return l'insieme degli URI ancora da scaricare (mai null)
     * @throws IllegalStateException se il SiteCrawler e' cancellato
     */
    @Override
    public Set<URI> getToLoad() throws IllegalStateException{
        if(isCancelled())
            throw new IllegalStateException();

        return toDownload;
    }

    /**
     * Ritorna l'insieme, possibilmente vuoto, degli URI che non e' stato
     * possibile scaricare a causa di errori.
     *
     * @return l'insieme degli URI che hanno prodotto errori (mai null)
     * @throws IllegalStateException se il SiteCrawler e' cancellato
     */
    @Override
    public Set<URI> getErrors() throws IllegalStateException{
        if(isCancelled())
            throw new IllegalStateException();

        return failDownload;
    }

    /**
     * Ritorna true se il SiteCrawler e' in esecuzione.
     *
     * @return true se il SiteCrawler e' in esecuzione
     */
    @Override
    public boolean isRunning() {
        return crawler.isRunning();
    }

    /**
     * Ritorna true se il SiteCrawler e' stato cancellato. In tal caso non puo'
     * piu' essere usato.
     *
     * @return true se il SiteCrawler e' stato cancellato
     */
    @Override
    public boolean isCancelled() {
        return crawler.isCancelled();
    }

    /** Sincronizza gli insiemi di questo SimpleSiteCrawler con quelli del Crawler utilizzato internamente.*/
    private void update(){
        succDownload=crawler.getLoaded();
        toDownload=crawler.getToLoad();
        failDownload=crawler.getErrors();
    }

    /** Se dir non e' null, archivia le seguenti informazioni sull'esplorazione:
     * - dominio
     * - gli insiemi succDownload, toDownload, failDownload
     * - mappa di URI con i rispettivi risultati
     * - mappa di URI con i rispettivi dati (numero occorrenze, numero link uscenti, numero immagini, numero nodi nell'albero)
     * - massimo numero di link in una pagina
     * - URI interni al dominio
     * Fa utilizzo del metodo di utilita' WindowsManager.salvaArchivio.
     *
     * @throws Exception qualsiasi errore di I/O ed eventuali errori di tipo
     */
    private void save() throws Exception{
        if(dir!=null) {
            Map<URI,CRSerializable> serial=new HashMap<>();
            results.forEach((u,cr)->serial.put(u,new CRSerializable(cr)));
            Map<URI,Integer[]> stats= Main.getStats(identify());
            Object[] o= Main.activeCrawlers.get(identify());

            Object[] array = {this.dom, succDownload,toDownload, failDownload, serial, stats, (int)o[5], (int)o[6]};
            WindowsManager.salvaArchivio(savePath, array);
        }
    }

    /** Apre un archivio, specificato tramite il nome file.
     * Un nome file accettato e' composto dal dominio dell'esplorazione seguito da un timestamp.
     * Fa utilizzo del metodo di utilita' WindowsManager.apriArchivio.
     *
     * @param fileName il nome del file da aprire com archivio
     * @throws Exception qualsiasi errore di I/O ed eventuali errori di tipo
     */
    private void open(String fileName) throws Exception{
        fileName=fileName.substring(0,fileName.lastIndexOf("h"));
        URI u=new URI(fileName);
        Object[] array=WindowsManager.apriArchivio(dir.toString());

        this.dom=(URI)array[0];
        Object[] objects= Main.activeCrawlers.get(Main.ID);
        objects[0]=this;
        objects[4]=this.dom;
        objects[5]=(int)array[6];
        objects[6]=(int)array[7];
        Main.activeCrawlers.put(Main.ID,objects);

        this.succDownload=(Set<URI>)array[1];
        this.toDownload=(Set<URI>)array[2];
        this.failDownload=(Set<URI>)array[3];
        this.results = new HashMap<>();

        ((HashMap<URI,CRSerializable>)array[4]).forEach((uri,cr)-> this.results.put(uri,new CrawlerResult(cr.u,cr.lp, cr.links, cr.err, cr.e)));
        results.forEach((uri, cr) -> Main.getData(Main.ID).add(new UriTableView(uri, (cr.exc==null?"Completato":"  Fallito"))));

        Map<URI,Integer[]> stats=(HashMap<URI,Integer[]>)array[5];
        objects[3]=stats;
        Main.activeCrawlers.put(identify(),objects);
    }

    /** Identifica il SimpleSiteCrawler in esecuzione
     * @return l'identita' di questo SimpleSiteCrawler se presente, altrimenti 0*/
    private int identify(){
        for(int i: Main.activeCrawlers.keySet())
            if (Main.getSiteCrawler(i).equals(this))
                return i;
        return 0;
    }
}