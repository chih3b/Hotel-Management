package HotelManagement;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;


public class reservation extends javax.swing.JFrame {

    /**
     * Creates new form reservation
     */
    public reservation() {
        initComponents();
        Connect();
        autoID();
        RoomTypeL();
        RoomNo();
        BedType();
        Load_reservation();
        loadReservationStats();
      displayMostPopularRooms();
      displayRoomStates();
      displayUnregisteredClients();
      displayRegularClients();
    }
    
      Connection con;
    PreparedStatement pst;
    DefaultTableModel d;
   
    
    
    
    public void Connect()
    {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost/hotelmanagement", "root","Mayar17mayar");     
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(room.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(room.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
    }
    
    
    public void autoID()
    {
        
        try {
            Statement s = con.createStatement();
            //affichage de la plus recente reservation
            ResultSet rs = s.executeQuery("select MAX(reid) from  reservation");
            rs.next();
            rs.getString("MAX(reid)");
            
            if(rs.getString("MAX(reid)")== null)
            {
                jLabel12.setText("RE001");
            }
            else
            {
                long id = Long.parseLong(rs.getString("MAX(reid)").substring(2,rs.getString("MAX(reid)").length()));
                id++;
                 jLabel12.setText("RE" + String.format("%03d", id));
                
            }
    
            
        } catch (SQLException ex) {
            Logger.getLogger(room.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
    }
    
    
    public void RoomTypeL()
    {
        try {
            //récupèrer les valeurs uniques de la colonne "rtype" de la table "room"
            pst = con.prepareStatement("select Distinct rtype from room");
             ResultSet rs = pst.executeQuery();
             txtrtype.removeAllItems();
             
             while(rs.next())
             {
                 txtrtype.addItem(rs.getString("rtype"));
             }
             
             
        } catch (SQLException ex) {
            Logger.getLogger(reservation.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
     public void Load_reservation()
    {
        
        int c;
        
        try {
            //affichage de tous les donnes du tableau reservation
            pst = con.prepareStatement("select * from  reservation");
            ResultSet rs = pst.executeQuery();
            
            ResultSetMetaData rsd = rs.getMetaData();
            c = rsd.getColumnCount();
            
            d = (DefaultTableModel)jTable1.getModel();
            d.setRowCount(0);
            
            while(rs.next())
            {
                Vector v2 = new Vector();
                
                for(int i =1; i<=c; i++)
                {
                    v2.add(rs.getString("reid"));
                    v2.add(rs.getString("name")); 
                    v2.add(rs.getString("mobile"));
                    v2.add(rs.getString("checkin"));
                    v2.add(rs.getString("checkout"));
                    v2.add(rs.getString("rtype"));
                    v2.add(rs.getString("roomno"));
                    v2.add(rs.getString("bedtype"));
                      v2.add(rs.getString("amount"));
                    
                }
                
                d.addRow(v2);
                
                
                
            }
            
            
            
            
            
            
            
        } catch (SQLException ex) {
            Logger.getLogger(room.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
        
        
    }
    
    
   public void loadReservationStats() {
    try {
        // Appel de la procédure stockée pour récupérer les statistiques
        CallableStatement cs = con.prepareCall("{CALL display_reservation_stats()}");
        ResultSet rs = cs.executeQuery();
         
            

        // Mise à jour du modèle de données du tableau
        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
        model.setRowCount(0);
        while (rs.next()) {
            Vector<Object> row = new Vector<Object>();
            row.add(rs.getString("Month"));
            row.add(rs.getInt("Number_of_Reservations"));
            row.add(rs.getInt("Monthly_Revenue"));
            model.addRow(row);
        }
        rs.close();
        cs.close();
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}

public void displayMostPopularRooms() {
    try {
        // Récupération des données sur les chambres les plus demandées depuis la base de données
        String query = "SELECT r.rid, r.rtype, COUNT(*) AS num_reservations " +
                       "FROM room r INNER JOIN reservation res ON r.rid = res.roomno " +
                       "GROUP BY r.rid " +
                       "ORDER BY num_reservations DESC";
        PreparedStatement statement = con.prepareStatement(query);
        ResultSet rs = statement.executeQuery();

        // Mise à jour du modèle de données du tableau
        DefaultTableModel model = (DefaultTableModel) jTable3.getModel();
        model.setRowCount(0);
        while (rs.next()) {
            Object[] row = new Object[3];
            row[0] = rs.getString("rid");
            row[1] = rs.getString("rtype");
            row[2] = rs.getInt("num_reservations");
            model.addRow(row);
        }
        rs.close();
        statement.close();
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}
public void displayRoomStates() {
    try {
        // Récupération des données sur les chambres et leurs états depuis la base de données
        String query = "SELECT r.rid, r.rtype, r.btype, r.amount, " +
                       "(SELECT COUNT(*) FROM reservation res WHERE res.roomno = r.rid AND res.checkin <= CURDATE() AND res.checkout >= CURDATE()) AS active_reservations " +
                       "FROM room r";
        PreparedStatement statement = con.prepareStatement(query);
        ResultSet rs = statement.executeQuery();

        // Mise à jour du modèle de données du tableau
        DefaultTableModel model = (DefaultTableModel) jTable4.getModel();
        model.setRowCount(0);
        while (rs.next()) {
            Object[] row = new Object[6];
            row[0] = rs.getString("rid");
            row[1] = rs.getString("rtype");
            row[2] = rs.getString("btype");
            row[3] = rs.getInt("amount");
            row[4] = rs.getInt("active_reservations");
            row[5] = (rs.getInt("active_reservations") > 0) ? "Réservée" : "Libre";
            model.addRow(row);
        }
        rs.close();
        statement.close();
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
} 
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {                                         
       
        
         String reno = jLabel12.getText();
    String name = txtname.getText();
    String address = txtaddress.getText();
    String mobile = txtmobile.getText();
    SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
    String StartDate = df1.format(txtcheckin.getDate());
    SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
    String EndDate = df2.format(txtcheckout.getDate());
    String rtype = txtrtype.getSelectedItem().toString();
    String roomno = txtro.getSelectedItem().toString();
    String bedtype = txtbtype.getSelectedItem().toString();
    String amount = txtamount.getText();
        
        try {
            pst = con.prepareStatement("UPDATE reservations SET name = ?, address = ?, mobile = ?, bedtype = ?, roomno = ?, rtype = ?, amount = ? WHERE reno = ?");
            pst.setString(1, name);
        pst.setString(2, address);
        pst.setString(3, mobile);
        pst.setString(4, StartDate);
        pst.setString(5, EndDate);
        pst.setString(6, bedtype);
        pst.setString(7, roomno);
        pst.setString(8, rtype);
        pst.setString(9, amount);
        pst.setString(10, reno);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Reservation Editeddddd");
            
            txtrtype.setSelectedIndex(-1);
            txtbtype.setSelectedIndex(-1);
            txtamount.setText("");
            autoID();
            Load_reservation();
            jButton1.setEnabled(true);
            
            
        } catch (SQLException ex) {
            Logger.getLogger(room.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
        
        
        
        
        
        
    } 
/*private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
    String reno = jLabel12.getText();
    String name = txtname.getText();
    String address = txtaddress.getText();
    String mobile = txtmobile.getText();
    SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
    String StartDate = df1.format(txtcheckin.getDate());
    SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
    String EndDate = df1.format(txtcheckout.getDate());
    String rtype = txtrtype.getSelectedItem().toString();
    String roomno = txtro.getSelectedItem().toString();
    String bedtype = txtbtype.getSelectedItem().toString();
    String amount = txtamount.getText();

    try {
        pst = con.prepareStatement("UPDATE reservations SET name = ?, address = ?, mobile = ?, checkin = ?, checkout = ?, bedtype = ?, roomno = ?, rtype = ?, amount = ? WHERE reno = ?");
        pst.setString(1, name);
        pst.setString(2, address);
        pst.setString(3, mobile);
        pst.setString(4, StartDate);
        pst.setString(5, EndDate);
        pst.setString(6, bedtype);
        pst.setString(7, roomno);
        pst.setString(8, rtype);
        pst.setString(9, amount);
        pst.setString(10, reno);
        pst.executeUpdate();
        JOptionPane.showMessageDialog(this, "Reservation Edited");
        
        // Rest of the code to clear fields and update UI as needed
        
    } catch (SQLException ex) {
        Logger.getLogger(room.class.getName()).log(Level.SEVERE, null, ex);
    }
}*/



public void displayUnregisteredClients() {
    try {
        // Récupération des données sur les clients non enregistrés depuis la base de données
        String query = "SELECT DISTINCT res.name, res.address, res.mobile " +
                       "FROM reservation res " +
                       "LEFT JOIN user u ON res.name = u.name " +
                       "WHERE u.uid IS NULL";
        PreparedStatement statement = con.prepareStatement(query);
        ResultSet rs = statement.executeQuery();

        // Mise à jour du modèle de données du tableau
        DefaultTableModel model = (DefaultTableModel) jTable5.getModel();
        model.setRowCount(0);
        while (rs.next()) {
            Object[] row = new Object[3];
            row[0] = rs.getString("name");
            row[1] = rs.getString("address");
            row[2] = rs.getInt("mobile");
            model.addRow(row);
        }
        rs.close();
        statement.close();
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}
 public void displayRegularClients() {
try {
    CallableStatement statement = con.prepareCall("{ CALL display_regular_clients() }");
    ResultSet rs = statement.executeQuery();

    // Mise à jour du modèle de données du tableau
    DefaultTableModel model = (DefaultTableModel) jTable6.getModel();
    model.setRowCount(0);
    while (rs.next()) {
        Object[] row = new Object[1];
        row[0] = rs.getString("name");
        
        model.addRow(row);
    }
    rs.close();
    statement.close();
} catch (SQLException ex) {
    ex.printStackTrace();
}

 }


    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public void RoomNo()
    {
        try {
            pst = con.prepareStatement("select Distinct rid from room");
             ResultSet rs = pst.executeQuery();
             txtro.removeAllItems();
             
             while(rs.next())
             {
                 txtro.addItem(rs.getString("rid"));
             }
             
             
        } catch (SQLException ex) {
            Logger.getLogger(reservation.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    
     public void BedType()
    {
        try {
            pst = con.prepareStatement("select Distinct btype from room");
             ResultSet rs = pst.executeQuery();
             txtbtype.removeAllItems();
             
             while(rs.next())
             {
                 txtbtype.addItem(rs.getString("btype"));
             }
             
             
        } catch (SQLException ex) {
            Logger.getLogger(reservation.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtname = new javax.swing.JTextField();
        txtaddress = new javax.swing.JTextField();
        txtmobile = new javax.swing.JTextField();
        txtcheckin = new com.toedter.calendar.JDateChooser();
        txtcheckout = new com.toedter.calendar.JDateChooser();
        txtrtype = new javax.swing.JComboBox<>();
        txtro = new javax.swing.JComboBox<>();
        txtbtype = new javax.swing.JComboBox<>();
        txtamount = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jButton4 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTable6 = new javax.swing.JTable();
        jButton5 = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTable5 = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable4 = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jLabel13 = new javax.swing.JLabel();

        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText("jCheckBoxMenuItem1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setBackground(new java.awt.Color(51, 102, 0));
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 48)); // NOI18N
        jLabel1.setText("Reservation");
        jLabel1.setToolTipText("");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 340, 60));

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel2.setText("Reservation No");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(9, 111, -1, -1));

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel3.setText("Name");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(9, 155, -1, -1));

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel4.setText("Address");
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(9, 199, -1, -1));

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel5.setText("Mobile");
        jPanel2.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(9, 242, -1, -1));

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel6.setText("Check In");
        jPanel2.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(9, 284, -1, -1));

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel7.setText("Check Out");
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(9, 332, -1, -1));

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel8.setText("Room Type");
        jPanel2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(9, 377, -1, -1));

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel9.setText("Room No");
        jPanel2.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(9, 424, -1, -1));

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel10.setText("Bed Type");
        jPanel2.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(9, 464, -1, -1));

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel11.setText("Amount");
        jPanel2.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(9, 521, -1, -1));
        jPanel2.add(txtname, new org.netbeans.lib.awtextra.AbsoluteConstraints(107, 151, 198, -1));
        jPanel2.add(txtaddress, new org.netbeans.lib.awtextra.AbsoluteConstraints(107, 195, 198, -1));
        jPanel2.add(txtmobile, new org.netbeans.lib.awtextra.AbsoluteConstraints(107, 238, 198, -1));
        jPanel2.add(txtcheckin, new org.netbeans.lib.awtextra.AbsoluteConstraints(107, 277, 198, -1));
        jPanel2.add(txtcheckout, new org.netbeans.lib.awtextra.AbsoluteConstraints(107, 325, 198, -1));

        txtrtype.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "A/C", "no A/C" }));
        jPanel2.add(txtrtype, new org.netbeans.lib.awtextra.AbsoluteConstraints(107, 373, 198, -1));

        jPanel2.add(txtro, new org.netbeans.lib.awtextra.AbsoluteConstraints(107, 420, 198, -1));

        txtbtype.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "single", "double" }));
        txtbtype.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtbtypeActionPerformed(evt);
            }
        });
        jPanel2.add(txtbtype, new org.netbeans.lib.awtextra.AbsoluteConstraints(107, 460, 198, -1));
        jPanel2.add(txtamount, new org.netbeans.lib.awtextra.AbsoluteConstraints(107, 517, 198, -1));

        jButton1.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        jButton1.setText("Save");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 520, 90, 20));

        jButton2.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        jButton2.setText("Edit");
        jPanel2.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 520, 80, 20));

        jButton3.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        jButton3.setText("Delete");
        jPanel2.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 520, 80, 20));

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "roomno", "rtype", "num_reservations"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane3.setViewportView(jTable3);

        jPanel2.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(1140, 40, 320, 50));

        jButton4.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        jButton4.setText("Clear");
        jPanel2.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 520, 80, 20));

        jTable1.setBackground(new java.awt.Color(232, 225, 217));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "RID", "Name", "Mobile", "CheckIn", "CheckOut", "RoomType", "RoomNo", "BedType", "Amount"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 150, 480, 328));

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(204, 0, 0));
        jLabel12.setText("jLabel12");
        jPanel2.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 110, -1, -1));

        jTable6.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "name"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane6.setViewportView(jTable6);

        jPanel2.add(jScrollPane6, new org.netbeans.lib.awtextra.AbsoluteConstraints(1140, 240, 320, 240));

        jButton5.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        jButton5.setText("Close");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 520, 60, 20));

        jTable5.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "name", "address", "mobile"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane5.setViewportView(jTable5);

        jPanel2.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(830, 240, 300, 240));

        jTable4.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "nid", "rtype", "btype", " amount", "active_reservations", "etat"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane4.setViewportView(jTable4);

        jPanel2.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(1140, 90, 320, 140));

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Month", "Number_of_Reservations", "Monthly_Revenue"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane2.setViewportView(jTable2);

        jPanel2.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(830, 40, 300, 190));

        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/HotelManagement/images/13.jpg"))); // NOI18N
        jLabel13.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                jLabel13AncestorAdded(evt);
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        jPanel2.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1610, 680));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 1480, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void txtbtypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtbtypeActionPerformed
       
    }//GEN-LAST:event_txtbtypeActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
       
        
        String reno = jLabel12.getText();
        String name = txtname.getText();
        String address = txtaddress.getText();
        String mobile = txtmobile.getText();
        SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
        String StartDate = df1.format(txtcheckin.getDate());
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
        String EndDate = df1.format(txtcheckout.getDate());
        
        

        
        String rtype = txtrtype.getSelectedItem().toString();
        String roomno = txtro.getSelectedItem().toString();
        String bedtype = txtbtype.getSelectedItem().toString();
        String amount = txtamount.getText();

        String sql = "{CALL addReservation(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
