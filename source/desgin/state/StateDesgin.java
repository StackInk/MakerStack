package desgin.state;

/**
 * @Author: zl
 * @Date: Create in 2020/10/27 22:12
 * @Description:
 */
public class StateDesgin {
    public static void main(String[] args) {
        TCPConnection tcpConnection = new TCPConnection(new TCPListened());
        tcpConnection.open();


    }
}


class TCPConnection{
    private State state ;

    public TCPConnection(State state){
        this.state = state ;
    }

    public void open(){
        state.open();
    }
    public void close(){
        state.close();
    }
}

interface State{
    void open();
    void close();
}

class TCPEstablished implements State{

    @Override
    public void open() {
        System.out.println("当前状态为正在建立连接状态");
    }

    @Override
    public void close() {

    }
}

class TCPListened implements State{
    @Override
    public void open() {
        System.out.println("当前状态为监听状态");
    }

    @Override
    public void close() {

    }
}

class TCPClosed implements State{
    @Override
    public void open() {
        System.out.println("当前状态为关闭状态");
    }

    @Override
    public void close() {

    }
}
