package src.wsa.web.html;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HTMLNode extends Parsed.Node {
    private List<HTMLNode> children = new ArrayList<>();
    HTMLNode(String t, Map<String, String> a, String c) {
        super(t, a, c);
    }
    HTMLNode(String t, Map<String, String> a, String c, List<HTMLNode> children) {
        super(t, a, c);
        this.children = children;
    }
    public void setChildren(List<HTMLNode> children){
        this.children = children;
    }
    public List<HTMLNode> getChildren(){
        return this.children;
    }
    public void addChildren(HTMLNode node){
        children.add(node);
    }
}