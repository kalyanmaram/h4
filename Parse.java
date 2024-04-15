import java.io.*;
import java.util.*;

public class Parse {
   private String line;
   private Scanner scan;
   private int[] vars;
   private int currentIndentLevel = 0;

   // constructor
   public Parse() {
      line = "";
      scan = new Scanner(System.in);

      vars = new int[26];
      for (int i = 0; i < 26; i++)
         vars[i] = 0;
   }

   // entry point into Parse
   public void run() throws IOException {
      String token = "";
      System.out.println("Welcome to SQRL...");

      token = getToken();
      parseCode(token); // <SQRL> ::= <code> .
   }

   // prase token for <code>
   private void parseCode(String token) throws IOException {
      do {
         parseStmt(token); // <code> ::= <statement> <code>
         token = getToken();
      } while (!token.equals("."));
   }

   // parse token for <statement>
   private void parseStmt(String token) throws IOException {
      int val;
      String str;

      if (token.equals("load")) // <statement> ::= load <string>
      {
         token = getToken();
         str = parseString(token);

         // execution part of my parser
         line = loadPgm(str) + line;
      } else if (token.equals("print")) // <statement> ::= print <expr> | print <string>
      {
         token = getToken();
         if (token.equals("\"")) {
            str = parseString(token);

            // execution part of my parser
            System.out.println(str);
         } else {
            val = parseExpr(token);

            // execution part of my parser
            System.out.println(val);
         }
      } else if (token.equals("input")) // <statement> ::= input <var>
      {
         token = getToken();
         if (!isVar(token))
            reportError(token);

         // execution part of my parser
         System.out.print("? ");
         val = scan.nextInt();
         storeVar(token, val);
      } else if (token.equals("if")) // <statement> ::= if <cond> <statement>
      {
         int condition = parseCond(); // Parse and evaluate the condition
         if (condition == 1) {
            // Condition is true, execute the following block
            token = getToken(); // Consume the condition
            while (!token.isEmpty()) {
               if (token.equals("print")) {
                  token = getToken();
                  if (token.equals("\"")) {
                     // Handle print with string
                     str = parseString(token);
                     System.out.println(str);
                  } else {
                     // Handle print with expression
                     val = parseExpr(token);
                     System.out.println(val);
                  }
               }
               // Add more conditions as needed for other statement types
               token = getToken();
            }
         } else {
            // Condition is false, skip the following block
            token = getToken(); // Consume the condition
            int indentLevel = 0; // Track the indentation level
            while (!token.isEmpty() && getIndentLevel(token) > indentLevel) {
               // Skip the block by parsing and consuming tokens until the indentation level
               // decreases
               token = getToken();
            }
         }
      } else if (isVar(token)) // <statement> ::= <var> := <expr>
      {
         String var = token;
         token = getToken(); // Expecting '='
         if (!token.equals("="))
            reportError(token);
         val = parseExpr(getToken());
         storeVar(var, val);

      } else
         reportError(token);
   }

   // load program from file
   private String loadPgm(String name) throws IOException {
      String buffer = "";
      File file = new File(name);
      Scanner fileScan = new Scanner(file);

      while (fileScan.hasNextLine())
         buffer += fileScan.nextLine() + "\n";

      return buffer;
   }

   // parse <string>
   private String parseString(String token) {
      int i;
      String str = "";

      if (!token.equals("\""))
         reportError(token);

      for (i = 0; i < line.length(); i++)
         if (line.charAt(i) == '"')
            break;

      if (i >= line.length())
         reportError(token);

      str = line.substring(0, i);
      line = line.substring(i + 1);

      return str;
   }

   // Parse and evaluate the condition
   private int parseCond() throws IOException {
      int leftVal = parseVal(getToken());
      String operator = getToken();
      int rightVal = parseVal(getToken());

      switch (operator) {
         case "==":
            return (leftVal == rightVal) ? 1 : 0;
         case ">":
            return (leftVal > rightVal) ? 1 : 0;
         case "<":
            return (leftVal < rightVal) ? 1 : 0;
         default:
            reportError("Invalid operator: " + operator);
            return 0;
      }
   }

   // Get the indentation level of a token
   private int getIndentLevel(String token) {
      int indentLevel = 0;
      while (token.charAt(indentLevel) == ' ') {
         indentLevel++;
      }
      return indentLevel / 2; // Assuming each indentation level is 2 spaces
   }

   // parse <expr>
   private int parseExpr(String token) {
      int val;

      val = parseVal(token);
      token = getToken();

      switch (token.charAt(0)) {
         case '+':
            token = getToken();
            val = val + parseVal(token); // ::= <val> + <val>
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
   private int parseVal(String token) {
      if (isNumeric(token))
         return Integer.parseInt(token);
      if (isVar(token))
         return parseVar(token);

      reportError(token);

      return -1; // will never happen
   }

   // checks to see if token is a <num>
   private boolean isNumeric(String token) {
      for (int i = 0; i < token.length(); i++)
         if (!Character.isDigit(token.charAt(i)))
            return false;

      return true;
   }

   // returns value of <var>
   private int parseVar(String token) {
      if (!isVar(token))
         reportError(token);

      return vars[((int) token.charAt(0)) - 97];
   }

   // store value into <var>
   private void storeVar(String token, int val) {
      vars[((int) token.charAt(0)) - 97] = val;
   }

   // check to see if token is a <var>
   private boolean isVar(String token) {
      return (token.length() == 1) & isAlpha(token.charAt(0));
   }

   // is it a through z?
   private boolean isAlpha(char ch) {
      return ((int) ch) >= 97 && ((int) ch) <= 122;
   }

   // reports syntax error
   private void reportError(String token) {
      line += "\n";
      line = line.substring(0, line.indexOf("\n"));

      System.out.println("ERROR: " + token + line);
      for (int i = 0; i < token.length() + 6; i++)
         System.out.print(" ");
      System.out.println("^");

      System.exit(-1);
   }

   // check for blank space
   private boolean isBlank(char ch) {
      switch (ch) {
         case ' ':
         case '\t':
         case '\r':
         case '\n':
            return true;
         default:
            return false;
      }
   }

   // check for delimiter
   private boolean isDelim(char ch) {
      switch (ch) {
         case '.':
         case '\"':
         case '_':
         case '>':
         case '<':
         case '=':
         case '+':
         case '-':
         case '*':
         case '/':
            return true;
         default:
            return isBlank(ch);
      }
   }

   // check for leading spaces
   private String skipLeadingBlanks(String buffer) {
      int i;

      // skip over blan spaces
      for (i = 0; i < buffer.length(); i++) {
         if (!isBlank(buffer.charAt(i)))
            break;
      }

      return buffer.substring(i);
   }

   // grab the next token
   private String getToken() {
      String token;

      line = skipLeadingBlanks(line);
      while (line.length() == 0) {
         line = scan.nextLine();
         line = skipLeadingBlanks(line);
      }

      for (int i = 0; i < line.length(); i++) {
         if (isDelim(line.charAt(i))) {
            if (i == 0) // if token is also delimster
               i++;

            token = line.substring(0, i);
            line = line.substring(i);

            return token;
         }
      }

      token = line;
      line = "";
      return token;
   }
}
