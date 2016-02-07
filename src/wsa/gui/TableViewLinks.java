package src.wsa.gui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.net.URI;

/**
 * Created by User on 03/02/2016.
 */
public class TableViewLinks {
    private final SimpleStringProperty uri;
    private final SimpleStringProperty seguito;
    private final SimpleStringProperty errore;

    public TableViewLinks(String uri, String seguito, String errore) {
        this.uri = new SimpleStringProperty(uri);
        this.seguito = new SimpleStringProperty(seguito);
        this.errore = new SimpleStringProperty(errore);
    }


    public String getSeguito() {
        return seguito.get();
    }


    public void setSeguito(String seguito) {
        this.seguito.set(seguito);
    }

    public String getUri() {
        return uri.get();
    }



    public void setUri(String uri) {
        this.uri.set(uri);
    }

    public String getErrore() {
        return errore.get();
    }


    public void setErrore(String errore) {
        this.errore.set(errore);
    }
}
