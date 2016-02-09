package src.wsa.web;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

/** Il risultato del tentativo di scaricare una pagina tramite un Crawler */

public class CrawlerResult{
    /** L'URI della pagina o null. Se null, significa che la prossima pagina
     * non e' ancora pronta. */
    public final URI uri;
    /** true se l'URI e' di una pagina i cui link sono seguiti. Se false i campi
     * links e errRawLinks sono null. */
    public final boolean linkPage;
    /** La lista degli URI assoluti dei link della pagina o null */
    public final List<URI> links;
    /** La lista dei link che non e' stato possibile trasformare in URI assoluti
     * o null */
    public final List<String> errRawLinks;
    /** Se e' null, la pagina e' stata scaricata altrimenti non e' stato possibile
     * scaricarla e l'eccezione ne da' la causa */
    public final Exception exc;

    public CrawlerResult(URI u, boolean lp, List<URI> ll, List<String> erl,
                         Exception e) {
        uri = u;
        linkPage = lp;
        links = ll;
        errRawLinks = erl;
        exc = e;
    }
}

