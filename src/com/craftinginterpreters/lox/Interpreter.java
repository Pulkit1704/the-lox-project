package com.craftinginterpreters.lox;

import java.util.List;

import com.craftinginterpreters.lox.Expr.*;
import com.craftinginterpreters.lox.Stmt.*;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private Environment environment = new Environment(); 

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
                return (double)left < (double)right; 
            
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
        Object right = expr.right; 
        
        switch (expr.opeartor.type){

            case MINUS:
                checkNumberType(expr.opeartor, right); 
                return -(double)right;


            case BANG: 
                return isTruthy((double)right); 
            default:
                return null; 
        } 
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
    public Object visitVarExpr(Expr.Var expr) {

        return environment.get(expr.name);
        
    }

    @Override
    public Object visitAssignmentExpr(Assignment expr) {

        Object expression  = evaluate(expr.value); 

        environment.assign(expr.name, expression); 

        return expression; 

    }

    @Override
    public Void visitBlockStmt(Block block) {

        executeBlock(block, new Environment(environment)); 

        return null; 
        
    }

    private void executeBlock(Block block, Environment environment){

        Environment previous = this.environment; 

        try{
            this.environment = environment; 

            for(Stmt statement: block.statements){
                execute(statement);
            }
        }finally{
            this.environment = previous; 
        }
    
    }

}
