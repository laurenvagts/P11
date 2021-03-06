import java.util.Scanner;
import java.io.*;

public class P11 {

   public static void main(String[] args) throws Exception {

      Scanner keys = new Scanner( System.in );
      String name;

      if ( args.length == 1 ) {
        name = args[0];
      }
      else {
        System.out.print("Enter name of P11 program file: ");
        name = keys.nextLine();
      }
   
      Lexer lex = new Lexer( name );
      Parser parser = new Parser( lex );

      // start with <statements>
      Node libraryExpressions = parser.parseDefs();
      Node.setRoot(libraryExpressions);

      // display parse tree for debugging/testing:
      // TreeViewer viewer = new TreeViewer("Parse Tree", 0, 0, 800, 500, libraryExpressions );
      

      System.out.print("? ");
      String input = keys.nextLine();
      
      while (!input.equals("exit")) {
          PrintWriter pw;
        //get additional lines of input
        pw = new PrintWriter("expression.txt"); //creates a new PrintWriter to clear the file expression.txt
        while (!input.equals("done")) {
          pw.write(input); //input is added to the file expression.txt here only
          input = keys.nextLine();
        }
          pw.close();
        //execute the input
        lex = new Lexer("expression.txt");
        parser = new Parser(lex);
        Node root = parser.parseList();
        Node result = root.evaluate(); //return type is a <list> or an <expr> holding a number
        result.printContents();
        System.out.println();
        
        //get the next expression or exit
        System.out.print("? ");
        input = keys.nextLine();
      }

   }// main

}

