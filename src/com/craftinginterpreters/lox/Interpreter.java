package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.craftinginterpreters.lox.Expr.*;
import com.craftinginterpreters.lox.Stmt.*;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expr, Integer> locals = new HashMap<>();

    Interpreter(){
        globals.define("clock", new LoxCallable(){
            @Override
            public int aerity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return System.currentTimeMillis()/ 1000.0;
            }

            public String toString(){return "<native fn>"; }
        });
    }

    void interpret(List<Stmt> statements){
        try{
            for(Stmt statement: statements){
                execute(statement); 
            }
        }
        catch (RuntimeError error){
            lox.runtimeError(error); 
        }
    }

    private void execute(Stmt stmt){
        stmt.accept(this); 
    }

    public void resolve(Expr expr, int depth){
        locals.put(expr, depth);
    }

    private String Stringify(Object object){
        if( object == null) return "nil"; 

        if(object instanceof Double){
            String text = object.toString(); 

            if(text.endsWith(".0")){
                text = text.substring(0, text.length() - 2); 
            }
            return text; 
        }
        return object.toString();
    }

    public Object visitBinaryExpr(Binary expr) {

        Object left = evaluate(expr.left); 
        Object right = evaluate(expr.right);
        
        switch (expr.operator.type){

            case MINUS:
                checkNumberType(expr.operator, left, right);

                return (double)left - (double)right; 
            
            case SLASH:
                checkNumberType(expr.operator, left, right);
                return (double)left / (double) right; 
            
            case STAR: 
                checkNumberType(expr.operator, left, right);
                return (double)left * (double)right; 

            case PLUS: 
                if (left instanceof String && right instanceof String){
                    return (String)left + (String)right; 
                }
                if (left instanceof Double && right instanceof Double){
                    return (double)left + (double)right; 
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two Strings"); 

            case BANG_EQUAL: 
                return !isEqual((double)left, (double)right); 
            case EQUAL_EQUAL: 
                return isEqual((double)left, (double)right);
            case LESS: 
                checkNumberType(expr.operator, left, right);
                return (double)left < (double)right;
            case GREATER: 
                checkNumberType(expr.operator, left, right);
                return (double)left > (double)right;
            case LESS_EQUAL: 
                checkNumberType(expr.operator, left, right);
                return (double)left <= (double)right;
            case GREATER_EQUAL: 
                checkNumberType(expr.operator, left, right);
                return (double)left >= (double)right; 
        }
        return right;
    }

    private boolean isEqual(Object left, Object right){
        if(left == null && right == null) return true; 
        if(left == null) return false; 

        return left.equals(right); 
    }

    @Override
    public Object visitGroupingExpr(Grouping expr) {
        return evaluate(expr.expression); 
    }

    private Object evaluate(Expr expr){
        return expr.accept(this);
    }

    @Override
    public Object visitLiteralExpr(Literal expr) {
        return expr.value; 
    }

    @Override
    public Object visitUnaryExpr(Unary expr) {
        Object right = evaluate(expr.right);

        return switch (expr.operator.type) {
            case MINUS -> {
                checkNumberType(expr.operator, right);
                yield -(double) right;
            }
            case BANG -> !isTruthy(right);
            default -> null;
        };
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();

        for(Expr argument: expr.arguments){
            arguments.add(evaluate(argument));
        }

        if(!(callee instanceof LoxCallable)){
            throw new RuntimeError(expr.paren,
                    "the expression is not callable");
        }

        LoxCallable function = (LoxCallable)callee;

        if(arguments.size() != function.aerity()){
            throw new RuntimeError(expr.paren, "Expected" +
                    function.aerity() + " arguments but got" +
                    arguments.size() + "arguments instead");
        }
        return function.call(this, arguments);
    }

    @Override
    public Object visitSetExpr(Set expr) {
        Object object = evaluate(expr.object);

        if(!(object instanceof LoxInstance)){
            throw new RuntimeError(expr.name, "Only instances have fields");
        }

        Object value = evaluate(expr.value);
        ((LoxInstance)object).set(expr.name, value);
        return null;
    }

    @Override
    public Object visitThisExpr(This expr) {
        return lookUpVariable(expr.keyword, expr);
    }

    @Override
    public Object visitGetExpr(Get expr) {
        Object object = evaluate(expr.object);
        if(object instanceof LoxInstance){
            return ((LoxInstance) object).get(expr.name);
        }

        throw new RuntimeError(expr.name, "Only instances have properties");
    }

    private void checkNumberType(Token operator, Object operand){

        if(operand instanceof Double) return; 

        throw new RuntimeError(operator, "Operand must be a number");
    }

    private void checkNumberType(Token operator, Object left, Object right){

        if(left instanceof Double && right instanceof Double) return; 

        throw new RuntimeError(operator, "operands must be numbers"); 
    }

    private boolean isTruthy(Object expr){
        if(expr == null) return false; 
        
        if(expr instanceof Boolean) return (boolean)expr;

        return true; 
    }

    @Override
    public Void visitExpressionStmt(Expression stmt) {
        evaluate(stmt.expression); 
        return null; 
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        environment.define(stmt.name.lexeme, null);

        Map<String, LoxFunction> methods = new HashMap<>();
        for (Stmt.Func method: stmt.methods){
            LoxFunction function = new LoxFunction(method, environment,
                    method.name.lexeme.equals("init"));
            methods.put(method.name.lexeme, function);
        }

        LoxClass klass = new LoxClass(stmt.name.lexeme, methods);
        environment.assign(stmt.name, klass);
        return null;
    }

    @Override
    public Void visitFuncStmt(Func stmt) {
        LoxFunction function = new LoxFunction(stmt, environment, false);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        Object value = evaluate(stmt.expression); 
        System.out.println(Stringify(value));
        return null; 
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {

        Object value = null; 

        if(stmt.Initializer != null){
            value = evaluate(stmt.Initializer); 
        }

        environment.define(stmt.name.lexeme, value);

        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;

        if(stmt.value != null){value = evaluate(stmt.value);}

        throw new Return(value);
    }

    @Override
    public Object visitVarExpr(Expr.Var expr) {

        return lookUpVariable(expr.name, expr);
        
    }

    @Override
    public Object visitAssignmentExpr(Assignment expr) {

        Object expression  = evaluate(expr.value); 

        Integer distance = locals.get(expr);

        if(distance != null){
            environment.assignAt(distance, expr.name, expression);
        }else{
            globals.assign(expr.name, expression);
        }

        return expression; 

    }

    @Override
    public Object visitlogicalExpr(logical expr) {
        Object left = evaluate(expr.left);

        if(expr.operator.type == TokenType.OR){
            if(isTruthy(left)) return left;
        }else{
            if(!isTruthy(left)) return left;
        }
        return evaluate(expr.right);
    }

    @Override
    public Void visitBlockStmt(Block block) {

        executeBlock(block.statements, new Environment(environment));

        return null; 
        
    }

    void executeBlock(List<Stmt> statements, Environment environment){

        Environment previous = this.environment; 

        try{
            this.environment = environment; 

            for(Stmt statement: statements){
                execute(statement);
            }
        }finally{
            this.environment = previous; 
        }
    }

    @Override
    public Void visitIfStmt(If stmt) {
        
        if(isTruthy(evaluate(stmt.condition))){
            execute(stmt.ThenStatement);
        }else if(stmt.ElseStatement != null){
            execute(stmt.ElseStatement);
        }
        return null; 
    }

    @Override
    public Void visitWhileStmt(While stmt) {

        while(isTruthy(evaluate(stmt.condition))){
            execute(stmt.WhileStatement);
        }

        return null;
    }

    private Object lookUpVariable(Token name, Expr expression){
        Integer distance = locals.get(expression);
        if(distance != null){
            return environment.getAt(distance, name.lexeme);
        }else{
            return globals.get(name);
        }
    }
}
