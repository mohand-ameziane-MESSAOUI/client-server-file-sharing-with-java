package projetReseau;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Server implements Runnable {

    public static String path = Paths.get(".").toAbsolutePath().normalize().toString() + "/File";
    public static Map<String, Integer> ListPair = new HashMap<String, Integer>(); //Liste des pairs
    public static File folder = new File(path);
    public static File[] listOfFiles = folder.listFiles(); //Liste des fichiers
    ServerSocketChannel ssc;
    Selector selector;
    ByteBuffer bb;
    Serialize serialize;
    Deserialize deserialize;


    public Server(int port) throws IOException {
        ssc = ServerSocketChannel.open();
        selector = Selector.open();
        bb = ByteBuffer.allocateDirect(4048);

        SocketAddress sa = new InetSocketAddress(port);
        ssc.bind(sa);
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        serialize = new Serialize(bb);
        deserialize = new Deserialize(bb);
    }

    public void sendFile(String nom, long sizeFile, long posFile, int taille, SocketChannel clientsock) throws IOException {
        bb.clear();
        bb.put((byte) 8);
        serialize.writeString(nom);
        serialize.writeLong(sizeFile);
        serialize.writeLong(posFile);
        serialize.writeInt(taille);
        serialize.writeFile(nom, sizeFile, posFile, taille);

        bb.flip();
        clientsock.write(bb);
    }

    public synchronized void send(int i, SocketChannel clientsock) throws IOException {
        bb.clear();
        if (i == 1) {
            bb.put((byte) 1);
            serialize.writeString("vous �tes connecter");
            System.out.println("port envoyé");
            bb.flip();
            clientsock.write(bb);
        }


        if (i == 4) {

            bb.put((byte) 4);
            this.serialize.writeInt(Server.ListPair.size());
            Iterator<String> tableauKey = Server.ListPair.keySet().iterator();
            String Key;
            while (tableauKey.hasNext()) {
                Key = tableauKey.next();
                this.serialize.writeInt(Server.ListPair.get(Key));
                this.serialize.writeString(Key);
                System.out.println("Key");
            }

            bb.flip();
            clientsock.write(bb);
        }


        if (i == 6) {
            bb.put((byte) 6);
            System.out.println("la taille des fichier" + Server.listOfFiles.length);
            bb.putInt(1);
            for (int k = 0; k < Server.listOfFiles.length; k++) {
                if (Server.listOfFiles[k].isFile()) {
                    System.out.println("file name send" + Server.listOfFiles[k].getName());
                    System.out.println("file name send" + Server.listOfFiles[k].length());
                    this.serialize.writeString(Server.listOfFiles[k].getName());
                    this.serialize.writeLong(Server.listOfFiles[k].length());

                }
            }
            bb.flip();
            clientsock.write(bb);

        }


        if (i > 8) {
            bb.put((byte) i);
            bb.flip();
            clientsock.write(bb);
        }

    }

    public synchronized void receive(int i, SocketChannel clientsock) throws IOException {
        if (i == 2) {
            System.out.println("receive ID 2");
            int port = deserialize.readInt();
            Server.ListPair.put(clientsock.socket().getInetAddress().getHostName(), port);
            System.out.println("ID=" + i + " " + port);
        }

        if (i == 3) {
            System.out.println("receive ID 3");
            send(4, clientsock);
        }
        if (i == 4) {
            System.out.println("receive ID 4");
            int nbrpair = deserialize.readInt();
            for (int j = 0; j < nbrpair; j++) {
                // String addr = this.readString();
                int port = deserialize.readInt();
                String adresse = deserialize.readString();
                Server.ListPair.put(adresse, port);
                System.out.println("[" + nbrpair + "," + "[" + port + "," + adresse + "]]");
            }
        }

        if (i == 5) {
            System.out.println("receive ID 5");
            send(6, clientsock);
        }

        if (i == 6) {
            System.out.println("receive ID 6");

            int nbrfile = deserialize.readInt();
            for (int j = 0; j < nbrfile; j++) {
                System.out.println(
                        "[" + nbrfile + "," + "[" + deserialize.readString() + "," + deserialize.readLong() + "]]");
            }
        }

        if (i == 7) {
            System.out.println("receive ID 7");

            System.out.println("la 7 est recu ");
            String nom = deserialize.readString();
            System.out.println("nom du fichier demandee :" + nom);
            Long sizeFile = deserialize.readLong();
            System.out.println("taille total du fichier demaandee :" + sizeFile);
            Long posFile = deserialize.readLong();
            System.out.println("position de debut du fichier demander :" + posFile);
            int taille = deserialize.readInt();
            System.out.println("taille du fragment demander :" + taille);
            sendFile(nom, sizeFile, posFile, taille, clientsock);

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

        if (i > 8) {
            if (i >= 64 && i < 128) {
                System.out.println("message d'extension");
            } else {
                clientsock.close();
            }
        }
    }

    public synchronized void accept() throws IOException {
        SocketChannel sc = ssc.accept();
        System.out.println("Nouvelle connection" + sc);
        bb.put((byte) 1);
        String response = "Connected to " + ssc.getLocalAddress().toString() + " From " + sc.getRemoteAddress().toString();
        serialize.writeString(response);
        bb.flip();
        sc.write(bb);
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ);
        bb.clear();


    }

    public synchronized void repeat(SelectionKey sk) {

        SocketChannel clientsock = (SocketChannel) sk.channel();
        try {
            int n = clientsock.read(bb);
            if (n < 0) {
                System.out.println("Deconnection " + sk.channel());
                sk.cancel();
                clientsock.close();
                return;
            }
        } catch (IOException e) {
            sk.cancel();
        }

        try {
            if (sk.channel() != ssc && sk.isValid()) {
                bb.rewind();

                int i = bb.get();
                System.out.println(i);
                receive(i, clientsock);

            }
        } catch (IOException e) {
            System.out.println("Deconnection " + sk.channel());
            sk.cancel();
            try {
                sk.channel().close();
            } catch (IOException ex) {

            }
        }


        bb.clear();
    }

    public void run() {
        while (true) {


            try {
                selector.select();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            for (SelectionKey sk : selector.selectedKeys()) {
                if (sk.isAcceptable()) {
                    try {
                        accept();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if (sk.isValid()) {
                    repeat(sk);
                }
            }


            selector.selectedKeys().clear();


        }

    }

    public static void main(String[] args) throws IOException {

        Server s = new Server(1111);
        Thread th = new Thread(s);
        th.run();
    }


}
