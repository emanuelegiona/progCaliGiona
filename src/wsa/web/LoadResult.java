package src.wsa.web;

import src.wsa.web.html.Parsed;
import java.net.URL;

/** Il risultato del tentativo di scaricare una pagina web */
public class LoadResult {
    /** L'URL della pagina web */
    public final URL url;
    /** L'analisi sintattica della pagina scaricata o null se e' accaduto un
     * errore */
    public final Parsed parsed;
    /** Se diverso da null, la pagina non e' stata scaricata e' la causa e'
     * specificata dall'eccezione */
    public final Exception exc;

    public LoadResult(URL u, Parsed p, Exception e) {
        url = u;
        parsed = p;
        exc = e;
    }
}

