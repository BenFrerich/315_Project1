package project1;
import java.io.File;
import java.io.FileNotFoundException;
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

        Table temp1 = new Table(3);
        ArrayList<String> labels = new ArrayList<String>(Arrays.asList("Name", "Kind", "Years"));
        temp1.insertRow(labels);
        ArrayList<String> row11 = new ArrayList<String>(Arrays.asList("Joe", "Dog", "10"));
        ArrayList<String> row12 = new ArrayList<String>(Arrays.asList("Ben", "Cat", "11"));
        ArrayList<String> row13 = new ArrayList<String>(Arrays.asList("Chris", "Bird", "120"));
        ArrayList<String> row14 = new ArrayList<String>(Arrays.asList("Brandon", "Dog", "140"));
        ArrayList<String> row15 = new ArrayList<String>(Arrays.asList("John", "Lizard", "1"));
        ArrayList<String> row16 = new ArrayList<String>(Arrays.asList("Beatrish", "Dog", "7"));
        ArrayList<String> row17 = new ArrayList<String>(Arrays.asList("Pual", "Cat", "76"));
        temp1.insertRow(row11);
        temp1.insertRow(row12);
        temp1.insertRow(row13);
        temp1.insertRow(row14);
        temp1.insertRow(row15);
        temp1.insertRow(row16);
        temp1.insertRow(row17);


        Table temp2 = new Table(3);
        ArrayList<String> labels2 = new ArrayList<String>(Arrays.asList("Name", "Kind", "Years"));
        temp2.insertRow(labels2);
        ArrayList<String> row22 = new ArrayList<String>(Arrays.asList("Billy", "Cat", "45"));
        temp2.insertRow(row22);

        Dbms data = new Dbms();

        data.addTable("temp1" , temp1);
        data.addTable("temp2" , temp2);

//        data.printDataBaseTest();
//        data.deleteTable("temp1");
//        data.printDataBaseTest();

//        String[] name = new String[3];
//        name[0] = "Name";
//        name[1] = "Kind";
//        name[2] = "Years";
//
//        String[] newName = new String[3];
//        newName[0] = "newName";
//        newName[1] = "newKind";
//        newName[2] = "newYears";
//
//
//        temp1.rename("temp1", temp1, name, newName);

        String[] name = new String[2];
        name[0] = "Cat";
        name[1] = "Dog";

        data.printDataBaseTest();
        temp1.delete("temp1", temp1, name);
        data.printDataBaseTest();

    }

}
