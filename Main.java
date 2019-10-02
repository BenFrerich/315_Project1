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

        /*
        Table animals = new Table(3);
        ArrayList<String> labels = new ArrayList<String>(Arrays.asList("name", "kind", "years"));
        animals.insertRow(labels);
        ArrayList<Object> row2 = new ArrayList<Object>(Arrays.asList("Joe", "cat", 4));
        animals.insertRow(row2);
        ArrayList<Object> row3 = new ArrayList<Object>(Arrays.asList("Spot", "dog", 10));
        animals.insertRow(row3);
        ArrayList<Object> row4 = new ArrayList<Object>(Arrays.asList("Snoopy", "dog", 3));
        animals.insertRow(row4);
        ArrayList<Object> row5 = new ArrayList<Object>(Arrays.asList("Tweety", "bird", 1));
        animals.insertRow(row5);
        ArrayList<Object> row6 = new ArrayList<Object>(Arrays.asList("Joe", "bird", 2));
        animals.insertRow(row6);
        ArrayList<Object> col1 = new ArrayList<>(Arrays.asList("aname", "Joe", "NOT", "Snoopy", "NOT" , "NOT"));
        animals.insertCol(col1);

        Dbms data = new Dbms();
        data.addTable("animals" , animals);
        data.printDataBase("animals");
        System.out.println();

        Table dogs = animals.select("kind","dog");
        data.addTable("dogs",dogs);
        data.printDataBase("dogs");
        System.out.println();

        Table oldDogs = dogs.select("year", ">", 10);
        data.addTable("Old Dogs", oldDogs);
        data.printDataBase("Old Dogs");
        System.out.println();

        Table temp1 = animals.select("kind","cat");
        data.addTable("temp1",temp1);
        Table catsOrDogs = data.union(dogs,temp1);
        data.addTable("cats or dogs", catsOrDogs);
        data.printDataBase("cats or dogs");
        System.out.println();

        Table species = new Table(1);
        ArrayList<String> label = new ArrayList<String>(Arrays.asList("kind"));
        species.insertRow(label);
        data.addTable("species", species);
        data.insertCommand("species", "animals", label);
        data.printDataBase("species");
        System.out.println();

        Table a = new Table();
        ArrayList<Object> header = animals.project("name");
        ArrayList<Object> header2 = animals.project("kind");
        ArrayList<String> label3 = new ArrayList<String>(Arrays.asList("name","kind"));
        ArrayList<String> label4 = new ArrayList<String>(Arrays.asList("aname","akind"));
        a.insertCol(header);
        a.insertCol(header2);
        a.rename("a", a, label3,label4);
        data.addTable("a",a);
        data.printDataBase("a");
        System.out.println();


        Table commonNames = animals.select("name", "==", "aname");
        data.addTable("common names", commonNames);
        data.printDataBase("common names");
        System.out.println();

        Table answer = new Table(commonNames);
        data.addTable("answer", answer);
        data.printDataBase("answer");
        System.out.println();



        */





    }

}


