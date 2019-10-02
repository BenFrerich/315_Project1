package project1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Dbms<T> {
    public HashMap<String, Table<T> > dataBase;

    public Dbms()
    {
        this.dataBase = new HashMap<String, Table<T>>();
    }

    public void addTable(String name, Table<T> T)
    {
        this.dataBase.put(name, T);
    }

    public void printDataBaseTest()
    {
        for(Map.Entry<String, Table<T>> j : dataBase.entrySet())
        {
            System.out.println(j.getKey());
            j.getValue().printTable();
        }
    }

    public ArrayList<T> removeDuplicates(ArrayList<T> list) {
        ArrayList<T> newList = new ArrayList<T>();

        for (T item : list) {
            if (!newList.contains(item)) { newList.add(item); }
        }
        return newList;
    }

    public void printDataBase(String name)
    {
        System.out.println(name);
        this.dataBase.get(name).printTable();
    }

    public void deleteTable(String name)
    {
        this.dataBase.remove(name);
    }

    public void update(String name, String header, T keys,T newKey)
    {
        int index = -1;
        ArrayList<T> inputs = new ArrayList<T>();

        Table<T> temp = dataBase.get(name).select(header,keys);


        for(int i = 0; i < temp.table.size(); i++)
        {
            if(header == temp.table.get(i).get(0))
            {
                index = i;
            }
        }


        temp.SET(index, newKey);

        for(int i = 1; i < dataBase.get(name).table.get(0).size(); i++)
        {
            for(int j = 1; j < temp.table.get(0).size(); j++)
            {
                int m = 1;
                for(int z = 0; z < temp.table.size(); z++)
                {
                    if(dataBase.get(name).table.get(z).get(i) == temp.table.get(z).get(j))
                    {
                        m++;
                    }
                    if (m == temp.table.size())
                    {
                        dataBase.get(name).deleteRow(i);
                    }
                }
            }
        }

        for (int i = 1; i < temp.table.get(0).size(); i++)
        {
            for (int j = 0; j < temp.table.size(); j++)
            {
                inputs.add(temp.table.get(j).get(i));
            }
            dataBase.get(name).insertRow(inputs);
            inputs.clear();
        }
    }

    public void insertCommand(String toTable, ArrayList<T> row) {
        this.dataBase.get(toTable).insertRow(row);
    }

    public void insertCommand(String toTable, String fromTable, ArrayList<String> headers) {
        //need to call project (maybe a few times for each string in headers
        for (int toTableIndex = 0; toTableIndex < this.dataBase.get(toTable).table.size(); toTableIndex++) {
            for (int i = 0; i < headers.size(); i++) {
                ArrayList<T> itemsToInsert = new ArrayList<T>();
                for (int j = 0; j < this.dataBase.get(fromTable).table.size(); j++) {

                    if (headers.get(i) == this.dataBase.get(fromTable).table.get(j).get(0)) {
                        // get items that we need to insert items into col
                        itemsToInsert = this.dataBase.get(fromTable).project(headers.get(i));
                        ArrayList<T> noDups = removeDuplicates(itemsToInsert);
                        this.dataBase.get(toTable).table.set(toTableIndex, noDups);
                        break;
                    }
                }

            }
        }

    }

    public Table union(Table<T> t1, Table<T> t2)
    {
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
                System.out.println(tempForRows);
                System.out.println(row);
            }
        }

        return temp;
    }

}
