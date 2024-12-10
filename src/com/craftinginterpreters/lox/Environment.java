package com.craftinginterpreters.lox;

import java.util.HashMap; 
import java.util.Map; 

public class Environment {
    private final Map<String, Object> values = new HashMap<String, Object>(); 
    final Environment enclosing; 

    Environment(){
        enclosing = null; 
    }

    Environment(Environment enclosing){
        this.enclosing = enclosing; 
    }

    public void define(String name, Object value){
        values.put(name, value); 
    }

    public Object get(Token name){

        if(values.containsKey(name.lexeme)){
            return values.get(name.lexeme); 
        }

        if(enclosing !=null) return enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable"); 
    }

    public void assign(Token name, Object expression){

        if(values.containsKey(name.lexeme)){
            values.put(name.lexeme, expression); 
            return; 
        }

        if(enclosing != null){
            enclosing.assign(name, expression); 
            return;
        }

        // we can decide here if we want to create a new variable if the one provided does not exist yet. 

        throw new RuntimeError( name, "Undefined variable " + name.lexeme + "."); 
    }

    public Object getAt(int distance, String name){
        return ancestor(distance).values.get(name);
    }

    private Environment ancestor(int distance){
        Environment current_environment = this;
        for(int i  =0; i < distance; i++){
            assert current_environment != null;
            current_environment = current_environment.enclosing;
        }

        return current_environment;
    }

    void assignAt(int distance, Token name, Object value){
        ancestor(distance).values.put(name.lexeme, value);
    }
}
