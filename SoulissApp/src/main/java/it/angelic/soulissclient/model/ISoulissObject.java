package it.angelic.soulissclient.model;

/**
 * Metodi comuni tra nodi e scene e tipici per il polimorfismo
 * delle rinominazioni e cambi icona
 * 
 * @author Ale
 *
 */
public interface ISoulissObject {

	public void setIconResourceId(int resId);
	public int getIconResourceId();
	
	public void setName(String newName);
    public String getName();
	
	public String getNiceName();
}
