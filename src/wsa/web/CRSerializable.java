package src.wsa.web;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

public class CRSerializable implements Serializable{
    public final URI u;
    public final boolean lp;
    public final List<URI> links;
    public final List<String> err;
    public final Exception e;


    public CRSerializable(CrawlerResult cr){
        this.u=cr.uri;
        this.lp=cr.linkPage;
        this.links=cr.links;
        this.err=cr.errRawLinks;
        this.e=cr.exc;

    }
}