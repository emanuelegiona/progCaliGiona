package src.wsa.web;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import src.wsa.gui.Main;
import src.wsa.gui.UriTableView;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;

/** Esegue il crawling di un insieme di URI, applicando un Predicate e dividendo gli URI scaricati con successo da quelli
 * con errori; implementa Crawler.*/
public class SimpleCrawler implements Crawler{
    private Set<URI> succDownload;
    private Set<URI> analyzingURIs;
    private volatile Set<URI> toDownload;
    private Set<URI> failDownload;
    private final Predicate<URI> rule;
    private AsyncLoader loader;
    private volatile boolean running;
    private ConcurrentLinkedQueue<Object[]> tasks;
    private ConcurrentLinkedQueue<CrawlerResult> results;
    private Thread analyzeThread;
    private volatile Map<URI, UriTableView> tableMap;

    /** Costruttore di SimpleCrawler.
     * Inizializza i vari insiemi, l'AsyncLoader, le code dei task e dei risultati.
     * @param succDownload l'insieme di URI scaricati con successo
     * @param toDownload l'insieme di URI da scaricare
     * @param failDownload l'insieme di URI con errori
     * @param rule il Predicate da applicare ad ogni URI*/
    public SimpleCrawler(Collection<URI> succDownload, Collection<URI> toDownload, Collection<URI> failDownload, Predicate<URI> rule) {
        this.succDownload = new HashSet<>(succDownload);
        this.toDownload = new ConcurrentSkipListSet<>(toDownload);
        this.failDownload = new HashSet<>(failDownload);
        this.analyzingURIs=new HashSet<>();
        this.rule=rule;
        loader=WebFactory.getAsyncLoader();
        running=false;
        tasks =new ConcurrentLinkedQueue<>();
        results=new ConcurrentLinkedQueue<>();
        tableMap = new HashMap<>();

        Main.crID.put(this, Main.ID);
        this.toDownload.forEach(u -> {
            UriTableView utv = new UriTableView(u, " In Download...");
            tableMap.put(u, utv);
            Main.getData(identify()).add(utv);
        });
    }

    /**
     * Aggiunge un URI all'insieme degli URI da scaricare. Se pero' e' presente
     * tra quelli gia' scaricati, quelli ancora da scaricare o quelli che sono
     * andati in errore, l'aggiunta non ha nessun effetto. Se invece e' un nuovo
     * URI, e' aggiunto all'insieme di quelli da scaricare.
     *
     * @param uri un URI che si vuole scaricare
     * @throws IllegalStateException se il Crawler e' cancellato
     */
    @Override
    public void add(URI uri) throws IllegalStateException{
        if(isCancelled())
            throw new IllegalStateException();

        if(!toDownload.contains(uri) && !succDownload.contains(uri) && !failDownload.contains(uri) && !analyzingURIs.contains(uri)) {
            toDownload.add(uri);

            UriTableView utv = new UriTableView(uri, " In Download...");
            tableMap.put(uri, utv);
            Main.getData(identify()).add(utv);
        }
    }

    /** Sottomette tutti gli URI nell'insieme toDownload all'AsyncLoader, limitandoli in base all'utilizzo del processore.
     * Non viene eseguita se toDownload e' vuoto; per ogni task, viene stabilito un numero massimo di tentativi di download.
     * Nel caso ci fosse un errore nella crazione dell'URL, produce un risultato.*/
    private void downloadFunc(){
        if(toDownload.size()==0)
            return;

        for (URI u : toDownload) {
            if(analyzeThread.isInterrupted()) {
                analyzeThread.interrupt();
                return;
            }

            if(analyzingURIs.size() == (Runtime.getRuntime().availableProcessors()*10))
                break;

            boolean tested = rule.test(u);
            try {
                analyzingURIs.add(u);
                Object o[] = {u, loader.submit(u.toURL()), 4};
                tasks.add(o);
            } catch (MalformedURLException e) {
                failDownload.add(u);
                results.add(new CrawlerResult(u, tested, null, null, e));
                updateTable(u, "  Fallito");
            }
        }
        toDownload.removeAll(analyzingURIs);
    }

    /** Rende una stringa utilizzabile nella crazione di un URI, secondo la codifica RFC3986
     * @param s la stringa da codificare.
     * @return un URI creato dalla stringa s*/
    private URI bonifica(String s){
        URI uri=null;
        try {
            uri=URI.create(s);
        }catch(IllegalArgumentException e1) {
            String reserved="!*'();:@&=+$,/?#[]{} ";
            String s1=s.substring(0,s.indexOf("//")+2);
            s1=s1.replace(" ","");
            String s2=s.substring(s.indexOf("//")+2);
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
                        } catch (UnsupportedEncodingException e) {}

                    if(xP.equals(" "))
                        xP="%20";
                    xEncoded[j]=xP;
                }

