package project1;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.DelayQueue;
public class Dbms<T> {

    public Deque<Table> tempStack = new ArrayDeque(); // create a temporary stack
    public HashMap<String, Table<T>> dataBase;

    // create a local database
    public Dbms() {
        this.dataBase = new HashMap<String, Table<T>>();
    }

    // function to add a table into the database
    public void addTable(String name, Table T) {
        this.dataBase.put(name, T);
    }

    // function that returns a table given the table name
    public Table getTable(String name)
    {
        return this.dataBase.get(name);
    }

    // print the full contents of the database
    public void printDataBaseAll() {
        for (Map.Entry<String, Table<T>> j : dataBase.entrySet()) {
            System.out.println(j.getKey());
            j.getValue().printTable();
        }
        System.out.println();
    }

    // pint a single table given the table name
    public void printDataBaseTable(String name) {
        System.out.println(name);
        this.dataBase.get(name).printTable();
        System.out.println();
    }

    // delete a table from the database given the table name
    public void deleteTable(String name) {
        this.dataBase.remove(name);
    }

    // return a temporary table that is the intersection between 2 tables
    public Table intersect(Table<T> t1, Table<T> t2) {
        Table<T> t3 = new Table<T>();
        if(t1.table.size() != t2.table.size())
        {
            System.out.println("Cannot intersect these tables, they contain a different number of columns");
        }
        else
        {
            //Headers
            for(int i = 0; i < t1.table.size(); i++)
            {
                t3.table.add(new ArrayList<T>());
                t3.table.get(i).add(t1.table.get(i).get(0));
            }
            for(int i = 1; i < t1.table.size(); i++)
            {
                for(int j = 1; j < t2.table.size(); j++)
                {
                    if(compareRows(t1.getRow(i), t2.getRow(j)))
                    {
                        //t3.table.add(new ArrayList<T>());
                        t3.insertRow(t1.getRow(i));
                    }
                }
            }
        }
        return t3;
    }

    // return a temporary table that is the union between 2 tables
    public Table union(Table<T> t1, Table<T> t2) {
        Table<T> t3 = new Table<T>(t1.table.size());
        if(t1.table.size() != t2.table.size())
        {
            System.out.println("Cannot union these tables, they contain a different number of columns");
        }
        else
        {
            //Headers
            for(int i = 0; i < t1.table.size(); i++)
            {
                t3.table.get(i).add(t1.table.get(i).get(0));
            }
            //Add first table
            for(int i = 0; i < t1.table.size(); i++)
            {
                for(int j = 1; j < t1.table.get(i).size(); j++)
                {
                    t3.table.get(i).add(t1.table.get(i).get(j));
                }
            }
            //Add second table
            for(int i = 0; i < t2.table.size(); i++)
            {
                for(int j = 1; j < t2.table.get(i).size(); j++)
                {
                    t3.table.get(i).add(t2.table.get(i).get(j));
                }
            }
        }
        return t3;
    }

    // compare specified rows
    public boolean compareRows(ArrayList<T> a1, ArrayList<T> a2) {
        for(int i = 0; i < a1.size(); i++)
        {
            if(a1.get(i) != a2.get(i))
            {
                return false;
            }
        }
        return true;
    }

    // return a temporary table that is the difference between 2 tables
    public Table difference(Table<T> t1, Table<T> t2) {
        Table<T> t3 = new Table<T>(t1);
        boolean delete = false;
        for(int i = t1.table.get(0).size()-1; i > 1; i--)
        {
            ArrayList<T> row = t1.getRow(i);
            for(int j = t2.table.get(0).size()-1; j > 1; j--)
            {
                ArrayList<T> cRow = t2.getRow(j);
                if(compareRows(row, cRow))
                {
                    delete = true;
                }
            }
            if(delete)
            {
                t3.deleteRow(i);
            }
        }
        return t3;
    }

    // insert into a specified table from another table
    public void insertCommand(String toTable, String fromTable, ArrayList<String> headers) {
        //need to call project (maybe a few times for each string in headers
        for (int i = 0; i < headers.size(); i++) {
            ArrayList<T> itemsToInsert = new ArrayList<T>();
            for (int j = 0; j < this.dataBase.get(fromTable).table.size(); j++) {
                if (headers.get(i) == this.dataBase.get(fromTable).table.get(j).get(0)) {
                    // get items that we need to insert items into col
                    itemsToInsert = this.dataBase.get(fromTable).project(headers.get(i));
                    this.dataBase.get(toTable).table.set(j, itemsToInsert);
                    break;
                }
            }
        }
    }

    // insert a given row into a specified table
    public void insertCommand(String toTable, ArrayList<T> row) {
        this.dataBase.get(toTable).insertRow(row);
    }

    // return a temporary table that is the product of 2 tables
    public Table product(Table<T> t1, Table<T> t2) {
        int numOfLabels;
        numOfLabels = t1.table.size() + t2.table.size();
        // make new table of size needed
        Table<T> temp = new Table<>(numOfLabels);
        ArrayList<T> labels = new ArrayList<>();
        //System.out.println(t2.getRow(0));
        //temp.insertRow(t1.getRow(0));
        labels = t1.getRow(0);
        labels.addAll(t2.getRow(0));
        temp.insertRow(labels);
        //System.out.println(t1.getRowCount());
        ArrayList<T> tempForRows = new ArrayList<>();
        int row = 0;
        for (int i = 1; i < t1.getRowCount(); i++) {
            for (int j = 1; j < t2.getRowCount(); j++) {
                tempForRows = t1.getRow(i);
                tempForRows.addAll(t2.getRow(j));
                row++;
                temp.insertRow(tempForRows);
//                System.out.println(tempForRows);
//                System.out.println(row);
            }
        }
        return temp;
    }

}
