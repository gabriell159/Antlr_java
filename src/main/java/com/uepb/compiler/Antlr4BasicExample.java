package com.uepb.compiler;

import java.io.File;
import java.io.IOException;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import com.uepb.ExprLexer;
import com.uepb.ExprParser;
import com.uepb.gui.GuiVizualizerTask;
import com.uepb.interfaces.CompilerEngine;

public class Antlr4BasicExample implements CompilerEngine{

    @Override
    public void execute(File input, File output, boolean verbose) throws IOException {
        var charStream = CharStreams.fromPath(input.toPath());
        var lexer = new ExprLexer(charStream);
        var tokens = new CommonTokenStream(lexer);
        var parser = new ExprParser(tokens);
        var tree = parser.prog();

        // 1. Mostra a interface se o verbose for true
        if(verbose){
            var guiTask = new GuiVizualizerTask(parser, tree);
            guiTask.run(); // Aqui ele pausa até o ENTER
        }

        // 2. AGORA SIM: Chame a sua Calculadora para validar e gerar código
        Calculadora calculadora = new Calculadora();
        calculadora.visit(tree); // <--- É AQUI que o RuntimeException vai estourar!

        // 3. (Opcional) Salvar o P-Code gerado no arquivo de saída
        String pCode = calculadora.getCode();
        java.nio.file.Files.writeString(output.toPath(), pCode);
        
        System.out.println("Compilação finalizada com sucesso. Arquivo gerado: " + output.getName());
    }

}
