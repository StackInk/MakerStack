package desgin.command;

/**
 * @Author: zl
 * @Date: Create in 2020/10/20 23:12
 * @Description:
 */
public class CommandDesgin {
    public static void main(String[] args) {
        Invoke invoke = new Invoke(new ConcreteCommand(new Receive()));
        invoke.action();
    }
}

class Invoke{

    private Command command ;
    public Invoke(Command command){
        this.command = command ;
    }
    public void action(){
        System.out.println("调用者执行代码");
        command.execute();
    }

}

interface Command{
    void execute();
}

class ConcreteCommand implements Command{

    private Receive receive ;

    public ConcreteCommand(Receive receive){
        this.receive = receive ;
    }

    @Override
    public void execute() {
        System.out.println("命令执行");
        receive.hello();
    }
}

class Receive{
    public void hello(){
        System.out.println("Hello , this is Receive");
    }
}