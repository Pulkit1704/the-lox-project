package com.craftinginterpreters.lox;

import java.util.List;

public interface LoxCallable {

    int aerity();

    Object call(Interpreter interpreter, List<Object> arguments);

}
