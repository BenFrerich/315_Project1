package project1.antlr4;
//import com.sun.scenario.effect.impl.state.LinearConvolveKernel;
//import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import com.google.gson.Gson;
import org.antlr.v4.runtime.tree.ParseTree;
import project1.Dbms;
import project1.Table;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.*;
public class MyRulesBaseListener extends RulesBaseListener{

    public static Dbms myDbms = new Dbms(); // create our static database
    public static Deque<String> listenerStack = new ArrayDeque<>(); // create our listener stack

    // create a temp database that copies our main database to be used in functions
    public MyRulesBaseListener() {
        Dbms myDbms = new Dbms();
    }

    // create command called by the walker
    @Override public void exitCreate_cmd(RulesParser.Create_cmdContext ctx) {
        ParseTree relationNameNode = ctx.getChild(1);
        ParseTree attributeListNode = ctx.getChild(3);
        ParseTree primaryKeysNode = ctx.getChild(7);

        ArrayList<String> attributeListLeaves = new ArrayList<>(Arrays.asList(attributeListNode.getText().split(", ")));
        ArrayList<String> newAttributeListLeaves = new ArrayList<>();
        ArrayList<String> primaryKeys = new ArrayList<>(Arrays.asList(primaryKeysNode.getText().split(", ")));

        Table temp = new Table(attributeListLeaves.size());
        temp.setPrimaryKey(primaryKeys);

        for(String s : attributeListLeaves)
        {
            if(s.contains("VARCHAR"))
            {
                int i = s.indexOf("VARCHAR");
                temp.addHeaderType(s.substring(i));
                s = s.substring(0,i);
            }
            else if(s.contains("INTEGER"))
            {
                int i = s.indexOf("INTEGER");
                temp.addHeaderType(s.substring(i));
                s = s.substring(0,i);
            }
            newAttributeListLeaves.add(s);
        }

        temp.insertRow(newAttributeListLeaves);
        myDbms.addTable(relationNameNode.getText(), temp);
    }

    // union command called by the walker
    @Override public void exitUnion(RulesParser.UnionContext ctx) {
        ParseTree tableOneNode = ctx.getChild(0);
        ParseTree tableTwoNode = ctx.getChild(2);

        if (myDbms.dataBase.containsKey(tableTwoNode.getText()) && myDbms.dataBase.containsKey(tableOneNode.getText())) {
            myDbms.tempStack.push(myDbms.union((Table) myDbms.dataBase.get(tableOneNode.getText()), (Table) myDbms.dataBase.get(tableTwoNode.getText())));
        }
        else if (myDbms.dataBase.containsKey(tableOneNode.getText())) { // tableOneNode found in database
            myDbms.tempStack.push(myDbms.union((Table) myDbms.dataBase.get(tableOneNode.getText()), (Table) myDbms.tempStack.pop()));
        }
        else if(myDbms.dataBase.containsKey(tableTwoNode)) { //tableTwoNode found in database
            myDbms.tempStack.push(myDbms.union((Table) myDbms.tempStack.pop(), (Table) myDbms.dataBase.get(tableOneNode.getText())));
        }
        else { // none in database
            myDbms.tempStack.push(myDbms.union((Table) myDbms.tempStack.pop(), (Table) myDbms.tempStack.pop()));
        }

    }

