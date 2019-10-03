package project1.antlr4;
import com.sun.scenario.effect.impl.state.LinearConvolveKernel;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.antlr.v4.runtime.tree.ParseTree;
import project1.Dbms;
import project1.Table;
import java.util.*;
public class MyRulesBaseListener extends RulesBaseListener{
    public static Dbms myDbms = new Dbms();
    public static Deque<String> listenerStack = new ArrayDeque<>();
    public MyRulesBaseListener() {
        Dbms myDbms = new Dbms();
    }
    @Override public void exitCreate_cmd(RulesParser.Create_cmdContext ctx) {
        ParseTree relationNameNode = ctx.getChild(1);
        ParseTree attributeListNode = ctx.getChild(3);
        //ParseTree primaryKeysNode = ctx.getChild(7);
        ArrayList<String> attributeListLeaves = new ArrayList<>(Arrays.asList(attributeListNode.getText().split(", ")));
        Table temp = new Table(attributeListLeaves.size());
        temp.insertRow(attributeListLeaves);
        myDbms.addTable(relationNameNode.getText(), temp);
        //myDbms.printDataBase(relationNameNode.getText());
    }
    @Override public void exitUnion(RulesParser.UnionContext ctx) {
        //ParseTree relationNameNode = ctx.getChild(1);
        for (int i = 0; i < ctx.getChildCount(); i++) {
            System.out.println(ctx.getChild(i).getText());
        }
    }
    @Override public void exitSelection(RulesParser.SelectionContext ctx) {
        ParseTree conditionNode = ctx.getChild(2);
        ParseTree expresionNode = ctx.getChild(4);
        List<String> conditionLeaves = new ArrayList<>();
        getLeafNodes(conditionNode,conditionLeaves);
        Deque<String> conditionOpStack = new ArrayDeque<>();
        Queue<String> condtionQueue = new LinkedList<>();
        Queue<String> conditionClone = new LinkedList<>();
        for (String leaf : conditionLeaves) {
            if(isConditionOp(leaf))
            {
                String top = conditionOpStack.peek();
                if(leaf.equals("&&") || leaf.equals("||"))
                {
                    if(!top.equals("&&") && !top.equals("||") && !top.equals("(") && !top.equals(")"))
                    {
                        condtionQueue.add(conditionOpStack.getFirst());
                        conditionClone.add(conditionOpStack.pop());
                    }
                }
                conditionOpStack.push(leaf);
            }
            else
            {
                condtionQueue.add(leaf);
                conditionClone.add(leaf);
            }
        }
        while(!conditionOpStack.isEmpty())
        {
            String value = conditionOpStack.pop();
            if(!value.equals("(") && !value.equals(")"))
            {
                condtionQueue.add(value);
                conditionClone.add(value);
            }
        }
        //System.out.println(conditionLeaves);
        //System.out.println(conditionOpStack);
        System.out.println("Condition Queue");
        System.out.println(condtionQueue);
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
        //System.out.println(expresionLeaves);
        //System.out.println(expressionOpStack);
        System.out.println("Expression Queue");
        System.out.println(expressionQueue);
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
        listenerStack.clear();
        Table pullFromTable = (Table) myDbms.tempStack.pop();
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
            }
            else {
                listenerStack.push(condtionQueue.remove());
            }
        }
        System.out.println(myDbms.tempStack.peek());
    }
    @Override public void exitQuery(RulesParser.QueryContext ctx) {
//        for (int i = 0; i < ctx.getChildCount(); i++) {
//            System.out.println(ctx.getChild(i).getText());
//        }
        ParseTree itemNode = ctx.getChild(0);
        String tableName = itemNode.getText();
        Table fromTempStack = (Table) myDbms.tempStack.pop();
        fromTempStack.setName(tableName);
        myDbms.addTable(tableName, fromTempStack);
    }
    @Override public void exitInsert_cmd(RulesParser.Insert_cmdContext ctx) {
        ParseTree entityInsertInto = ctx.getChild(1);   //table that you are inserting into
        List<Object> valuesInserting = new ArrayList<Object>();
        for(int i = 4; i < ctx.getChildCount(); i++)
        {
            if(ctx.getChild(i).getText().equals(")"))
            {
                break;
            }
            else
            {
                if(!ctx.getChild(i).getText().contains(",")) {
                    valuesInserting.add(ctx.getChild(i).getText());
                }
            }
        }
        myDbms.getTable(entityInsertInto.getText()).insertRow((ArrayList) valuesInserting);
        //myDbms.printDataBaseTest();
    }
    @Override public void exitShow_cmd(RulesParser.Show_cmdContext ctx) {
        myDbms.printDataBase(ctx.getChild(1).getText());
    }
    public void getLeafNodes(ParseTree node, List<String> leaves)
    {
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
    public boolean isConditionOp(String value)
    {
        if(value == null)
        {
            return false;
        }
        else return value.equals("==") || value.equals("!=") || value.equals(">") || value.equals(">=") || value.equals("<") || value.equals("<=") || value.equals("&&") || value.equals("||") || value.equals("(") || value.equals(")");
    }
    public boolean isExpressionOp(String value)
    {
        if(value == null)
        {
            return false;
        }
        else return value.equals("+") || value.equals("-") || value.equals("*") || value.equals("(") || value.equals(")");
    }
    public Table computeExpression(String op, Table first, Table second) {
        Table temp = new Table();
        temp.name = "temp";
        if (op.equals("+")) {
            temp = myDbms.union(first, second);
            temp.printTable();
        }
        else if (op.equals("-")) {
            temp = myDbms.difference(first, second);
        }
        else if (op.equals("*")) {
            temp = myDbms.product(first, second);
        }
        return temp;
    }
    public Table computeCondition(String op, String first, String second, Table pullFrom) {
        Table temp = new Table();
        int n;
        if(isInteger(second)) {
            n = Integer.parseInt(second);
        }
        if(op.equals("==")) {
            if(isInteger(second)) {
                n = Integer.parseInt(second);
                temp = pullFrom.select(first, op, n);
            }
            else {
                temp = pullFrom.select(second, op, first);
            }
        }
        else if(op.equals("!=")) {
            if(isInteger(second)) {
                n = Integer.parseInt(second);
                temp = pullFrom.select(first, op, n);
            }
            else {
                temp = pullFrom.select(second, op, first);
            }
        }
        else if(op.equals(">")) {
            if(isInteger(second)) {
                n = Integer.parseInt(second);
                temp = pullFrom.select(first, op, n);
            }
            else {
                temp = pullFrom.select(second, op, first);
            }
        }
        else if(op.equals(">=")) {
            if(isInteger(second)) {
                n = Integer.parseInt(second);
                temp = pullFrom.select(first, op, n);
            }
            else {
                temp = pullFrom.select(second, op, first);
            }
        }
        else if(op.equals("<")) {
            if(isInteger(second)) {
                n = Integer.parseInt(second);
                temp = pullFrom.select(first, op, n);
            }
            else {
                temp = pullFrom.select(second, op, first);
            }
        }
        else if(op.equals("<=")) {
            if(isInteger(second)) {
                n = Integer.parseInt(second);
                temp = pullFrom.select(first, op, n);
            }
            else {
                temp = pullFrom.select(second, op, first);
            }
        }
        return temp;
    }
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
    public static boolean isInteger(String s) {
        boolean isValidInteger = false;
        try
        {
            Integer.parseInt(s);
            // s is a valid integer
            isValidInteger = true;
        }
        catch (NumberFormatException ex)
        {
            // s is not an integer
        }
        return isValidInteger;
    }
}
