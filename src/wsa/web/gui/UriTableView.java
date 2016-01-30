package src.wsa.web.gui;

import javafx.beans.property.SimpleObjectProperty;


/**
 * Created by User on 08/01/2016.
 */
public class UriTableView {
    private final SimpleObjectProperty uri;
    private final SimpleObjectProperty p;

    protected UriTableView(Object uri, Object p) {
        this.uri = new SimpleObjectProperty(uri);
        this.p = new SimpleObjectProperty(p);
    }

    public Object getUri() {
        return uri.get();
    }



    public void setUri(String uri) {
        this.uri.set(uri);
    }


    public Object getP() {
        return p.get();
    }



    public void setP(Object p) {
        this.p.set(p);
    }
}