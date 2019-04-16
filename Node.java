/*  a Node holds one node of a parse tree
    with several pointers to children used
    depending on the kind of node
*/

import java.util.*;
import java.io.*;
import java.awt.*;

public class Node {

  public static int count = 0;  // maintain unique id for each node

  private int id;

  private String kind;  // non-terminal or terminal category for the node
  private String info;  // extra information about the node such as
                        // the actual identifier for an I

  // references to children in the parse tree
  private Node first, second, third; 

  // stack of memories for all pending calls
  private static ArrayList<MemTable> memStack = new ArrayList<MemTable>();
  // convenience reference to top MemTable on stack
  private static MemTable table = new MemTable();

  // status flag that causes <stmts> nodes to abort asking second
  // to execute
  private static boolean returning = false;

  // value being returned
  private static double returnValue = 0;

  private static Node root;  // root of the entire parse tree

  private static Scanner keys = new Scanner( System.in );

  // construct a common node with no info specified
  public Node( String k, Node one, Node two, Node three ) {
    kind = k;  info = "";  
    first = one;  second = two;  third = three;
    id = count;
    count++;
    System.out.println( this );
  }

  // construct a node with specified info
  public Node( String k, String inf, Node one, Node two, Node three ) {
    kind = k;  info = inf;  
    first = one;  second = two;  third = three;
    id = count;
    count++;
    System.out.println( this );
  }

  // construct a node that is essentially a token
  public Node( Token token ) {
    kind = token.getKind();  info = token.getDetails();  
    first = null;  second = null;  third = null;
    id = count;
    count++;
    System.out.println( this );
  }
    
   //construct a node that is essentially a number
   public Node( String str ) {
      kind = "number";  info = str;  
      first = null;  second = null;  third = null;
   }
    
  public String toString() {
    return "#" + id + "[" + kind + "," + info + "]<" + nice(first) + 
              " " + nice(second) + ">";
  }

  public String nice( Node node ) {
     if ( node == null ) {
        return "-";
     }
     else {
        return "" + node.id;
     }
  }

  // produce array with the non-null children
  // in order
  private Node[] getChildren() {
    int count = 0;
    if( first != null ) count++;
    if( second != null ) count++;
    if( third != null ) count++;
    Node[] children = new Node[count];
    int k=0;
    if( first != null ) {  children[k] = first; k++; }
    if( second != null ) {  children[k] = second; k++; }
    if( third != null ) {  children[k] = third; k++; }

     return children;
  }

  //******************************************************
  // graphical display of this node and its subtree
  // in given camera, with specified location (x,y) of this
  // node, and specified distances horizontally and vertically
  // to children
  public void draw( Camera cam, double x, double y, double h, double v ) {

System.out.println("draw node " + id );

    // set drawing color
    cam.setColor( Color.black );

    String text = kind;
    if( ! info.equals("") ) text += "(" + info + ")";
    cam.drawHorizCenteredText( text, x, y );

    // positioning of children depends on how many
    // in a nice, uniform manner
    Node[] children = getChildren();
    int number = children.length;
System.out.println("has " + number + " children");

    double top = y - 0.75*v;

    if( number == 0 ) {
      return;
    }
    else if( number == 1 ) {
      children[0].draw( cam, x, y-v, h/2, v );     cam.drawLine( x, y, x, top );
    }
    else if( number == 2 ) {
      children[0].draw( cam, x-h/2, y-v, h/2, v );     cam.drawLine( x, y, x-h/2, top );
      children[1].draw( cam, x+h/2, y-v, h/2, v );     cam.drawLine( x, y, x+h/2, top );
    }
    else if( number == 3 ) {
      children[0].draw( cam, x-h, y-v, h/2, v );     cam.drawLine( x, y, x-h, top );
      children[1].draw( cam, x, y-v, h/2, v );     cam.drawLine( x, y, x, top );
      children[2].draw( cam, x+h, y-v, h/2, v );     cam.drawLine( x, y, x+h, top );
    }
    else {
      System.out.println("no Node kind has more than 3 children???");
      System.exit(1);
    }

  }// draw

  public static void error( String message ) {
    System.out.println( message );
    System.exit(1);
  }

   private final static String[] bif0 = { "input", "nl" };
   private final static String[] bif1 = { "sqrt", "cos", "sin", "atan", 
                             "round", "trunc", "not" };
   private final static String[] bif2 = { "lt", "le", "eq", "ne", "pow",
                                          "or", "and"};

   // return whether target is a member of array
   private static boolean member( String target, String[] array ) {
      for (int k=0; k<array.length; k++) {
             if ( target.equals(array[k]) ) {
            return true;
         }
      }
      return false;
   }

