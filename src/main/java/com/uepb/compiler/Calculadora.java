package com.uepb.compiler;

import com.uepb.ExprBaseVisitor;
import com.uepb.ExprParser;

public class Calculadora extends ExprBaseVisitor<Void> {
    private final ScopeControl scopes = new ScopeControl();
    private final MemoryMapper memoryMapper = new MemoryMapper();
    private final StringBuilder code = new StringBuilder();
    private int labelCounter = 0;

    private String createLabel(){
        var labelGerada = "L" + String.valueOf(labelCounter);
        labelCounter++;
        return labelGerada;
    }

    public String getCode(){
        return code.toString();
    }
    @Override
    public Void visitProg(ExprParser.ProgContext ctx) {
        for (var expressao : ctx.stmt()) {
            visit(expressao);
        }
        code.append("out\n");
        code.append("hlt\n");
        return null;
    }
    @Override
    public Void visitExprParenteses(ExprParser.ExprParentesesContext ctx) {
        visit(ctx.expr());
        return null;
    }
    @Override
    public Void visitPotencia(ExprParser.PotenciaContext ctx) {
        visit(ctx.base()); //esquerda 
        visit(ctx.fator()); //direita 
        code.append("exp\n");
        return null;
    }

    @Override
    public Void visitMultiDiv(ExprParser.MultiDivContext ctx) {
        visit(ctx.termo());
        visit(ctx.fator());

        if (ctx.op.getType() == ExprParser.MUL) {
            code.append("mul\n");
        } else {
            code.append("div\n");
        }

        return null;
    }
    @Override
    public Void visitSomaSub(ExprParser.SomaSubContext ctx) {
        visit(ctx.expr());
        visit(ctx.termo());

        if (ctx.op.getType() == ExprParser.PLUS) {
            code.append("add\n");
        } else {
            code.append("sub\n");
        }
        return null;
    }

    @Override
    public Void visitNumero(ExprParser.NumeroContext ctx) {
        var numeroString = ctx.NUMBER().getText();
        code.append("push ").append(numeroString).append("\n");
        return null;
    }

    @Override
    public Void visitNegacaoUnaria(ExprParser.NegacaoUnariaContext ctx) {
        visit(ctx.fator()); // visita o número ou expressão
        code.append("push -1\n");
        code.append("mul\n");

        return null;
    }

    @Override
    public Void visitUsoVariavel(ExprParser.UsoVariavelContext ctx) {
        var nomeVar = ctx.ID().getText();
        var tk = ctx.ID().getSymbol();
        var declaracaoOpt = scopes.lookup(nomeVar);

        if (declaracaoOpt.isEmpty()) {
            throw new RuntimeException(
                    "A variavel declarada ('%s') não foi declarada na linha %d e coluna %d."
                            .formatted(nomeVar, tk.getLine(), tk.getCharPositionInLine()));
        }

        var address = declaracaoOpt.get().address();
        code.append("push $").append(address).append("\n");
        code.append("lod\n");
        return null;
    }

   @Override
    public Void visitVarDeclComValor(ExprParser.VarDeclComValorContext ctx) {
        String nome = ctx.ID().getText();

        int address = memoryMapper.allocate(); // 🔥 usa o mapper
        scopes.declare(nome, address);         // 🔥 registra no escopo

        visit(ctx.expr());

        code.append("push $").append(address).append("\n");
        code.append("sto\n");

        return null;
    }

    @Override
    public Void visitVarDeclSemValor(ExprParser.VarDeclSemValorContext ctx) {
        String nome = ctx.ID().getText();

        int address = memoryMapper.allocate(); // 🔥 usa o mapper
        scopes.declare(nome, address);         // 🔥 registra no escopo

        code.append("push 0\n"); // valor padrão
        code.append("store ").append(nome).append("\n");

        return null;
    }

