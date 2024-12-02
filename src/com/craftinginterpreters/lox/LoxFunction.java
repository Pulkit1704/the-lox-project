package com.craftinginterpreters.lox;

import java.util.List;

public class LoxFunction implements LoxCallable{

    private final Stmt.Func declaration;
    private final Environment closure;

    LoxFunction(Stmt.Func declaration, Environment closure){
        this.declaration = declaration;
        this.closure = closure;
    }


    @Override
    public int aerity() {
        return declaration.arguments.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {

        Environment environment = new Environment(closure);
        for(int i = 0; i < this.declaration.arguments.size(); i++){
            environment.define(this.declaration.arguments.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(this.declaration.body, environment);
        }catch(Return returnValue){
            return returnValue.value;
        }
        return null;
    }

    @Override
    public String toString(){
        return "< fn" + declaration.name.lexeme + ">";
    }
}
