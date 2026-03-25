package com.uepb.compiler;

import java.io.PrintWriter;

public class PCodeVisitor extends ExprBaseVisitor<Void> {
    private final PrintWriter out;
    private final SymbolTable symbolTable;
    private int labelCount = 0;

    public PCodeVisitor(PrintWriter out, SymbolTable symbolTable) {
        this.out = out;
        this.symbolTable = symbolTable;
    }

    private String newLabel() {
        return "L" + (labelCount++);
    }

    // Declarações e Atribuições

    @Override
    public Void visitVarDeclComValor(ExprParser.VarDeclComValorContext ctx) {
        String id = ctx.ID().getText();
        int addr = symbolTable.declare(id);
        visit(ctx.expr()); // Calcula o valor
        out.println("sto " + addr);
        return null;
    }

    @Override
    public Void visitAtribuicao(ExprParser.AtribuicaoContext ctx) {
        String id = ctx.ID().getText();
        int addr = symbolTable.getAddress(id);
        visit(ctx.expr());
        out.println("sto " + addr);
        return null;
    }

    // Entrada e Saída 

    @Override
    public Void visitPrintExpr(ExprParser.PrintExprContext ctx) {
        visit(ctx.expr());
        out.println("out"); // No seu manual_pcode, 'out' escreve o topo da pilha
        return null;
    }

    @Override
    public Void visitInput(ExprParser.InputContext ctx) {
        int addr = symbolTable.getAddress(ctx.ID().getText());
        out.println("in");  // Lê valor para o topo da pilha
        out.println("sto " + addr);
        return null;
    }

    // Expressões Aritméticas

    @Override
    public Void visitSoma(ExprParser.SomaContext ctx) {
        visit(ctx.O1);
        visit(ctx.O2);
        out.println("add");
        return null;
    }

    @Override
    public Void visitSubtracao(ExprParser.SubtracaoContext ctx) {
        visit(ctx.O1);
        visit(ctx.O2);
        out.println("sub");
        return null;
    }

    @Override
    public Void visitNumero(ExprParser.NumeroContext ctx) {
        out.println("lit " + ctx.NUMBER().getText());
        return null;
    }

    @Override
    public Void visitVariavel(ExprParser.VariavelContext ctx) {
        int addr = symbolTable.getAddress(ctx.ID().getText());
        out.println("lod " + addr);
        return null;
    }

    // IF e While

    @Override
    public Void visitIf(ExprParser.IfContext ctx) {
        String labelFim = newLabel();
        visit(ctx.COND); // Gera código da condição (deixa true/false na pilha)
        out.println("fjp " + labelFim); // Pula para o fim se for falso
        visit(ctx.BLOCO);
        out.println(labelFim + ":");
        return null;
    }

    @Override
    public Void visitWhile(ExprParser.WhileContext ctx) {
        String labelInicio = newLabel();
        String labelFim = newLabel();

        out.println(labelInicio + ":");
        visit(ctx.COND);
        out.println("fjp " + labelFim);
        
        visit(ctx.BLOCO);
        
        out.println("ujp " + labelInicio); // Pulo incondicional para o início
        out.println(labelFim + ":");
        return null;
    }

    // Condicionais

    @Override
    public Void visitCondComparacao(ExprParser.CondComparacaoContext ctx) {
        visit(ctx.E1);
        visit(ctx.E2);
        String op = ctx.OP().getText();
        switch (op) {
            case "<"  -> out.println("les"); // Less than
            case ">"  -> out.println("grt"); // Greater than
            case "==" -> out.println("equ"); // Equal
            case "!=" -> out.println("neq"); // Not equal
        }
        return null;
    }
}
