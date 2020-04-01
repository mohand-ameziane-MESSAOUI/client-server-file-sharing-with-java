package projetReseau;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Interface {
    public JPanel PeerToPeer;
    Communication communication;
    private JButton Attendre;

    public Interface(Communication communication) {
        this.communication = communication;

        Attendre = new JButton("Atendre/lancer");
        PeerToPeer = new JPanel();

        PeerToPeer.add(Attendre);

        Attendre.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                communication.clickButton = !communication.clickButton;


            }
        });

    }
}
