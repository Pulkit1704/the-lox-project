package com.craftinginterpreters.lox; 

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

class Parser {

    private static class ParseError extends RuntimeException{}
    
    private final List<Token> tokens; 

    private int current = 0; 

    Parser(List<Token> tokens){
        this.tokens = tokens; 
    }

    List<Stmt> parse(){
        List<Stmt> statements = new ArrayList<Stmt>(); 

        while(!isAtEnd()){
            statements.add(Declaration()); 
        }

        return statements; 
    }

    private Stmt Declaration(){
        try{
            if(match(VAR)){return VarDeclaration();}; 

            return statement();
        }catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt VarDeclaration(){

        Token name = consume(IDENTIFIER, "expect a name for a variable"); 

        Expr initializer = null;
        if(match(EQUAL)){
            initializer = expression(); 
        }

        consume(SEMICOLON, "expect a semicolon to end the declaration"); 

        return new Stmt.Var(name, initializer); 
    }
    
    private Stmt statement(){

        if(match(PRINT)) return PrintStatement();

        if(match(CLASS)) return ClasStatement();

        if(match(FUNC)) return Function("function");

        if(match(RETURN)) return Return();

        if(match(IF)) return IfStatement();

        if(match(WHILE)) return WhileStatement();

        if(match(FOR)) return ForStatement();

        if(match(LEFT_BRACE)){return new Stmt.Block(block());}

        return ExpressionStatement(); 
    }

    private Stmt ClasStatement(){
        Token name = consume(IDENTIFIER, "Expect class name");
        Expr.Var superclass = null;

        if(match(LESS)){
            consume(IDENTIFIER, "expect super class name after <");
            superclass = new Expr.Var(previous());
        }

        consume(LEFT_BRACE, "Expect left paren after class name");

        List<Stmt.Func> methods = new ArrayList<>();

        while(!check(RIGHT_BRACE) && !isAtEnd()){
            methods.add(Function("method"));
        }

        consume(RIGHT_BRACE, "Expect closing right parenthesis");

        return new Stmt.Class(name, superclass, methods);
    }

    private Stmt Return(){
        Token keyword = previous();

        Expr value = null;
        if(!check(SEMICOLON)){
            value = expression();
        }

        consume(SEMICOLON, "Expect ; after return keyword");

        return new Stmt.Return(keyword, value);
    }

    private Stmt.Func Function(String kind){
        Token identifier = consume(IDENTIFIER, "Expect " + kind + " name");
        consume(LEFT_PAREN, "Expect ( after " + kind + " name");
        List<Token> arguments = new ArrayList<>();

        if(!check(RIGHT_PAREN)){
            do{

                if (arguments.size() >= 255){
                    error(peek(), "can't have more than 255 arguments for a " + kind);
                }
                arguments.add(consume(IDENTIFIER, "Expect , after each argument"));
            }while(match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect closing ) after arguments");

        consume(LEFT_BRACE,"Expect { before " + kind + " body");

        List<Stmt> body = block();

        return new Stmt.Func(identifier, arguments, body);
    }

    private Stmt ForStatement(){

        consume(LEFT_PAREN, "expect ( after for keyword");

        Stmt initializer;
        if(match(SEMICOLON)){
            initializer = null;
        }else if(match(VAR)){
            initializer = VarDeclaration();
        }else {
            initializer = ExpressionStatement();
        }
        Expr condition = null;
        if(!check(SEMICOLON)){
            condition = expression();
        }
        consume(SEMICOLON, "expect semicolon after condition in for loop"); 

        Expr increment=null;
        if(!check(RIGHT_PAREN)){
            increment = expression();
        }
        consume(RIGHT_PAREN, "expect ) after the definition of for loop");

        Stmt body = statement();

        if(increment != null){
            body = new Stmt.Block(
                    Arrays.asList(
                            body,
                            new Stmt.Expression(increment)
                    )
            );
        }

        if(condition == null) condition = new Expr.Literal(true);

        body = new Stmt.While(condition, body);

        if(initializer != null){
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }

        return body;

    }

    private Stmt WhileStatement(){

        consume(LEFT_PAREN, "expect ( before while condition");
        Expr condition = expression();
        consume(RIGHT_PAREN, "expect ) after while condition");

        Stmt WhileStatement = statement();

        return new Stmt.While(condition, WhileStatement);
    }

    private Stmt IfStatement(){

        // we are not supporting single line if statements. 

        consume(LEFT_PAREN, "expect ( before condition"); 
        Expr condition = expression(); 
        consume(RIGHT_PAREN, "expect ) after condition"); 

        consume(LEFT_BRACE, "expected { to start the if execution block");  

        Stmt thenStatement = statement(); 

        consume(RIGHT_BRACE, "unterminated if execution block"); 

        Stmt elseStatement = null; 

        if(match(ELSE)){
            consume(LEFT_BRACE, "expected { to start the else execution block"); 

            elseStatement = statement(); 

            consume(RIGHT_BRACE, "unterminated else execution block"); 
        }


        return new Stmt.If(condition, thenStatement, elseStatement); 
    }

    private List<Stmt> block(){

        List<Stmt> statements = new ArrayList<Stmt>(); 

        while (!check(RIGHT_BRACE) && !isAtEnd()){
            statements.add(Declaration()); 
        }

        consume(RIGHT_BRACE, "Expected } at the end of block"); 

        return statements; 

    }

    private Stmt PrintStatement(){
        Expr value = expression(); 

        consume(SEMICOLON, "expect ; after statement"); 

        return new Stmt.Print(value); 
    }

    private Stmt ExpressionStatement(){
        Expr value = expression(); 

        consume(SEMICOLON, "expect ; after statement"); 

        return new Stmt.Expression(value); 
    }

    private Expr expression(){
        return assignment(); 
    }

    private Expr assignment(){
        Expr expression = or();

        if(match(EQUAL)){
            Token equals = previous(); 
            Expr value = assignment(); 

            if(expression instanceof Expr.Var){
                Token name = ((Expr.Var) expression).name; 

                return new Expr.Assignment(name, value); 

            }else if(expression instanceof Expr.Get){
                Expr.Get get = (Expr.Get)expression;
                return new Expr.Set(get.object, get.name, value);
            }

            throw error(equals, "invalid assignment target");

        }
        
        return expression;
    }

    private Expr or(){
        Expr left = and();

        if(match(OR)){
            Token operator = previous();
            Expr right = and();
            left = new Expr.logical(left, operator, right);
        }

        return left;
    }

    private Expr and(){
        Expr left = equality();

        if(match(AND)){
            Token operator = previous();
            Expr right = equality();

            left = new Expr.logical(left, operator, right);
        }

        return left;
    }
    
    private Expr equality(){
        // we directly descend and evaluate the nested expressions first before evaluating the equality. 
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
        return call();
    }

    private Expr call(){
        Expr expr = primary();

        while(true){
            if(match(LEFT_PAREN)){
                expr = finishcall(expr);
            }else if (match(DOT)){
                Token name = consume(IDENTIFIER, "expect property name after '.'");
                expr = new Expr.Get(expr, name);
            }else{
                break;
            }
        }
        return expr;
    }

    private Expr finishcall(Expr callee){

        List<Expr> arguments = new ArrayList<>();

        if(!check(RIGHT_PAREN)){
            do{
                if(arguments.size() >=255){
                    error(peek(),"max argument count for a function is 255");
                }
                arguments.add(expression());
            }while(match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN, "expect ) to close a function call");

        return new Expr.Call(callee, paren, arguments);
    }


    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);
    
        if (match(NUMBER, STRING)) {
          return new Expr.Literal(previous().literal);
        }

        if(match(SUPER)){
            Token keyword = previous();
            consume(DOT, "expect '.' after super keyword");
            Token method = consume(IDENTIFIER, "expect method name after '.'");
            return new Expr.Super(keyword, method);
        }
    
        if (match(LEFT_PAREN)) {
          Expr expr = expression();
          consume(RIGHT_PAREN, "Expect ')' after expression.");
          return new Expr.Grouping(expr);
        }

        if (match(THIS)) return new Expr.This(previous());

        if(match(IDENTIFIER)){
            return new Expr.Var(previous()); 
        }

        throw error(peek(), "invalid expression"); 
    }


    private Token consume(TokenType type, String message) {

        if(check(type)) return advance(); 
        

        throw error(peek(), message); 
    }

    private ParseError error(Token token, String message){
        lox.error(token, message); // this line connects tha parser to the lox error handler. 
        return new ParseError(); 
    }

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

    private boolean match(TokenType... types){
        
        for(TokenType type: types){

            if(check(type)){
                advance(); 
                return true;
            }
        }

        return false; 
    }

    private boolean check(TokenType type){

        if(isAtEnd()) return false; 

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
        return this.tokens.get(current); 
    }

    private Token previous(){

        return this.tokens.get(current - 1); 
    }
}
