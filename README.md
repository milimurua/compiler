## Compilación

Desde la carpeta raíz del proyecto (`compiler/`), ejecutar:

```bash
javac -d build src/*.java
```
🔹 Esto compila todos los archivos .java dentro de src/ y genera los .class dentro de la carpeta build/.
🔹 Si la carpeta build/ no existe, se crea automáticamente.

## Ejecución
Para ejecutar el analizador léxico con un archivo de prueba:

bash
Copy code
java -cp build LexerTest test/success/case1.txt
Para probar un caso con error:

```bash
java -cp build LexerTest test/error/case1.txt
```

Los tokens reconocidos.
Mensajes de error léxico si existen.

## Crear un archivo .jar ejecutable (opcional)
Si se desea generar un archivo .jar ejecutable:

Crear un archivo manifest.txt con este contenido:

```css
Main-Class: LexerTest
```
Generar el .jar:
```bash
jar cfm lexer.jar manifest.txt -C build .
```
Ejecutar el .jar:

bash
```
java -jar lexer.jar test/success/case1.txt
```

## Casos de prueba
Los casos de prueba se encuentran dentro de las carpetas:

bash
```
test/success/
test/error/
```
