package com.craftinginterpreters.lox;
import com.craftinginterpreters.lox.Expr.*;

public class Interpreter implements Expr.Visitor<Object> {

    @SuppressWarnings("incomplete-switch")
    public Object visitBinaryExpr(Binary expr) {

        Object left = evaluate(expr.left); 
        Object right = evaluate(expr.right); 

        
        switch (expr.operator.type){
            // arithematic operators 
            case MINUS:
                return (double)left - (double)right; 
            
            case SLASH:
                return (double)left / (double) right; 
            
            case STAR: 
                return (double)left * (double)right; 

            case PLUS: 
                if (left instanceof String && right instanceof String){
                    return (String)left + (String)right; 
                }

                if (left instanceof Double && right instanceof Double){
                    return (double)left + (double)right; 
                }

            case BANG_EQUAL: 
                return !isEqual((double)left, (double)right); 
            case EQUAL_EQUAL: 
                return isEqual((double)left, (double)right); 

            case LESS: 
                return (double)left < (double)right; 
            
            case GREATER: 
                return (double)left < (double)right; 
            
            case LESS_EQUAL: 
                return (double)left <= (double)right; 
            
            case GREATER_EQUAL: 
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
                return -(double)right;


            case BANG: 
                return isTruthy((double)right); 
            default:
                return null; 
        } 
    }

    private boolean isTruthy(Object expr){
        if(expr == null) return false; 
        
        if(expr instanceof Boolean) return (boolean)expr; 

        return true; 
    }

}
