package desgin.composite;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: zl
 * @Date: Create in 2020/10/4 20:31
 * @Description: 组合设计模式
 */
public class CompositeDesgin {
    public static void main(String[] args) {
        MiddleNode node = new MiddleNode("root");
        MiddleNode node1 = new MiddleNode("chapter1");
        MiddleNode node2 = new MiddleNode("chapter2");
        Node node1_1 = new ConentNode("content1_1");
        Node node1_2 = new ConentNode("content1_2");
        Node node1_3 = new ConentNode("content1_3");
        Node node2_1 = new ConentNode("content2_1");
        Node node2_2 = new ConentNode("content2_2");
        node.addNode(node1).addNode(node2);
        node1.addNode(node1_1).addNode(node1_2).addNode(node1_3);
        node2.addNode(node2_1).addNode(node2_2);
        printNodeContent(node,0);
    }

    static void printNodeContent(Node node,int depth){
        for (int i = 0; i < depth; i++) {
            System.out.print("--");
        }
        node.print();
        if(node instanceof MiddleNode){
            MiddleNode node1  = (MiddleNode)node ;
            for (Node node2 : node1.getListNodes()){
                printNodeContent(node2,depth+1);
            }
        }
    }

}

abstract class Node {
    abstract void print();
}


class ConentNode extends Node {
    private String content ;
    public ConentNode(String content){ this.content = content ; }
    void print() {
        System.out.println(this.content);
    }
}

class MiddleNode extends Node {
    private List<Node> listNodes = new ArrayList<>();
    private String content ;
    public List<Node> getListNodes(){return listNodes;}
    public MiddleNode(String content){ this.content = content ; }
    void print() {
        System.out.println(this.content);
    }
    public MiddleNode addNode(Node node){
        this.listNodes.add(node);
        return this ;
    }
}

