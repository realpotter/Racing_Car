import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

public class Signup extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private JPasswordField passwordField;
	private JPasswordField passwordField_1;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			Signup dialog = new Signup();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public Signup() {
		setTitle("New User Signup");

		setBackground(SystemColor.textHighlight);
		setForeground(SystemColor.textHighlight);
		setBounds(100, 100, 385, 225);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setForeground(SystemColor.textHighlight);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		JLabel lblUser = new JLabel("User");
		lblUser.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblUser.setBounds(93, 25, 35, 14);
		contentPanel.add(lblUser);

		JLabel lblPassword = new JLabel("Password");
		lblPassword.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblPassword.setBounds(68, 62, 67, 14);
		contentPanel.add(lblPassword);

		textField = new JTextField();
		textField.setBounds(154, 22, 102, 20);
		contentPanel.add(textField);
		textField.setColumns(10);

		passwordField = new JPasswordField();
		passwordField.setBounds(154, 59, 102, 20);
		contentPanel.add(passwordField);

		JLabel lblResetPassword = new JLabel("Reset password");
		lblResetPassword.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblResetPassword.setBounds(33, 92, 102, 14);
		contentPanel.add(lblResetPassword);

		passwordField_1 = new JPasswordField();
		passwordField_1.setBounds(154, 90, 102, 20);
		contentPanel.add(passwordField_1);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setForeground(SystemColor.textHighlight);
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						Connection conn = null;
						try {
							conn = (Connection) DriverManager.getConnection("jdbc:mysql://localhost/racing-game-login",
									"root", "163261");
							String username = "";
							String password = "";
							Boolean status = true;

							// get values using getText() method
							username = textField.getText();
							password = passwordField.getText();
							String Iquery = "select * from Login where User='" + username + "'";
							PreparedStatement Ipst = (PreparedStatement) conn.prepareStatement(Iquery);
							// Ipst.setString(2, textField.getText());
							ResultSet Irec = Ipst.executeQuery();

							while (Irec.next()) {
								String Username = Irec.getString("User");
								if (Username.equals(username)) {
									status = false;
									break;
								}
							}

							if (status == false) {
								JOptionPane.showMessageDialog(null, "User available", "Error",
										JOptionPane.PLAIN_MESSAGE);
								Signup NU = new Signup();

							} else {
								if (username.equals("") || password.equals("")) {
									JOptionPane.showMessageDialog(null, "Name or password or Role is wrong", "Error",
											JOptionPane.ERROR_MESSAGE);
								} else if ((passwordField_1.getText().equals(password)) == false) {
									JOptionPane.showMessageDialog(null, "Password no match", "Error",
											JOptionPane.PLAIN_MESSAGE);
								}

								else {
									String query = "INSERT INTO `racing-game-login`.`login` (user , password) VALUES (?, ?)";
									System.out.println(query);

									java.sql.PreparedStatement pstmt = ((Connection) conn).prepareStatement(query);

									pstmt.setString(1, username); // set input
																	// parameter
																	// 2
									pstmt.setString(2, password); // set input
																	// parameter
																	// 3
									pstmt.executeUpdate();

									String SMessage = "Signed up successfully";

									JOptionPane.showMessageDialog(null, SMessage, "Message", JOptionPane.PLAIN_MESSAGE);
									((java.sql.Connection) conn).close();
									dispose();
								}
							}
						} catch (SQLException se) {
							se.printStackTrace();
						} catch (Exception a) {
							a.printStackTrace();
						}
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent arg0) {
						dispose();
						System.exit(0);
					}

				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);

			}
		}
	}
}
