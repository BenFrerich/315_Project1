package project1;
import java.security.PublicKey;
import java.util.*;

public class Table<T> {
    public String name;
    public ArrayList<String> primaryKey;
    public  ArrayList<ArrayList<T>> table;

    public Table()
    {
        table = new ArrayList<ArrayList<T>>();
    }

    public Table(int j)
    {
        this.table = new ArrayList<ArrayList<T>>();
        for (int i = 0; i < j; i++)
        {
            ArrayList<T> a = new ArrayList<T>();
            table.add(a);
        }
    }

    public Table(Table<T> t)
    {
        this.table = new ArrayList<ArrayList<T>>();
        this.name = t.name;
        this.primaryKey = t.primaryKey;
        for(int i = 0; i < t.table.size(); i++)
        {
            table.add(new ArrayList<T>());
            for(int j = 0; j < t.table.get(0).size(); j++)
            {
                table.get(i).add(t.table.get(i).get(j));
            }
        }

    }

    public void insertRow(ArrayList<T> inputs)
    {
        int i = 0;
        for(T items : inputs)
        {
            table.get(i).add(items);
            i++;
        }
    }

    public void insertCol(ArrayList<T> inputs)
    {
        table.add(inputs);
    }

    public void deleteRow(int index)
    {
        for(int i = 0; i < table.size(); i++)
        {
            table.get(i).remove(index);
        }
    }

    public int getRowCount() {
        return this.table.get(1).size();
    }

    public ArrayList<T> getRow(int index)
    {
        ArrayList<T> row = new ArrayList<T>();
        for(ArrayList<T> col : this.table)
        {
            row.add(col.get(index));
        }
        return  row;
    }

    public void printTable()
    {
        for (ArrayList<T> ts : table) {
            for (T t : ts) {
                System.out.print(t + " ");
            }
            System.out.println();
        }
    }

    public Table<T> select(String header, T key)
    {
        int index = -1;
        Table<T> temp = new Table<T>(this.table.size());
        ArrayList<T> a = new ArrayList<T>();
        ArrayList<Integer> b = new ArrayList<Integer>();
        ArrayList<T> c = new ArrayList<T>();


        for (ArrayList<T> tArrayList : this.table) {
            a.add(tArrayList.get(0));
        }

        temp.insertRow(a);

        for (int i = 0 ; i < table.size(); i++)
        {
            if(table.get(i).get(0) == header)
            {
                index = i;
            }
        }

        if(index < 0)
        {
            System.out.println("Header Not Found!!");
            return null;
        }
        else
        {
            for (int i = 1; i < table.get(index).size(); i++)
            {
                if(key == table.get(index).get(i))
                {
                    b.add(i);
                }
            }

            for (int i = 0; i < b.size(); i++)
            {
                for (int j = 0; j < temp.table.size(); j++)
                {
                    c.add(this.table.get(j).get(b.get(i)));
                }
                temp.insertRow(c);
                c.clear();
            }

            return temp;
        }

    }

    public void SET(Integer index, T keys)
    {
        for(int i = 1; i < table.get(0).size(); i++)
        {
            table.get(index).set(i,keys);
        }
    }

    public ArrayList<T> project(String header) {
        ArrayList<T> colWanted = new ArrayList<T>();
        for (ArrayList<T> cols : table) {
            if (cols.get(0) == header) {
                colWanted = (ArrayList<T>) cols.clone();
            }
        }
        return colWanted;
    }

    public Table rename(String name, Table<T> t, ArrayList<String> header, ArrayList<String> newHeader) //  rename specified header(s)
    {
        for(int h = 0; h < header.size(); h++)
        {
            for(int i = 0; i < t.table.size(); i++)
            {
                for(int j = 1; j < t.table.get(i).size(); j++)
                {
                    if((String) table.get(i).get(0) == header.get(h))
                    {
                        table.get(i).set(0, (T) (newHeader.get(h)));
                    }

                }
            }
        }
        return t;
    }

    public Table delete(String name, Table<T> t, String[] relationName)
    {
        for(int h = 0; h < relationName.length; h++) // iterate through relationName list
        {
            for(int i = 0; i < t.table.size(); i++) //
            {
                for(int j = 1; j < t.table.get(i).size(); j++)
                {
                    if((String) table.get(i).get(j) == relationName[h])
                    {
                        for(int k = 0; k < table.size(); k++)
                        {
                            table.get(k).remove(j);
                        }
                    }

                }
            }
        }
        return t;
    }

    boolean condition(int f, String cond, int s) {
        if(cond == ">") {
            return (f > s);
        }
        else if (cond == "<") {
            return (f < s);
        }
        else if (cond == "!=") {
            return (f != s);
        }
        else if(cond == ">=") {
            return (f >= s);
        }
        else if(cond == "<=") {
            return (f <= s);
        }
        else {
            return false;
        }
    }

    public Table<T> select(String header, String cond, String headerComp)
    {
        ArrayList<String> headerCol = new ArrayList<>();
        ArrayList<String> headerColComp = new ArrayList<>();
        Table<T> newTable = new Table<>(this.table.size());
        newTable.insertRow(this.getRow(0));

        for(ArrayList<T> col : this.table)
        {
            if(col.get(0) == header)
            {
                headerCol = (ArrayList<String>) col.clone();
            }
            else if(col.get(0) == headerComp)
            {
                headerColComp = (ArrayList<String>) col.clone();
            }
        }
        for(int i = 1; i < headerCol.size(); i++)
        {
            if(condition(headerCol.get(i), cond, headerColComp.get(i)))
            {
                newTable.insertRow(this.getRow(i));
            }
        }
        return newTable;
    }

    boolean condition(String f, String cond, String s)
    {
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

    public Table<T> select(String header, String cond, int number) {
        int numHeaders = this.table.size();
        Table<T> tableWithCond = new Table<T>(numHeaders);
        for (int i = 0; i < table.size(); i++) {

            for (int j = 0; j < table.get(i).size(); j++) {

                if (table.get(i).get(j) == header) {
                    tableWithCond.insertRow(getRow(0));
                    for (int x = j+1; x < table.get(i).size(); x++) {
                        boolean b = condition((Integer) table.get(i).get(x), cond, number);
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

}
