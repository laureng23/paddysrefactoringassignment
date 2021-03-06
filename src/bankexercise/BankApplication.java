package bankexercise;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;

public class BankApplication extends JFrame {

	private static final long serialVersionUID = 1L;
	static HashMap<Integer, BankAccount> table = new HashMap<>();
	final static int TABLE_SIZE = 29;
	private JMenuBar menuBar;
	private JMenu navigateMenu, recordsMenu, transactionsMenu, fileMenu, exitMenu;
	private JMenuItem closeApp;
	private static JFileChooser fc;
	private JTable jTable;
	private double interestRate;
	private int currentItem = 0;
	private boolean openValues;

	String[] details = {"Account ID", "Account Number", "First Name", "Surname", "Account Type", "Balance", "Overdraft"};
	Map<String, JLabel> labels = new HashMap<String, JLabel>();
	Map<String, JTextField> fields = new HashMap<String, JTextField>();

	private Map<String, JMenuItem> navMenuItems = new HashMap<String, JMenuItem>();

	private Map<String, JMenuItem> recordMenuItems = new HashMap<String, JMenuItem>();

	private Map<String, JMenuItem> transactionMenuItems = new HashMap<String, JMenuItem>();

	private Map<String, JMenuItem> fileMenuItems = new HashMap<String, JMenuItem>();

	private String [] images = {"first.png", "prev.png", "next.png", "last.png"};
	private JButton[] imageButtons = new JButton[images.length];

	static String fileToSaveAs = "";
	private static RandomAccessFile input; 
	private static RandomAccessFile output;
	private FileHelper fileHelper;


	public BankApplication() {

		super("Bank Application");
		initComponents();
		fileHelper = new FileHelper(fileToSaveAs, input, output);
	}

