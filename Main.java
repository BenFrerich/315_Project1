package project1;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.*;

import project1.antlr4.MyRulesBaseListener;
import project1.antlr4.RulesLexer;
import project1.antlr4.RulesParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        File file = new File("src/project1/input.txt");
        Scanner scanner = new Scanner(file);
        List<String> lines = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.length() != 0) {
                lines.add(line);
            }
        }
        for (String line : lines) {
            CharStream charStream = CharStreams.fromString(line);
            RulesLexer lexer = new RulesLexer(charStream);
            CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
            RulesParser parser = new RulesParser(commonTokenStream);
            lexer.removeErrorListeners();
            parser.removeErrorListeners();
            RulesParser.ProgramContext programContext = parser.program();
            ParseTreeWalker walker = new ParseTreeWalker();
            MyRulesBaseListener listener = new MyRulesBaseListener();
            walker.walk(listener, programContext);

        }

    }

}
