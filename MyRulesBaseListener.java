package project1.antlr4;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.antlr.v4.runtime.tree.ParseTree;
import project1.Dbms;

import java.util.*;

public class MyRulesBaseListener extends RulesBaseListener{

    public MyRulesBaseListener() {
        Dbms myDbms = new Dbms();
    }

    @Override public void exitSelection(RulesParser.SelectionContext ctx) {

        ParseTree conditionNode = ctx.getChild(2);
        ParseTree expresionNode = ctx.getChild(4);
        List<String> conditionLeaves = new ArrayList<>();
        getLeafNodes(conditionNode,conditionLeaves);
        Deque<String> conditionOpStack = new ArrayDeque<>();
        Queue<String> condtionQueue = new LinkedList<>();
        for (String leaf : conditionLeaves) {
            if(isConditionOp(leaf))
            {
                String top = conditionOpStack.peek();
                if(leaf.equals("&&") || leaf.equals("||"))
                {
                    if(!top.equals("&&") && !top.equals("||") && !top.equals("(") && !top.equals(")"))
                    {
                        condtionQueue.add(conditionOpStack.pop());
                    }
                }
                conditionOpStack.push(leaf);
            }
            else
            {
                condtionQueue.add(leaf);
            }
        }

        while(!conditionOpStack.isEmpty())
        {
            String value = conditionOpStack.pop();
            if(!value.equals("(") && !value.equals(")"))
            {
                condtionQueue.add(value);
            }
        }

        //System.out.println(conditionLeaves);
        //System.out.println(conditionOpStack);
        //System.out.println(condtionQueue);

        List<String> expresionLeaves = new ArrayList<>();
        getLeafNodes(expresionNode, expresionLeaves);
        Deque<String> expressionOpStack = new ArrayDeque<>();
        Queue<String> expressionQueue = new LinkedList<>();

        for (String leaf: expresionLeaves) {
            if(isExpressionOp(leaf))
            {
                expressionOpStack.push(leaf);
            }
            else
            {
                expressionQueue.add(leaf);
            }
        }
        while(!expressionOpStack.isEmpty())
        {
            String value = expressionOpStack.pop();
            if(!value.equals("(") && !value.equals(")"))
            {
                expressionQueue.add(value);
            }
        }

        //System.out.println(expresionLeaves);
        //System.out.println(expressionOpStack);
        //System.out.println(expressionQueue);
    }

    @Override public void exitShow_cmd(RulesParser.Show_cmdContext ctx) {
        System.out.println("SHOW");
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


}