   // given a funcCall node, and for convenience its name,
   // locate the function in the function defs and
   // create new memory table with arguments values assigned
   // to parameters
   // Also, return root node of body of the function being called
   private static Node passArgs( Node funcCallNode, String funcName) {

      // locate the function in the function definitions

      Node node = root;  // the defs node
      Node fdnode = null;
      while ( node != null && fdnode == null ) {
         if ( node.first.info.equals(funcName) ) {// found it
            fdnode = node.first;
            // System.out.println("located " + funcName + " at node " + 
            //                     fdnode.id );
         }
         else
           node = node.second;
      }

      MemTable newTable = new MemTable();

      if ( fdnode == null ) {// function not found
         error( "Function definition for [" + funcName + "] not found" );
         return null;
      }
      else {// function name found
         Node pnode = fdnode.first; // current params node
         Node anode = funcCallNode.first.second;  // current args node
         while ( pnode != null && anode != null ) {
            // store argument value under parameter name
            newTable.store( pnode.first.info,
                            anode.first.evaluate() );
            // move ahead
            pnode = pnode.second;
            anode = anode.second;
         }

         // detect errors
         if ( pnode != null ) {
            error("there are more parameters than arguments");
         }
         else if ( anode != null ) {
            error("there are more arguments than parameters");
         }

//         System.out.println("at start of call to " + funcName +
//                           " memory table is:\n" + newTable );

         // manage the memtable stack
         memStack.add( newTable );
         table = newTable;

         return fdnode;  

      }// function name found

   }// passArguments
    
   public void printContents() {
      if (kind.equals("expr")) {
         first.printContents();
      }
      else if (kind.equals("number") || kind.equals("name")) {
         System.out.print(info);
      }
      else if (kind.equals("list")) {
         System.out.print("(");
         if (first != null)
            first.printContents();
         System.out.print(")");
      }
      else if (kind.equals("items")) {
         first.printContents();
         if (second != null) {
            System.out.print(" ");
            second.printContents();
         }
      }
      else {
         System.out.println("Tried to print an invalid node type.");
      }
      System.out.println();
       
   }// printContents
    
   public static void setRoot(Node rt) {
      root = rt;
   }//setRoot
   
   //This is basically passArgs for functions without any arguments
   private static Node findFunction(String funcName) {

      // locate the function in the function definitions

      Node node = root;  // the defs node
      Node fdnode = null;
      while ( node != null && fdnode == null ) {
         if ( node.first.info.equals(funcName) ) {// found it
            fdnode = node.first;
            // System.out.println("located " + funcName + " at node " + 
            //                     fdnode.id );
         }
         else
           node = node.second;
      }

      if ( fdnode == null ) {// function not found
         error( "Function definition for [" + funcName + "] not found" );
         return null;
      }

      return fdnode;  

   }// findFunction
    
