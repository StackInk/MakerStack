package desgin.observer.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @Author: zl
 * @Date: Create in 2020/9/3 21:15
 * @Description:
 */
public class SwingFrame extends JFrame {
    private final int WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width ;
    private final int HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height ;

    public SwingFrame() {
        JPanel jp = new JPanel();
        jp.setSize(600,200);
        JButton button = new JButton();
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        jp.add(button);

        this.add(jp);
        this.setSize(600, 550);
        this.setLocation((WIDTH-600)/2, (HEIGHT - 550)/2);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }
    public static void main(String[] args) {
        new SwingFrame();
    }
}
