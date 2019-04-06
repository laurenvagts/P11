import java.util.Scanner;

public class P11 {

   public static void main(String[] args) throws Exception {

      String name;

      if ( args.length == 1 ) {
         name = args[0];
      }
      else {
         System.out.print("Enter name of P11 program file: ");
         Scanner keys = new Scanner( System.in );
         name = keys.nextLine();
      }

      Lexer lex = new Lexer( name );
      Parser parser = new Parser( lex );

      // start with <statements>
      Node root = parser.parseDefs();

      // display parse tree for debugging/testing:
      TreeViewer viewer = new TreeViewer("Parse Tree", 0, 0, 800, 500, root );

      // execute the parse tree
      // root.execute();

   }// main

}
