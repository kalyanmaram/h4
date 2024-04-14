import java.io.*;
import java.util.*;

public class Parse
{
   private String line;
   private Scanner scan;
   private int[] vars;

   // constructor
   public Parse()
   {
      line = "";
      scan = new Scanner(System.in);

      vars = new int[26];
      for (int i=0; i<26; i++)
          vars[i] = 0;
   }

   // entry point into Parse
   public void run() throws IOException
   {
      String token = "";
      System.out.println("Welcome to SQRL...");

      token = getToken();
      parseCode(token);                     // <SQRL> ::= <code> .
   }

   // prase token for <code>
   private void parseCode(String token) throws IOException
   {
       do {
          parseStmt(token);                // <code> ::= <statement> <code>
          token = getToken();
       } while (!token.equals("."));
   }

   // parse token for <statement>
   private void parseStmt(String token) throws IOException
   {
      int val;
      String str;

      if (token.equals("load"))            // <statement> ::= load <string>
      {
         token = getToken();
         str = parseString(token);

         // execution part of my parser
         line = loadPgm(str) + line;
      }
      else if (token.equals("print"))      // <statement> ::= print <expr> | print <string>
      {
         token = getToken();
         if (token.equals("\""))
         {
            str = parseString(token);

            // execution part of my parser
            System.out.println(str);
         }
         else
         {
            val = parseExpr(token);

            // execution part of my parser
           System.out.println(val);
         }
      }
      else if (token.equals("input"))      // <statement> ::= input <var>
      {
         token = getToken();
         if (!isVar(token))
            reportError(token);

         // execution part of my parser
         System.out.print("? ");
         val = scan.nextInt();
         storeVar(token, val);
      }
      else if (token.equals("if"))         // <statement> ::= if <cond> <statement>
      {
      }
      else if (isVar(token))               // <statement> ::= <var> := <expr>
      {
         
      }
      else
         reportError(token);
   }

   // load program from file
   private String loadPgm(String name) throws IOException
   {
      String buffer = "";
      File file = new File(name);
      Scanner fileScan = new Scanner(file);

      while (fileScan.hasNextLine())
         buffer += fileScan.nextLine() + "\n";

      return buffer;
   }

   // parse <string>
   private String parseString(String token)
   {
      int i;
      String str = "";

      if (!token.equals("\""))
         reportError(token);

      for (i=0; i<line.length(); i++)
         if (line.charAt(i) == '"')
            break;

      if (i >= line.length())
         reportError(token);

      str = line.substring(0, i);
      line = line.substring(i+1);

      return str;
   }

   // parse <expr>
   private int parseExpr(String token)
   {
      int val;

      val = parseVal(token);
      token = getToken();

      switch (token.charAt(0))
      {
         case '+':
            token = getToken();
            val = val + parseVal(token);  // ::= <val> + <val>
         case '-':
            break;
         case '*':
            break;
         case '/':
            break;
         default:
             line = token + line;
      }

      return val;
   }

   // parses token for <val>, returns an integer value
   private int parseVal(String token)
   {
      if (isNumeric(token))
         return Integer.parseInt(token);
      if (isVar(token))
         return parseVar(token);

      reportError(token);

      return -1;  // will never happen
   }

   // checks to see if token is a <num>
   private boolean isNumeric(String token)
   {
      for (int i=0; i<token.length(); i++)
         if ( !Character.isDigit(token.charAt(i)) )
            return false;

      return true;
   }

   // returns value of <var>
   private int parseVar(String token)
   {
      if (!isVar(token))
         reportError(token);

      return vars[ ((int)token.charAt(0)) - 97 ];
   }

   // store value into <var>
   private void storeVar(String token, int val)
   {
      vars[ ((int)token.charAt(0)) - 97 ] = val;
   }

   // check to see if token is a <var>
   private boolean isVar(String token)
   {
       return (token.length() == 1) & isAlpha(token.charAt(0));
   }

   // is it a through z?
   private boolean isAlpha(char ch)
   {
      return ((int)ch) >= 97 && ((int)ch) <= 122;
   }

   // reports syntax error
   private void reportError(String token)
   {
      line += "\n";
      line = line.substring(0, line.indexOf("\n"));

      System.out.println("ERROR: " + token + line);
      for (int i=0; i<token.length()+6; i++)
         System.out.print(" ");
      System.out.println("^");

      System.exit(-1);
   }

   // check for blank space
   private boolean isBlank(char ch)
   {
      switch (ch)
      {
         case ' ':
         case '\t':
         case '\r': case '\n':
            return true;
         default:
            return false;
       }
   }

   // check for delimiter
   private boolean isDelim(char ch)
   {
      switch (ch)
      {
         case '.':
         case '\"':
         case '_':
         case '>': case '<': case '=':
         case '+': case '-': case '*': case '/':
            return true;
         default:
            return isBlank(ch);
      }
   }

   // check for leading spaces
   private String skipLeadingBlanks(String buffer)
   {
      int i;

      // skip over blan spaces
      for (i=0; i<buffer.length(); i++)
      {
         if (!isBlank(buffer.charAt(i)))
            break;
      }

      return buffer.substring(i);
   }

   // grab the next token
   private String getToken()
   {
      String token;

      line = skipLeadingBlanks(line);
      while (line.length() == 0)
      {
         line = scan.nextLine();
         line = skipLeadingBlanks(line);
      }

      for (int i=0; i<line.length(); i++)
      {
         if (isDelim(line.charAt(i)))
         {
            if (i == 0)  // if token is also delimster
               i++;

            token = line.substring(0,i);
            line = line.substring(i);

            return token;
         }
      }

      token = line;
      line = "";
      return token;
   }
}
