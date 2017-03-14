/**
 * Lexer for Pattern Extractor
 */
package edu.uchicago.cs.encsel.colpattern.lexer;

import java_cup.runtime.*;
%%
%class Lexer
%unicode
%cupsym Sym
%function scan
%type Symbol
%{
  private Symbol symbol(int type) {
    return new Symbol(type, yyline, yycolumn);
  }
  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
  }
%}

LineTerminator = \r|\n|\r\n
Whitespace     = ({LineTerminator} | [ \t\f])+

IntLiteral=[0-9]+
DoubleLiteral=[0-9]+\.[0-9]+
WordLiteral=[a-zA-Z][a-zA-Z\.']*
%%

<YYINITIAL> {
{IntLiteral}        {return symbol(Sym.INTEGER, yytext());}
{DoubleLiteral}     {return symbol(Sym.DOUBLE, yytext());}
{WordLiteral}       {return symbol(Sym.WORD, yytext());}
{Whitespace}        {return symbol(Sym.SPACE);}
"-"                 {return symbol(Sym.DASH);}
"_"                 {return symbol(Sym.UNDERSCORE);}
"("                 {return symbol(Sym.LPARA);}
")"                 {return symbol(Sym.RPARA);}
"["                 {return symbol(Sym.LBRAK);}
"]"                 {return symbol(Sym.RBRAK);}
"{"                 {return symbol(Sym.LBRAC);}
"}"                 {return symbol(Sym.RBRAC);}
","                 {return symbol(Sym.COMMA);}
"."                 {return symbol(Sym.PERIOD);}
":"                 {return symbol(Sym.SEMICOLON);}
";"                 {return symbol(Sym.COLON);}
"/"                 {return symbol(Sym.SLASH);}
"\\"                 {return symbol(Sym.BACKSLASH);}
}
.                   {return symbol(Sym.OTHER);}

