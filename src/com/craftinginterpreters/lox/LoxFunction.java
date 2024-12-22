package com.craftinginterpreters.lox;

import java.util.List;

public class LoxFunction implements LoxCallable{

    private final Stmt.Func declaration;
    private final Environment closure;

    private final boolean isInitializer;

    LoxFunction(Stmt.Func declaration, Environment closure,
                boolean isInitializer){
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    LoxFunction bind(LoxInstance instance){
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new LoxFunction(declaration, environment, isInitializer);
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
            if (isInitializer) return closure.getAt(0, "this");
            return returnValue.value;
        }
        return null;
    }

    @Override
    public String toString(){
        return "< fn" + declaration.name.lexeme + ">";
    }
}
