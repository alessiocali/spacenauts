package com.gff.spacenauts.dialogs;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.gff.spacenauts.AssetsPaths;
import com.gff.spacenauts.Globals;
import com.gff.spacenauts.Logger;
import com.gff.spacenauts.Logger.LogLevel;

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
	
	private static final String TAG = "Dialog";

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
	
	/**
	 * Parses the XML file containing all game dialogs, then returns the Dialog matching the given ID.
	 * @param id the dialog's ID.
	 * @return the dialog matching the given ID, or an empty Dialog if the research fails.
	 */
	public static Dialog loadDialogById(String id){
		try {
			Element parse = new XmlReader().parse(Gdx.files.internal(AssetsPaths.DATA_DIALOGS));
			String locale = parse.getAttribute("locale", "??");
			
			//Cycle all tables
			for (Element table : parse.getChildrenByName("dialog_table")){
				//Stop when you find the correct locale
				if (table.getAttribute("locale", "??").equals(Globals.locale)){
					//Cycle all dialog children
					for (Element child : table.getChildrenByName("dialog")){
						//Stop when you find the given ID
						if (child.getAttribute("id", "??").equals(id)){
							Array<Element> dialogPieces = child.getChildrenByName("dialog_piece");
							LinkedList<DialogPiece> dialogList = new LinkedList<DialogPiece>();
							//Iterate over all dialog_piece children
							for (Iterator<Element> iterator = dialogPieces.iterator() ; iterator.hasNext() ;){
								Element currentPiece = iterator.next();
								DialogPiece piece = new DialogPiece(currentPiece.getChildByName("speaker").getText(),
																	currentPiece.getChildByName("text").getText(),
																	table.getAttribute("locale"),
																	iterator.hasNext(),
																	currentPiece.getFloatAttribute("duration", 3));
								dialogList.add(piece);
							}
							return new Dialog(dialogList, id);
						}
					}
					//If no matching ID is found, return empty dialog and raise an error
					Logger.log(LogLevel.ERROR, TAG, "No dialog found with id: " + id);
					return new Dialog();
				}
			}
			//If no table matches the current locale, return empty dialog and raise an error
			Logger.log(LogLevel.ERROR, TAG, "No table found for locale: " + locale);
			return new Dialog();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//If something goes wrong, raise an error and return empty dialog
		Logger.log(LogLevel.ERROR, TAG, "Incorrect dialog search");
		return new Dialog();
	}
}
