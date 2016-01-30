package src.wsa.web;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;

public class SimpleCrawler implements Crawler{
    private volatile Set<URI> succDownload;
    private volatile Set<URI> analyzingURIs;
    private volatile Set<URI> toDownload;
    private volatile Set<URI> failDownload;
    private final Predicate<URI> rule;
    private volatile AsyncLoader loader;
    private volatile boolean running;
    private volatile ConcurrentLinkedQueue<Object[]> tasks;
    private volatile ConcurrentLinkedQueue<CrawlerResult> results;
    private volatile Thread taskThread;
    private volatile Thread analyzeThread;

    public SimpleCrawler(Collection<URI> succDownload, Collection<URI> toDownload, Collection<URI> failDownload, Predicate<URI> rule) {
        this.succDownload = new ConcurrentSkipListSet<>(succDownload);
        this.toDownload = new ConcurrentSkipListSet<>(toDownload);
        this.failDownload = new ConcurrentSkipListSet<>(failDownload);
        this.analyzingURIs=new ConcurrentSkipListSet<>();
        if(rule!=null)
            this.rule=rule;
        else
            this.rule=(s)->true;
        loader=WebFactory.getAsyncLoader();
        running=false;
        tasks =new ConcurrentLinkedQueue<>();
        results=new ConcurrentLinkedQueue<>();
    }

    /**
     * Aggiunge un URI all'insieme degli URI da scaricare. Se però è presente
     * tra quelli già scaricati, quelli ancora da scaricare o quelli che sono
     * andati in errore, l'aggiunta non ha nessun effetto. Se invece è un nuovo
     * URI, è aggiunto all'insieme di quelli da scaricare.
     *
     * @param uri un URI che si vuole scaricare
     * @throws IllegalStateException se il Crawler è cancellato
     */
    @Override
    public void add(URI uri) throws IllegalStateException{
        if(isCancelled())
            throw new IllegalStateException();
        if(!toDownload.contains(uri) && !succDownload.contains(uri) && !failDownload.contains(uri) && !analyzingURIs.contains(uri)) {
            toDownload.add(uri);
            System.out.println(uri);
        }
    }

    private void downloadFunc(){
        while (true) {
            if(taskThread.isInterrupted()) {
                taskThread.interrupt();
                return;
            }

            Iterator<URI> uris = toDownload.iterator();
            while (uris.hasNext()) {
                URI u = uris.next();
                boolean tested = rule.test(u);
                try {
                    Object o[]={u,loader.submit(u.toURL()),4};
                    tasks.add(o);
                    analyzingURIs.add(u);
                } catch (MalformedURLException e) {
                    failDownload.add(u);
                    results.add(new CrawlerResult(u, tested, null, null, e));
                }
                uris.remove();

                if(taskThread.isInterrupted()) {
                    taskThread.interrupt();
                    break;
                }
            }
        }
    }

    private URI bonifica(String s){
        URI uri=null;
        try {
            uri=URI.create(s);
        }catch(IllegalArgumentException e1) {
            try {
                s=s.replace(" ","+");
                int p1 = s.lastIndexOf(" ") + 1;
                s = s.substring(0, p1) + s.substring(p1);
                int p2 = s.lastIndexOf("/") + 1;
                s = s.substring(0, p2) + URLEncoder.encode(s.substring(p2), "UTF-8");
                uri = URI.create(s);
            } catch (UnsupportedEncodingException e2) {}
        }
        return uri;
    }

