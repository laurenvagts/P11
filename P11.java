import java.util.Scanner;

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

      // display parse tree for debugging/testing:
      TreeViewer viewer = new TreeViewer("Parse Tree", 0, 0, 800, 500, root );

      while (true) {
        String input = keys.nextLine();
          String FileNamePath = "expression.txt";
	       PrintWriter printWriter=new PrintWriter(bw);
          bw.write( input);
          lex = new Lexer( "expression.txt" );
          parser = new Parser( lex );
          Node root = parser.parseExpr();
          root.execute(libraryExpressions);
      }

   }// main

}
