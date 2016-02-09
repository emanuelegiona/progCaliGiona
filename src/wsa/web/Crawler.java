package src.wsa.web;

import java.net.URI;
import java.util.Optional;
import java.util.Set;

/** Un web crawler che partendo da uno o piu' URI scarica le relative pagine e
 * poi fa lo stesso per i link contenuti nelle pagine scaricate. Pero' il crawler
 * puo' essere creato in modo tale che solamente le pagine di URI selezionati
 * sono usate per continuare il crawling. */
public interface Crawler {
    /** Aggiunge un URI all'insieme degli URI da scaricare. Se pero' e' presente
     * tra quelli gia' scaricati, quelli ancora da scaricare o quelli che sono
     * andati in errore, l'aggiunta non ha nessun effetto. Se invece e' un nuovo
     * URI, e' aggiunto all'insieme di quelli da scaricare.
     * @throws IllegalStateException se il Crawler e' cancellato
     * @param uri  un URI che si vuole scaricare */
    void add(URI uri);

    /** Inizia l'esecuzione del Crawler se non e' gia' in esecuzione e ci sono URI
     * da scaricare, altrimenti l'invocazione e' ignorata. Quando e' in esecuzione
     * il metodo isRunning ritorna true.
     * @throws IllegalStateException se il Crawler e' cancellato */
    void start();

    /** Sospende l'esecuzione del Crawler. Se non e' in esecuzione, ignora
     * l'invocazione. L'esecuzione puo' essere ripresa invocando start. Durante
     * la sospensione l'attivita' del Crawler dovrebbe essere ridotta al minimo
     * possibile (eventuali thread dovrebbero essere terminati).
     * @throws IllegalStateException se il Crawler e' cancellato */
    void suspend();

    /** Cancella il Crawler per sempre. Dopo questa invocazione il Crawler non
     * puo' piu' essere usato. Tutte le risorse devono essere rilasciate. */
    void cancel();

    /** Ritorna il risultato relativo al prossimo URI. Se il Crawler non e' in
     * esecuzione, ritorna un Optional vuoto. Non e' bloccante, ritorna
     * immediatamente anche se il prossimo risultato non e' ancora pronto.
     * @throws IllegalStateException se il Crawler e' cancellato
     * @return  il risultato relativo al prossimo URI scaricato */
    Optional<CrawlerResult> get();

    /** Ritorna l'insieme di tutti gli URI scaricati, possibilmente vuoto.
     * @throws IllegalStateException se il Crawler e' cancellato
     * @return l'insieme di tutti gli URI scaricati (mai null) */
    Set<URI> getLoaded();

    /** Ritorna l'insieme, possibilmente vuoto, degli URI che devono essere
     * ancora scaricati. Quando l'esecuzione del crawler termina normalmente
     * l'insieme e' vuoto.
     * @throws IllegalStateException se il Crawler e' cancellato
     * @return l'insieme degli URI ancora da scaricare (mai null) */
    Set<URI> getToLoad();

    /** Ritorna l'insieme, possibilmente vuoto, degli URI che non e' stato
     * possibile scaricare a causa di errori.
     * @throws IllegalStateException se il crawler e' cancellato
     * @return l'insieme degli URI che hanno prodotto errori (mai null) */
    Set<URI> getErrors();

    /** Ritorna true se il Crawler e' in esecuzione.
     * @return true se il Crawler e' in esecuzione */
    boolean isRunning();

    /** Ritorna true se il Crawler e' stato cancellato. In tal caso non puo' piu'
     * essere usato.
     * @return true se il Crawler e' stato cancellato */
    boolean isCancelled();
}

