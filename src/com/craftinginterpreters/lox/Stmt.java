package com.craftinginterpreters.lox;

import java.util.List;

abstract class Stmt {
	interface Visitor<R> {
		R visitBlockStmt(Block stmt);
		R visitExpressionStmt(Expression stmt);
		R visitIfStmt(If stmt);
		R visitWhileStmt(While stmt);
		R visitPrintStmt(Print stmt);
		R visitVarStmt(Var stmt);
	}
	static class Block extends Stmt {
		Block(List<Stmt> statements) {
			this.statements = statements;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBlockStmt(this);
		}

		final List<Stmt> statements;
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

	static class If extends Stmt {
		If(Expr condition, Stmt ThenStatement, Stmt ElseStatement) {
			this.condition = condition;
			this.ThenStatement = ThenStatement;
			this.ElseStatement = ElseStatement;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitIfStmt(this);
		}

		final Expr condition;
		final  Stmt ThenStatement;
		final  Stmt ElseStatement;
	}

	static class While extends Stmt {
		While(Expr condition, Stmt WhileStatement) {
			this.condition = condition;
			this.WhileStatement = WhileStatement;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitWhileStmt(this);
		}

		final Expr condition;
		final  Stmt WhileStatement;
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
