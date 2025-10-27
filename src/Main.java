import compiler_lexer.Scanner;
import compiler_lexer.Token;
import compiler_lexer.TokenType;
import compiler_sintactic.*;
import compiler_semantic.*;

import java.io.IOException;
import java.nio.file.*;

public class Main {

    public static void main(String[] args) throws SemanticError {

        String[] folders = {"test/success", "test/error"};

        for (String folder : folders) {
            System.out.println("Analizando carpeta: " + folder);

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(folder), "*.txt")) {

                for (Path path : stream) {
                    String fileName = path.getFileName().toString();
                    System.out.println("\nCaso de prueba: " + fileName);

                    System.out.println("Contenido:");
                    System.out.println("----------------------------------------");
                    String content = Files.readString(path);
                    System.out.println(content.trim());
                    System.out.println("----------------------------------------");

                    boolean lexicalOK = false;
                    boolean syntacticOK = false;

                    System.out.println("Análisis léxico:");
                    Scanner scanner = new Scanner(content);
                    Token token;
                    boolean lexError = false;
                    do {
                        token = scanner.nextToken();
                        if (token.getType() == TokenType.ERROR) {
                            System.out.println("Error léxico: " + token.getLexeme());
                            lexError = true;
                        }
                    } while (token.getType() != TokenType.EOF);

                    if (!lexError) {
                        lexicalOK = true;
                        System.out.println("Léxico correcto");
                    } else {
                        System.out.println("Error en análisis léxico");
                    }

                    if (!lexicalOK) continue;

                    System.out.println("Análisis sintáctico:");
                    try {
                        SyntacticAnalyzer parser = new SyntacticAnalyzer(content);
                        parser.parseProgram();
                        syntacticOK = true;
                        System.out.println("Sintáctico correcto");
                    } catch (SyntacticError e) {
                        System.out.println("Error sintáctico: " + e.getMessage());
                    }

                    if (!syntacticOK) continue;

                    System.out.println("Análisis semántico:");
                    try {
                        SemanticAnalyzer semantic = new SemanticAnalyzer(content);
                        semantic.analyze();
                        System.out.println("Semántico correcto");
                    } catch (SemanticError e) {
                        System.out.println(e.getMessage());
                        System.out.println("Error semántico");
                    }


                    System.out.println("Resultado final: hubo errores en alguna fase");

                }

            } catch (IOException e) {
                System.out.println("No se pudo abrir la carpeta " + folder);
            }
        }

        System.out.println("\nAnálisis completado");
    }
}
