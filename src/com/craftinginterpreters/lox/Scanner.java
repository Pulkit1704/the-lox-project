package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*; 

public class Scanner {
    private final String source; // this is the soruce code presented as a single string. 
    private final List<Token> tokens = new ArrayList<>(); 

    private int start = 0;
    private int current = 0; 
    private int line = 1; 

    // constructor returns an instance of the scanner class. 
    Scanner(String source){
        this.source = source; 
    }

    List<Token> scanTokens(){
        while (!isAtEnd()){
            start = current; 
            scanToken(); 
        }

        // as soon as we reach the end of file, we add te eof as a token as well. 
        tokens.add(new Token(EOF, "", null, line)); 
        return tokens; 
    }

    private static final Map<String, TokenType> keywords; 

    static{
        keywords = new HashMap<>(); 

        keywords.put("and", AND); 
        keywords.put("class", CLASS); 
        keywords.put("else", ELSE); 
        keywords.put("or", OR); 
        keywords.put("false", FALSE); 
        keywords.put("for", FOR); 
        keywords.put("func", FUNC); 
        keywords.put("if", IF); 
        keywords.put("nil", NIL); 
        keywords.put("print", PRINT); 
        keywords.put("return", RETURN); 
        keywords.put("super", SUPER); 
        keywords.put("self", SELF); 
        keywords.put("true", TRUE); 
        keywords.put("var", VAR); 
        keywords.put("while", WHILE);
        keywords.put("this", THIS);
    }

    private void scanToken(){
        char c = advance(); 

        switch (c){
            // single character lexemes 
            case '(': addToken(LEFT_PAREN); break; 
            case ')': addToken(RIGHT_PAREN); break; 
            case '{': addToken(LEFT_BRACE); break; 
            case '}': addToken(RIGHT_BRACE); break; 
            case ',': addToken(COMMA); break; 
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break; 
            
            //operator lexemes 
            case '!':
                addToken(match('=') ? BANG_EQUAL: BANG);break; 
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);break;

            case '/':
                if(match('/')){
                    while(peek() != '\n' && !isAtEnd()) advance(); 
                }else{
                    addToken(SLASH);
                }
                break; 
            
            //ignore all these characters and do nothing, 
            case ' ': 
            case '\r': 
            case '\t': 
                break; 
            
            case '\n': 
                line++; 
                break; 
            
            case '"': string(); break; 
            default:
                if(isDigit(c)){
                    Number(); 
                }else if (isAlpha(c)){
                    identifier(); 
                }else{
                    lox.error(line, "Unexpected character...");
                }
                
            break; 
        }
    }

    private void identifier(){
        while(isAlphaNumeric(peek())) advance(); 

        String text = source.substring(start, current); 
        TokenType type = keywords.get(text); 

        if (type == null) type = IDENTIFIER; 

        addToken(type);
    }

    private boolean isAlpha(char c){
        return(
            (c >= 'a' && c <= 'z') ||
            (c >= 'A' && c <= 'Z') ||

            (c == '_') 
        );
    }

    private boolean isAlphaNumeric(char c){
        return isAlpha(c) || isDigit(c);

    }

    private boolean isDigit(char c){
        return c >= '0' && c <= '9'; 
    }

    private void Number(){
        while (isDigit(peek())) advance(); 

        if (peek() == '.' && isDigit(peekNext())){
            advance(); 
        }

        while (isDigit(peek())) advance(); 

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private char peek(){
        if (isAtEnd()) return '\0'; 
        return source.charAt(current); 
    }

    private char peekNext(){
        if (current +1 > source.length()) return '\0'; 

        return source.charAt(current + 1); 
    }

    private void string(){
        while(peek() != '"' && !isAtEnd()){
           if (peek() == '\n'){ 
                line ++;
            } 

            advance(); 
        }

        if  (isAtEnd()){
            lox.error(line, "unterminated string");
        }

        advance(); 

        String value = source.substring(start+1, current -1); 
        addToken(STRING, value);
    }

    private boolean match(char expected){
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false; 

        current++; 
        return true;
    }

    private boolean isAtEnd(){
        return current >= source.length(); 
    }

    private char advance(){
        return source.charAt(current++);
    }

    private void addToken(TokenType type){
        addToken(type, null );
    }

    private void addToken(TokenType type, Object literal){
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line)); 
    }
}
