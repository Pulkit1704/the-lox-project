package com.craftinginterpreters.lox; 

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class lox {

    static boolean hadError= false;
    public static void main(String[] args) throws IOException {
        
        if (args.length > 1){
            System.out.println("usage: jlox [script]");
            System.exit(64);
        }else{
            if (args.length == 1){
                runFile(args[0]); 
            }else{
                runPrompt(); 
            }
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path)); 
        run(new String(bytes, Charset.defaultCharset()));  
        if (hadError) System.exit(65);
    }

    private static void runPrompt() throws IOException{
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input); 

        for (;;){
            System.out.print(">"); 
            String line = reader.readLine(); 
            if (line == null) break; 
            run(line); 
            hadError = false; 
        }
    }

    private static void run(String source){
        // this is a custom implementation of the scanner, not the default utils.Scanner
        Scanner scanner = new Scanner(source); 

        List<Token> tokens = scanner.scanTokens(); 

        for (Token token: tokens){
            System.out.println(token); 
        }
    }

    //error handling 
    static void error(int line, String message){

        /*
         * find a way to report the exact place of the error 
         * possible way: 
         * start by reporting the start and end column of the error line. 
         * keep a track of the columns while scanning and get the column number. 
        */

        report(line, "", message); 
    }

    private static void report(int line, String where, String message){
        System.err.println("line" + line + ": Error"+ where + "-" + message);
        hadError = true; 
    }
}