    // select command called by the walker
    @Override public void exitSelection(RulesParser.SelectionContext ctx) {
        ParseTree conditionNode = ctx.getChild(2);
        ParseTree expresionNode = ctx.getChild(4);

        List<String> conditionLeaves = new ArrayList<>();
        getLeafNodes(conditionNode,conditionLeaves);
        Deque<String> conditionOpStack = new ArrayDeque<>();
        Queue<String> conditionQueue = new LinkedList<>();
        Queue<String> conditionClone = new LinkedList<>();

        for (String leaf : conditionLeaves) {
            if(isConditionOp(leaf))
            {
                String top = conditionOpStack.peek();
                if(leaf.equals("&&") || leaf.equals("||"))
                {
                    if(!top.equals("&&") && !top.equals("||") && !top.equals("(") && !top.equals(")"))
                    {
                        conditionQueue.add(conditionOpStack.getFirst().replace("\"",""));
                        conditionClone.add(conditionOpStack.pop());
                    }
                }
                conditionOpStack.push(leaf);
            }
            else
            {
                conditionQueue.add(leaf.replace("\"",""));
                conditionClone.add(leaf);
            }
        }

        while(!conditionOpStack.isEmpty())
        {
            String value = conditionOpStack.pop();
            if(!value.equals("(") && !value.equals(")"))
            {
                conditionQueue.add(value.replace("\"",""));
                conditionClone.add(value.replace("\"",""));
            }
        }

        List<String> expresionLeaves = new ArrayList<>();
        getLeafNodes(expresionNode, expresionLeaves);
        Deque<String> expressionOpStack = new ArrayDeque<>();
        Queue<String> expressionQueue = new LinkedList<>();
        Queue<String> cloneExpression = new LinkedList<>();

        for (String leaf: expresionLeaves) {
            if(isExpressionOp(leaf))
            {
                expressionOpStack.push(leaf);
            }
            else
            {
                expressionQueue.add(leaf);
                cloneExpression.add(leaf);
            }
        }

        while(!expressionOpStack.isEmpty())
        {
            String value = expressionOpStack.pop();
            if(!value.equals("(") && !value.equals(")"))
            {
                expressionQueue.add(value);
                cloneExpression.add(value);
            }
        }

        for (String item : cloneExpression) {
            if (isExpressionOp(item)) {
                String secItem = listenerStack.pop();
                String firstItem = listenerStack.pop();
                if (secItem.equals("temp")) {
                    myDbms.tempStack.push(computeExpression(item, myDbms.getTable(firstItem), (Table) myDbms.tempStack.pop()));
                }
                else {
                    myDbms.tempStack.push(computeExpression(item, myDbms.getTable(firstItem), myDbms.getTable(secItem)));
                }
                listenerStack.push("temp");
            }
            if (!isExpressionOp(item)) {
                listenerStack.push(expressionQueue.remove());
            }
        }

        Table pullFromTable;

        if(myDbms.tempStack.isEmpty())
        {
            pullFromTable = (Table)myDbms.dataBase.get(listenerStack.peek());
        }
        else{ pullFromTable = (Table) myDbms.tempStack.pop(); }

        listenerStack.clear();

        for (String item : conditionClone) {
            if (isConditionOp(item)) {
                if (item.equals("&&") || item.equals("||")) {
                    Table secItem = (Table) myDbms.tempStack.pop();
                    Table firstItem = (Table) myDbms.tempStack.pop();
                    myDbms.tempStack.push(computeAndOr(item, firstItem, secItem));
                }
                else {
                    String secItem = listenerStack.pop();
                    String firstItem = listenerStack.pop();
                    myDbms.tempStack.push(computeCondition(item, secItem, firstItem, pullFromTable));
                }
                conditionQueue.remove();
            }
            else {
                listenerStack.push(conditionQueue.remove());
            }
        }
    }

    // query command called by the walker
    @Override public void exitQuery(RulesParser.QueryContext ctx) {

        ParseTree itemNode = ctx.getChild(0);
        String tableName = itemNode.getText();
        ParseTree notFirstName = ctx.getChild(2);
        String notFirst = notFirstName.getText();

        if(myDbms.dataBase.containsKey(tableName)) { //if answer exists in database
            if (myDbms.dataBase.containsKey(notFirst)) {
                myDbms.tempStack.push(myDbms.dataBase.get(notFirst));
                Table fromTempStack = (Table) myDbms.tempStack.pop();
                fromTempStack.setName(tableName);
                myDbms.addTable(tableName, fromTempStack);
            }
            else {
                Table fromTempStack = (Table) myDbms.tempStack.pop();
                fromTempStack.setName(tableName);
                myDbms.addTable(tableName, fromTempStack);
            }
        }
        else { //doesnt
            if (myDbms.dataBase.containsKey(notFirst)) { //common names is in database
                myDbms.tempStack.push(myDbms.dataBase.get(notFirst));
                Table fromTempStack = (Table) myDbms.tempStack.pop();
                fromTempStack.setName(tableName);
                myDbms.addTable(tableName, fromTempStack);
            }
            else {
                Table fromTempStack = (Table) myDbms.tempStack.pop();
                fromTempStack.setName(tableName);
                myDbms.addTable(tableName, fromTempStack);
            }
        }
    }

