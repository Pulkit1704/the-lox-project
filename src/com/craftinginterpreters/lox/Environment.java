package com.craftinginterpreters.lox;

import java.util.HashMap; 
import java.util.Map; 

public class Environment {
    private final Map<String, Object> values = new HashMap<String, Object>(); 

    public void define(String name, Object value){
        values.put(name, value); 
    }

    public Object get(Token name){

        if(values.containsKey(name.lexeme)){
            return values.get(name.lexeme); 
        }

        throw new RuntimeError(name, "Undefined variable"); 
    }

    public void assign(Token name, Object expression){

        if(values.containsKey(name.lexeme)){
            values.put(name.lexeme, expression); 
            return; 
        }

        // we can decide here if we want to create a new variable if the one provided does not exist yet. 

        throw new RuntimeError( name, "Undefined variable " + name.lexeme + "."); 
    }
}
