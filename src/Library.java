import net.proteanit.sql.DbUtils;

import javax.swing.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class Library {
    //Login Function
    public static void login() {
        JFrame login_frame = new JFrame("Login");

        JLabel lb_username = new JLabel("Username");
        lb_username.setBounds(30, 15, 100, 30);

        JLabel lb_password = new JLabel("Password");
        lb_password.setBounds(30, 50, 100, 30);

        JTextField F_user = new JTextField();
        F_user.setBounds(110, 15, 200, 30);

        JPasswordField F_pass = new JPasswordField();
        F_pass.setBounds(110, 50, 200, 30);

        JButton login_btn = new JButton("Login");
        login_btn.setBounds(130, 90, 80, 25);
        login_btn.addActionListener(e -> {
            String username = F_user.getText();
            String password = new String(F_pass.getPassword());

            if (username.equals("")) {
                JOptionPane.showMessageDialog(null, "Please Enter Username.");
            } else if (password.equals("")) {
                JOptionPane.showMessageDialog(null, "Please Enter Password.");
            } else {
                Connection connection = connect();
                try {
//                    assert connection != null;
                    assert connection != null;
                    Statement stmt = connection.createStatement();
                    stmt.execute("USE LIBRARY");
                    PreparedStatement pstat = connection.prepareStatement("select * from users where username=? and password=?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    pstat.setString(1, username);
                    pstat.setString(2, password);
                    ResultSet rs = pstat.executeQuery();


                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(null,"No User");
                        JOptionPane.showMessageDialog(null, "Wrong Username/Password.");
                    } else {
                        login_frame.dispose();
                        rs.beforeFirst();
                        while (rs.next()) {
                            String admin = rs.getString("ADMIN");
                            String UID = rs.getString("UID");
                            if (admin.equals("1")) {
                                admin_menu();
                            } else {
                                user_menu(UID);
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("Here");
                    System.out.println(ex.getStackTrace()[0]);
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }
        });
        login_frame.add(F_pass);
        login_frame.add(login_btn);
        login_frame.add(F_user);
        login_frame.add(lb_username);
        login_frame.add(lb_password);

        login_frame.setSize(400, 180);
        login_frame.setLayout(null);
        login_frame.setVisible(true);
        login_frame.setLocationRelativeTo(null);
    }

    //Connection Function
    public static Connection connect() {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Loaded Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost/mysql?user=root&password=");
            System.out.println("Connected To MYSQL.");
            return con;
        } catch (Exception ex) {
            System.out.println(ex.getStackTrace()[0]);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        return null;
    }

    //Create Database Function
    public static void create() {
        try {
            Connection connection = connect();
            assert connection != null;
            ResultSet resultSet = connection.getMetaData().getCatalogs();
            while (resultSet.next()) {
                String databaseName = resultSet.getString(1);
                if (databaseName.equals("library")) {
                    Statement stmt = connection.createStatement();
                    stmt.executeUpdate("DROP DATABASE library");
                }
            }
            Statement stmt = connection.createStatement();

            stmt.executeUpdate("CREATE DATABASE LIBRARY");
            stmt.executeUpdate("USE LIBRARY");
            //create user table
            stmt.executeUpdate("CREATE TABLE USERS(UID INT NOT NULL AUTO_INCREMENT PRIMARY KEY, USERNAME VARCHAR(30),PASSWORD VARCHAR(30),ADMIN BOOLEAN)");
            //insert user table
            stmt.executeUpdate("INSERT INTO USERS(USERNAME,PASSWORD,ADMIN) VALUES('admin','admin',TRUE)");
            //create books table
            stmt.executeUpdate("CREATE TABLE BOOKS(BID INT NOT NULL AUTO_INCREMENT PRIMARY KEY, BNAME VARCHAR(50), GENRE VARCHAR(20),PRICE INT)");
            //create issued table
            stmt.executeUpdate("CREATE TABLE ISSUED(IID INT NOT NULL AUTO_INCREMENT PRIMARY KEY, UID INT, BID INT, ISSUED_DATE VARCHAR(20), RETURN_DATE VARCHAR(20), PERIOD INT, FINE INT)");
            //Insert into books table
            stmt.executeUpdate("INSERT INTO BOOKS(BNAME, GENRE, PRICE) VALUES ('War and Peace', 'Mystery', 200),  ('The Guest Book', 'Fiction', 300), ('The Perfect Murder','Mystery', 150), ('Accidental Presidents', 'Biography', 250), ('The Wicked King','Fiction', 350)");
            resultSet.close();
        } catch (Exception ex) {
            System.out.println(ex.getStackTrace()[0]);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    //User Menu Function
    public static void user_menu(String UID) {
        JFrame user_frame = new JFrame("User Function");
        JButton view_btn = new JButton("View Books");
        view_btn.setBounds(20, 20, 120, 25);
        view_btn.addActionListener(e -> {
            JFrame frame_available_books = new JFrame("Books Available");

            Connection connection = connect();
            try {
                assert connection != null;
                Statement stmt = connection.createStatement();
                stmt.executeUpdate("USE LIBRARY");
                stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM BOOKS");
                JTable book_list = new JTable();
                book_list.setModel(DbUtils.resultSetToTableModel(rs));

                JScrollPane scrollPane = new JScrollPane(book_list);

                frame_available_books.add(scrollPane);
                frame_available_books.setSize(800, 400);
                frame_available_books.setVisible(true);
                frame_available_books.setLocationRelativeTo(null);
            } catch (Exception ex) {
                System.out.println(ex.getStackTrace()[0]);
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
        });

        JButton my_book = new JButton("My Books");
        my_book.setBounds(150, 20, 120, 25);
        my_book.addActionListener(e -> {
            JFrame frame_mybook = new JFrame("My Books");
            int UID_int = Integer.parseInt(UID);

            Connection connection = connect();
            try {
                assert connection != null;
                Statement stmt = connection.createStatement();
                stmt.executeUpdate("USE LIBRARY");
                stmt = connection.createStatement();

                ResultSet rs = stmt.executeQuery("SELECT DISTINCT ISSUED.*,BOOKS.BNAME,BOOKS.GENRE,BOOKS.PRICE FROM ISSUED,BOOKS " + "WHERE ((ISSUED.UID=" + UID_int + ") AND (BOOKS.BID IN (SELECT BID FROM ISSUED WHERE ISSUED.UID=" + UID_int + "))) GROUP BY IID");
                JTable book_list = new JTable();
                book_list.setModel(DbUtils.resultSetToTableModel(rs));
                JScrollPane scrollPane = new JScrollPane(book_list);

                frame_mybook.add(scrollPane);
                frame_mybook.setVisible(true);
                frame_mybook.setSize(800, 400);
                frame_mybook.setLocationRelativeTo(null);
            } catch (Exception ex) {
                System.out.println(ex.getStackTrace()[0]);
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
        });

        JButton logout_btn = new JButton("Logout");
        logout_btn.setBounds(90, 60, 120, 25);
        logout_btn.addActionListener(e -> {
            user_frame.dispose();
            login();
        });
        user_frame.add(logout_btn);
        user_frame.add(my_book);
        user_frame.add(view_btn);
        user_frame.setSize(300, 150);
        user_frame.setLayout(null);
        user_frame.setVisible(true);
        user_frame.setLocationRelativeTo(null);
    }

    //Admin Menu Function
    public static void admin_menu() {
        JFrame admin_frame = new JFrame("Admin Functions");
        admin_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton create_btn = new JButton("Create/Reset");
        create_btn.setBounds(450, 60, 120, 25);
        create_btn.addActionListener(e -> {
            create();
            JOptionPane.showMessageDialog(null, "Database Created/Reset!");
        });

        JButton view_btn = new JButton("View Books");
        view_btn.setBounds(20, 20, 120, 25);
        view_btn.addActionListener(e -> {
            JFrame view_books_frame = new JFrame("Books Available");

            Connection connection = connect();
            try {
                assert connection != null;
                Statement stmt = connection.createStatement();
                stmt.executeUpdate("USE LIBRARY");
                stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM BOOKS");
                JTable book_list = new JTable();
                book_list.setModel(DbUtils.resultSetToTableModel(rs));
                JScrollPane scrollPane = new JScrollPane(book_list);

                view_books_frame.add(scrollPane);
                view_books_frame.setVisible(true);
                view_books_frame.setSize(800, 400);
                view_books_frame.setLocationRelativeTo(null);
            } catch (Exception ex) {
                System.out.println(ex.getStackTrace()[0]);
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
        });

        JButton users_btn = new JButton("View Users");
        users_btn.setBounds(150, 20, 120, 25);
        users_btn.addActionListener(e -> {
            JFrame view_users_frame = new JFrame("Users List");

            Connection connection = connect();
            try {
                assert connection != null;
                Statement stmt = connection.createStatement();
                stmt.executeUpdate("USE LIBRARY");
                stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM USERS");
                JTable book_list = new JTable();
                book_list.setModel(DbUtils.resultSetToTableModel(rs));
                JScrollPane scrollPane = new JScrollPane(book_list);

                view_users_frame.add(scrollPane);
                view_users_frame.setSize(800, 400);
                view_users_frame.setVisible(true);
                view_users_frame.setLocationRelativeTo(null);
            } catch (Exception ex) {
                System.out.println(ex.getStackTrace()[0]);
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
        });

        JButton issue_btn = new JButton("View Issued Books");
        issue_btn.setBounds(280, 20, 160, 25);
        issue_btn.addActionListener(e -> {
            JFrame view_issued_books_frame = new JFrame("View Issued Books");
            //f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            Connection connection = connect();
            try {
                assert connection != null;
                Statement stmt = connection.createStatement();
                stmt.executeUpdate("USE LIBRARY");
                stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM ISSUED");
                JTable book_list = new JTable();
                book_list.setModel(DbUtils.resultSetToTableModel(rs));
                JScrollPane scrollPane = new JScrollPane(book_list);

                view_issued_books_frame.add(scrollPane);
                view_issued_books_frame.setSize(800, 400);
                view_issued_books_frame.setVisible(true);
                view_issued_books_frame.setLocationRelativeTo(null);
            } catch (Exception ex) {
                System.out.println(ex.getStackTrace()[0]);
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
        });
        JButton add_user = new JButton("Add User");
        add_user.setBounds(20, 60, 120, 25);
        add_user.addActionListener(e -> {
            JFrame g = new JFrame("Enter User Details");
            JLabel l1, l2;
            l1 = new JLabel("Username");
            l1.setBounds(30, 15, 100, 30);


            l2 = new JLabel("Password");
            l2.setBounds(30, 50, 100, 30);

            JTextField F_user = new JTextField();
            F_user.setBounds(110, 15, 200, 30);

            JPasswordField F_pass = new JPasswordField();
            F_pass.setBounds(110, 50, 200, 30);

            JRadioButton a1 = new JRadioButton("Admin");
            a1.setBounds(55, 80, 200, 30);

            JRadioButton a2 = new JRadioButton("User");
            a2.setBounds(130, 80, 200, 30);

            ButtonGroup bg = new ButtonGroup();
            bg.add(a1);
            bg.add(a2);


            JButton create_but1 = new JButton("Create");//creating instance of JButton for Create
            create_but1.setBounds(130, 130, 80, 25);//x axis, y axis, width, height

            create_but1.addActionListener(e1 -> {
                String username = F_user.getText();
                String password = new String(F_pass.getPassword());
                boolean admin = false;

                if (a1.isSelected()) {
                    admin = true;
                }

                Connection connection = connect();
                try {
                    assert connection != null;
                    Statement stmt = connection.createStatement();
                    stmt.executeUpdate("USE LIBRARY");
                    stmt.executeUpdate("INSERT INTO USERS(USERNAME,PASSWORD,ADMIN) VALUES ('" + username + "','" + password + "'," + admin + ")");
                    JOptionPane.showMessageDialog(null, "User Added!");
                    g.dispose();
                } catch (Exception ex) {
                    System.out.println(ex.getStackTrace()[0]);
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            });
            g.add(create_but1);
            g.add(a2);
            g.add(a1);
            g.add(l1);
            g.add(l2);
            g.add(F_user);
            g.add(F_pass);
            g.setSize(350, 200);
            g.setLayout(null);
            g.setVisible(true);
            g.setLocationRelativeTo(null);
        });
        JButton add_book = new JButton("Add Book");
        add_book.setBounds(150, 60, 120, 25);
        add_book.addActionListener(e -> {
            JFrame add_book_frame = new JFrame("Enter Book Details");

            JLabel lb_book_name = new JLabel("Book Name");  //lebel 1 for book name
            lb_book_name.setBounds(30, 15, 100, 30);


            JLabel lb_book_genre = new JLabel("Genre");  //label 2 for genre
            lb_book_genre.setBounds(30, 53, 100, 30);

            JLabel lb_book_price = new JLabel("Price");  //label 2 for price
            lb_book_price.setBounds(30, 90, 100, 30);

            JTextField F_bname = new JTextField();
            F_bname.setBounds(110, 15, 200, 30);

            JTextField F_genre = new JTextField();
            F_genre.setBounds(110, 53, 200, 30);

            JTextField F_price = new JTextField();
            F_price.setBounds(110, 90, 200, 30);


            JButton book_submit_btn = new JButton("Submit");//creating instance of JButton to submit details
            book_submit_btn.setBounds(130, 130, 80, 25);//x axis, y axis, width, height
            book_submit_btn.addActionListener(e12 -> {
                String bname = F_bname.getText();
                String genre = F_genre.getText();
                String price = F_price.getText();
                int price_int = Integer.parseInt(price);

                Connection connection = connect();

                try {
                    assert connection != null;
                    Statement stmt = connection.createStatement();
                    stmt.executeUpdate("USE LIBRARY");
                    stmt.executeUpdate("INSERT INTO BOOKS(BNAME,GENRE,PRICE)VALUES ('" + bname + "','" + genre + "'," + price_int + ")");
                    JOptionPane.showMessageDialog(null, "Book Added!");
                    add_book_frame.dispose();
                } catch (Exception ex) {
                    System.out.println(ex.getStackTrace()[0]);
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            });
            add_book_frame.add(lb_book_price);
            add_book_frame.add(book_submit_btn);
            add_book_frame.add(lb_book_name);
            add_book_frame.add(lb_book_genre);
            add_book_frame.add(F_bname);
            add_book_frame.add(F_genre);
            add_book_frame.add(F_price);
            add_book_frame.setSize(350, 200);
            add_book_frame.setLayout(null);
            add_book_frame.setVisible(true);
            add_book_frame.setLocationRelativeTo(null);
        });

        JButton issue_book = new JButton("Issue Book");
        issue_book.setBounds(450, 20, 120, 25);
        issue_book.addActionListener(e -> {
            JFrame issue_book_frame = new JFrame("Enter Issue Details");

            JLabel lb_bid = new JLabel("Book ID(BID)");
            lb_bid.setBounds(30, 15, 100, 30);

            JLabel lb_uid = new JLabel("User ID(UID)");
            lb_uid.setBounds(30, 53, 100, 30);

            JLabel lb_period = new JLabel("Period(Days)");
            lb_period.setBounds(30, 90, 100, 30);

            JLabel lb_date = new JLabel("Issued Date(DD-MM-YYYY)");
            lb_date.setBounds(30, 127, 150, 30);

            JTextField F_bid = new JTextField();
            F_bid.setBounds(110, 15, 200, 30);

            JTextField F_uid = new JTextField();
            F_uid.setBounds(110, 53, 200, 30);

            JTextField F_period = new JTextField();
            F_period.setBounds(110, 90, 200, 30);

            JTextField F_issue = new JTextField();
            F_issue.setBounds(180, 130, 130, 30);

            JButton submit_btn = new JButton("Submit");
            submit_btn.setBounds(130, 170, 80, 25);
            submit_btn.addActionListener(e13 -> {
                String uid = F_uid.getText();
                String bid = F_bid.getText();
                String period = F_period.getText();
                String issued_date = F_issue.getText();

                int period_int = Integer.parseInt(period);

                Connection connection = connect();

                try {
                    assert connection != null;
                    Statement stmt = connection.createStatement();
                    stmt.executeUpdate("USE LIBRARY");
                    stmt.executeUpdate("INSERT INTO ISSUED(UID,BID,ISSUED_DATE,PERIOD) VALUES ('" + uid + "','" + bid + "','" + issued_date + "'," + period_int + ")");
                    JOptionPane.showMessageDialog(null, "Book Issued!");
                    issue_book_frame.dispose();

                } catch (Exception ex) {
                    System.out.println(ex.getStackTrace()[0]);
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            });
            issue_book_frame.add(lb_period);
            issue_book_frame.add(lb_bid);
            issue_book_frame.add(lb_date);
            issue_book_frame.add(lb_uid);
            issue_book_frame.add(F_bid);
            issue_book_frame.add(F_uid);
            issue_book_frame.add(F_issue);
            issue_book_frame.add(F_period);
            issue_book_frame.add(submit_btn);
            issue_book_frame.setLayout(null);
            issue_book_frame.setSize(350, 250);
            issue_book_frame.setVisible(true);
            issue_book_frame.setLocationRelativeTo(null);
        });
        JButton return_book = new JButton("Return Book");
        return_book.setBounds(280, 60, 160, 25);
        return_book.addActionListener(e -> {
            JFrame return_book_frame = new JFrame("Enter Details");

            JLabel lb_iid = new JLabel("Issue ID(IID)");
            lb_iid.setBounds(30, 15, 100, 30);


            JLabel lb_return = new JLabel("Return Date(DD-MM-YYYY)");
            lb_return.setBounds(30, 50, 150, 30);

            JTextField F_iid = new JTextField();
            F_iid.setBounds(110, 15, 200, 30);


            JTextField F_return = new JTextField();
            F_return.setBounds(180, 50, 130, 30);

            JButton return_btn = new JButton("Return");
            return_btn.setBounds(130, 170, 80, 25);
            return_btn.addActionListener(e14 -> {
                String iid = F_iid.getText();
                String return_date = F_return.getText();

                Connection connection = connect();

                try {
                    assert connection != null;
                    Statement stmt = connection.createStatement();
                    stmt.executeUpdate("USE LIBRARY");
                    String date1 = null;

                    ResultSet rs = stmt.executeQuery("SELECT ISSUED_DATE FROM ISSUED WHERE IID=" + iid);
                    while (rs.next()) {
                        date1 = rs.getString(1);
                    }
                    try {
                        Date date_1 = new SimpleDateFormat("dd-MM-yyyy").parse(date1);
                        Date date_2 = new SimpleDateFormat("dd-MM-yyyy").parse(return_date);
                        long diff = date_2.getTime() - date_1.getTime();
                        ex.days = (int) (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage());
                    }
                    stmt.executeUpdate("UPDATE ISSUED SET RETURN_DATE='" + return_date + "' WHERE IID=" + iid);
                    return_book_frame.dispose();

                    Connection connection1 = connect();
                    assert connection1 != null;
                    Statement stmt1 = connection1.createStatement();
                    stmt1.executeUpdate("USE LIBRARY");
                    ResultSet rs1 = stmt1.executeQuery("SELECT PERIOD FROM ISSUED WHERE IID=" + iid);
                    String diff = null;
                    while (rs1.next()) {
                        diff = rs1.getString(1);
                    }
                    assert diff != null;
                    int diff_int = Integer.parseInt(diff);
                    if (ex.days >= diff_int) {
                        int fine = (ex.days - diff_int) * 10;
                        stmt1.executeUpdate("UPDATE ISSUED SET FINE=" + fine + " WHERE IID=" + iid);
                        String fine_str = ("Fine: Rs." + fine);
                        JOptionPane.showMessageDialog(null, fine_str);
                    }
                    JOptionPane.showMessageDialog(null, "Book Returned!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            });
            return_book_frame.add(lb_iid);
            return_book_frame.add(lb_return);
            return_book_frame.add(F_iid);
            return_book_frame.add(F_return);
            return_book_frame.add(return_btn);
            return_book_frame.setSize(350, 250);
            return_book_frame.setLayout(null);
            return_book_frame.setVisible(true);
            return_book_frame.setLocationRelativeTo(null);
        });

        JButton remove_user_btn = new JButton("Remove User");
        remove_user_btn.setBounds(90, 100, 120, 25);
        remove_user_btn.addActionListener(e -> {
            JFrame remove_user_frame = new JFrame("Enter User Details");

            JLabel lb_uid = new JLabel("UID:");
            lb_uid.setBounds(30, 15, 100, 30);

            JTextField F_uid = new JTextField();
            F_uid.setBounds(110, 15, 200, 30);

            JRadioButton user_radio = new JRadioButton("User");
            user_radio.setBounds(130, 80, 200, 30);

            ButtonGroup bg = new ButtonGroup();
            bg.add(user_radio);

            JButton search_user_btn = new JButton("Search");
            search_user_btn.setBounds(130, 130, 80, 25);
            search_user_btn.addActionListener(e15 -> {
                String uid = F_uid.getText();
                Connection connection = connect();
                try {
                    assert connection != null;
                    int confirm = 0;
                    Statement stmt = connection.createStatement();
                    stmt.executeUpdate("USE LIBRARY");
                    stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM USERS WHERE UID=" + uid);
                    if (rs.next()) {
                        confirm = JOptionPane.showConfirmDialog(null, "UID:" + rs.getString(1) + "\nUsername: " + rs.getString(2));
                    }
                    if (confirm == JOptionPane.YES_OPTION) {
                        Connection connection1 = connect();
                        assert connection1 != null;
                        Statement statement = connection1.createStatement();
                        statement.executeUpdate("USE LIBRARY");
                        int i = statement.executeUpdate("DELETE FROM USERS WHERE UID=" + uid);
                        if (i != 0) {
                            JOptionPane.showMessageDialog(null, "User Deleted!");
                        } else {
                            JOptionPane.showMessageDialog(null, "User Not Found!");
                        }
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            });

            remove_user_frame.add(search_user_btn);
            remove_user_frame.add(F_uid);
            remove_user_frame.add(lb_uid);
            remove_user_frame.add(user_radio);
            remove_user_frame.setVisible(true);
            remove_user_frame.setSize(350, 200);
            remove_user_frame.setLayout(null);
            remove_user_frame.setLocationRelativeTo(null);

        });

        JButton remove_book_btn = new JButton("Remove Book");
        remove_book_btn.setBounds(220, 100, 120, 25);
        remove_book_btn.addActionListener(e -> {
            JFrame remove_book_frame = new JFrame("Enter User Details");

            JLabel lb_bid = new JLabel("BID:");
            lb_bid.setBounds(30, 15, 100, 30);

            JTextField F_bid = new JTextField();
            F_bid.setBounds(110, 15, 200, 30);

            JRadioButton user_radio = new JRadioButton("User");
            user_radio.setBounds(130, 80, 200, 30);

            ButtonGroup bg = new ButtonGroup();
            bg.add(user_radio);

            JButton search_user_btn = new JButton("Search");
            search_user_btn.setBounds(130, 130, 80, 25);
            search_user_btn.addActionListener(e16 -> {
                String bid = F_bid.getText();
                Connection connection = connect();
                try {
                    assert connection != null;
                    int confirm = 0;
                    Statement stmt = connection.createStatement();
                    stmt.executeUpdate("USE LIBRARY");
                    stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM BOOKS WHERE BID=" + bid);
                    if (rs.next()) {
                        confirm = JOptionPane.showConfirmDialog(null, "BID:" + rs.getString(1) + "\nBook Name: " + rs.getString(2) + "\nGenre:" + rs.getString(3) + "\nPrice: " + rs.getString(4));
                    }
                    if (confirm == JOptionPane.YES_OPTION) {
                        Connection connection1 = connect();
                        assert connection1 != null;
                        Statement statement = connection1.createStatement();
                        statement.executeUpdate("USE LIBRARY");
                        int i = statement.executeUpdate("DELETE FROM BOOKS WHERE BID=" + bid);
                        if (i != 0) {
                            JOptionPane.showMessageDialog(null, "Book Deleted!");
                        } else {
                            JOptionPane.showMessageDialog(null, "Book Not Found!");
                        }
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            });

            remove_book_frame.add(search_user_btn);
            remove_book_frame.add(F_bid);
            remove_book_frame.add(lb_bid);
            remove_book_frame.add(user_radio);
            remove_book_frame.setVisible(true);
            remove_book_frame.setSize(350, 200);
            remove_book_frame.setLayout(null);
            remove_book_frame.setLocationRelativeTo(null);

        });

        JButton logout_btn = new JButton("Logout");
        logout_btn.setBounds(350, 100, 120, 25);
        logout_btn.addActionListener(e -> {
            admin_frame.dispose();
            admin_frame.dispose();
            login();
        });

        admin_frame.add(logout_btn);
        admin_frame.add(remove_book_btn);
        admin_frame.add(remove_user_btn);
        admin_frame.add(create_btn);
        admin_frame.add(return_book);
        admin_frame.add(issue_book);
        admin_frame.add(issue_btn);
        admin_frame.add(users_btn);
        admin_frame.add(add_user);
        admin_frame.add(add_book);
        admin_frame.add(view_btn);
        admin_frame.setSize(600, 200);
        admin_frame.setLayout(null);
        admin_frame.setVisible(true);
        admin_frame.setLocationRelativeTo(null);
    }

    public static class ex {
        public static int days = 0;
    }
}