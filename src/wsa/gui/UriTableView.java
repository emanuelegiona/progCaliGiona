package src.wsa.gui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import java.net.URI;

public class UriTableView {
    private final SimpleObjectProperty uri;
    private final SimpleStringProperty stato;


    public UriTableView(URI uri, String stato) {
        this.uri = new SimpleObjectProperty(uri);
        this.stato = new SimpleStringProperty(stato);
    }

    public URI getUri() {
        return (URI)uri.get();
    }

    public void setUri(String uri) {
        this.uri.set(uri);
    }

    public String getStato() {
        return stato.get();
    }

    public void setStato(String stato) {
        this.stato.set(stato);
    }
}