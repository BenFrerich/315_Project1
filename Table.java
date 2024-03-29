package project1;
import java.lang.reflect.Array;
import java.security.PublicKey;
import java.util.*;
public class Table<T> {

    private String name;
    public ArrayList<String> headerType;
    public ArrayList<String> primaryKey;
    public  ArrayList<ArrayList<T>> table;

    // table constructor
    public Table() {
        this.table = new ArrayList<ArrayList<T>>();
        this.headerType = new ArrayList<>();
        this.primaryKey = new ArrayList<>();
    }

    // table constructor
    public Table(int j) {
        this.table = new ArrayList<ArrayList<T>>();
        this.headerType = new ArrayList<>();
        this.primaryKey = new ArrayList<>();
        for (int i = 0; i < j; i++)
        {
            ArrayList<T> a = new ArrayList<T>();
            table.add(a);
        }
    }

    // table constructor
    public Table(Table<T> t) {
        this.table = new ArrayList<ArrayList<T>>();
        this.headerType = new ArrayList<>();
        this.primaryKey = new ArrayList<>();
        this.name = t.getName();
        this.primaryKey = t.getPrimaryKeys();
        for(int i = 0; i < t.table.size(); i++)
        {
            table.add(new ArrayList<T>());
            for(int j = 0; j < t.table.get(0).size(); j++)
            {
                table.get(i).add(t.table.get(i).get(j));
            }
        }
    }

    //copy a specified table
    public void copyTable(Table t1) {
        if (this.table.size() == t1.table.size()) {
            for (Object col : t1.table) {
                this.table.clear();
                this.insertCol((ArrayList<T>) col);
            }
        }
    }

    // set the primary key
    public void setPrimaryKey(ArrayList<String> list)
    {
        this.primaryKey.addAll(list);
    }

    // add header type
    public void addHeaderType(String type)
    {
        this.headerType.add(type);
    }

    // set the name of a table
    public void setName(String name)
    {
        this.name = name;
    }

    // get the name of a table
    public String getName()
    {
        return name;
    }

    // get the primary key(s)
    public ArrayList<String> getPrimaryKeys()
    {
        return primaryKey;
    }

    // insert row(s)
    public void insertRow(ArrayList<T> inputs) {
        int i = 0;
        for(T items : inputs)
        {
            table.get(i).add(items);
            i++;
        }
    }

    // get a row at an index
    public ArrayList<T> getRow(int index) {
        ArrayList<T> row = new ArrayList<T>();
        for(ArrayList<T> col : this.table)
        {
            row.add(col.get(index));
        }
        return  row;
    }

    // delete the row at an index
    public void deleteRow(int index) {
        for(int i = 0; i < table.size(); i++)
        {
            table.get(i).remove(index);
        }
    }

    // insert a column
    public void insertCol(ArrayList<T> inputs)
    {
        table.add(inputs);
    }

    // project function
    public ArrayList<T> project(String header) {
        ArrayList<T> colWanted = new ArrayList<T>();
        for (ArrayList<T> cols : table) {
            if (cols.get(0).equals(header)) {
                colWanted = (ArrayList<T>) cols.clone();
            }
        }
        return colWanted;
    }

    // print table function
    public void printTable() {
        for (ArrayList<T> ts : table) {
            for (T t : ts) {
                System.out.print(t + " ");
            }
            System.out.println();
        }
    }

    // select table function
    public Table<T> select(String header, String cond, int number) {
        int numHeaders = this.table.size();
        Table<T> tableWithCond = new Table<T>(numHeaders);
        for (int i = 0; i < table.size(); i++) {
            for (int j = 0; j < table.get(i).size(); j++) {
                if (table.get(i).get(j).toString().replace(" ","").equals(header.toString().replace(" ",""))) {
                    tableWithCond.insertRow(getRow(0));
                    for (int x = j+1; x < table.get(i).size(); x++) {
                        boolean b = condition(Integer.parseInt(table.get(i).get(x).toString()), cond, number);
                        if(b)
                        {
                            tableWithCond.insertRow(getRow(x));
                        }
                    }
                }
            }
        }
        return tableWithCond;
    }

