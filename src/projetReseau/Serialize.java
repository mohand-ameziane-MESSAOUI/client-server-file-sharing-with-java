package projetReseau;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class Serialize {

    public static final Charset c = Charset.forName("UTF-8");
    private ByteBuffer bb;

    public Serialize(ByteBuffer bb) {
        this.bb = bb;
    } // Constructor

    public void writeInt(int i) {
        bb.putInt(i);
    } // Serialize integer

    public void writeLong(long l) {
        bb.putLong(l);
    } // Serialize long

    public void writeString(String s) {
        // Serialize string

        ByteBuffer cb = c.encode(s);
        bb.putInt(cb.remaining());
        bb.put(cb);
    }

    public void writeFile(String nom, long sizeFile, long posFile, int taille) throws IOException {
        // Serialize File

        byte[] tabByte = new byte[20480000];

        for (int k = 0; k < Server.listOfFiles.length; k++) {

            if (Server.listOfFiles[k].isFile()) {
                System.out.println("nom" + Server.listOfFiles[k].getName());
                System.out.println("nom" + nom);
                if (nom.equals(Server.listOfFiles[k].getName())) {
                    System.out.println("jai trouver le bon fichier");
                    FileInputStream fis = new FileInputStream(Server.listOfFiles[k]);
                    fis.read(tabByte);
                    for (int x = (int) posFile; x < posFile + taille; x++) {
                        bb.put(tabByte[x]);
                    }
                }

            }
        }

    }

}
