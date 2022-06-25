package org.lab3.task;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

class PostgreConnection{
	final String DB_URL = "jdbc:postgresql://127.0.0.1:5432/";
	final String USER = "postgres";
	final String PASS = "qwerty";
	private Connection connection;
	
	public boolean checkConnection() throws ClassNotFoundException, SQLException {
		Class.forName("org.postgresql.Driver");
		connection = null;
		connection = DriverManager.getConnection(DB_URL, USER, PASS);
		if (connection != null) {
    		return true;
    	} else {
    		return false;
    	}
	}
	
	public void createTable() throws SQLException{
		Statement statement = connection.createStatement();
		statement.executeUpdate("CREATE TABLE IF NOT EXISTS public.series\n"
				+ "(\n"
				+ "    id serial NOT NULL,\n"
				+ "    a0 double precision NOT NULL,\n"
				+ "    d double precision NOT NULL,\n"
				+ "    name character varying(255) NOT NULL,\n"
				+ "    type integer NOT NULL,\n"
				+ "    PRIMARY KEY (id)\n"
				+ ");");
		statement.close();
	}
	
	public int insertData(ArrayList<Series> series) throws SQLException {
		int count = 0;
		String SQL = "INSERT INTO series(a0,d,name,type) VALUES(?,?,?,?)";
		for(Series serie: series)	{
			 PreparedStatement pstmt = connection.prepareStatement(SQL, Statement.NO_GENERATED_KEYS);
			 pstmt.setInt(1, serie.a0);
			 pstmt.setInt(2, serie.d);
	         pstmt.setString(3, serie.getName());
	         if(serie instanceof Linear)
	        	 pstmt.setInt(4, 0);
	         if(serie instanceof Exponential)
	        	 pstmt.setInt(4, 1);
	         count += pstmt.executeUpdate();
		}
		return count;
	}
	
	public ArrayList<Series> getAll() throws SQLException
	{
		ArrayList<Series> series = new ArrayList<>();
		
		Statement stmt = connection.createStatement();

	    ResultSet rs = stmt.executeQuery( "select * from public.series" );

	    while ( rs.next() ) {

	         int a0 = rs.getInt("a0");
	         
	         int d  = rs.getInt("d");

	         String  name = rs.getString("name");

	         int t  = rs.getInt("type");

	         if(t == 0) {
	        	 Linear obj = new Linear(a0, d, name);
	        	 series.add(obj);
	         }
	         if(t == 1) {
	        	 Exponential obj = new Exponential(a0, d, name);
	        	 series.add(obj);
	         }
	    }
	    return series;
	}
	
	public ArrayList<Series> getByD(int d) throws SQLException
	{
		ArrayList<Series> series = new ArrayList<>();
		
		String SQL = "SELECT * FROM series WHERE d=?";
		
		PreparedStatement pstmt = connection.prepareStatement(SQL, Statement.NO_GENERATED_KEYS);
		pstmt.setInt(1, d);

	    ResultSet rs = pstmt.executeQuery();

	    while ( rs.next() ) {

	         int a0 = rs.getInt("a0");
	         
	         int dt  = rs.getInt("d");

	         String  name = rs.getString("name");

	         int t  = rs.getInt("type");

	         if(t == 0) {
	        	 Linear obj = new Linear(a0, dt, name);
	        	 series.add(obj);
	         }
	         if(t == 1) {
	        	 Exponential obj = new Exponential(a0, dt, name);
	        	 series.add(obj);
	         }
	    }
	    return series;
	}
	
	public void dirtyRead() throws SQLException {
		connection.setAutoCommit(false);
		
		Statement statement = connection.createStatement();
		statement.executeUpdate("UPDATE public.series SET name='Третий' WHERE id=3");
		
		Statement statement2 = connection.createStatement();
		statement2.executeUpdate("DELETE FROM public.series WHERE id=4");

	    connection.commit();
	}
}


public class Main {
	
	
    public static void main(String[] args) {
        if(args.length < 1){
            System.out.println("Нужно задать имя файла csv при запуске программы");
            return;
        }

        String file = args[0];

        // создаем объект класса CSVSerialization, для десериализации входного файла
        CSVSerialization csv = new CSVSerialization(file);

        ArrayList<Series> series = null;
        try {
            series = csv.read();
        } catch (IOException e) {
        	System.out.println("Ошибка при чтении входного файла");
        }
        
        System.out.println("Прочитан входной файл");
        System.out.println("Количество объектов: " + series.size());
        
        PostgreConnection ps = new PostgreConnection();
        
        System.out.println("Проверяем соединение с базой данных");
        
        try {
			ps.checkConnection();
		} catch (ClassNotFoundException | SQLException e) {
			System.out.println("Нет соединения с базой данных");
			return;
		}
        System.out.println("Соединение с базой данных установлено");
       
        System.out.println("Пробуем создать таблицу");
        try {
			ps.createTable();
			System.out.println("Таблица создана");
		} catch (SQLException e) {}
    	
        System.out.println("Добавляем объекты в таблицу");
        try {
			int cnt = ps.insertData(series);
			System.out.println("Добавлено объектов: " + cnt);
		} catch (SQLException e) {
			System.out.println("Не удалось добавить объекты");
			return;
		}
		
        System.out.println("Получаем все объекты из таблицы");
        try {
			ArrayList<Series> s = ps.getAll();
			System.out.println("Количество полученных объектов: " + s.size());
			System.out.println("Имена объектов: ");
			for(Series sr: s) {
				System.out.println(sr.getName());
			}
		} catch (SQLException e) {
			System.out.println("Не удалось получить объекты");
			return;
		}
        
        System.out.println("Получаем объект по указанному d");
        Scanner scan = new Scanner(System.in);
        System.out.print("Значение d: ");
        int num = scan.nextInt();
        try {
        	ArrayList<Series> s = ps.getByD(num);
        	System.out.println("Количество полученных объектов: " + s.size());
			System.out.println("Имена объектов: ");
			for(Series sr: s) {
				System.out.println(sr.getName());
			}
		} catch (SQLException e) {
			System.out.println(e);
			System.out.println("Не удалось получить объекты");
			return;
		}
        
        System.out.println("Запрос с уровнем изоляции Dirty Read изменяем имя объекта с id=3 и удаляем объект с id=4");
        try {
			ps.dirtyRead();
			System.out.println("Состояние таблицы после запроса");
			ArrayList<Series> s = ps.getAll();
			System.out.println("Количество объектов: " + s.size());
			System.out.println("Имена объектов: ");
			for(Series sr: s) {
				System.out.println(sr.getName());
			}
		} catch (SQLException e) {
			System.out.println(e);
			System.out.println("Ошибка при запросе");
		}
        
		System.out.println("Завершено");
    }
}