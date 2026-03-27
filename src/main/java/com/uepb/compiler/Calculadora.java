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
        int addrBase = memoryMapper.allocate();
        int addrExp = memoryMapper.allocate();
        int addrAcc = memoryMapper.allocate();

        //base
        code.append("push $").append(addrBase).append("\n");
        visit(ctx.base()); 
        code.append("sto\n");

        //expoente
        code.append("push $").append(addrExp).append("\n");
        visit(ctx.fator()); 
        code.append("sto\n");

        // acumulador
        code.append("push $").append(addrAcc).append("\n");
        code.append("push 1\n");
        code.append("sto\n");

        String inicioLoop = createLabel();
        String fimLoop = createLabel();
        code.append(inicioLoop).append(":\n");

        code.append("push $").append(addrExp).append("\n");
        code.append("lod\n");
        code.append("push 0\n"); 
        code.append("grt\n");
        code.append("fjp ").append(fimLoop).append("\n");

        code.append("push $").append(addrAcc).append("\n");
        code.append("push $").append(addrAcc).append("\n");
        code.append("lod\n");
        code.append("push $").append(addrBase).append("\n");
        code.append("lod\n");
        code.append("mul\n");
        code.append("sto\n");

        code.append("push $").append(addrExp).append("\n");
        code.append("push $").append(addrExp).append("\n");
        code.append("lod\n");
        code.append("push 1\n");
        code.append("sub\n");
        code.append("sto\n");

        code.append("ujp ").append(inicioLoop).append("\n");
        code.append(fimLoop).append(":\n");
        code.append("push $").append(addrAcc).append("\n");
        code.append("lod\n");
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

        int address = memoryMapper.allocate(); // usa o mapper
        scopes.declare(nome, address);         // registra no escopo

        code.append("push $").append(address).append("\n");
        visit(ctx.expr());
        code.append("sto\n");

        return null;
    }

    @Override
    public Void visitVarDeclSemValor(ExprParser.VarDeclSemValorContext ctx) {
        String nome = ctx.ID().getText();

        int address = memoryMapper.allocate(); // usa o mapper
        scopes.declare(nome, address);         // registra no escopo

        code.append("push $").append(address).append("\n");
        code.append("push 0\n"); // valor padrão
        code.append("sto\n");

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

        code.append("push $").append(address).append("\n");
        visit(ctx.expr()); 
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

        code.append("push $").append(address).append("\n");
        code.append("in\n");
        code.append("sto\n");

        return null;
    }

    @Override
    public Void visitPrintTexto(ExprParser.PrintTextoContext ctx) {
        code.append("push").append(ctx.STRING().getText()).append("\n");
        code.append("out\n");
        return null;
    }

    @Override
    public Void visitPrintExpr(ExprParser.PrintExprContext ctx) {
        visit(ctx.expr());
        code.append("out\n");
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
            case "<"  -> code.append("let\n");
            case "<=" -> code.append("lte\n");
            case ">"  -> code.append("grt\n");
            case ">=" -> code.append("gte\n");
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
        code.append("push true\n");
        return null;
    }
    @Override
    public Void visitCondFalse(ExprParser.CondFalseContext ctx) {
        code.append("push false\n");
        return null;
    }
    @Override
    public Void visitCondParenteses(ExprParser.CondParentesesContext ctx) {
    visit(ctx.cond());
    return null;
    }
}
