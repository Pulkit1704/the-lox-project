package com.craftinginterpreters.lox;

import java.util.List;

abstract class Stmt {
	interface Visitor<R> {
		R visitBlockStmt(Block stmt);
		R visitExpressionStmt(Expression stmt);
		R visitClassStmt(Class stmt);
		R visitFuncStmt(Func stmt);
		R visitIfStmt(If stmt);
		R visitWhileStmt(While stmt);
		R visitPrintStmt(Print stmt);
		R visitVarStmt(Var stmt);
		R visitReturnStmt(Return stmt);
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

	static class Class extends Stmt {
		Class(Token name, List<Stmt.Func> methods) {
			this.name = name;
			this.methods = methods;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitClassStmt(this);
		}

		final Token name;
		final  List<Stmt.Func> methods;
	}

	static class Func extends Stmt {
		Func(Token name, List<Token> arguments, List<Stmt> body) {
			this.name = name;
			this.arguments = arguments;
			this.body = body;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitFuncStmt(this);
		}

		final Token name;
		final  List<Token> arguments;
		final  List<Stmt> body;
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

	static class Return extends Stmt {
		Return(Token keyword, Expr value) {
			this.keyword = keyword;
			this.value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitReturnStmt(this);
		}

		final Token keyword;
		final  Expr value;
	}


	abstract <R> R accept(Visitor <R> visitor);
}
