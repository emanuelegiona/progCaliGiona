package src.wsa.web;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

/** Classe di utilita' per permettere la scrittura di un oggetto CrawlerResult; implementa l'interfaccia Serializable*/
public class CRSerializable implements Serializable{
    public final URI u;
    public final boolean lp;
    public final List<URI> links;
    public final List<String> err;
    public final Exception e;

    /** Costruttore di CRSerializable
     * @param cr l'oggetto CrawlerResult da rendere serializzabile*/
    public CRSerializable(CrawlerResult cr){
        this.u=cr.uri;
        this.lp=cr.linkPage;
        this.links=cr.links;
        this.err=cr.errRawLinks;
        this.e=cr.exc;
    }
}