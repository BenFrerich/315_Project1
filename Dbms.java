package project1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Dbms {
    public HashMap<String, Table> dataBase;

    public Dbms() {
        this.dataBase = new HashMap<String, Table>();
    }

    public void addTable(String name, Table T) {
        this.dataBase.put(name, T);
    }

    public void printDataBaseTest() {
        for (Map.Entry<String, Table> j : dataBase.entrySet()) {
            System.out.println(j.getKey());
            j.getValue().printTable();
        }
    }

    public void printDataBase(String name) {
        System.out.println(name);
        this.dataBase.get(name).printTable();
    }

    public void rename(String name, Table T, String[] header, String[] newHeader)
    {this.dataBase.get(T).rename(name, T, header, newHeader); }

    public void deleteTable(String name) {
        this.dataBase.remove(name);
    }
}