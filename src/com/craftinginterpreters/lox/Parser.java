package com.craftinginterpreters.lox; 

import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

class Parser {

    private class ParseError extends RuntimeException{}
    
    private final List<Token> tokens; 

    private int current = 0; 

    Parser(List<Token> tokens){
        this.tokens = tokens; 
    }

    Expr parse(){
        try{
            return expression(); 
        }
        catch(ParseError error){
            return null; 
        }
    }

    private Expr expression(){
        return equality(); 
    }
    
    private Expr equality(){
        Expr expression = comparision(); 
    
        while (match(BANG_EQUAL, EQUAL_EQUAL)){
            Token operator = previous(); 
            Expr right = comparision(); 
            
            expression = new Expr.Binary(expression, operator, right); 
        }
    
        return expression; 
    }
    
    private Expr comparision(){
        Expr expression = term(); 
    
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)){
            Token operator = previous();
            Expr right_term = term(); 
            
            expression = new Expr.Binary(expression, operator, right_term); 
        }

        return expression; 
    }

    private Expr term(){

        Expr expression = factor(); 

        while(match(MINUS, PLUS)){
            Token operator = previous(); 
            Expr right_factor = factor(); 

            expression = new Expr.Binary(expression, operator, right_factor);
        }

        return expression; 
    }

    private Expr factor(){

        Expr expression = unary(); 

        while (match(SLASH, STAR)){

            Token operator = previous(); 
            Expr right_unary = unary(); 

            expression = new Expr.Binary(expression, operator, right_unary); 
        }

        return expression; 
    }

    private Expr unary(){

        if(match(BANG, MINUS)){
            Token operator = previous(); 
            Expr right = unary(); 

            return new Expr.Unary(operator, right); 
        }
        return primary(); 
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);
    
        if (match(NUMBER, STRING)) {
          return new Expr.Literal(previous().literal);
        }
    
        if (match(LEFT_PAREN)) {
          Expr expr = expression();
          consume(RIGHT_PAREN, "Expect ')' after expression.");
          return new Expr.Grouping(expr);
        }

        throw error(peek(), "invalid expression"); 
    }


    private void consume(TokenType rightParen, String message) {

        if(check(rightParen)) advance(); 
        

        throw error(peek(), message); 
    }

    private ParseError error(Token token, String message){
        lox.error(token, message); // this line connects tha parser to the lox error handler. 
        return new ParseError(); 
    }

    @SuppressWarnings("incomplete-switch")
    private void synchronize(){
        advance(); 

        while (!isAtEnd()){
            if(previous().type == SEMICOLON) {return; }
        }

        switch(peek().type){
            case CLASS:
            case FUNC:
            case VAR:
            case WHILE: 
            case FOR:
            case IF:
            case PRINT:
            case RETURN: 
                return;
        }

        advance(); 
    }



    private boolean match(TokenType... tokens){
        
        for(TokenType token: tokens){

            if(check(token)){
                advance(); 
                return true;
            }
        }

        return false; 
    }

    private boolean check(TokenType type){

        if(!isAtEnd()) return false; 
        return peek().type == type; 
    }

    private Token advance(){
        if(!isAtEnd()){
            current++; 
        }
        return previous(); 
    }

    private boolean isAtEnd(){
        return peek().type == EOF; 
    }

    private Token peek(){
        return tokens.get(current); 
    }

    private Token previous(){

        return this.tokens.get(current - 1); 
    }
}
