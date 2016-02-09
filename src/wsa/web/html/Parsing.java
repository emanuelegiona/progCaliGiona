package src.wsa.web.html;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Rappresenta l'albero di analisi sintattica (parsing) di una pagina web */
public class Parsing implements Parsed {
    private HTMLNode ParsedTree;
    private Map<String, String> attributes = new HashMap<>();

    /** Costruttore che costruisce l'intero albero, selezionando la radice corretta.
     * @param d Document della pagina*/
    public Parsing(Document d){
        NodeList nodelist = d.getChildNodes();
        org.w3c.dom.Node firstRoot = null;
        for (int i = 0; i < nodelist.getLength(); i++)
            if (nodelist.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
                firstRoot = nodelist.item(i);

        ParsedTree = parse(firstRoot);
        attributes = null;
    }

    /** Esegue il parsing della pagina, in modo ricorsivo, partendo da un eventuale nodo radice.
     * @param node nodo di tipo org.w3c.dom.Node
     * @return node di tipo HTMLNode, che rappresenta la pagina.*/
     private HTMLNode parse(org.w3c.dom.Node node){
        List<HTMLNode> list = new ArrayList<>();
        NodeList nodelist = node.getChildNodes();
        NamedNodeMap nodemap = node.getAttributes();
        HTMLNode root = new HTMLNode(node.getNodeName(), getAttributes(nodemap), node.getNodeValue());
        org.w3c.dom.Node v;
        for (int i=0; i< nodelist.getLength(); i++){
            v = nodelist.item(i);
            root.addChildren(new HTMLNode(v.getNodeName(), getAttributes(v.getAttributes()), v.getNodeValue()));
            list.add(parse(v));
        }
        root.setChildren(list);
        return root;
    }

    /** Rimappa una nodemap in una Map.
     * @param nodeMap l'oggetto di tipo NamedNodeMap da rimappare
     * @return una Map con i valori all'interno della nodemap in input*/
    Map<String, String> getAttributes(NamedNodeMap nodeMap){
        Map<String, String> attr = new HashMap<>();
        if (nodeMap != null) {
            for (int i = 0; i < nodeMap.getLength(); i++) {
                org.w3c.dom.Node n = nodeMap.item(i);
                attr.put(n.getNodeName(), n.getNodeValue());
            }
            this.attributes = attr;
        }
        return attributes;
    }

    /** Esegue la visita dell'intero albero di parsing.
     * @param visitor  visitatore invocato su ogni nodo dell'albero */
    @Override
    public void visit(Consumer<Node> visitor) {
        trueVisit(visitor, ParsedTree);
    }

    /** Applica il consumer a tutti i nodi dell'albero a partire dalla radice.
     * @param visitor un consumer da applicare
     * @param node la radice dell'albero*/
    private void trueVisit(Consumer<Node> visitor, HTMLNode node){
        visitor.accept(node);
        for (HTMLNode nodo : node.getChildren())
            trueVisit(visitor, nodo);
    }

    /** Applica visit con un consumer per prelevare i link della pagina
     * @return la lista dei link della pagina*/
    @Override
    public List<String> getLinks() {
        List<String> links = new ArrayList<>();
        Consumer<Node> consumer = (node) -> {
            if (node.tag.toUpperCase().equals("A"))
                if (node.attr.get("href") != null)
                    links.add(node.attr.get("href"));
        };
        visit(consumer);
        return links;
    }

    /** Applica un consumer a visit per trovare i tag desiderati
     * @param tag un nome di tag
     * @return la lista dei nodi con tag "tag"*/
    @Override
    public List<Node> getByTag(String tag) {
        String tagUpperCase = tag.toUpperCase();
        List<Node> StringsByTag = new ArrayList<>();
        Consumer<Node> consumer = (node) -> {
            if (node.tag.toUpperCase().equals(tagUpperCase))
                StringsByTag.add(node);
        };
        visit(consumer);
        return StringsByTag;
    }
}
