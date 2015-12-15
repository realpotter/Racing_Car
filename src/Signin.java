
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;

@SuppressWarnings("serial")
public class Signin extends JDialog {
	
	private JTextField textField;
	private JPasswordField passwordField;
	static JLabel lblWelcome;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		final JFrame jframe = new JFrame("JDialog Demo");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Signin frame = new Signin(/*jframe, true*/);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Signin(/*Frame parent, boolean modal*/) {
		
		//super(parent, true);
		
		
		getContentPane().setForeground(new Color(0, 0, 0));
		getContentPane().setFont(new Font("Tahoma", Font.BOLD, 13));
		getContentPane().setBackground(new Color(34, 139, 34));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 346, 202);
		getContentPane().setLayout(null);
		
		JLabel lblUser = new JLabel("User");
		lblUser.setForeground(Color.BLACK);
		lblUser.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblUser.setBounds(49, 43, 46, 14);
		getContentPane().add(lblUser);
		
		JLabel lblPassword = new JLabel("Password");
		lblPassword.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblPassword.setBounds(49, 68, 63, 14);
		getContentPane().add(lblPassword);
		
		textField = new JTextField();
		textField.setForeground(Color.BLUE);
		textField.setBounds(122, 41, 111, 20);
		getContentPane().add(textField);
		textField.setColumns(10);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(122, 66, 111, 20);
		getContentPane().add(passwordField);
		
		JButton btnLogin = new JButton("Login");
		btnLogin.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnLogin.setForeground(Color.BLUE);
		btnLogin.setBounds(99, 96, 71, 23);
		getContentPane().add(btnLogin);
		SwingUtilities.getRootPane(btnLogin).setDefaultButton(btnLogin);
		btnLogin.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				Connection conn;
				Boolean status = false;
				
				try {
					conn = (Connection) DriverManager.getConnection("jdbc:mysql://localhost/racing-game-login", "root", "163261");
					
					String query = "select * from Login where User=? and Password=?";
					PreparedStatement pst = (PreparedStatement) conn.prepareStatement(query);
					pst.setString(1, textField.getText());
					pst.setString(2, passwordField.getText());
					
					ResultSet rec = pst.executeQuery();
					if(rec.next()) {
						dispose ();
		                
		                //f1.setVisible(true);
						JOptionPane.showMessageDialog(null, "Logged successfully !", "Message", JOptionPane.PLAIN_MESSAGE);
					} else {
		     		JOptionPane.showMessageDialog(null, "Incorrect User/Password");
					}
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					JOptionPane.showMessageDialog(null, e.getMessage());
					e.printStackTrace();
				}
				
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setForeground(Color.BLUE);
		btnCancel.setBounds(190, 95, 71, 22);
		getContentPane().add(btnCancel);
		btnCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				dispose ();
	            System.exit (0);
			}
		});
		btnCancel.setFont(new Font("Tahoma", Font.BOLD, 11));
		
		
		JButton btnSignUp = new JButton("Sign up");
		btnSignUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					dispose();
					Signup dialog = new Signup();
					
					//dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					
					dialog.setVisible(true);
					new Frame().add(dialog, BorderLayout.CENTER);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		
		btnSignUp.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnSignUp.setForeground(Color.BLUE);
		btnSignUp.setBounds(231, 130, 89, 23);
		getContentPane().add(btnSignUp);
	}
}