try (CallableStatement cst = con.prepareCall(sql)) {
    cst.setString(1, reno);
    cst.setString(2, name);
    cst.setString(3, address);
    cst.setString(4, mobile);
    cst.setString(5, StartDate);
    cst.setString(6, EndDate);
    cst.setString(7, bedtype);
    cst.setString(8, roomno);
    cst.setString(9, rtype);
    cst.setString(10, amount);
    cst.execute();
    JOptionPane.showMessageDialog(this, "Reservation Added");
    // Rest of the code to clear fields and update UI as needed
} catch (SQLException ex) {
    Logger.getLogger(room.class.getName()).log(Level.SEVERE, null, ex);
}
        
        
        
        
        
        
        
        
        
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        
        this.setVisible(false);
        
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jLabel13AncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_jLabel13AncestorAdded
       
    }//GEN-LAST:event_jLabel13AncestorAdded

   
    public static void main(String args[]) {
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new reservation().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTable jTable4;
    private javax.swing.JTable jTable5;
    private javax.swing.JTable jTable6;
    private javax.swing.JTextField txtaddress;
    private javax.swing.JTextField txtamount;
    private javax.swing.JComboBox<String> txtbtype;
    private com.toedter.calendar.JDateChooser txtcheckin;
    private com.toedter.calendar.JDateChooser txtcheckout;
    private javax.swing.JTextField txtmobile;
    private javax.swing.JTextField txtname;
    private javax.swing.JComboBox<String> txtro;
    private javax.swing.JComboBox<String> txtrtype;
    // End of variables declaration//GEN-END:variables
}
