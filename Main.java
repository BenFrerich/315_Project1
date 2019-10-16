package project1;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import csce315.project1.Credits;
import csce315.project1.Movie;
import csce315.project1.MovieDatabaseParser;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import project1.antlr4.MyRulesBaseListener;
import project1.antlr4.RulesLexer;
import project1.antlr4.RulesParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import javax.xml.crypto.dsig.keyinfo.KeyValue;

public class Main extends Application {

    public static MyRulesBaseListener listener = new MyRulesBaseListener();

    // main function, clears input text and loads application
    public static void main(String[] args) throws IOException {
        writeToInputText("");
        Application.launch(args);
    }

    // initializing application window
    Stage window;
    Scene scene1, scene2;

    // start function, called by launch(args) in main
    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;

        //Co Stars pane:
        SplitPane splitPaneCoStars = new SplitPane(); // creating the split pane
        splitPaneCoStars.setOrientation(Orientation.VERTICAL);
        GridPane gridPaneCoStars = new GridPane(); // create input grid plane
        gridPaneCoStars.setHgap(25);
        gridPaneCoStars.setVgap(25);
        GridPane gridPaneCoStarsOutput = new GridPane(); // create output grid pane
        gridPaneCoStarsOutput.setHgap(25);
        gridPaneCoStarsOutput.setVgap(25);
        splitPaneCoStars.getItems().addAll(gridPaneCoStars, gridPaneCoStarsOutput); // add grid panes to split pane

        //Typecasting gridpane:
        SplitPane splitPaneType = new SplitPane();
        splitPaneType.setOrientation(Orientation.VERTICAL);
        GridPane gridPaneType = new GridPane();
        gridPaneType.setHgap(25);
        gridPaneType.setVgap(25);
        GridPane gridPaneTypeOutput = new GridPane();
        gridPaneTypeOutput.setHgap(25);
        gridPaneTypeOutput.setVgap(25);
        splitPaneType.getItems().addAll(gridPaneType, gridPaneTypeOutput);

        //Cover Roles gridpane:
        SplitPane splitPaneRoles = new SplitPane();
        splitPaneRoles.setOrientation(Orientation.VERTICAL);
        GridPane gridPaneRoles = new GridPane();
        gridPaneRoles.setHgap(25);
        gridPaneRoles.setVgap(25);
        GridPane gridPaneRolesOutput = new GridPane();
        gridPaneRolesOutput.setHgap(25);
        gridPaneRolesOutput.setVgap(25);
        splitPaneRoles.getItems().addAll(gridPaneRoles, gridPaneRolesOutput);

        //TabPane that holds all the gridpanes
        TabPane tabPane = new TabPane();
        Tab coStars = new Tab("Co-Stars", splitPaneCoStars);
        Tab typeCasting = new Tab("Typecasting", splitPaneType);
        Tab coverRoles = new Tab("Cover Roles", splitPaneRoles);
        tabPane.getTabs().add(coStars);
        tabPane.getTabs().add(typeCasting);
        tabPane.getTabs().add(coverRoles);

        //***********************************************************//