    @Override
    public Void visitAtribuicao(ExprParser.AtribuicaoContext ctx) {
        var nomeVar = ctx.ID().getText();
        var tk = ctx.ID().getSymbol();
        var declaracaoOpt = scopes.lookup(nomeVar);

        if (declaracaoOpt.isEmpty()) {
            throw new RuntimeException(
                    "A variavel '%s' não foi declarada na linha %d e coluna %d."
                            .formatted(nomeVar, tk.getLine(), tk.getCharPositionInLine()));
        }

        var address = declaracaoOpt.get().address();

        visit(ctx.expr()); // valor primeiro

        code.append("push $").append(address).append("\n");
        code.append("sto\n");

        return null;
    }

    @Override
    public Void visitInput(ExprParser.InputContext ctx) {
        var nomeVar = ctx.ID().getText();
        var tk = ctx.ID().getSymbol();
        var declaracaoOpt = scopes.lookup(nomeVar);

        if (declaracaoOpt.isEmpty()) {
            throw new RuntimeException(
                    "A variavel '%s' não foi declarada na linha %d e coluna %d."
                            .formatted(nomeVar, tk.getLine(), tk.getCharPositionInLine()));
        }

        var address = declaracaoOpt.get().address();

        code.append("in\n");
        code.append("push $").append(address).append("\n");
        code.append("sto\n");

        return null;
    }

    @Override
    public Void visitPrintTexto(ExprParser.PrintTextoContext ctx) {
        code.append("prts ").append(ctx.STRING().getText()).append("\n");
        return null;
    }

    @Override
    public Void visitPrintExpr(ExprParser.PrintExprContext ctx) {
        visit(ctx.expr());
        code.append("prt\n");
        return null;
    }

    @Override
    public Void visitBlock(ExprParser.BlockContext ctx) {
        scopes.createScope();

        int baseAddress = memoryMapper.getCurrentAddress(); 

        try {
            for (var stmt : ctx.stmt()) {
                visit(stmt);
            }
        } finally {
            scopes.dropScope();
            memoryMapper.restore(baseAddress); 
        }

        return null;
    }

    @Override
    public Void visitIf(ExprParser.IfContext ctx) {
        var fimIf = createLabel();

        visit(ctx.cond());

        code.append("fjp ").append(fimIf).append("\n");

        visit(ctx.block());

        code.append(fimIf).append(":\n");

        return null;
    }

    @Override
    public Void visitWhile(ExprParser.WhileContext ctx) {
        var inicioLoop = createLabel();
        var fimLoop = createLabel();

        code.append(inicioLoop).append(":\n");

        visit(ctx.cond());

        code.append("fjp ").append(fimLoop).append("\n");

        visit(ctx.block());

        code.append("ujp ").append(inicioLoop).append("\n");
        code.append(fimLoop).append(":\n");

        return null;
    }
   @Override
    public Void visitCondComparacao(ExprParser.CondComparacaoContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));

        switch (ctx.condOp().getText()) {
            case "==" -> code.append("equ\n");
            case "!=" -> code.append("neq\n");
            case "<"  -> code.append("ltn\n");
            case "<=" -> code.append("leq\n");
            case ">"  -> code.append("gtn\n");
            case ">=" -> code.append("geq\n");
        }
        return null;
    }

    @Override
    public Void visitCondAnd(ExprParser.CondAndContext ctx) {
        visit(ctx.cond2());
        visit(ctx.cond3());
        code.append("and\n");
        return null;
    }
    @Override
    public Void visitCondOr(ExprParser.CondOrContext ctx) {
        visit(ctx.cond());
        visit(ctx.cond2());
        code.append("or\n");
        return null;
    }
    @Override
    public Void visitCondTrue(ExprParser.CondTrueContext ctx) {
        code.append("push 1\n");
        return null;
    }
    @Override
    public Void visitCondFalse(ExprParser.CondFalseContext ctx) {
        code.append("push 0\n");
        return null;
    }
    @Override
    public Void visitCondParenteses(ExprParser.CondParentesesContext ctx) {
    visit(ctx.cond());
    return null;
    }
}
