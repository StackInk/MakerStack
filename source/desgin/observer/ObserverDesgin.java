package desgin.observer;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: zl
 * @Date: Create in 2020/9/2 22:19
 * @Description:
 */
public class ObserverDesgin {
    public static void main(String[] args) {
        Button button = new Button();
        button.addListener(e -> System.out.println("Button press"));
        button.addListener(e-> System.out.println("Button press 02"));
        button.pressButton();
    }
}



//事件监听机制
interface ActionListener{

    public void actionPerformed(ButtonEvent e) ;

}
//事件源对象
class Button{
    private List<ActionListener> list = new ArrayList<>();

    public void addListener(ActionListener actionListener){
        list.add(actionListener);
    }

    public void pressButton(){
        ButtonEvent buttonEvent = new ButtonEvent(this);
        for (ActionListener actionListener : list) {
            actionListener.actionPerformed(buttonEvent);
        }
    }

}

//事件对象
interface Event<T>{
    T getSource();
}

class ButtonEvent implements Event<Button>{
    private Button button ;

    public ButtonEvent(Button button){
        this.button = button ;
    }

    @Override
    public Button getSource() {
        return button;
    }
}

