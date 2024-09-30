package com.craftinginterpreters.lox;

import java.util.List;

abstract class Stmt {
    interface Visitor<R> {
	 	R visitExpressionStmt(Expression stmt);
	 	R visitPrintStmt(Print stmt);
	 	R visitVarStmt(Var stmt);
	}
    static class Expression extends Stmt {
		Expression(Expr expression) {
    this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitExpressionStmt(this);
    }

    final Expr expression;
    }
    static class Print extends Stmt {
		Print(Expr expression) {
    this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitPrintStmt(this);
    }

    final Expr expression;
    }
    static class Var extends Stmt {
		Var(Token name, Expr Initializer) {
    this.name = name;
    this.Initializer = Initializer;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitVarStmt(this);
    }

    final Token name;
    final  Expr Initializer;
    }

    abstract <R> R accept(Visitor <R> visitor);
}