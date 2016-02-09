package src.wsa.web;

import java.net.URI;
import java.util.Optional;
import java.util.Set;

/** Un crawler specializzato per siti web */
public interface SiteCrawler {
    /** Controlla se l'URI specificato e' un dominio. e' un dominio se e' un URI
     * assoluto gerarchico in cui la parte authority consiste solamente
     * nell'host (che puo' essere vuoto), ci puo' essere il path ma non ci
     * possono essere query e fragment.
     * @param dom  un URI
     * @return true se l'URI specificato e' un dominio */
    static boolean checkDomain(URI dom) {
        boolean test1=true;
        boolean test2=true;

        if(!dom.isAbsolute())
            return false;

        String s=dom.getPath();
        if(s!=null) {
            if (s.contains(".")) {
                String s1 = s.substring(s.lastIndexOf(".") + 1);
                s1 = s1.toLowerCase();
                if(!s1.equals("htm") && !s1.equals("html"))
                    test1=false;
            }
        }

        try {
            if (!dom.getAuthority().equals(dom.getHost()))
                test2 = false;

            if (dom.getQuery()!=null || dom.getFragment()!=null)
                test2 = false;
        } catch (Exception e) {
            test2=false;
        }

        return (test1||test2);
    }

    /** Controlla se l'URI seed appartiene al dominio dom. Si assume che dom
     * sia un dominio valido. Quindi ritorna true se dom.equals(seed) or not
     * dom.relativize(seed).equals(seed).
     * @param dom  un dominio
     * @param seed  un URI
     * @return true se seed appartiene al dominio dom */
    static boolean checkSeed(URI dom, URI seed) {
        return ( dom.equals(seed) || !dom.relativize(seed).equals(seed));
    }

    /** Aggiunge un seed URI. Se pero' e' presente tra quelli gia' scaricati,
     * quelli ancora da scaricare o quelli che sono andati in errore,
     * l'aggiunta non ha nessun effetto. Se invece e' un nuovo URI, e' aggiunto
     * all'insieme di quelli da scaricare.
     * @throws IllegalArgumentException se uri non appartiene al dominio di
     * questo SuteCrawlerrawler
     * @throws IllegalStateException se il SiteCrawler e' cancellato
     * @param uri  un URI */
    void addSeed(URI uri);

    /** Inizia l'esecuzione del SiteCrawler se non e' gia' in esecuzione e ci sono
     * URI da scaricare, altrimenti l'invocazione e' ignorata. Quando e' in
     * esecuzione il metodo isRunning ritorna true.
     * @throws IllegalStateException se il SiteCrawler e' cancellato */
    void start();

    /** Sospende l'esecuzione del SiteCrawler. Se non e' in esecuzione, ignora
     * l'invocazione. L'esecuzione puo' essere ripresa invocando start. Durante
     * la sospensione l'attivita' dovrebbe essere ridotta al minimo possibile
     * (eventuali thread dovrebbero essere terminati). Se e' stata specificata
     * una directory per l'archiviazione, lo stato del crawling e' archiviato.
     * @throws IllegalStateException se il SiteCrawler e' cancellato */
    void suspend();

    /** Cancella il SiteCrawler per sempre. Dopo questa invocazione il
     * SiteCrawler non puo' piu' essere usato. Tutte le risorse sono
     * rilasciate. */
    void cancel();

    /** Ritorna il risultato relativo al prossimo URI. Se il SiteCrawler non e'
     * in esecuzione, ritorna un Optional vuoto. Non e' bloccante, ritorna
     * immediatamente anche se il prossimo risultato non e' ancora pronto.
     * @throws IllegalStateException se il SiteCrawler e' cancellato
     * @return  il risultato relativo al prossimo URI scaricato */
    Optional<CrawlerResult> get();

    /** Ritorna il risultato del tentativo di scaricare la pagina che
     * corrisponde all'URI dato.
     * @param uri  un URI
     * @throws IllegalArgumentException se uri non e' nell'insieme degli URI
     * scaricati né nell'insieme degli URI che hanno prodotto errori.
     * @throws IllegalStateException se il SiteCrawler e' cancellato
     * @return il risultato del tentativo di scaricare la pagina */
    CrawlerResult get(URI uri);

    /** Ritorna l'insieme di tutti gli URI scaricati, possibilmente vuoto.
     * @throws IllegalStateException se il SiteCrawler e' cancellato
     * @return l'insieme di tutti gli URI scaricati (mai null) */
    Set<URI> getLoaded();

    /** Ritorna l'insieme, possibilmente vuoto, degli URI che devono essere
     * ancora scaricati. Quando l'esecuzione del crawler termina normalmente
     * l'insieme e' vuoto.
     * @throws IllegalStateException se il SiteCrawler e' cancellato
     * @return l'insieme degli URI ancora da scaricare (mai null) */
    Set<URI> getToLoad();

    /** Ritorna l'insieme, possibilmente vuoto, degli URI che non e' stato
     * possibile scaricare a causa di errori.
     * @throws IllegalStateException se il SiteCrawler e' cancellato
     * @return l'insieme degli URI che hanno prodotto errori (mai null) */
    Set<URI> getErrors();

    /** Ritorna true se il SiteCrawler e' in esecuzione.
     * @return true se il SiteCrawler e' in esecuzione */
    boolean isRunning();

    /** Ritorna true se il SiteCrawler e' stato cancellato. In tal caso non puo'
     * piu' essere usato.
     * @return true se il SiteCrawler e' stato cancellato */
    boolean isCancelled();
}
