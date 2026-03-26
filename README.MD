# 📚 ANTLR4 Compiler Shell

Este é um projeto base para a disciplina de Compiladores, desenvolvido em Java utilizando o ecossistema Maven. O projeto integra o ANTLR4 para análise léxica e sintática, o Picocli para uma interface de linha de comando robusta e uma interface gráfica customizada para inspeção de árvores sintáticas (Parse Trees).

## Executar projeto

```bash
./mvnw clean package && java -jar target/compiler.jar -v true -i example.expr

 java -jar target/compiler.jar -v -i example.expr
```
```