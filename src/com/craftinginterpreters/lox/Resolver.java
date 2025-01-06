package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    private final Interpreter interpreter;

    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;

    private enum FunctionType{
        NONE,
        FUNCTION,
        INITIALIZER,
        METHOD
    }

    private enum ClassType{
        NONE,
        CLASS
    }

    private ClassType currentClass = ClassType.NONE;

    Resolver(Interpreter interpreter){
        this.interpreter = interpreter;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitAssignmentExpr(Expr.Assignment expr) {
        resolve(expr.value); 
        ResolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitlogicalExpr(Expr.logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);

        for(Expr argument: expr.arguments){
            resolve(argument);
        }
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr) {
        ResolveLocal(expr, expr.keyword);
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr) {
        if (currentClass == ClassType.NONE){
            lox.error(expr.keyword,
                    "Can't use 'this' outside of a class");
            return null;
        }

        ResolveLocal(expr, expr.keyword);
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitVarExpr(Expr.Var expr) {

        if(!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE){
            lox.error(expr.name, "can't resolve variable in its own initializer");
        }

        ResolveLocal(expr, expr.name);

        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {

        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;

        declare(stmt.name);
        define(stmt.name);

        if(stmt.superClass != null &&
        stmt.name.lexeme.equals(stmt.superClass.name.lexeme)){
            lox.error(stmt.superClass.name, "class cannot inherit from itself");
        }

        if(stmt.superClass != null){
            resolve(stmt.superClass);
        }

        if(stmt.superClass != null){
            beginScope();
            scopes.peek().put("super", true);
        }

        beginScope();
        scopes.peek().put("this", true);

        for(Stmt.Func method: stmt.methods){
            FunctionType declaration = FunctionType.METHOD;
            if (method.name.lexeme.equals("init")){
                declaration = FunctionType.INITIALIZER;
            }
            resolveFunction(method, declaration);
        }

        endScope();

        if(stmt.superClass != null){
            endScope();
        }
        currentClass = enclosingClass;

        return null;
    }

    @Override
    public Void visitFuncStmt(Stmt.Func stmt) {
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.ThenStatement);
        if(stmt.ElseStatement != null) resolve(stmt.ElseStatement);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.WhileStatement);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);

        if(stmt.Initializer != null){
            resolve(stmt.Initializer);
        }

        define(stmt.name);

        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {

        if(currentFunction == FunctionType.NONE){
            lox.error(stmt.keyword, "Can't return from top-level code");
        }

        if(stmt.value != null){
            if(currentFunction == FunctionType.INITIALIZER){
                lox.error(stmt.keyword,
                        "Can't return a value from an initializer.");
            }

            resolve(stmt.value);
        }
        return null;
    }

    private void beginScope(){
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope(){
        scopes.pop();
    }

    void resolve(List<Stmt> statements){

        for(Stmt statement: statements){
            resolve(statement);
        }
    }

    private void resolve(Stmt statement){
        statement.accept(this);
    }

    private void resolve(Expr statement){
        statement.accept(this);
    }

    private void declare(Token name){

        if(scopes.isEmpty()) return;

        Map<String, Boolean> scope = scopes.peek();

        if(scope.containsKey(name.lexeme)){
            lox.error(name, "already a variable with this name in this scope");
        }

        scope.put(name.lexeme, false); 

    }

    private void define(Token name){
        if(scopes.isEmpty()) return;

        scopes.peek().put(name.lexeme, true);
    }

    private void ResolveLocal(Expr expression, Token name){

        for(int i = scopes.size() -1; i >=0; i--){
            if(scopes.get(i).containsKey(name.lexeme)){
                interpreter.resolve(expression, scopes.size() - 1 -i);
            }
        }
    }

    private void resolveFunction(Stmt.Func function, FunctionType type){

        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for(Token param: function.arguments){
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
        currentFunction = enclosingFunction;
    }
}