   public Node evaluate() {
      if (kind.equals("number")) {
         return this;
      }
      else if (kind.equals("list")) {
         if (first == null) { //is an empty list
            return this;
         }
         else { //list contains an expression to evaluate
            Node expression = first.first;
            if (expression == null) //empty list
               return this;
            if (expression.info.equals("plus")) {
               double arg1 = Double.parseDouble(first.second.first.evaluate().info);
               double arg2 = Double.parseDouble(first.second.second.first.evaluate().info);
               return new Node(Double.toString(arg1 + arg2));
            }
            else if (expression.info.equals("minus")) {
               double arg1 = Double.parseDouble(first.second.first.evaluate().info);
               double arg2 = Double.parseDouble(first.second.second.first.evaluate().info);
               return new Node(Double.toString(arg1 - arg2));
            }
            else if (expression.info.equals("times")) {
               double arg1 = Double.parseDouble(first.second.first.evaluate().info);
               double arg2 = Double.parseDouble(first.second.second.first.evaluate().info);
               return new Node(Double.toString(arg1 * arg2));
            }
            else if (expression.info.equals("div")) {
               double arg1 = Double.parseDouble(first.second.first.evaluate().info);
               double arg2 = Double.parseDouble(first.second.second.first.evaluate().info);
               return new Node(Double.toString(arg1 / arg2));
            }
            else if (expression.info.equals("lt")) {
               double arg1 = Double.parseDouble(first.second.first.evaluate().info);
               double arg2 = Double.parseDouble(first.second.second.first.evaluate().info);
               if (arg1 < arg2)
                  return new Node("1");
               else
                  return new Node("0");
            }
            else if (expression.info.equals("le")) {
               double arg1 = Double.parseDouble(first.second.first.evaluate().info);
               double arg2 = Double.parseDouble(first.second.second.first.evaluate().info);
               if (arg1 <= arg2)
                  return new Node("1");
               else
                  return new Node("0");
            }
            else if (expression.info.equals("eq")) {
               double arg1 = Double.parseDouble(first.second.first.evaluate().info);
               double arg2 = Double.parseDouble(first.second.second.first.evaluate().info);
               if (arg1 == arg2)
                  return new Node("1");
               else
                  return new Node("0");
            }
            else if (expression.info.equals("ne")) {
               double arg1 = Double.parseDouble(first.second.first.evaluate().info);
               double arg2 = Double.parseDouble(first.second.second.first.evaluate().info);
               if (arg1 != arg2)
                  return new Node("1");
               else
                  return new Node("0");
            }
            else if (expression.info.equals("and")) {
               double arg1 = Double.parseDouble(first.second.first.evaluate().info);
               double arg2 = Double.parseDouble(first.second.second.first.evaluate().info);
               if ((arg1 != 0) && (arg2 != 0))
                  return new Node("1");
               else
                  return new Node("0");
            }
            else if (expression.info.equals("or")) {
               double arg1 = Double.parseDouble(first.second.first.evaluate().info);
               double arg2 = Double.parseDouble(first.second.second.first.evaluate().info);
               if ((arg1 != 0) || (arg2 != 0))
                  return new Node("1");
               else
                  return new Node("0");
            }
            else if (expression.info.equals("not")) {
               double arg1 = Double.parseDouble(first.second.first.evaluate().info);
               if (arg1 == 0)
                  return new Node("1");
               else
                  return new Node("0");
            }
            else if (expression.info.equals("ins")) {
               Node arg1 = first.second.first.evaluate(); //arg1 is an expression
               Node arg2 = first.second.second.first.evaluate(); //arg2 is a list
               if (arg2.first == null) { //arg2 is empty
                  Node front = new Node("items", arg1, null, null);
                  arg2.first = front;
                  return arg2;
               }
               else {
                  Node front = new Node("items", arg1, arg2.first, null);
                  return new Node("list", front, null, null);
               }
            }
            else if (expression.info.equals("first")) {
               Node arg1 = first.second.first.evaluate(); //arg1 is a list
               if (arg1.first == null) {
                  error("Error: Tried to get the first element of an empty list");
               }
               else {
                  return arg1.first.first;
               }
            }
            else if (expression.info.equals("rest")) { //currently assumes arg1 has at least one item
               Node arg1 = first.second.first.evaluate(); //arg1 is a list
               arg1.first = arg1.first.second;
               return arg1;
            }
            else if (expression.info.equals("null")) {
                Node arg1 = first.second.first;
                if (arg1.first == null) {
                    return new Node("1");
                }
                else {
                    return new Node("0");
                }
            }
            else if (expression.info.equals("num")) {
                Node arg1 = first.second.first;
                if (arg1.kind == "num") {
                    return new Node("1");
                }
                else {
                    return new Node("0");
                }
            }
            else if (expression.info.equals("list")) {
                Node arg1 = first.second.first;
                if (arg1.kind == "list") {
                    return new Node("1");
                }
                else {
                    return new Node("0");
                }
            }
            else if (expression.info.equals("read")) {
                System.out.println("?");
                Scanner input = new Scanner(System.in);
                //String in = input;
                return new Node(input.next());
            }
            else if (expression.info.equals("write")) {
                System.out.print(first.second.first.evaluate() + " ");
            }
            else if (expression.info.equals("nl")) {
                System.out.println();
            }
            else if (expression.info.equals("quote")) {
                return first.second.first;
            }
            else if (expression.info.equals("quit")) {
                System.exit(1);
            }
            else if (expression.info.equals("if")) {
                Node arg1 = first.second.first;
                if(arg1.evaluate().info.equals("0")){
                    Node arg3 = first.second.second.second.first;
                    return arg3.evaluate();
                }
                else{
                    Node arg2 = first.second.second.first;
                    return arg2.evaluate();
                }
            }
            else if (expression.kind.equals("number")) {
               return expression;
            }
            else if (expression.kind.equals("expr")) { //can only be an expression holding a list
               return expression.first.evaluate();
            }
            else { //user defined expression
               Node body = passArgs(this, expression.info);
               Node returnValueNode = body.second.evaluate();
               memStack.remove(memStack.size() - 1); //removes the memTable holding our parameters once we are done with it
               return returnValueNode;
            }
         }
      }
      else if (kind.equals("expr")) {
         return first.evaluate(); //first will always be a list
      }
      else if (kind.equals("name")) { //node has to be a parameter or a user defined function without any parameters
          Node body = findFunction(info);
          if (body == null) { //name is not found in user defined functions so the node is a parameter
              return memStack.get(memStack.size() - 1).retrieve(info); //gets the value of the parameter from the memTable at the top of memStack
          } else //name was found in the user defined functions
              return body.second.evaluate();
      }
      return new Node("0");
   }//evaluate

}// Node