                x=String.join("",xEncoded);
                split[i]=x;
            }
            finale+=String.join("/",split);
            try {
                uri = URI.create(finale);
            }catch(Exception e){}
        }
        return uri;
    }

    /** Esegue downloadFunc; per ogni task completato, costruisce il risultato, ne estrae i link contenuti nella pagina
     * e per ognuno ne esegue la bonifica.
     * Se non si presentano errori nella creazione delle URL rispettive, vengono mandati in toDownload.
     * Tiene traccia dei link appartenenti al dominio, delle occorrenze verso un determinato link e del numero massimo
     * di link in una pagina.*/
    private void analyzeFunc(){
        boolean done=false;

        while (true) {
            if(analyzeThread.isInterrupted()) {
                analyzeThread.interrupt();
                return;
            }

            if(toDownload.size()==0 && (analyzingURIs.size()==0 || analyzingURIs.size()==failDownload.size())){
                if(!done) {
                    int ID = Main.crID.get(this);
                    for (Tab t : Main.tabCrawlers.keySet()) {
                        if (Main.tabCrawlers.get(t).equals(ID)) {
                            Platform.runLater(() -> t.setText("Completato"));
                            done = true;
                            break;
                        }
                    }
                }
                continue;
            }
            downloadFunc();

            Object[] o;
            while((o=tasks.poll())!= null){
                if(analyzeThread.isInterrupted()) {
                    analyzeThread.interrupt();
                    break;
                }

                Future<LoadResult> t=(Future<LoadResult>)o[1];
                try {
                    LoadResult res=t.get(3000,TimeUnit.MILLISECONDS);
                    try {
                        URI u = res.url.toURI();
                        analyzingURIs.remove(u);
                        succDownload.add(u);

                        List<String> links = res.parsed.getLinks();
                        List<URI> absLinks = new ArrayList<>();
                        List<String> errLinks = new ArrayList<>();

                        if (rule.test(u)) {
                            int out = 0;
                            int ID = Main.crID.get(this);
                            Object[] objects = Main.activeCrawlers.get(ID);
                            if(links.size()>(int)objects[5])
                                objects[5] = links.size();
                            objects[6] = (int)objects[6]+1;
                            Main.activeCrawlers.put(ID,objects);

                            for (String s: links) {
                                if (analyzeThread.isInterrupted()) {
                                    analyzeThread.interrupt();
                                    break;
                                }

                                URI uri = bonifica(s);
                                URI nUri = u.resolve(uri);
                                try {
                                    URL testUrl = nUri.toURL();
                                    absLinks.add(nUri);
                                    updateOccur(nUri,0,0);
                                } catch (MalformedURLException e) {
                                    errLinks.add(s);
                                }
                                add(nUri);

                                if(!rule.test(nUri))
                                    out++;
                            }
                            updateOccur(u, 1, out);
                        }
                        results.add(new CrawlerResult(u, rule.test(u), (absLinks.isEmpty() ? null : absLinks), (errLinks.isEmpty() ? null : errLinks), null));
                        updateTable(u, "Completato");
                    }catch(URISyntaxException e){
                        results.add(new CrawlerResult((URI)o[0], rule.test((URI)o[0]), null, null, e));
                        failDownload.add((URI)o[0]);
                        updateTable((URI)o[0], "  Fallito");
                    }
                } catch (InterruptedException e) {
                    tasks.add(o);
                    analyzeThread.interrupt();
                    break;
                } catch (ExecutionException | TimeoutException e) {
                    if((int)o[2]>0) {
                        o[2] = (int) o[2] - 1;
                        tasks.add(o);
                    }
                    else{
                        URI u=(URI)o[0];
                        analyzingURIs.remove(u);
                        failDownload.add(u);
                        results.add(new CrawlerResult(u,rule.test(u),null,null, new Exception("Tentativi di download falliti: 5")));
                        updateTable(u, "  Fallito");
                    }
                }
            }
        }
    }

    /** Aggiorna la TableView collegata a questo processo di crawling.
     * @param u URI di cui aggiornare lo stato
     * @param s lo stato da impostare
     */
    private void updateTable(URI u, String s){
        UriTableView utv = tableMap.get(u);
        utv.setStato(s);
        try{
            Main.getData(identify()).set(Main.getData(identify()).indexOf(utv),utv);
        }catch (Exception e){}
    }

    /** Aggiorna le occorrenze verso un URI e ne imposta il numero di link verso pagine non appartenenti al dominio (link uscenti).
     * @param u l'URI di cui aggiornare i dati
     * @param v se 0, incrementa le occorrenze verso l'URI u; se 1, imposta il valore out come numero di link uscenti
     * @param out il valore di link uscenti contenuti in questa pagina
     */
    private void updateOccur(URI u, int v, int out){
        if(Main.getStats(identify()).containsKey(u)) {
            if(v==0) {
                Integer[] o= Main.getStats(identify()).get(u);
                o[0]=o[0]+1;
                Main.getStats(identify()).put(u, o);
            }
            else{
                Integer[] o= Main.getStats(identify()).get(u);
                o[1]=out;
                Main.getStats(identify()).put(u,o);
            }
        }
        else {
            Integer[] o={1,0,-1,-1};
            Main.getStats(identify()).put(u, o);
        }
    }

    /**
     * Inizia l'esecuzione del Crawler se non e' gia' in esecuzione e ci sono URI
     * da scaricare, altrimenti l'invocazione e' ignorata. Quando e' in esecuzione
     * il metodo isRunning ritorna true.
     *
     * @throws IllegalStateException se il Crawler e' cancellato
     */
    public void start() throws IllegalArgumentException{
        if(isCancelled())
            throw new IllegalArgumentException();

        Main.getData(identify()).forEach(utv -> tableMap.put(utv.getUri(), utv));
        analyzeThread=new Thread(()->{
            if(!isRunning()) {
                running = true;
                analyzeFunc();
            }
        });
        analyzeThread.setDaemon(true);
        analyzeThread.start();
    }

    /**
     * Sospende l'esecuzione del Crawler. Se non e' in esecuzione, ignora
     * l'invocazione. L'esecuzione puo' essere ripresa invocando start. Durante
     * la sospensione l'attivita' del Crawler dovrebbe essere ridotta al minimo
     * possibile (eventuali thread dovrebbero essere terminati).
     *
     * @throws IllegalStateException se il Crawler e' cancellato
     */
    @Override
    public void suspend() throws IllegalStateException{
        if(isCancelled())
            throw new IllegalStateException();

        if (isRunning()) {
            analyzeThread.interrupt();
            running=false;
        }
    }

    /**
     * Cancella il Crawler per sempre. Dopo questa invocazione il Crawler non
     * puo' piu' essere usato. Tutte le risorse devono essere rilasciate.
     */
    @Override
    public void cancel() {
        suspend();
        toDownload = null;
        succDownload = null;
        failDownload = null;
        loader.shutdown();
    }

    /**
     * Ritorna il risultato relativo al prossimo URI. Se il Crawler non e' in
     * esecuzione, ritorna un Optional vuoto. Non e' bloccante, ritorna
     * immediatamente anche se il prossimo risultato non e' ancora pronto.
     *
     * @return il risultato relativo al prossimo URI scaricato
     * @throws IllegalStateException se il Crawler e' cancellato
     */
    @Override
    public Optional<CrawlerResult> get() throws IllegalStateException{
        if(isCancelled())
            throw new IllegalStateException();

        if(isRunning() && !results.isEmpty())
            return Optional.of(results.poll());

        return Optional.empty();
    }

    /**
     * Ritorna l'insieme di tutti gli URI scaricati, possibilmente vuoto.
     *
     * @return l'insieme di tutti gli URI scaricati (mai null)
     * @throws IllegalStateException se il Crawler e' cancellato
     */
    @Override
    public Set<URI> getLoaded() throws IllegalStateException{
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
     * @throws IllegalStateException se il Crawler e' cancellato
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
     * @throws IllegalStateException se il crawler e' cancellato
     */
    @Override
    public Set<URI> getErrors() throws IllegalStateException{
        if(isCancelled())
            throw new IllegalStateException();

        return failDownload;
    }

    /**
     * Ritorna true se il Crawler e' in esecuzione.
     *
     * @return true se il Crawler e' in esecuzione
     */
    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * Ritorna true se il Crawler e' stato cancellato. In tal caso non puo' piu'
     * essere usato.
     *
     * @return true se il Crawler e' stato cancellato
     */
    @Override
    public boolean isCancelled() {
        return loader.isShutdown();
    }

    /** Identifica il Crawler corrente per attivita' di sincronizzazione
     * @return l'identita' del crawler corrente*/
    private int identify(){
        return Main.crID.get(this);
    }
}