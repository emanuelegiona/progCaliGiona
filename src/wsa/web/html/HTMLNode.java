package src.wsa.web.html;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Estende la classe interna a Parsed. Rappresenta un singolo nodo dell'albero.*/
public class HTMLNode extends Parsed.Node {
    private List<HTMLNode> children = new ArrayList<>();

    /**Costruttore di HTMLNode
     * @param t tag del nodo
     * @param a attributi del nodo
     * @param c contenuto del nodo, se testo, altrimenti e' null.*/
    HTMLNode(String t, Map<String, String> a, String c) {
        super(t, a, c);
    }

    /** Imposta i figli di un nodo.
     * @param children lista dei figli da impostare*/
    public void setChildren(List<HTMLNode> children){
        this.children = children;
    }

    /** Ritorna la lista dei figli di un nodo.
     * @return la lista dei figli HTMLNode.*/
    public List<HTMLNode> getChildren(){
        return this.children;
    }

    /** Aggiunge un singolo HTMLNode come figlio al nodo.
     * @param node il nodo da aggiungere.*/
    public void addChildren(HTMLNode node){
        children.add(node);
    }
}