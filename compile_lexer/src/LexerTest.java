import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LexerTest {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java LexerTest <archivo.txt>");
            return;
        }

        String path = args[0];
        StringBuilder sb = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo: " + path);
            return;
        }

        Scanner scanner = new Scanner(sb.toString());
        Token token;

        System.out.println("---Analisis Lexico---\n");

        do {
            token = scanner.nextToken();
            System.out.println(token);
        } while (token.getType() != TokenType.EOF && token.getType() != TokenType.ERROR);

        System.out.println("\n----------");
    }
}

