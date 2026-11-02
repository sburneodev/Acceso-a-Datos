import java.nio.file.*;

public class Comparar {
    public static void main(String[] args) throws Exception {
        long pos = Files.mismatch(Path.of(args[0]), Path.of(args[1]));
        System.out.println(pos == -1 ? "IDÉNTICOS" : "DIFERENTES, primera diferencia en el byte " + pos);
    }
}