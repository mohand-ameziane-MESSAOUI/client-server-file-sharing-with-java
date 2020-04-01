package projetReseau;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class Client {
    public static final Charset charset = Charset.forName("UTF-8");

    boolean isConnected = false;
    SocketChannel socket;

    public Client(String url, int port) throws IOException {
        SocketAddress sa = new InetSocketAddress(url, port);
        socket = SocketChannel.open();
        socket.configureBlocking(true);
        socket.connect(sa);
        isConnected = true;
    }

    public void setConnected(boolean is) {
        isConnected = is;
    }


}