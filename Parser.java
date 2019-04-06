/*
    This class provides a recursive descent parser
    for Corgi (the new version),
    creating a parse tree which can be interpreted
    to simulate execution of a Corgi program
*/

import java.util.*;
import java.io.*;

public class Parser {

    private Lexer lex;

    public Parser( Lexer lexer ) {
        lex = lexer;
    }

    public Node parseDefs() {
        System.out.println("-----> parsing <defs>:");

        Node first = parseDef();

        Token token = lex.getNextToken();

        if ( token.isKind("eof") ) {
            return new Node( "defs", first, null, null );
        }
        else {
            lex.putBackToken( token );
            Node second = parseDefs();
            return new Node( "defs", first, second, null );
        }
    }//<defs>

    public Node parseDef() {
        System.out.println("-----> parsing <funcDef>:");

        Token token = lex.getNextToken();
        errorCheck( token, "(" );

        token = lex.getNextToken();  // the function name
        errorCheck( token, "define" );

        token = lex.getNextToken();
        errorCheck( token, "(" );

        Token name = lex.getNextToken();
        errorCheck( name, "name");

        token = lex.getNextToken();

        if ( token.isKind( ")" )) {// no params

            Node second = parseExpr();

            token = lex.getNextToken();
            errorCheck(token, ")");

            return new Node ("def", name.getDetails(), null, second, null);
        }// no params
        else {// have params
            lex.putBackToken( token );
            Node first = parseParams();
            token = lex.getNextToken();
            errorCheck( token,")" );
            Node second = parseExpr();
            token = lex.getNextToken();
            errorCheck(token, ")");
            return new Node("defs", name.getDetails(), first, second, null);
        }// have params

    }// parseDef


    private Node parseParams() {
        System.out.println("-----> parsing <params>:");

        Token name = lex.getNextToken();
        errorCheck( name, "name" );

        Token token = lex.getNextToken();

        if ( token.isKind( ")" ) ) {// no more params
            lex.putBackToken( token );  // funcCall handles the )
            return new Node( "params", name.getDetails(), null, null, null );
        }
        else {// have more params
            Node first = parseParams();
            return new Node( "params", name.getDetails(), first, null, null );
        }

    }// <params>

    private Node parseExpr() {
        System.out.println("-----> parsing <expr>:");

        Token token = lex.getNextToken();

        if ( token.isKind( "name" ) ) {
            return new Node( "expr", token.getDetails(), null, null, null );
        }
        else if ( token.isKind( "number" ) ) {
            return new Node ( "expr", token.getDetails(), null, null, null );
        }
        else {
            lex.putBackToken( token );
            Node first = parseList();
            return new Node( "expr", first, null, null);
        }

    }// <expr>

    private Node parseList() {
        System.out.println("-----> parsing <list>:");

        Token token = lex.getNextToken(); // function name
        errorCheck( token, "(" );

        token = lex.getNextToken();

        if ( token.isKind( ")" ) ) {
            return new Node( "list", null, null, null );
        }
        else {
            lex.putBackToken( token );
            Node first = parseItems();
            token = lex.getNextToken();
            errorCheck( token, ")" );
            return new Node( "list", first, null, null );
        }

    }// <list>

    private Node parseItems() {
        System.out.println("-----> parsing <items>:");

        Node first = parseExpr();

        Token token = lex.getNextToken();

        if ( token.isKind(")") ) {
            lex.putBackToken(token);
            return new Node( "items", first, null, null );
        }
        else {
            lex.putBackToken( token );
            Node second = parseItems();
            return new Node( "items", first, second, null );
        }
    }//<items>

    // check whether token is correct kind
    private void errorCheck( Token token, String kind ) {
        if( ! token.isKind( kind ) ) {
            System.out.println("Error:  expected " + token +
                    " to be of kind " + kind );
            System.exit(1);
        }
    }

    // check whether token is correct kind and details
    private void errorCheck( Token token, String kind, String details ) {
        if( ! token.isKind( kind ) ||
                ! token.getDetails().equals( details ) ) {
            System.out.println("Error:  expected " + token +
                    " to be kind= " + kind +
                    " and details= " + details );
            System.exit(1);
        }
    }

}