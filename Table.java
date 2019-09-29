package project1;
import java.security.PublicKey;
import java.util.*;

public class Table<T> {
    private String name;
    private ArrayList<String> primaryKey;
    public  ArrayList<ArrayList<T>> table;

    public Table(int j)
    {
        this.table = new ArrayList<ArrayList<T>>();
        for (int i = 0; i < j; i++)
        {
            ArrayList<T> a = new ArrayList<T>();
            table.add(a);
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

    public void printTable()
    {
        for (int i = 0; i < table.size(); i++)
        {
            for (int j = 0; j < table.get(i).size(); j++)
            {
                System.out.print(table.get(i).get(j) + " ");
            }
            System.out.println();
        }
    }

    public Table where(String header, T key)
    {
        int index = -1;
        Table temp = new Table(this.table.size());
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

    public Table rename(String name, Table<T> t, String[] header, String[] newHeader) //  rename specified header(s)
    {
        for(int h = 0; h < header.length; h++)
        {
            for(int i = 0; i < t.table.size(); i++)
            {
                for(int j = 1; j < t.table.get(i).size(); j++)
                {
                    if((String) table.get(i).get(0) == header[h])
                    {
                        table.get(i).set(0, (T) (newHeader[h]));
                    }

                }
            }
        }
        return t;
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

    public void deleteRow(int index)
    {
        for(int i = 0; i < table.size(); i++)
        {
            table.get(i).remove(index);
        }
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
}