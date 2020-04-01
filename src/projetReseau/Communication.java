package projetReseau;

import javax.swing.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Communication implements Runnable {
    public static final Charset c = Charset.forName("UTF-8");
    Client cl;
    Scanner input = new Scanner(System.in);

    Serialize serialize;
    Deserialize deserialize;
    int g;
    private ByteBuffer bb;
    public boolean go = true;
    private Lock lock = new ReentrantLock();
    public boolean clickButton = true;


    public Communication(Client cl) {
        bb = ByteBuffer.allocateDirect(400000048);
        this.cl = cl;
        serialize = new Serialize(bb);
        deserialize = new Deserialize(bb);
        Interface anInterface = new Interface(this);
        JFrame frame = new JFrame("PeerToPeer");
        frame.setContentPane(anInterface.PeerToPeer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }


    public void sendFile(String nom, long sizeFile, long posFile, int taille) throws IOException {
        bb.clear();
        bb.put((byte) 8);
        serialize.writeString(nom);
        serialize.writeLong(sizeFile);
        serialize.writeLong(posFile);
        serialize.writeInt(taille);
        serialize.writeFile(nom, sizeFile, posFile, taille);

        bb.flip();
        cl.socket.write(bb);
    }

    public void demandeFile(String nameFile, Long tailleFile, Long tailleDebut, int tailleFragment) throws IOException {
        bb.put((byte) 7);

        serialize.writeString(nameFile);

        serialize.writeLong(tailleFile);

        serialize.writeLong(0);

        serialize.writeInt(tailleFragment);

        bb.flip();
        cl.socket.write(bb);
        bb.clear();
        System.out.println("jai envoyer la demande de fichier");
    }

    public synchronized void send(int i) throws IOException {

        bb.clear();
        if (i == 2) {
            bb.put((byte) 2);
            serialize.writeInt(1111);

            System.out.println("port envoyé");
            bb.flip();
            cl.socket.write(bb);
        }

        if (i == 3) {
            System.out.println("envoie l'ID 3");
            bb.put((byte) 3);
            bb.flip();
            cl.socket.write(bb);
        }

        if (i == 4) {
            System.out.println("j'envoie la liste des paire ");
            bb.put((byte) 4);
            this.serialize.writeInt(Server.ListPair.size());
            Iterator<String> tableauKey = Server.ListPair.keySet().iterator();
            String Key;
            while (tableauKey.hasNext()) {
                Key = tableauKey.next();
                this.serialize.writeInt(Server.ListPair.get(Key));
                this.serialize.writeString(Key);
            }
            bb.flip();
            cl.socket.write(bb);
        }
        if (i == 5) {
            bb.put((byte) 5);
            bb.flip();
            cl.socket.write(bb);
        }

        if (i == 6) {
            bb.put((byte) 6);
            System.out.println("la taille des fichier" + Server.listOfFiles.length);
            bb.putInt(Server.listOfFiles.length);
            for (int k = 0; k < Server.listOfFiles.length; k++) {
                if (Server.listOfFiles[k].isFile()) {
                    this.serialize.writeString(Server.listOfFiles[k].getName());
                    this.serialize.writeLong(Server.listOfFiles[k].length());

                }
            }
            bb.flip();
            cl.socket.write(bb);
            // System.out.println("jai envoyer les fichier" );

        }

        if (i == 7) {
            System.out.println("saisissez le nom du fichier");
            String nameFile = input.next();
            System.out.println("saisissez la taille du fichier");
            Long tailleFile = input.nextLong();
            System.out.println("saisissez la taille du debut de fragment");
            Long tailleDebut = input.nextLong();
            System.out.println("saisissez la taille du fragment");
            int tailleFragment = input.nextInt();

            demandeFile(nameFile, tailleFile, tailleDebut, tailleFragment);


        }

        if (i > 8) {
            bb.put((byte) i);
            bb.flip();
            cl.socket.write(bb);
        }

    }

    public synchronized void receive(int i) throws IOException {

        if (i == 1) {
            String s = deserialize.readString();
            System.out.println("ID=" + i + " " + s);
        }

        if (i == 3) {
            System.out.println("receive ID 3");
            send(4);
        }
        if (i == 4) {
            System.out.println("receive ID 4");
            int nbrpair = deserialize.readInt();
            for (int j = 0; j < nbrpair; j++) {

                int port = deserialize.readInt();
                String adresse = deserialize.readString();
                Server.ListPair.put(adresse, port);
                System.out.println("[" + nbrpair + "," + "[" + port + "," + adresse + "]]");
            }
        }

        if (i == 5) {
            System.out.println("receive ID 5");
            send(6);
        }

        if (i == 6) {
            System.out.println("receive ID 6");

            int nbrfile = deserialize.readInt();
            for (int j = 0; j < nbrfile; j++) {
                // long port = this.readLong();
                // String addr = this.readString();
                System.out.println(
                        "[" + nbrfile + "," + "[" + deserialize.readString() + "," + deserialize.readLong() + "]]");
            }
        }

        if (i == 7) {
            System.out.println("receive ID 7");

            String nom = deserialize.readString();

            Long sizeFile = deserialize.readLong();
            System.out.println("taille total du fichier demaandee :" + sizeFile);
            Long posFile = deserialize.readLong();
            System.out.println("position de debut du fichier demander :" + posFile);
            int taille = deserialize.readInt();
            System.out.println("taille du fragment demander :" + taille);
            sendFile(nom, sizeFile, posFile, taille);


        }

        if (i == 8) {
            System.out.println("receive ID 8");
            String nom = deserialize.readString();
            System.out.println("nom du fichier :" + nom);
            Long sizeFile = deserialize.readLong();
            System.out.println("taille total du fichier :" + sizeFile);
            Long posFile = deserialize.readLong();
            System.out.println("position de debut du fichier demander :" + posFile);
            int taille = deserialize.readInt();
            System.out.println("taille du fragment demander :" + taille);

            FileOutputStream fos = this.deserialize.readFile(nom, sizeFile, posFile, taille);

        }

    }

    @Override
    public synchronized void run() {
        try {
            while (cl.socket.isConnected()) {

                bb.clear();
                if (g == 2) {
                    bb.flip();
                    System.out.println("saisir un ID2 a envoyer");
                    g = input.nextInt();
                    System.out.println("ID saisi" + g);
                    send(g);
                } else {

                    int n = cl.socket.read(bb);

                    if (n < 0) {
                        System.out.println("Deconnection -1");
                        cl.socket.close();
                        cl.setConnected(false);
                        return;
                    }

                    bb.flip();


                    int i = bb.get();
                    System.out.println("le serveur ma demander l'ID " + i);

                    receive(i);

                    Scanner();


                }
            }
        } catch (IOException e) {
            System.out.println("Deconnection Exception" + e);
            cl.setConnected(false);
        }
    }

    public void Scanner() throws IOException {

        if (this.clickButton == true) {
            input = new Scanner(System.in);
            System.out.println("saisir un ID a envoyer");
            g = input.nextInt();
            send(g);
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner portAdresse = new Scanner(System.in);
        System.out.println("saisir une Adresse d'un paire");
        String Adresse = portAdresse.next();
        System.out.println("saisir le port du paire");
        int port = portAdresse.nextInt();
        Client c = new Client(Adresse, port);
        //Client c = new Client("prog-reseau-m1.lacl.fr", 5486);
        // Client c = new Client("localhost", 1111);
        // Client c = new Client("10.188.133.72", 1111);
        Communication co = new Communication(c);

        Thread th1 = new Thread(co);
        th1.run();


    }
}
