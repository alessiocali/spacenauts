package com.gff.spacenauts.dialogs;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * A sequence of DialogPieces. It acts as a wrapper for a LinkedLists and offers methods for advancing or rewinding the dialog.<br>
 * All dialogs are loaded from an XML file where each dialog is identified by their respective IDs. Also see 
 * {@link com.gff.spacenauts.Level#loadDialogById loadDialogById}.
 * 
 * @author Alessio Cali'
 *
 */
public class Dialog implements Serializable {

	private static final long serialVersionUID = 739876313995586289L;

	private String dialogID;
	private LinkedList<DialogPiece> dialog; 
	private ListIterator<DialogPiece> iterator;
	private DialogPiece currentPiece = EMPTY_DIALOG;
	
	public static final DialogPiece EMPTY_DIALOG = new DialogPiece("NULL", "NULL", "NULL", false, 0);
	
	/**
	 * Instantiates a new empty Dialog
	 */
	public Dialog(){
		dialog = new LinkedList<DialogPiece>();
		iterator = dialog.listIterator();
		dialogID = "";
	}
	
	public Dialog(LinkedList<DialogPiece> dialog, String id){
		this.dialog = dialog;
		iterator = dialog.listIterator();
		dialogID = id;
	}
	
	public void next(){
		if (iterator.hasNext())
			currentPiece = iterator.next();
		else
			currentPiece = EMPTY_DIALOG;
	}
	
	public void previous(){
		if (iterator.hasPrevious())
			currentPiece = iterator.previous();
		else
			currentPiece = EMPTY_DIALOG;
	}
	
	public DialogPiece getCurrent(){
		return currentPiece;
	}
	
	public void setDialogList(LinkedList<DialogPiece> dialog){
		this.dialog = dialog;
	}
	
	public void setID(String dialogID){
		this.dialogID = dialogID;
	}
	
	public String getID(){
		return dialogID;
	}
	
	public boolean hasNext(){
		return iterator.hasNext();
	}
}
