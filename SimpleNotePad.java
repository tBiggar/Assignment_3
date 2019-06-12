/*
File Name: SimpleNotePad.java
Student Name: Timothy Biggar
Date: 6/11/2019

Part 1, Code Smells:
1) Poor field/variable naming:
	All instance fields of SimpleNotePad have one or two letter names that offer
	no information as to the purpose of that field.

	The other places where this occurs is in actionPerformed(...):
		FileChooser fc, Graphics pg, and PageFormat pf are the remaining
		poorly named variables. As an example, these became fileChooser,
		printGraphics, and pageFormat during my refactoring.

2) Long methods (Does too much):
	SimpleNotePad(): Constructors should generally perform instantiation and
	call set-up methods, and do little else. A few methods can be extracted from
	the constructor.
	
	actionPerformed(ActionEvent e): Several methods can be extracted from inside
	the multiple if/if-else blocks in this method.
	
	Extracting groups of similar operations will make both of these methods
	shorter, more readable, and therefore increase maintainability. This will
	also make the next code smell both more apparent and easier to handle.

3) Branching over possible actions:
	actionPerformed(ActionEvent e): The if/if-else blocks in this method is
	branching over several possible actions, and becomes hard to maintain as
	more are added or changes become necessary.

4) Speculative generality (?):
	else if (e.getActionCommand().equals("undo")) {
		// TODO: implement undo operation
	}
	
	(or after performing some refactoring...)
	
	public void undo() {
		// TODO: implement undo operation
	}
	
	Not too sure about this one, since it would be a typical feature of a
	note-pad type application, but it does not show up in edit menu during the
	sample runs in the Project Requirements Specification document. If this
	counts as a code smell, I would remove this method and the JMenuItem
	associated with it.

Part 2, Refactoring:
I renamed the following fields/variables to improve readability and usefulness.
	JMenuBar mb -> menuBar
	JMenu fm -> fileMenu
	JMenu em -> editMenu
	JTextPane d -> display
	JMenuItem nf -> newFile
	JMenuItem sf -> saveFile
	JMenuItem pf -> printFile
	JMenuItem u -> undo
	JMenuItem c -> copy
	JMenuItem p -> paste
	
	Additionally, "fc", "pg", and "pf" in actionPerformed(...) were renamed as
	mentioned in the code smells segment.
	
I marked the instance fields of SimpleNotePad as private to improve readability.

I extracted the following six methods from actionPerformed to improve
maintainability and readability:	
	private void newFile();
	private void saveFile();
	private void printFile();
	private void copy();
	private void paste();
	private void undo();

From the constructor of SimpleNotePad, I extracted the following two methods:
	setUpAppearance();
	setUpActionListners();

For my largest bit of refactoring, I noticed that the ActionListner interface is
a functional interface, meaning I can use a lambda expression as follows:
	swing_object.addActionListner(e -> {
		methodCall();
	}});
	
	to start the command, instead of having set the action command for each
	action, and have actionPerformed() choose the correct action to perform.
	To clean up I removed the "implements ActionListener" class modifier from
	SimpleNotePad, all setActionCommand() calls, the entire actionPerformed
	method, and the now unneeded imports for ActionListner and ActionEvent.


(Not technically refactoring, because it made a (very minor) visible change, but
I added a return statement to saveFile() to quit if
	!(returnVal == JFileChooser.APPROVE_OPTION)
to prevent an exception.
)

I ran out of time 

 */

package notepad;

import java.awt.Dimension;
import java.awt.Graphics;

import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.io.PrintWriter;
import java.util.Scanner;

import javax.swing.JFileChooser;

import javax.swing.JFrame;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

public class SimpleNotePad extends JFrame {
	// Menus
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenu editMenu;
	
	// Text display/editor
	private JTextPane display;
	
	// File menu options
	private JMenuItem openFile;
	private JMenuItem newFile;
	private JMenuItem saveFile;
	private JMenuItem printFile;
	
	// private JMenu ??? recent;
	// private Stack<File> recentlyOpened
	// not sure which data structure to use for this, ran out of time
	
	// Edit menu options
	private JMenuItem undo;
	private JMenuItem copy;
	private JMenuItem paste;
	private JMenuItem replace; // not fully implemented, currently same as paste
	