    // select table function
    public Table<T> select(String header, String cond, String headerComp) {
        ArrayList<String> headerCol = new ArrayList<>();
        //ArrayList<String> headerColComp = new ArrayList<>();
        Table<T> newTable = new Table<>(this.table.size());
        newTable.insertRow(this.getRow(0));
        for(ArrayList<T> col : this.table)
        {
            if(col.get(0).toString().equals(header))
            {
                headerCol = (ArrayList<String>) col.clone();
            }/*
            else if(col.get(0) == headerComp)
            {
                headerColComp = (ArrayList<String>) col.clone();
            }*/
        }
        for(int i = 1; i < headerCol.size(); i++)
        {
            //System.out.println(headerCol.get(i).toString());
            //System.out.println(headerCol.get(i).toString().replace("\"", ""));
            if(condition(headerCol.get(i).toString().replace("\"",""), cond, headerComp) )
            {
                newTable.insertRow(this.getRow(i));
            }
        }
        return newTable;
    }

    // select header function
    public Table<T> selectHeader(String header, String cond, String headerComp) {
        ArrayList<String> headerCol = new ArrayList<>();
        ArrayList<String> headerColComp = new ArrayList<>();
        Table<T> newTable = new Table<>(this.table.size());
        newTable.insertRow(this.getRow(0));
        for(ArrayList<T> col : this.table)
        {
            if(col.get(0).toString().equals(header))
            {
                headerCol = (ArrayList<String>) col.clone();
            }
            else if(col.get(0).toString().equals(headerComp))
            {
                headerColComp = (ArrayList<String>) col.clone();
            }
        }
        for(int i = 1; i < headerCol.size(); i++)
        {
            if(condition(headerCol.get(i), cond, headerColComp.get(i)) )
            {
                newTable.insertRow(this.getRow(i));
            }
        }
        return newTable;
    }

    // condition function
    boolean condition(String f, String cond, String s) {
        //Compare two strings
        if(cond.equals("=="))
        {
            return f.equals(s);
        }
        else if(cond.equals("!="))
        {
            return !f.equals(s);
        }
        else
        {
            return false;
        }
    }


    boolean condition(int f, String cond, int s) {
        //compare two ints
        if(cond.equals(">")) {
            return (f > s);
        }
        else if (cond.equals("<")) {
            return (f < s);
        }
        else if (cond.equals("!=")) {
            return (f != s);
        }
        else if(cond.equals(">=")) {
            return (f >= s);
        }
        else if(cond.equals("<=")) {
            return (f <= s);
        }
        else if(cond.equals("==")) {
            return (f == s);
        }
        else {
            return false;
        }
    }

    //  rename specified header(s)
    public Table rename(Table<T> t, ArrayList<String> header, ArrayList<String> newHeader) {
        for(int h = 0; h < header.size(); h++)
        {
            for(int i = 0; i < t.table.size(); i++)
            {
                for(int j = 1; j < t.table.get(i).size(); j++)
                {
                    if(table.get(i).get(0).equals(header.get(h)))
                    {
                        table.get(i).set(0, (T) (newHeader.get(h)));
                    }
                }
            }
        }
        return t;
    }


    public int getRowCount() {
        return this.table.get(1).size();
    }

    public Table setTable(HashMap<String, String> changingVariables) {
        for(Map.Entry i : changingVariables.entrySet())
        {
            for(int j = 0; j < this.table.size(); j++)
            {
                //System.out.println(j);
                if(i.getKey().equals(this.table.get(j).get(0)))
                {
                    for(int k = 1; k < this.table.get(j).size(); k++)
                    {
                        this.table.get(j).set(k, (T) i.getValue());
                    }
                }

            }
        }
        return this;
    }

    public ArrayList<Integer> getIndex(Table temp) {
        ArrayList<Integer> index = new ArrayList<>();
        for(int i = 1; i < this.table.get(0).size(); i++)
        {
            for(int j = 1; j < temp.getRowCount(); j++)
            {
                for(int k = 0; k < this.table.size(); k++)
                {
                    if(!this.table.get(k).get(i).equals(temp.getRow(j).get(k)))
                    {
                        if(!index.contains(i))
                        {
                            index.add(i);
                        }

                    }
                }
            }
        }
        return index;
    }

}
