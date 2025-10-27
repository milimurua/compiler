package compile_sintactic.src;

public class ParserTest {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java compile_sintactic.src.ParserTest <archivo>");
            return;
        }
        SyntacticAnalyzer.analyzeFile(args[0]);
    }
}