	public SimpleNotePad() {
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		editMenu = new JMenu("Edit");
		display = new JTextPane();
		newFile = new JMenuItem("New File");
		openFile = new JMenuItem("Open");
		saveFile = new JMenuItem("Save File");
		printFile = new JMenuItem("Print File");
		replace = new JMenuItem("Replace");
		undo = new JMenuItem("Undo");
		copy = new JMenuItem("Copy");
		paste = new JMenuItem("Paste");
		
		// Sets up the visual aspects of the GUI
		setUpAppearance();
		
		// This is what makes the GUI respond
		setUpActionListners();
		
		// These are not extracted
		setPreferredSize(new Dimension(600,600));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		
		pack();
	}
	
	// This is what makes the GUI respond
	private void setUpActionListners() {
		openFile.addActionListener(e -> {
			openFile();
		});
		newFile.addActionListener(e -> {
			newFile();
		});
		saveFile.addActionListener(e -> {
			saveFile();
		});
		printFile.addActionListener(e -> {
			printFile();
		});
		replace.addActionListener(e -> {
			replace();
		});
		copy.addActionListener(e -> {
			copy();
		});
		paste.addActionListener(e -> {
			paste();
		});
		undo.addActionListener(e -> {
			undo();
		});
	}
	
	// Sets up the visual aspects of the GUI
	private void setUpAppearance() {
		setTitle("A Simple Notepad Tool");
		
		fileMenu.add(openFile);
		fileMenu.addSeparator();
		fileMenu.add(newFile);
		fileMenu.addSeparator();
		fileMenu.add(saveFile);
		fileMenu.addSeparator();
		fileMenu.add(printFile);
		
		editMenu.add(replace);
		editMenu.add(undo);
		editMenu.add(copy);
		editMenu.add(paste);
		
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		setJMenuBar(menuBar);
		add(new JScrollPane(display));
	}
	
	public static void main(String[] args) {
		SimpleNotePad app = new SimpleNotePad();
	}

	private void copy() {
		display.copy();
	}

	private void newFile() {
		display.setText("");
	}

	private void saveFile() {
		File fileToWrite = null;
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			fileToWrite = fileChooser.getSelectedFile();
		} else {
			return; // logical bug fix that prevents an exception in console
		}
		try {
			PrintWriter out = new PrintWriter(new FileWriter(fileToWrite));
			out.println(display.getText());
			JOptionPane.showMessageDialog(null,
					"File is saved successfully...");
			out.close();
		} catch (IOException ex) {
			
		}
	}

	private void openFile() {
		File fileToOpen = null;
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			fileToOpen = fileChooser.getSelectedFile();
		} else {
			return; // the user chose to cancel or closed the window
		}
		try {
			Scanner in = new Scanner(fileToOpen);
			
			StringBuilder text = new StringBuilder();
			
			if (in.hasNextLine()) {
				text.append(in.nextLine());
			}
			while (in.hasNextLine()) {
				text.append("\n" + in.nextLine());
			}
			
			System.out.println(text.toString());
			
			display.setText(text.toString());
			
			// TODO: implement add file to top of recently opened data structure
			
			in.close();
		} catch (IOException ex) {
			
		}
	}

	private void printFile() {
		try {
			PrinterJob pjob = PrinterJob.getPrinterJob();
			pjob.setJobName("Sample Command Pattern");
			pjob.setCopies(1);
			pjob.setPrintable(new Printable() {
				
				public int print(Graphics pg, PageFormat pf, int pageNum) {
					if (pageNum > 0) {
						return Printable.NO_SUCH_PAGE;
					}
					
					pg.drawString(display.getText(), 500, 500);
					paint(pg);
					
					return Printable.PAGE_EXISTS;
				}
			});
			
			if (pjob.printDialog() == false) {
				return;
			}
			
			pjob.print();
		} catch (PrinterException pe) {
			JOptionPane.showMessageDialog(null,
					"Printer error" + pe, "Printing error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void paste() {
		// --- this looks like debug information ---
		StyledDocument doc = display.getStyledDocument();
		Position position = doc.getEndPosition();
		System.out.println("offset" + position.getOffset());
		// -----------------------------------------
		display.paste(); // actually doing the paste operation
	}

	private void undo() {
		// TODO: implement undo operation
		
		
	}
	
	private void replace() {
		// TODO: implement replace operation
		
		// open text field dialogue?
		// if (okay button used) {
		// 	copy text to clip-board
		display.paste();
		// }
	}
	
	
	
	
}