    // insert command called by the walker
    @Override public void exitInsert_cmd(RulesParser.Insert_cmdContext ctx) {

        ParseTree entityInsertInto = ctx.getChild(1);   //table that you are inserting into
        ParseTree typeOfInsert = ctx.getChild(2);
        List<Object> valuesInserting = new ArrayList<Object>();


        // VALUES FROM RELATION
        if(typeOfInsert.getText().contains("RELATION")) {
            myDbms.getTable(entityInsertInto.getText()).copyTable((Table) myDbms.tempStack.pop());
        }
        else { // VALUES FROM

            for (int i = 4; i < ctx.getChildCount(); i++) {
                if (ctx.getChild(i).getText().equals(")")) {
                    break;
                } else {
                    if (!ctx.getChild(i).getText().contains(",")) {
                        valuesInserting.add(ctx.getChild(i).getText().replace("\"", ""));
                    }
                }
            }
            myDbms.getTable(entityInsertInto.getText()).insertRow((ArrayList) valuesInserting);
        }
    }

    // show command called by the walker
    @Override public void exitShow_cmd(RulesParser.Show_cmdContext ctx) {
        myDbms.printDataBaseTable(ctx.getChild(1).getText());
    }

    // rename command called by the walker
    @Override public void exitRenaming(RulesParser.RenamingContext ctx) {

        ParseTree renameNodes = ctx.getChild(2);
        ArrayList<String> renameNodesList = new ArrayList<>(Arrays.asList(renameNodes.getText().split(", ")));

        Table temp = (Table) myDbms.tempStack.pop();
        ArrayList<String> headersToReplace = new ArrayList<>();

        for (int i = 0; i < temp.table.size(); i++) {
            headersToReplace.add((String) temp.getRow(0).get(i));
        }

        temp.rename(temp, headersToReplace, renameNodesList);
        myDbms.tempStack.push(temp);

    }

    // project command called by the walker
    @Override public void exitProjection(RulesParser.ProjectionContext ctx) {

        ParseTree itemsNode = ctx.getChild(2);
        ParseTree fromNode = ctx.getChild(4);

        ArrayList<String> itemsNodeList = new ArrayList<>(Arrays.asList(itemsNode.getText().split(", ")));
        Table temp = new Table();

        if(myDbms.dataBase.containsKey(fromNode.getText())) { //in database
            for (String s : itemsNodeList) {

                temp.insertCol(myDbms.getTable(fromNode.getText()).project(s));
            }
        }
        else { //not in database so in tempstack
            for (String s : itemsNodeList) {
                //get table from tempstack
                Table fromTempStack = (Table) myDbms.tempStack.pop();
                temp.insertCol(fromTempStack.project(s));
            }
        }

        myDbms.tempStack.push(temp);
    }

    // product command called by the walker
    @Override public void exitProduct(RulesParser.ProductContext ctx) {

        ParseTree tableOneNode = ctx.getChild(0);
        ParseTree tableTwoNode = ctx.getChild(2);

        if (myDbms.dataBase.containsKey(tableTwoNode.getText()) && myDbms.dataBase.containsKey(tableOneNode.getText())) {
            myDbms.tempStack.push(myDbms.product((Table) myDbms.dataBase.get(tableOneNode.getText()), (Table) myDbms.dataBase.get(tableTwoNode.getText())));
        }
        else if (myDbms.dataBase.containsKey(tableOneNode.getText())) { // tableOneNode found in database
            myDbms.tempStack.push(myDbms.product((Table) myDbms.dataBase.get(tableOneNode.getText()), (Table) myDbms.tempStack.pop()));
        }
        else if(myDbms.dataBase.containsKey(tableTwoNode)) { //tableTwoNode found in database
            myDbms.tempStack.push(myDbms.product((Table) myDbms.tempStack.pop(), (Table) myDbms.dataBase.get(tableOneNode.getText())));
        }
        else { // none in database
            myDbms.tempStack.push(myDbms.product((Table) myDbms.tempStack.pop(), (Table) myDbms.tempStack.pop()));
        }

    }

