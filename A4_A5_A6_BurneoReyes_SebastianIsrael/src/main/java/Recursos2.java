public class Recursos2 {

    static class Recurso implements AutoCloseable {
        public void close() {
            throw new IllegalStateException("Fallo al cerrar");
        }
    }

    public static void main(String[] args) {

        System.out.println("--- Con try-with-resources ---");
        try (Recurso r = new Recurso()) {
            throw new IllegalArgumentException("Fallo en el cuerpo");
        } catch (Exception e) {
            System.out.println("Llega: " + e.getMessage());
            for (Throwable s : e.getSuppressed()) {
                System.out.println("Suprimida: " + s.getMessage());
            }
        }

        System.out.println("--- Con try/finally y close() a mano ---");
        try {
            Recurso r = new Recurso();
            try {
                throw new IllegalArgumentException("Fallo en el cuerpo");
            } finally {
                r.close();
            }
        } catch (Exception e) {
            System.out.println("Llega: " + e.getMessage());
            System.out.println("Suprimidas: " + e.getSuppressed().length);
        }
    }
}