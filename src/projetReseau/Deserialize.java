package projetReseau;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class Deserialize {
    public static final Charset c = Charset.forName("UTF-8");
    private ByteBuffer bb;

    public Deserialize(ByteBuffer bb) {
        this.bb = bb;
    } //Constructeur

    public int readInt() {
        return bb.getInt();
    } // Deserialize integer

    public long readLong() {
        return bb.getLong();
    }  // Deserialize long

    public String readString() {
        // Deserialize string
        int n = bb.getInt();
        int limit = bb.limit();
        bb.limit(bb.position() + n);
        String s = c.decode(bb).toString();
        bb.limit(limit);

        return s;
    }

    public FileOutputStream readFile(String nom, Long sizeFile, Long posFile, int taille) throws IOException {
        // Deserialize file

        File file = new File("C:/Users/Mohand Ameziane/Desktop/Git Project/yanis-tagherset-mohand-ameziane-messaoui/File/" + nom);

        FileOutputStream fos = new FileOutputStream(file);
        if (!file.exists()) {
            file.createNewFile();
        }
        byte[] tabByte = new byte[20480000];
        System.out.println("position bytebuffer :" + bb.position());
        System.out.println("nombre de pair :" + bb.limit());

        for (int k = 0; k < taille - 1; k++) {

            tabByte[k] = bb.get();
            fos.write(tabByte[k]);
        }
        return fos;
    }

}