    private void analyzeFunc(){
        while (true) {
            if(analyzeThread.isInterrupted()) {
                analyzeThread.interrupt();
                return;
            }

            Object[] o;
            while((o=tasks.poll())!=null){
                if(analyzeThread.isInterrupted()) {
                    analyzeThread.interrupt();
                    break;
                }

                Future<LoadResult> t=(Future<LoadResult>)o[1];

                try {
                    LoadResult res=t.get(2000,TimeUnit.MILLISECONDS);
                    try {
                        URI u = res.url.toURI();
                        analyzingURIs.remove(u);
                        succDownload.add(u);

                        List<String> links = res.parsed.getLinks();
                        List<URI> absLinks = new ArrayList<>();
                        List<String> errLinks = new ArrayList<>();
                        Iterator<String> l = links.iterator();

                        if (rule.test(u)) {
                            while (l.hasNext()) {
                                if (analyzeThread.isInterrupted()) {
                                    analyzeThread.interrupt();
                                    break;
                                }

                                String s = l.next();
                                URI uri = bonifica(s);
                                URI nUri = u.resolve(uri);
                                try {
                                    URL testUrl = nUri.toURL();
                                    absLinks.add(nUri);
                                    add(nUri);
                                } catch (MalformedURLException e) {
                                    errLinks.add(s);
                                    if(failDownload.add(nUri))
                                        results.add(new CrawlerResult(nUri,rule.test(nUri),null,null,e));
                                } finally {
                                    l.remove();
                                }
                            }
                        }
                        results.add(new CrawlerResult(u, rule.test(u), (absLinks.isEmpty() ? null : absLinks), (errLinks.isEmpty() ? null : errLinks), null));
                    }catch(URISyntaxException e){
                        results.add(new CrawlerResult((URI)o[0], rule.test((URI)o[0]), null, null, e));
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
                    }
                }
            }
        }
    }

    /**
     * Inizia l'esecuzione del Crawler se non è già in esecuzione e ci sono URI
     * da scaricare, altrimenti l'invocazione è ignorata. Quando è in esecuzione
     * il metodo isRunning ritorna true.
     *
     * @throws IllegalStateException se il Crawler è cancellato
     */
    //@Override
    public void start() throws IllegalArgumentException{
        if(isCancelled())
            throw new IllegalArgumentException();
        if(!isRunning()){
            running=true;

            taskThread=new Thread(()->{
                downloadFunc();
            });
            taskThread.setDaemon(true);
            taskThread.start();

            analyzeThread=new Thread(()->{
                analyzeFunc();
            });
            analyzeThread.setDaemon(true);
            analyzeThread.start();
        }
    }

    /**
     * Sospende l'esecuzione del Crawler. Se non è in esecuzione, ignora
     * l'invocazione. L'esecuzione può essere ripresa invocando start. Durante
     * la sospensione l'attività del Crawler dovrebbe essere ridotta al minimo
     * possibile (eventuali thread dovrebbero essere terminati).
     *
     * @throws IllegalStateException se il Crawler è cancellato
     */
    @Override
    public void suspend() throws IllegalStateException{
        if(isCancelled())
            throw new IllegalStateException();
        if (isRunning()) {
            taskThread.interrupt();
            analyzeThread.interrupt();
            running=false;
        }
    }

    /**
     * Cancella il Crawler per sempre. Dopo questa invocazione il Crawler non
     * può più essere usato. Tutte le risorse devono essere rilasciate.
     */
    @Override
    public void cancel() {
        suspend();
        loader.shutdown();
    }

    /**
     * Ritorna il risultato relativo al prossimo URI. Se il Crawler non è in
     * esecuzione, ritorna un Optional vuoto. Non è bloccante, ritorna
     * immediatamente anche se il prossimo risultato non è ancora pronto.
     *
     * @return il risultato relativo al prossimo URI scaricato
     * @throws IllegalStateException se il Crawler è cancellato
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
     * @throws IllegalStateException se il Crawler è cancellato
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
     * l'insieme è vuoto.
     *
     * @return l'insieme degli URI ancora da scaricare (mai null)
     * @throws IllegalStateException se il Crawler è cancellato
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
     * @throws IllegalStateException se il crawler è cancellato
     */
    @Override
    public Set<URI> getErrors() throws IllegalStateException{
        if(isCancelled())
            throw new IllegalStateException();
        return failDownload;
    }

    /**
     * Ritorna true se il Crawler è in esecuzione.
     *
     * @return true se il Crawler è in esecuzione
     */
    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * Ritorna true se il Crawler è stato cancellato. In tal caso non può più
     * essere usato.
     *
     * @return true se il Crawler è stato cancellato
     */
    @Override
    public boolean isCancelled() {
        return loader.isShutdown();
    }
}
