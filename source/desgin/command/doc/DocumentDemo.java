package desgin.command.doc;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: zl
 * @Date: Create in 2020/10/20 23:17
 * @Description:
 */
public class DocumentDemo {
    public static void main(String[] args) {
        Document document = new Document();
        UserInvoke userInvoke = new UserInvoke();
        userInvoke.addCommand(new CloseCommand(document));
        userInvoke.addCommand(new OpenCommand(document));
    }
}



interface DocumentCommand{
    void execute();
}
//接收命令
class Document{
    public void create(){
        System.out.println("Create");
    }

    public void open(){
        System.out.println("open");
    }

    public void close(){
        System.out.println("Close");
    }

}
class CreateCommand implements DocumentCommand{

    private Document document ;

    public CreateCommand(Document document){
        this.document = document ;
    }


    @Override
    public void execute() {
        document.close();
    }
}

class CloseCommand implements DocumentCommand{

    private Document document ;

    public CloseCommand(Document document){
        this.document = document ;
    }


    @Override
    public void execute() {
        document.close();
    }
}

class OpenCommand implements DocumentCommand{

    private Document document ;

    public OpenCommand(Document document){
        this.document = document ;
    }


    @Override
    public void execute() {
        document.open();
    }
}


class UserInvoke{
    private List<DocumentCommand> lists ;

    public UserInvoke(){
        lists = new ArrayList<>();
    }

    public void addCommand(DocumentCommand documentCommand){
        lists.add(documentCommand);
    }

    public void removeCommand(DocumentCommand documentCommand){
        lists.remove(documentCommand);
    }

    public void execute(){
        for (DocumentCommand list : lists) {
            list.execute();
        }
    }

}


