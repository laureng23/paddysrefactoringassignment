package bankexercise;

import java.io.*;
import java.util.*;
import javax.swing.*;


public class FileHelper {
	
	private static final int TABLE_SIZE = 29;
	private String fileToSaveAs;
	private RandomAccessFile input;
	private RandomAccessFile output;
	
	FileHelper(String fileToSaveAs, RandomAccessFile input, RandomAccessFile output){
		this.fileToSaveAs = fileToSaveAs;
		this.input = input;
		this.output = output;
	}
	
	private void openFileWrite(HashMap<Integer, BankAccount>table, JFileChooser fc) {
		this.fileToSaveAs = "";
		if(fileToSaveAs != "") {
			try {
				output = new RandomAccessFile(fileToSaveAs, "rw");
				JOptionPane.showMessageDialog(null,  "Accounts saved as " + fileToSaveAs);
				
			}
			catch(IOException ioException) {
				JOptionPane.showMessageDialog(null,"File does not exist.");
			}
		}else
			saveToFileAs(fc);
	}
	
	
	
	private void saveToFile(HashMap<Integer, BankAccount>table) {
		RandomAccessBankAccount record = new RandomAccessBankAccount();
		
		Scanner input = new Scanner(System.in);
		
		for (Map.Entry<Integer, BankAccount> entry : table.entrySet()) {
			   record.setAccountID(entry.getValue().getAccountID());
			   record.setAccountNumber(entry.getValue().getAccountNumber());
			   record.setFirstName(entry.getValue().getFirstName());
			   record.setSurname(entry.getValue().getSurname());
			   record.setAccountType(entry.getValue().getAccountType());
			   record.setBalance(entry.getValue().getBalance());
			   record.setOverdraft(entry.getValue().getOverdraft());
			   
			   if(output!=null){
			   
			      try {
						record.write( output );
					} catch (IOException u) {
						u.printStackTrace();
					}
			   }
			   
			}
	}
	
	
	private void openFileRead(HashMap<Integer, BankAccount>table, JFileChooser fc) {
		table.clear();
		fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(null);
		
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
		}
		
		try {
			if(fc.getSelectedFile() !=null) {
				input = new RandomAccessFile(fc.getSelectedFile(), "r");
			}
		}catch(IOException ioException) {
			JOptionPane.showConfirmDialog(null, "File Does Not Exist.");
		}
	}
	
	private void saveToFileAs(JFileChooser fc) {
		fc = new JFileChooser();
		
		int returnVal = fc.showSaveDialog(null);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			this.fileToSaveAs = file.getName();
			JOptionPane.showConfirmDialog(null, "Accounts saved to "+ file.getName());
		}else {
			JOptionPane.showMessageDialog(null, "Save cancelled by user");
		}
	try {
		if(fc.getSelectedFile()==null) {
			JOptionPane.showMessageDialog(null, "Cancelled");
		}else
			output = new RandomAccessFile(fc.getSelectedFile(), "rw");
	}catch (FileNotFoundException e) {
		e.printStackTrace();
		}
	}
	
	private void closeFile() {
		try {
			if(input !=null) {
				input.close();
			}
		}
		catch (IOException ioException) {
			JOptionPane.showMessageDialog(null, "Error closing file");
		}
	}
	
	
	private void readRecords(HashMap<Integer, BankAccount>table) {
		RandomAccessBankAccount record = new RandomAccessBankAccount();
		
		try {
			while (true) {
				do {
					if(input != null) 
						record.read(input);
				}while(record.getAccountID()==0);
				
				BankAccount ba = new BankAccount(record.getAccountID(), record.getAccountNumber(), record.getSurname(), record.getFirstName(), record.getAccountType(),
						record.getBalance(), record.getOverdraft());
				
				Integer key = Integer.valueOf(ba.getAccountNumber().trim());
				
				int hash = (key % TABLE_SIZE);
				while(table.containsKey(hash)) {
					hash = hash +1;
				}
				
				table.put(hash,  ba);
			}
		}catch (EOFException eofException) {
			return;
		}catch(IOException ioException) {
			JOptionPane.showMessageDialog(null, "Error reading file");
			System.exit(1);
		}
	}
	
	public void writeFile(HashMap<Integer, BankAccount>table, JFileChooser fc) {
		openFileWrite(table, fc);
		saveToFile(table);
		closeFile();
	}
	
	public void saveFileAs(HashMap<Integer, BankAccount>table, JFileChooser fc) {
		saveToFileAs(fc);
		saveToFile(table);
		closeFile();
	}
	
	public void readFile(HashMap<Integer, BankAccount> table, JFileChooser fc) {
		openFileRead(table, fc);
		readRecords(table);
		closeFile();
	}

}
