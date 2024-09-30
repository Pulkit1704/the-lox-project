package com.craftinginterpreters.lox;

import java.util.List;

abstract class Expr {
    interface Visitor<R> {
	 	R visitBinaryExpr(Binary expr);
	 	R visitAssignmentExpr(Assignment expr);
	 	R visitGroupingExpr(Grouping expr);
	 	R visitLiteralExpr(Literal expr);
	 	R visitUnaryExpr(Unary expr);
	 	R visitVarExpr(Var expr);
	}
    static class Binary extends Expr {
		Binary(Expr left, Token operator, Expr right) {
    this.left = left;
    this.operator = operator;
    this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitBinaryExpr(this);
    }

    final Expr left;
    final  Token operator;
    final  Expr right;
    }
    static class Assignment extends Expr {
		Assignment(Token name, Expr value) {
    this.name = name;
    this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitAssignmentExpr(this);
    }

    final Token name;
    final  Expr value;
    }
    static class Grouping extends Expr {
		Grouping(Expr expression) {
    this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitGroupingExpr(this);
    }

    final Expr expression;
    }
    static class Literal extends Expr {
		Literal(Object value) {
    this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitLiteralExpr(this);
    }

    final Object value;
    }
    static class Unary extends Expr {
		Unary(Token opeartor, Expr right) {
    this.opeartor = opeartor;
    this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitUnaryExpr(this);
    }

    final Token opeartor;
    final  Expr right;
    }
    static class Var extends Expr {
		Var(Token name) {
    this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitVarExpr(this);
    }

    final Token name;
    }

    abstract <R> R accept(Visitor <R> visitor);
}
