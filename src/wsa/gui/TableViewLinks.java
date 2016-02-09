package src.wsa.gui;

import javafx.beans.property.SimpleStringProperty;

/** Oggetto contenuto in una riga di Tableview*/
public class TableViewLinks {
    private final SimpleStringProperty uri;
    private final SimpleStringProperty seguito;
    private final SimpleStringProperty errore;

    /** Costruttore per TableViewLinks
     * @param uri URI alla quale si riferisce
     * @param seguito esito del Predicate del SiteCrawler
     * @param errore eventuale eccezione, puo' essere null
     */
    public TableViewLinks(String uri, String seguito, String errore) {
        this.uri = new SimpleStringProperty(uri);
        this.seguito = new SimpleStringProperty(seguito);
        this.errore = new SimpleStringProperty(errore);
    }


    /** Getter per seguito
     * @return seguito, come String*/
    public String getSeguito() {
        return seguito.get();
    }

    /** Setter per seguito
     * @param seguito il nuovo valore per seguito*/
    public void setSeguito(String seguito) {
        this.seguito.set(seguito);
    }

    /** Getter per uri
     * @return uri, come String*/
    public String getUri() {
        return uri.get();
    }

    /** Setter per uri
     * @param uri il nuovo valore per uri
     */
    public void setUri(String uri) {
        this.uri.set(uri);
    }

    /** Getter per errore
     * @return errore, come String*/
    public String getErrore() {
        return errore.get();
    }

    /** Setter per errore
     * @param errore il nuovo valore per errore*/
    public void setErrore(String errore) {
        this.errore.set(errore);
    }
}
