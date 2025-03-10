package com.craftinginterpreters.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    
    public static void main(String[] args) throws IOException{
        if(args.length != 1){
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
            "Binary: Expr left, Token operator, Expr right", 
            "Assignment: Token name, Expr value",
            "logical: Expr left, Token operator, Expr right",
            "Grouping: Expr expression",
            "Literal: Object value", 
            "Unary: Token operator, Expr right",
            "Call: Expr callee, Token paren, List<Expr> arguments",
            "Set: Expr object, Token name, Expr value",
            "Super: Token keyword, Token method",
            "This: Token keyword",
            "Get: Expr object, Token name",
            "Var: Token name"
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
            "Block: List<Stmt> statements",
            "Expression: Expr expression",
            "Class: Token name, Expr.Var superClass, List<Stmt.Func> methods",
            "Func: Token name, List<Token> arguments, List<Stmt> body",
            "If: Expr condition, Stmt ThenStatement, Stmt ElseStatement",
            "While: Expr condition, Stmt WhileStatement",
            "Print: Expr expression",
            "Var: Token name, Expr Initializer",
            "Return: Token keyword, Expr value"
        ));
    }

    // defines the abstract AST class. 
    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException{
        String path = outputDir + "/" + baseName + ".java"; 

        PrintWriter writer = new PrintWriter(path, "UTF-8"); 
        writer.println("package com.craftinginterpreters.lox;");
        writer.println();
        writer.println("import java.util.List;"); 
        writer.println();
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types); 

        for (String type: types){
            String className = type.split(":")[0].trim(); 
            String fields = type.split(":")[1].trim(); 
            defineType(writer, baseName, className, fields); 
        }

        writer.println();
        writer.println("\t"+"abstract <R> R accept(Visitor <R> visitor);");

        writer.println("}");
        writer.close(); 
    }

    private static void defineVisitor( PrintWriter writer, String baseName, List<String> types ){
        writer.println("\t"+"interface Visitor<R> {");

        for (String type: types){
            String typeName = type.split(":")[0].trim(); 
            writer.println("\t\t"+"R visit" + typeName + baseName + "(" +
            typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("\t"+"}");
    }

    //defines each specific type which implements the AST class. 
   private static void defineType(
    PrintWriter writer, String baseName, 
    String className, String fieldList){
        writer.println("\t"+"static class " + className + " extends " + baseName + " {");

        writer.println("\t\t" + className + "(" + fieldList + ") {");

        String[] fields = fieldList.split(","); 
        for (String field: fields) {
            String name = field.split(" ")[field.split(" ").length - 1 ]; 
            writer.println("\t\t\t"+"this." + name + " = " + name + ";");
        }

        writer.println("\t\t"+"}");

        writer.println();
        writer.println("\t\t"+"@Override");
        writer.println("\t\t"+"<R> R accept(Visitor<R> visitor) {");
        writer.println("\t\t\t"+"return visitor.visit" + className + baseName + "(this);");
        writer.println("\t\t"+"}");
        writer.println();
        for (String field: fields){
            writer.println("\t\t"+"final " + field + ";");
        }

        writer.println("\t"+"}");
        writer.println();
    }
}