	public void initComponents() {
		setLayout(new BorderLayout());
		JPanel displayPanel = new JPanel(new MigLayout());

		for(String string: details) {
			labels.put(string, new JLabel(string + ": "));
			fields.put(string, new JTextField(20));
			fields.get(string).setEditable(false);

			displayPanel.add(labels.get(string), "growx, pushx");
			displayPanel.add(fields.get(string), "growx, pushx");
		}

		add(displayPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new GridLayout(1, 4));

		for(int i =0; i<images.length;i++) {
			imageButtons[i] = new JButton(new ImageIcon(images[i]));
			buttonPanel.add(imageButtons[i]);
		}
		add(buttonPanel, BorderLayout.SOUTH);

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		navigateMenu = new JMenu("Navigate");

		ArrayList<String> menuItems = new ArrayList<String>(
				Arrays.asList("Next Item", "Previous Item", "First Item", "Last Item", "Find By Account Number", "Find By Surname", "List All Records" ));
		setMenuItems(navMenuItems, navigateMenu, menuItems);

		menuBar.add(navigateMenu);

		recordsMenu = new JMenu("Records");

		ArrayList<String> recordMenuLabels = new ArrayList<String>(
				Arrays.asList("Create Item", "Modify Item", "Delete Item", "Set Overdraft", "Set Interest"));
		setMenuItems(recordMenuItems, recordsMenu, recordMenuLabels);

		menuBar.add(recordsMenu);

		transactionsMenu = new JMenu("Transactions");

		ArrayList<String> transactionMenuLabels = new ArrayList<String>(
				Arrays.asList("Deposit", "Withdraw", "Calculate Interest"));
		setMenuItems(transactionMenuItems, transactionsMenu, transactionMenuLabels);


		menuBar.add(transactionsMenu);

		fileMenu = new JMenu("File");

		ArrayList<String> fileMenuLabels = new ArrayList<String>(
				Arrays.asList("Open File", "Save File", "Save As"));
		setMenuItems(fileMenuItems, fileMenu, fileMenuLabels);


		menuBar.add(fileMenu);

		exitMenu = new JMenu("Exit");

		closeApp = new JMenuItem("Close Application");

		exitMenu.add(closeApp);

		menuBar.add(exitMenu);

		setDefaultCloseOperation(EXIT_ON_CLOSE);

		recordMenuItems.get("Set Overdraft").addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e){
				if(table.size()==0) {
					JOptionPane.showMessageDialog(null, "No accounts found");
				}
				else if(table.get(currentItem)== null) {
					JOptionPane.showMessageDialog(null, "No accounts selected");
				}
				if(table.get(currentItem).getAccountType().trim().equals("Current")){
					String newOverdraftStr = JOptionPane.showInputDialog(null, "Enter new Overdraft", JOptionPane.OK_CANCEL_OPTION);
					fields.get("Overdraft").setText(newOverdraftStr);
					table.get(currentItem).setOverdraft(Double.parseDouble(newOverdraftStr));
				}
				else
					JOptionPane.showMessageDialog(null, "Overdraft only applies to Current Accounts");
			}
		}

	);


		ActionListener first = new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if(table.size() ==0) {
					JOptionPane.showMessageDialog(null, "No accounts in the system");
				}if(table.get(currentItem)==null) {
					JOptionPane.showMessageDialog(null, "No accounts selected");
				}
				else {
					saveOpenValues();

					displayCurrentItem();
				}
			}
		};


		ActionListener next1 = new ActionListener(){
			public void actionPerformed(ActionEvent e){

				ArrayList<Integer> keyList = new ArrayList<Integer>();
				int i=0;

				if(table.size() ==0) {
					JOptionPane.showMessageDialog(null, "No accounts in the system");
				}
				else {
					while(i<TABLE_SIZE){
						i++;
						if(table.containsKey(i))
							keyList.add(i);
					}
					int maxKey = Collections.max(keyList);

					saveOpenValues();	

					if(currentItem<maxKey){
						currentItem++;
						while(!table.containsKey(currentItem)){
							currentItem++;
						}
					}
					displayDetails(currentItem);	

					if(currentItem == 0) {
						JOptionPane.showMessageDialog(null, "Nothing to show");
					}
				}
			}
		};



		ActionListener prev = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				ArrayList<Integer> keyList = new ArrayList<Integer>();

				int i=0;

				if(table.size() ==0) {
					JOptionPane.showMessageDialog(null, "No accounts in the system");
				}if(table.get(currentItem)==null) {
					JOptionPane.showMessageDialog(null, "No accounts selected");
				}
				else {
					while(i<TABLE_SIZE){
						i++;
						if(table.containsKey(i))
							keyList.add(i);
					}
					int minKey = Collections.min(keyList);
					if(currentItem>minKey){
						currentItem--;
						while(!table.containsKey(currentItem)){

							currentItem--;
						}
					}
					displayDetails(currentItem);		
				}
			}
		};

		ActionListener last = new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if(table.size() ==0) {
					JOptionPane.showMessageDialog(null, "No accounts in the system");
				}if(table.get(currentItem)==null) {
					JOptionPane.showMessageDialog(null, "No accounts selected");
				}
				else {
					saveOpenValues();

					currentItem =29;

					while(!table.containsKey(currentItem)){
						currentItem--;
					}
					displayDetails(currentItem);
				}
			}
		};

		imageButtons[2].addActionListener(next1);
		navMenuItems.get("Next Item").addActionListener(next1);

		imageButtons[1].addActionListener(prev);
		navMenuItems.get("Previous Item").addActionListener(prev);

		imageButtons[0].addActionListener(first);
		navMenuItems.get("First Item").addActionListener(first);

		imageButtons[3].addActionListener(last);
		navMenuItems.get("First Item").addActionListener(last);

		recordMenuItems.get("Delete Item").addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

				if(table.size()==0) {
					JOptionPane.showMessageDialog(null, "No accounts found");
				}
				else if(table.get(currentItem)== null) {
					JOptionPane.showMessageDialog(null, "No accounts selected");
				}else {

					table.remove(currentItem);
					JOptionPane.showMessageDialog(null, "Account Deleted");
					displayCurrentItem();
				}
			}
		});

		recordMenuItems.get("Create Item").addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				new CreateBankDialog(table);		
			}
		});


		recordMenuItems.get("Modify Item").addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(table.size()==0) {
					JOptionPane.showMessageDialog(null, "No accounts found");
				}
				else if(table.get(currentItem)== null) {
					JOptionPane.showMessageDialog(null, "No accounts selected");
				}else {
					
					fields.get("Surname").setEditable(true);
					fields.get("First Name").setEditable(true);
					openValues = true;
				}
			}
		});

		recordMenuItems.get("Set Interest").addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

				if(table.size()==0) {
					JOptionPane.showMessageDialog(null, "No accounts found");
				}
				else if(table.get(currentItem)== null) {
					JOptionPane.showMessageDialog(null, "No accounts selected");}
				else {

					String interestRateStr = JOptionPane.showInputDialog("Enter Interest Rate: (do not type the % sign)");
					if(interestRateStr!=null)
						interestRate = Double.parseDouble(interestRateStr);
				}
			}
		});

		navMenuItems.get("List All Records").addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){

				if(table.size()==0) {
					JOptionPane.showMessageDialog(null, "No accounts in the system");
				}else {

					JFrame frame = new JFrame("TableDemo");
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					String col[] = {"ID","Number","Name", "Account Type", "Balance", "Overdraft"};

					DefaultTableModel tableModel = new DefaultTableModel(col, 0);
					jTable = new JTable(tableModel);
					JScrollPane scrollPane = new JScrollPane(jTable);
					jTable.setAutoCreateRowSorter(true);

					for (Map.Entry<Integer, BankAccount> entry : table.entrySet()) {


						Object[] objs = {entry.getValue().getAccountID(), entry.getValue().getAccountNumber(), 
								entry.getValue().getFirstName().trim() + " " + entry.getValue().getSurname().trim(), 
								entry.getValue().getAccountType(), entry.getValue().getBalance(), 
								entry.getValue().getOverdraft()};

						tableModel.addRow(objs);
					}
					frame.setSize(600,500);
					frame.add(scrollPane);
					frame.setVisible(true);			
				}
			}
		});

		fileMenuItems.get("Open File").addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

				fileHelper.readFile(table, fc);
				displayCurrentItem();
			}
		});

		fileMenuItems.get("Save File").addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(table != null && table.size()>0) {
					fileHelper.writeFile(table, fc);
				}else {
					JOptionPane.showMessageDialog(null, "Nothing to save");
				}
			}
		});

		fileMenuItems.get("Save As").addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

				if(table != null && table.size()>0) {
					fileHelper.saveFileAs(table,fc);
				}else {
					JOptionPane.showMessageDialog(null, "Nothing to save");
				}
			}
		});

		closeApp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int answer = JOptionPane.showConfirmDialog(BankApplication.this, "Do you want to save before quitting?");
				if (answer == JOptionPane.YES_OPTION) {
					fileHelper.saveFileAs(table, fc);
					dispose();
				}
				else if(answer == JOptionPane.NO_OPTION)
					dispose();
			}
		});	

		navMenuItems.get("Find By Surname").addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				if(table.size()==0) {
					JOptionPane.showMessageDialog(null, "No accounts in the system");
				}else {

					String sName = JOptionPane.showInputDialog("Search for surname: ");
					boolean found = false;

					for (Map.Entry<Integer, BankAccount> entry : table.entrySet()) {

						if(sName.equalsIgnoreCase((entry.getValue().getSurname().trim()))){
							found = true;
							
							setTextEntry(entry);
						}
					}		
					if(found)
						JOptionPane.showMessageDialog(null, "Surname  " + sName + " found.");
					else
						JOptionPane.showMessageDialog(null, "Surname " + sName + " not found.");
				}
			}
		});

		navMenuItems.get("Find By Account Number").addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				if(table.size()==0) {
					JOptionPane.showMessageDialog(null, "No accounts in the system");
				}else {

					String accNum = JOptionPane.showInputDialog("Search for account number: ");
					boolean found = false;

					for (Map.Entry<Integer, BankAccount> entry : table.entrySet()) {

						if(accNum.equals(entry.getValue().getAccountNumber().trim())){
							found = true;
							setTextEntry(entry);
						}			 
					}
					if(found)
						JOptionPane.showMessageDialog(null, "Account number " + accNum + " found.");
					else
						JOptionPane.showMessageDialog(null, "Account number " + accNum + " not found.");
				}
			}
		});

		transactionMenuItems.get("Deposit").addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(table.size()==0) {
					JOptionPane.showMessageDialog(null, "No Accounts to deposit into.");
				}else {

					String accNum = JOptionPane.showInputDialog("Account number to deposit into: ");
					boolean found = false;

					for (Map.Entry<Integer, BankAccount> entry : table.entrySet()) {
						if(accNum.equals(entry.getValue().getAccountNumber().trim())){
							found = true;
							String toDeposit = JOptionPane.showInputDialog("Account found, Enter Amount to Deposit: ");
							entry.getValue().setBalance(entry.getValue().getBalance() + Double.parseDouble(toDeposit));
							displayDetails(entry.getKey());

						}
					}
					if (!found)
						JOptionPane.showMessageDialog(null, "Account number " + accNum + " not found.");
				}
			}
		});

		transactionMenuItems.get("Withdraw").addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(table.size()==0) {
					JOptionPane.showMessageDialog(null, "No Accounts to withdraw from.");
				}else {

					String accNum = JOptionPane.showInputDialog("Account number to withdraw from: ");

					boolean found = false;

					for (Map.Entry<Integer, BankAccount> entry : table.entrySet()) {


						if(accNum.equals(entry.getValue().getAccountNumber().trim())){
							found = true;
							String toWithdraw = JOptionPane.showInputDialog("Account found, Enter Amount to Withdraw: ");
							if(entry.getValue().getAccountType().trim().equals("Current")){

								if(Double.parseDouble(toWithdraw) > entry.getValue().getBalance() + entry.getValue().getOverdraft())
									JOptionPane.showMessageDialog(null, "Transaction exceeds overdraft limit");
								else{
									entry.getValue().setBalance(entry.getValue().getBalance() - Double.parseDouble(toWithdraw));
									displayDetails(entry.getKey());
								}
							}
							else if(entry.getValue().getAccountType().trim().equals("Deposit")){

								if(Double.parseDouble(toWithdraw) <= entry.getValue().getBalance()){
									entry.getValue().setBalance(entry.getValue().getBalance()-Double.parseDouble(toWithdraw));
									displayDetails(entry.getKey());
								}
								else
									JOptionPane.showMessageDialog(null, "Insufficient funds.");
							}
						}
					}if(!found)
						JOptionPane.showMessageDialog(null, "Account number " + accNum + " not found.");
				}
			}
		});

		transactionMenuItems.get("Calculate Interest").addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(table.size()==0) {
					JOptionPane.showMessageDialog(null, "No accounts found");
				}
				else if(table.get(currentItem)== null) {
					JOptionPane.showMessageDialog(null, "No accounts selected");
				}else {
					for (Map.Entry<Integer, BankAccount> entry : table.entrySet()) {
						if(entry.getValue().getAccountType().equals("Deposit")){
							double equation = 1 + ((interestRate)/100);
							entry.getValue().setBalance(entry.getValue().getBalance()*equation);
							JOptionPane.showMessageDialog(null, "Balances Updated");
							displayDetails(entry.getKey());
						}
					}
				}
			}
		});		
	}

	private void saveOpenValues(){		
		if (openValues){
			fields.get("Surname").setEditable(false);
			fields.get("First Name").setEditable(false);

			table.get(currentItem).setSurname(fields.get("Surname").getText());
			table.get(currentItem).setFirstName(fields.get("First Name").getText());
		}
	}	

	public void displayDetails(int currentItem) {	

		fields.get("Account ID").setText(table.get(currentItem).getAccountID()+"");
		fields.get("Account Number").setText(table.get(currentItem).getAccountNumber());
		fields.get("Surname").setText(table.get(currentItem).getSurname());
		fields.get("First Name").setText(table.get(currentItem).getFirstName());
		fields.get("Account Type").setText(table.get(currentItem).getAccountType());
		fields.get("Balance").setText(table.get(currentItem).getBalance()+"");
		if(fields.get("Account Type").getText().trim().equals("Current"))
			fields.get("Overdraft").setText(table.get(currentItem).getOverdraft()+"");
		else
			fields.get("Overdraft").setText("Only applies to current accs");
	}
	
	private void displayCurrentItem() {
		currentItem =0;
		do {
			currentItem++;
		}while ( !table.containsKey(currentItem));
		displayDetails(currentItem);
		
	}

	private void setMenuItems(Map<String, JMenuItem> items, JMenu menu, ArrayList<String> menuItems) {

		menuItems.forEach(item ->{
			items.put(item, new JMenuItem(item));
			menu.add(items.get(item));
		});
	}

	public void setTextEntry(Entry<Integer, BankAccount> entry) {

		setTextEntry(entry);
	}

	public void put(int key, BankAccount value){
		int hash = (key%TABLE_SIZE);

		while(table.containsKey(key)){
			hash = hash+1;
		}
		table.put(hash, value);
	}

	public static void main(String[] args) {
		BankApplication ba = new BankApplication();
		ba.setSize(1200,400);
		ba.pack();
		ba.setVisible(true);
	}


}
