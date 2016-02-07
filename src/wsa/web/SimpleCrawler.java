package src.wsa.web;


import javafx.application.Platform;
import javafx.scene.control.Alert;
import src.wsa.gui.MainGUI;

import src.wsa.gui.UriTableView;
import src.wsa.gui.WindowsManager;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;

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


    //modifica
    public SimpleCrawler(Collection<URI> succDownload, Collection<URI> toDownload, Collection<URI> failDownload, Predicate<URI> rule) {
        this.succDownload = new HashSet<>(succDownload);
        this.toDownload = new HashSet<>(toDownload);
        this.failDownload = new HashSet<>(failDownload);
        this.analyzingURIs=new HashSet<>();
        this.rule=rule;
        loader=WebFactory.getAsyncLoader();
        running=false;
        tasks =new ConcurrentLinkedQueue<>();
        results=new ConcurrentLinkedQueue<>();
        tableMap = new HashMap<>();

        this.toDownload.forEach(u -> {
            UriTableView utv = new UriTableView(u, " In Download...");
            tableMap.put(u, utv);
            MainGUI.getData(identify()).add(utv);
        });
    }
    /**
     * Aggiunge un URI all'insieme degli URI da scaricare. Se per� � presente
     * tra quelli gi� scaricati, quelli ancora da scaricare o quelli che sono
     * andati in errore, l'aggiunta non ha nessun effetto. Se invece � un nuovo
     * URI, � aggiunto all'insieme di quelli da scaricare.
     *
     * @param uri un URI che si vuole scaricare
     * @throws IllegalStateException se il Crawler � cancellato
     */
    @Override
    public void add(URI uri) throws IllegalStateException{
        if(isCancelled())
            throw new IllegalStateException();
        if(!toDownload.contains(uri) && !succDownload.contains(uri) && !failDownload.contains(uri) && !analyzingURIs.contains(uri)) {
            toDownload.add(uri);

            UriTableView utv = new UriTableView(uri, " In Download...");
            tableMap.put(uri, utv);
            MainGUI.getData(identify()).add(utv);
        }
    }


    private void downloadFunc(){
            for (URI u : toDownload) {
                if(analyzeThread.isInterrupted()) {
                    analyzeThread.interrupt();
                    return;
                }

                if(analyzingURIs.size() == (Runtime.getRuntime().availableProcessors()*10)) break;

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



    private URI bonifica(String s){
        URI uri=null;
        try {
            uri=URI.create(s);
        }catch(IllegalArgumentException e1) {
            String reserved="!*'();:@&=+$,/?#[] ";
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
            uri=URI.create(finale);
        }
        return uri;
    }



    private void analyzeFunc(){
        boolean mostrato = false;

        while (true) {

            if(analyzeThread.isInterrupted()) {
                analyzeThread.interrupt();
                return;
            }

            if(toDownload.size() == 0){
                if(!mostrato){Platform.runLater(() ->{
                    Alert alert = WindowsManager.creaAlert(Alert.AlertType.INFORMATION, "Informazione", "Esplorazione completata");
                    alert.showAndWait();
                });
                    mostrato = true;
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


                        //TODO: controllare dominio con i file e conteggi
                        if (rule.test(u)) {
                            int out=0;
                            for (String s: links) {
                                if (analyzeThread.isInterrupted()) {
                                    analyzeThread.interrupt();
                                    break;
                                }

                                URI uri = bonifica(s);
                                URI nUri = u.resolve(uri);
                                if(nUri.toString().contains("file:/"))
                                    System.out.println("file");
                                try {
                                    URL testUrl = nUri.toURL();
                                    absLinks.add(nUri);
                                    updateOccur(nUri,0,0);
                                } catch (MalformedURLException e) {
                                    errLinks.add(s);
                                }
                                add(nUri);
                                if(!rule.test(nUri)) {
                                    System.out.println(nUri+" fuori dominio");
                                    out++;
                                }
                            }
                            System.out.println("uri " + u + ":" + out);
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

    private void updateTable(URI u, String s){
        UriTableView utv = tableMap.get(u);
        utv.setStato(s);
        try{
            MainGUI.getData(identify()).set(MainGUI.getData(identify()).indexOf(utv),utv);
        }catch (Exception e){
            //System.out.println(utv.getUri());
        }
    }

    private void updateOccur(URI u, int v, int out){
        if(MainGUI.getStats(identify()).containsKey(u)) {
            if(v==0) {
                Integer[] o=MainGUI.getStats(identify()).get(u);
                o[0]=o[0]+1;
                MainGUI.getStats(identify()).put(u, o);
            }
            else{
                Integer[] o=MainGUI.getStats(identify()).get(u);
                o[1]=out;
                MainGUI.getStats(identify()).put(u,o);
            }
        }
        else {
            Integer[] o={1,0};
            MainGUI.getStats(identify()).put(u, o);
        }
    }

    /**
     * Inizia l'esecuzione del Crawler se non � gi� in esecuzione e ci sono URI
     * da scaricare, altrimenti l'invocazione � ignorata. Quando � in esecuzione
     * il metodo isRunning ritorna true.
     *
     * @throws IllegalStateException se il Crawler � cancellato
     */
    public void start() throws IllegalArgumentException{
        if(isCancelled())
            throw new IllegalArgumentException();


        MainGUI.getData(identify()).forEach(utv -> tableMap.put(utv.getUri(), utv));

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
     * Sospende l'esecuzione del Crawler. Se non � in esecuzione, ignora
     * l'invocazione. L'esecuzione pu� essere ripresa invocando start. Durante
     * la sospensione l'attivit� del Crawler dovrebbe essere ridotta al minimo
     * possibile (eventuali thread dovrebbero essere terminati).
     *
     * @throws IllegalStateException se il Crawler � cancellato
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
     * pu� pi� essere usato. Tutte le risorse devono essere rilasciate.
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
     * Ritorna il risultato relativo al prossimo URI. Se il Crawler non � in
     * esecuzione, ritorna un Optional vuoto. Non � bloccante, ritorna
     * immediatamente anche se il prossimo risultato non � ancora pronto.
     *
     * @return il risultato relativo al prossimo URI scaricato
     * @throws IllegalStateException se il Crawler � cancellato
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
     * @throws IllegalStateException se il Crawler � cancellato
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
     * l'insieme � vuoto.
     *
     * @return l'insieme degli URI ancora da scaricare (mai null)
     * @throws IllegalStateException se il Crawler � cancellato
     */
    @Override
    public Set<URI> getToLoad() throws IllegalStateException{
        if(isCancelled())
            throw new IllegalStateException();
        return toDownload;
    }

    /**
     * Ritorna l'insieme, possibilmente vuoto, degli URI che non � stato
     * possibile scaricare a causa di errori.
     *
     * @return l'insieme degli URI che hanno prodotto errori (mai null)
     * @throws IllegalStateException se il crawler � cancellato
     */
    @Override
    public Set<URI> getErrors() throws IllegalStateException{
        if(isCancelled())
            throw new IllegalStateException();
        return failDownload;
    }

    /**
     * Ritorna true se il Crawler � in esecuzione.
     *
     * @return true se il Crawler � in esecuzione
     */
    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * Ritorna true se il Crawler � stato cancellato. In tal caso non pu� pi�
     * essere usato.
     *
     * @return true se il Crawler � stato cancellato
     */
    @Override
    public boolean isCancelled() {
        return loader.isShutdown();
    }

    private int identify(){
        return MainGUI.crID.get(this);
    }
}