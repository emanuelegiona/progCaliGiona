package src.wsa.web;

import java.net.URL;
import java.util.concurrent.Future;

/** Un loader asincrono per pagine web */
public interface AsyncLoader {
    /** Sottomette il downloading della pagina dello specificato URL e ritorna
     * un Future per ottenere il risultato in modo asincrono.
     * @param url  un URL di una pagina web
     * @throws IllegalStateException se il loader e' chiuso
     * @return Future per ottenere il risultato in modo asincrono */
    Future<LoadResult> submit(URL url);

    /** Chiude il loader e rilascia tutte le risorse. Dopo di cio' non puo' piu'
     * essere usato. */
    void shutdown();

    /** Ritorna true se e' chiuso.
     * @return true se e' chiuso */
    boolean isShutdown();
}