        //Co-Stars tab
        Label coStarLabel = new Label("&");
        Label coStarsOutput = new Label();
        coStarsOutput.setWrapText(true);
        coStarsOutput.setTextAlignment(TextAlignment.JUSTIFY);
        TextField mainActor = new TextField();
        mainActor.setText("Main Actor");
        TextField coStar = new TextField();
        coStar.setText("# of co-star appearances");
        gridPaneCoStars.add(mainActor, 2, 4);
        gridPaneCoStars.add(coStar, 4, 4);
        gridPaneCoStars.add(coStarLabel, 3, 4);
        Button findCoStars = new Button("Find!"); // find co-starts that correspond to the correct number of appearances needed
        gridPaneCoStars.add(findCoStars, 3, 5);
        GridPane.setHalignment(coStarLabel, HPos.CENTER);
        findCoStars.setOnAction(action -> {

            listener.myDbms.dataBase.clear();

            try {
                initializeBigTables();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ArrayList<String> actors = new ArrayList<>();

            try {
                actors = coStars(mainActor.getText(), Integer.parseInt(coStar.getText()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            gridPaneCoStarsOutput.getChildren().remove(coStarsOutput);

            if(actors.isEmpty()){
                coStarsOutput.setText("None were found");
                coStarsOutput.setFont(new Font("Arial", 15));
                gridPaneCoStarsOutput.add(coStarsOutput,9,1);
            } else {
                coStarsOutput.setText(actors.toString().replace("[", "").replace("]",""));
                coStarsOutput.setFont(new Font("Arial", 15));
                gridPaneCoStarsOutput.add(coStarsOutput,1,1);
            }

            GridPane.setHalignment(coStarsOutput, HPos.CENTER);


        });

        //**********************Typecasting**********************//
        Label typeOutput = new Label();
        typeOutput.setWrapText(true);
        typeOutput.setTextAlignment(TextAlignment.JUSTIFY);
        TextField typecastActor = new TextField();
        typecastActor.setText("Actor Name");
        Button findGenre = new Button("Find Genres!");
        gridPaneType.add(typecastActor, 5, 4);
        gridPaneType.add(findGenre, 6, 4);
        findGenre.setOnAction(action -> {

            ArrayList<String> genres = new ArrayList<>();
            listener.myDbms.dataBase.clear();

            try {
                initializeBigTables();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                genres = typeCasting(typecastActor.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }

            gridPaneTypeOutput.getChildren().remove(typeOutput);

            if(genres.isEmpty()){
                typeOutput.setText("None were found");
                typeOutput.setFont(new Font("Arial", 15));
                gridPaneTypeOutput.add(typeOutput,9,1);
            } else {
                typeOutput.setText(genres.toString().replace("[", "").replace("]",""));
                typeOutput.setFont(new Font("Arial", 15));
                gridPaneTypeOutput.add(typeOutput,9,1);
            }

            GridPane.setHalignment(typeOutput, HPos.CENTER);
        });

        //**********************Cover-Roles**********************//
        TextField charName = new TextField();
        Label rolesOutput = new Label();
        rolesOutput.setWrapText(true);
        rolesOutput.setTextAlignment(TextAlignment.JUSTIFY);
        charName.setText("Character Name");
        Button findActorsForChar = new Button("Find Actors!");
        gridPaneRoles.add(charName, 5, 4);
        gridPaneRoles.add(findActorsForChar, 6, 4);
        findActorsForChar.setOnAction(action -> {

            List<String> actors = new ArrayList<>();

            listener.myDbms.dataBase.clear();

            try {
                initializeBigTables();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                actors = coverRoles(charName.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }

            gridPaneRolesOutput.getChildren().remove(rolesOutput);

            if(actors.isEmpty()){
                rolesOutput.setText("None were found");
                rolesOutput.setFont(new Font("Arial", 15));
                gridPaneRolesOutput.add(rolesOutput,9,1);
            } else {
                rolesOutput.setText(actors.toString().replace("[", "").replace("]",""));
                rolesOutput.setFont(new Font("Arial", 15));
                gridPaneRolesOutput.add(rolesOutput,1,1);
            }

            GridPane.setHalignment(rolesOutput, HPos.CENTER);
        });

        //**********************Window**********************//
        scene1 = new Scene(tabPane, 600, 600);
        scene1.getStylesheets().add(getClass().getResource("style.css").toString());
        window.setScene(scene1);
        window.setTitle("Movie Magic");
        window.setResizable(false);
        window.show();
    }

    // writes our queries into the input.txt file to be used later
    public static void writeToInputText(String query) throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/Benjey/onedrive/csce-315/csce315-p1/src/project1/input.txt"));
        writer.write(query);
        writer.close();
    }

    // calls the listener to walk through the queries in input.txt
    public static void callListener() throws IOException {

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
            walker.walk(listener, programContext);
        }

    }

    // parses and initializes the cast table in our database
    public static Table initCastTable( String id, ArrayList<Credits.CastMember> cast) {

        Table castTable = new Table();
        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> genders = new ArrayList<>();
        ArrayList<String> characters = new ArrayList<>();
        ArrayList<String> orders = new ArrayList<>();
        ArrayList<String> cast_ids = new ArrayList<>();
        ArrayList<String> ids = new ArrayList<>();

        names.add("name");
        genders.add("gender");
        characters.add("character");
        orders.add("order");
        cast_ids.add("cast_id");
        ids.add("id");

        for (Credits.CastMember member : cast) {
            names.add(member.getName());
            genders.add(Integer.toString(member.getGender()));
            characters.add(member.getCharacter().replace("(", "").replace(")", "").replace(" voice", "").replace(" ", "_"));
            orders.add(Integer.toString(member.getOrder()));
            cast_ids.add(Integer.toString(member.getCast_id()));
            ids.add(Integer.toString(member.getId()));
        }

        castTable.insertCol(names);
        castTable.insertCol(genders);
        castTable.insertCol(characters);
        castTable.insertCol(orders);
        castTable.insertCol(cast_ids);
        castTable.insertCol(ids);
        castTable.setName("cast" + id);

        return castTable;

    }

    // parses and initializes the crew table in our database
    public static Table initCrewTable(String id, ArrayList<Credits.CrewMember> crew) {
        Table crewTable = new Table();

        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> genders = new ArrayList<>();
        ArrayList<String> department = new ArrayList<>();
        ArrayList<String> job = new ArrayList<>();
        ArrayList<String> ids = new ArrayList<>();

        names.add("name");
        genders.add("gender");
        department.add("department");
        job.add("job");
        ids.add("id");

        for (Credits.CrewMember member: crew) {
            names.add(member.getName());
            genders.add(Integer.toString(member.getGender()));
            department.add(member.getDepartment());
            job.add(member.getJob());
            ids.add(Integer.toString(member.getId()));
        }

        crewTable.insertCol(names);
        crewTable.insertCol(genders);
        crewTable.insertCol(department);
        crewTable.insertCol(job);
        crewTable.insertCol(ids);
        crewTable.setName("crew" + id);

        return crewTable;
    }

    // parses and initializes the movie table in our database
    public static Table initMovieTable(int id, Movie movie) {

        Table movieTable = new Table();

        ArrayList<String> adult = new ArrayList<>();
        ArrayList<String> budget = new ArrayList<>();
        ArrayList<String> genres = new ArrayList<>();
        ArrayList<String> ids = new ArrayList<>();
        ArrayList<String> orgLang = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();
        ArrayList<String> popularity = new ArrayList<>();
        ArrayList<String> productionCompany = new ArrayList<>();
        ArrayList<String> countries = new ArrayList<>();
        ArrayList<String> releaseDate = new ArrayList<>();
        ArrayList<String> spokenLang = new ArrayList<>();
        ArrayList<String> voteAvg = new ArrayList<>();
        ArrayList<String> status = new ArrayList<>();

        adult.add("adult");
        budget.add("budget");
        genres.add("genres");
        ids.add("id");
        orgLang.add("original language");
        titles.add("title");
        popularity.add("popularity");
        productionCompany.add("production company");
        countries.add("countries");
        releaseDate.add("release date");
        spokenLang.add("spoken languages");
        voteAvg.add("vote average");
        status.add("status");

        adult.add(Boolean.toString(movie.getAdult()));
        budget.add(Integer.toString(movie.getBudget()));
        String combinedGenre = "";

        if (movie.getGenres().size() != 0) {
            for (int i = 0; i < movie.getGenres().size(); i++) {
                combinedGenre += movie.getGenres().get(i).getName().replace(" ", "_") + " ";

            }
        }

        genres.add(combinedGenre);
        ids.add(Integer.toString(movie.getId()));
        orgLang.add(movie.getOriginal_language());
        titles.add(movie.getTitle());
        popularity.add(Double.toString(movie.getPopularity()));
        String productionCompString = "";

        if (movie.getProduction_companies().size() != 0) {
            for (int i = 0; i < movie.getProduction_companies().size(); i++) {
                productionCompString += movie.getProduction_companies().get(i).getName();
            }
        }

        productionCompany.add(productionCompString);
        String countryString= "";

        if (movie.getProduction_countries().size() != 0) {
            for (int i = 0; i < movie.getProduction_countries().size(); i++) {
                countryString += movie.getProduction_countries().get(i).getName();
            }
        }

        countries.add(countryString);
        releaseDate.add(movie.getRelease_date());
        String spokenLangString = "";

        if (movie.getSpoken_languages().size() != 0) {
            for (int i = 0; i < movie.getSpoken_languages().size(); i++) {
                spokenLangString += movie.getSpoken_languages().get(i).getName();
            }
        }

        spokenLang.add(spokenLangString);
        voteAvg.add(Double.toString(movie.getVote_average()));
        status.add(movie.getStatus());

        movieTable.insertCol(adult);
        movieTable.insertCol(budget);
        movieTable.insertCol(genres);
        movieTable.insertCol(ids);
        movieTable.insertCol(titles);
        movieTable.insertCol(popularity);
        movieTable.insertCol(productionCompany);
        movieTable.insertCol(countries);
        movieTable.insertCol(releaseDate);
        movieTable.insertCol(spokenLang);
        movieTable.insertCol(orgLang);
        movieTable.insertCol(voteAvg);
        movieTable.insertCol(status);
        movieTable.setName(Integer.toString(id));

        return movieTable;
    }

    // co-star function called when the button in co-star panel is pressed
    public static ArrayList<String> coStars(String actor, Integer numAppearances) throws IOException {

        HashMap<String, Integer> actors = new HashMap<String, Integer>();
        ArrayList<String> tableNames = new ArrayList<>();
        ArrayList<String> actorNames = new ArrayList<>();
        String query = "CREATE TABLE costars (name VARCHAR(20)) PRIMARY KEY (name);\n";

        for(Object s : listener.myDbms.dataBase.keySet()) {
            if(s.toString().contains("cast")) {
                String tableName = s.toString();
                Table newTable = (Table)listener.myDbms.dataBase.get(tableName);
                for(int i = 1; i < newTable.getRowCount(); i++)
                {
                    ArrayList<String> col = (ArrayList<String>) newTable.table.get(0);
                    if(col.get(i).equals(actor))
                    {
                        query += "CREATE TABLE costars_" + tableName + " (name VARCHAR(20)) PRIMARY KEY (name);\nINSERT INTO costars_"
                                + tableName + " VALUES FROM RELATION project (name) " + tableName + ";\n";
                        tableNames.add("costars_" + s.toString());
                        break;
                    }
                }
            }
        }

        for(String table : tableNames) {
            query += "costars <- costars + " + table + ";\n";
        }

        writeToInputText(query);
        callListener();
        Table newTableCostars = listener.myDbms.getTable("costars");
        ArrayList<String> col = (ArrayList<String>) (newTableCostars.table.get(0));

        for(int i = 1; i < col.size(); i++) {
            if(!col.get(i).equals(actor)) {
                if(actors.containsKey(col.get(i))) {
                    Integer temp = actors.get(col.get(i));
                    temp++;
                    actors.replace(col.get(i), temp);
                }
                else {
                    actors.put(col.get(i), 1);
                }
            }
        }

        for(Map.Entry<String, Integer> entry : actors.entrySet()) {
            if(entry.getValue().equals(numAppearances)) {
                actorNames.add(entry.getKey());
            }
        }

        return actorNames;
    }

    // type casting function called when the button in type casting panel is pressed
    public static ArrayList<String> typeCasting(String actor) throws IOException {

        String query = "CREATE TABLE genres (genre VARCHAR(20)) PRIMARY KEY (genre);\n";
        ArrayList<String> movieIds = new ArrayList<>();
        ArrayList<String> tableNames = new ArrayList<>();
        ArrayList<String> genres = new ArrayList<>();
        HashMap<String, Integer> genreCounter = new HashMap<>();

        for(Object s : listener.myDbms.dataBase.keySet()) {
            if(s.toString().contains("cast")) {
                String tableName = s.toString();
                Table newTable = (Table)listener.myDbms.dataBase.get(tableName);
                for(int i = 1; i < newTable.getRowCount(); i++)
                {
                    ArrayList<String> col = (ArrayList<String>) newTable.table.get(0);
                    if(col.get(i).equals(actor))
                    {
                        movieIds.add(tableName.replace("cast",""));
                    }
                }
            }
        }

        for(String id : movieIds) {
            Table temp = listener.myDbms.getTable(id);
            query += "CREATE TABLE genres_" + id + " (genre VARCHAR(20)) PRIMARY KEY (genre);\nINSERT INTO genres_"
                    + id + " VALUES FROM RELATION project (genres) " + id + ";\n";
            tableNames.add("genres_" + id);

        }

        for(String table : tableNames) {
            query += "genres <- genres + " + table + ";\n";
        }

        writeToInputText(query);
        callListener();

        Table genreTable = listener.myDbms.getTable("genres");
        ArrayList<String> col = new ArrayList<>();
        col = (ArrayList<String>)genreTable.table.get(0);

        for(int i = 1; i < col.size(); i++) {
            String[] temp = col.get(i).split(" ");
            for(String s : temp) {
                if(genreCounter.containsKey(s)) {
                    Integer t = genreCounter.get(s);
                    t++;
                    genreCounter.replace(s, t);
                }
                else {
                    genreCounter.put(s, 1);
                }
            }

        }

        Integer max = 0;
        for(Map.Entry<String, Integer> entry : genreCounter.entrySet()) {
            if(entry.getValue() > max) { max = entry.getValue(); }
        }

        for(Map.Entry<String, Integer> entry : genreCounter.entrySet()) {
            if(entry.getValue().equals(max)) { genres.add(entry.getKey());  }
        }

        return genres;
    }

    // cover roles function called when the button in cover roles panel is pressed
    public static List<String> coverRoles(String character) throws IOException {

        String query = "CREATE TABLE coverRoles (name VARCHAR(20), gender INTEGER, character VARCHAR(20), order INTEGER, cast_id INTEGER, id INTEGER) PRIMARY KEY (id);\n";
        ArrayList<String> tableNames = new ArrayList<>();
        ArrayList<String> actors = new ArrayList<>();
        character = character.replace(" ", "_");

        for(Object s : listener.myDbms.dataBase.keySet()) {
            if(s.toString().contains("cast")) {
                String tableName = s.toString();
                query += "coverRoles" + s.toString().replace("cast","") + " <- select (character == " + character
                        + ") " + tableName + ";\n";
                tableNames.add("coverRoles" + s.toString().replace("cast",""));
            }
        }

        for(String name : tableNames) {
            query += "coverRoles <- coverRoles + " + name + ";\n";
        }

        query += "CREATE TABLE coverRolesNames (name VARCHAR(20)) PRIMARY KEY (name);\n" +
                "INSERT INTO coverRolesNames VALUES FROM RELATION project (name) coverRoles;\n";

        writeToInputText(query);
        callListener();

        Table coverRoles = listener.myDbms.getTable("coverRolesNames");
        ArrayList<String> col = new ArrayList<>();
        col = (ArrayList<String>)coverRoles.table.get(0);

        for(int i = 1; i < col.size(); i++) {
            actors.add(col.get(i));
        }

        List<String> newActors = actors.stream().distinct().collect(Collectors.toList());
        return newActors;
    }

    // creates a single large table with cast, crew, and movies in it. Calls the init functions
    public static void initializeBigTables() throws IOException {

        MovieDatabaseParser p = new MovieDatabaseParser();
        List<Movie> moviesList = p.deserializeMovies("/Users/Benjey/onedrive/csce-315/csce315-p1/src/project1/datajson/movies.json");
        List<Credits> creditsList = p.deserializeCredits("/Users/Benjey/onedrive/csce-315/csce315-p1/src/project1/datajson/credits.json");

        for (Credits credits : creditsList) {
            Table castTable = initCastTable(credits.getId(), (ArrayList<Credits.CastMember>) credits.getCastMember());
            listener.myDbms.addTable(castTable.getName(), castTable);

            Table crewTable = initCrewTable(credits.getId(), (ArrayList<Credits.CrewMember>) credits.getCrewMember());
            listener.myDbms.addTable(crewTable.getName(), crewTable);
        }

        for (Movie movie : moviesList){
            Table movieTable = initMovieTable(movie.getId(), movie);
            listener.myDbms.addTable(Integer.toString(movie.getId()), movieTable);
        }
    }
}
