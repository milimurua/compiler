# compilar
javac -d build compile_sintactic/src/*.java

# ejecutar (caso de éxito)
java -cp build compile_sintactic.src.ParserTest test/success/case1.txt

# ejecutar (caso de error)
java -cp build compile_sintactic.src.ParserTest test/error/case1.txt
