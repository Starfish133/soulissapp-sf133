package it.angelic.soulissclient.model;

/**
 * Created by Ale on 10/02/2015.
 */
public interface ISoulissCommand extends ISoulissObject, ISoulissExecutable{

    public int getType();

    public SoulissTypical getParentTypical();
}