    // write command called by the walker
    @Override public void exitWrite_cmd(RulesParser.Write_cmdContext ctx) {

        ParseTree itemToWrite = ctx.getChild(1);
        String tableName = itemToWrite.getText();

        Gson json = new Gson();
        String response = json.toJson(myDbms.dataBase.get(tableName));

        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter("/Users/Benjey/onedrive/csce-315/csce315-p1/src/project1/" + tableName + ".db"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            writer.write(response);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // close command called by the walker
    @Override public void exitClose_cmd(RulesParser.Close_cmdContext ctx) {

        ParseTree itemToWrite = ctx.getChild(1);
        String tableName = itemToWrite.getText();

        Gson json = new Gson();
        String response = json.toJson(myDbms.dataBase.get(tableName));

        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter("/Users/Benjey/onedrive/csce-315/csce315-p1/src/project1/" + tableName + ".db"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            writer.write(response);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        myDbms.dataBase.remove(tableName);
    }

    // exit command called by the walker
    @Override public void exitExit_cmd(RulesParser.Exit_cmdContext ctx) {
        System.out.println("Session Terminated");
        myDbms.dataBase.clear();
    }

    // update command called by the walker
    @Override public void exitUpdate_cmd(RulesParser.Update_cmdContext ctx) {

        //Get the headers that we need to change and put them and what you are changing them to in a hashmap
        listenerStack.clear();
        ParseTree conditionNode = null;
        HashMap<String, String> changingVariables = new HashMap<>();
        ArrayList<String> q = new ArrayList();
        Table pullFromTable = myDbms.getTable(ctx.getChild(1).getText());
        for(int i = 3; i < ctx.getChildCount(); i++)
        {
            if(ctx.getChild(i).getText().equals("WHERE"))
            {
                conditionNode = ctx.getChild(i+1);
                break;
            }
            if(!ctx.getChild(i).getText().replace(" ", "").equals(","))
            {
                q.add(ctx.getChild(i).getText().replace("\"",""));
            }
        }
        for(int i = 0; i < q.size(); i++)
        {
            if(q.get(i).replace(" ","").equals("="))
            {
                changingVariables.put(q.get(i-1), q.get(i+1));
            }
        }
        //System.out.println(changingVariables);
        //Parse the WHERE statement in the same way we did in the select command
        List<String> conditionLeaves = new ArrayList<>();
        getLeafNodes(conditionNode,conditionLeaves);
        Deque<String> conditionOpStack = new ArrayDeque<>();
        Queue<String> conditionQueue = new LinkedList<>();
        Queue<String> conditionClone = new LinkedList<>();
        for (String leaf : conditionLeaves) {
            if(isConditionOp(leaf))
            {
                String top = conditionOpStack.peek();
                if(leaf.equals("&&") || leaf.equals("||"))
                {
                    if(!top.equals("&&") && !top.equals("||") && !top.equals("(") && !top.equals(")"))
                    {
                        conditionQueue.add(conditionOpStack.getFirst().replace("\"",""));
                        conditionClone.add(conditionOpStack.pop());
                    }
                }
                conditionOpStack.push(leaf);
            }
            else
            {
                conditionQueue.add(leaf.replace("\"",""));
                conditionClone.add(leaf);
            }
        }
        while(!conditionOpStack.isEmpty())
        {
            String value = conditionOpStack.pop();
            if(!value.equals("(") && !value.equals(")"))
            {
                conditionQueue.add(value.replace("\"",""));
                conditionClone.add(value.replace("\"",""));
            }
        }
        //System.out.println(conditionQueue);
        for (String item : conditionClone) {
            if (isConditionOp(item)) {
                if (item.equals("&&") || item.equals("||")) {
                    Table secItem = (Table) myDbms.tempStack.pop();
                    Table firstItem = (Table) myDbms.tempStack.pop();
                    myDbms.tempStack.push(computeAndOr(item, firstItem, secItem));
                }
                else {
                    String secItem = listenerStack.pop();
                    String firstItem = listenerStack.pop();
                    myDbms.tempStack.push(computeCondition(item, secItem, firstItem, pullFromTable));
                }
                conditionQueue.remove();
            }
            else {
                listenerStack.push(conditionQueue.remove());
            }
        }
        Table temp = (Table)myDbms.tempStack.pop();
        ArrayList<Integer> indexToRemove = new ArrayList<>();
        indexToRemove = pullFromTable.getIndex(temp);
        //System.out.println(indexToRemove);
        temp = temp.setTable(changingVariables); //table with the updated values
        for(int row : indexToRemove)
        {
            temp.insertRow(pullFromTable.getRow(row));
        }
        myDbms.dataBase.remove(ctx.getChild(1).getText());
        myDbms.addTable(ctx.getChild(1).getText(), temp);
    }

    // difference command called by the walker
    @Override public void exitDifference(RulesParser.DifferenceContext ctx) {

        ParseTree tableOneNode = ctx.getChild(0);
        ParseTree tableTwoNode = ctx.getChild(2);

        if (myDbms.dataBase.containsKey(tableTwoNode.getText()) && myDbms.dataBase.containsKey(tableOneNode.getText())) {
            myDbms.tempStack.push(myDbms.difference((Table) myDbms.dataBase.get(tableOneNode.getText()), (Table) myDbms.dataBase.get(tableTwoNode.getText())));
        }
        else if (myDbms.dataBase.containsKey(tableOneNode.getText())) { // tableOneNode found in database
            myDbms.tempStack.push(myDbms.difference((Table) myDbms.dataBase.get(tableOneNode.getText()), (Table) myDbms.tempStack.pop()));
        }
        else if(myDbms.dataBase.containsKey(tableTwoNode)) { //tableTwoNode found in database
            myDbms.tempStack.push(myDbms.difference((Table) myDbms.tempStack.pop(), (Table) myDbms.dataBase.get(tableOneNode.getText())));
        }
        else { // none in database
            Table t2 = (Table) myDbms.tempStack.pop();
            Table t1 = (Table) myDbms.tempStack.pop();
            myDbms.tempStack.push(myDbms.difference(t1, t2));
        }
    }

    // function that gets the leaf nodes of a specified parsetree
    public void getLeafNodes(ParseTree node, List<String> leaves) {

        if(node.getChildCount() == 0)
        {
            leaves.add(node.getText());
        }
        else
        {
            for (int i = 0; i < node.getChildCount(); i++)
            {
                getLeafNodes(node.getChild(i),leaves);
            }
        }
    }

    // function that checks if the string is a condition
    public boolean isConditionOp(String value) {

        if(value == null)
        {
            return false;
        }
        else return value.equals("==") || value.equals("!=") || value.equals(">") || value.equals(">=") || value.equals("<") || value.equals("<=") || value.equals("&&") || value.equals("||") || value.equals("(") || value.equals(")");
    }

    // function that checks if a string is an expression
    public boolean isExpressionOp(String value) {

        if(value == null) {
            return false;
        }
        else return value.equals("+") || value.equals("-") || value.equals("*") || value.equals("(") || value.equals(")");
    }

    // function that calculates a specified expression of 2 tables
    public Table computeExpression(String op, Table first, Table second) {

        Table temp = new Table();
        temp.setName("temp");

        if (op.equals("+")) {
            temp = myDbms.union(first, second);
            temp.printTable();
        }
        else if (op.equals("-")) {
            temp = myDbms.difference(first, second);
        }
        else if (op.equals("*")) {
            System.out.println("Product Call from Select");
            temp = myDbms.product(first, second);
        }

        return temp;
    }

    // function that calculates a specified condition of 2 tables
    public Table computeCondition(String op, String first, String second, Table pullFrom) {

        Table temp = new Table();
        int n;

        if(op.equals("==")) {
            if(isInteger(second)) {
                n = Integer.parseInt(second);
                temp = pullFrom.select(first, op, n);
            }
            else if(isInteger(first))
            {
                n = Integer.parseInt(first);
                temp = pullFrom.select(second, op, n);
            }
            else {
                boolean firstFound = false;
                boolean secondFound = false;
                for (int i = 0; i < pullFrom.table.size(); i++) {
                    //System.out.println(pullFrom.getRow(0).get(i));
                    if (first.equals(pullFrom.getRow(0).get(i))) {

                        firstFound = true;
                    }
                    if (second.equals(pullFrom.getRow(0).get(i))) {
                        secondFound = true;
                    }
                }
                if (firstFound && secondFound){temp = pullFrom.selectHeader(second, op, first);}
                else {temp = pullFrom.select(second, op, first);}
            }
        }
        else if(op.equals("!=")) {
            if(isInteger(second)) {
                n = Integer.parseInt(second);
                temp = pullFrom.select(first, op, n);
            }
            else if(isInteger(first))
            {
                n = Integer.parseInt(first);
                temp = pullFrom.select(second, op, n);
            }
            else {
                boolean firstFound = false;
                boolean secondFound = false;
                for (int i = 0; i < pullFrom.table.size(); i++) {

                    if (first.equals(pullFrom.getRow(0).get(i))) {

                        firstFound = true;
                    }
                    if (second.equals(pullFrom.getRow(0).get(i))) {
                        secondFound = true;
                    }
                }
                if (firstFound && secondFound){temp = pullFrom.selectHeader(second, op, first);}
                else {temp = pullFrom.select(second, op, first);}
            }
        }
        else if(op.equals(">")) {
            if(isInteger(second)) {
                n = Integer.parseInt(second);
                temp = pullFrom.select(first, op, n);
            }
            else if(isInteger(first))
            {
                n = Integer.parseInt(first);
                temp = pullFrom.select(second, op, n);
            }
            else {
                boolean firstFound = false;
                boolean secondFound = false;
                for (int i = 0; i < pullFrom.table.size(); i++) {

                    if (first.equals(pullFrom.getRow(0).get(i))) {

                        firstFound = true;
                    }
                    if (second.equals(pullFrom.getRow(0).get(i))) {
                        secondFound = true;
                    }
                }
                if (firstFound && secondFound){temp = pullFrom.selectHeader(second, op, first);}
                else {temp = pullFrom.select(second, op, first);}
            }
        }
        else if(op.equals(">=")) {
            if(isInteger(second)) {
                n = Integer.parseInt(second);
                temp = pullFrom.select(first, op, n);
            }
            else if(isInteger(first))
            {
                n = Integer.parseInt(first);
                temp = pullFrom.select(second, op, n);
            }
            else {
                boolean firstFound = false;
                boolean secondFound = false;
                for (int i = 0; i < pullFrom.table.size(); i++) {

                    if (first.equals(pullFrom.getRow(0).get(i))) {

                        firstFound = true;
                    }
                    if (second.equals(pullFrom.getRow(0).get(i))) {
                        secondFound = true;
                    }
                }
                if (firstFound && secondFound){temp = pullFrom.selectHeader(second, op, first);}
                else {temp = pullFrom.select(second, op, first);}
            }
        }
        else if(op.equals("<")) {
            if(isInteger(second)) {
                n = Integer.parseInt(second);
                temp = pullFrom.select(first, op, n);
            }
            else if(isInteger(first))
            {
                n = Integer.parseInt(first);
                temp = pullFrom.select(second, op, n);
            }
            else {
                boolean firstFound = false;
                boolean secondFound = false;
                for (int i = 0; i < pullFrom.table.size(); i++) {

                    if (first.equals(pullFrom.getRow(0).get(i))) {

                        firstFound = true;
                    }
                    if (second.equals(pullFrom.getRow(0).get(i))) {
                        secondFound = true;
                    }
                }
                if (firstFound && secondFound){temp = pullFrom.selectHeader(second, op, first);}
                else {temp = pullFrom.select(second, op, first);}
            }
        }
        else if(op.equals("<=")) {
            if(isInteger(second)) {
                n = Integer.parseInt(second);
                temp = pullFrom.select(first, op, n);
            }
            else if(isInteger(first))
            {
                n = Integer.parseInt(first);
                temp = pullFrom.select(second, op, n);
            }
            else {
                boolean firstFound = false;
                boolean secondFound = false;
                for (int i = 0; i < pullFrom.table.size(); i++) {

                    if (first.equals(pullFrom.getRow(0).get(i))) {

                        firstFound = true;
                    }
                    if (second.equals(pullFrom.getRow(0).get(i))) {
                        secondFound = true;
                    }
                }
                if (firstFound && secondFound){temp = pullFrom.selectHeader(second, op, first);}
                else {temp = pullFrom.select(second, op, first);}
            }
        }

        return temp;
    }

    // function that calls union or intersect depending on the specified operation
    public Table computeAndOr(String op, Table first, Table second) {

        Table temp = new Table();

        if (op.equals("&&")) {
            temp = myDbms.intersect(first, second);
        }
        else if (op.equals("||")) {
            temp = myDbms.union(first, second);
        }

        return temp;
    }

    // function that determines if a string is an integer
    public static boolean isInteger(String s) {

        boolean isValidInteger = false;

        try {
            Integer.parseInt(s);
            // s is a valid integer
            isValidInteger = true;
        }
        catch (NumberFormatException ex) {/* s is not an integer*/}

        return isValidInteger;
    }
}
