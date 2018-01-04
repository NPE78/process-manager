/*
 * Fichier : Gate.java
 * Projet  : GPSTrainsSMSCnx
 * Date    : 12 nov. 2004
 * Auteur  : sps
 * -----------------------------------------------------------------------------
 * CVS :
 * $Header: /home/cvs_gpstrains/GPSTrainsServeurs/src/com/gpstrains/serveurs/Gate.java,v 1.4 2005/01/12 16:24:05 sps Exp $
 */
package com.talanlabs.processmanager.messages.gate;

public interface Gate {

    String getName();

    void createNewFile(String msgID, String data);

    void reinject(String msgID);

    void accept(String msgID);

    void reject(String msgID);

    void retry(String msgID);

    void trash(String msgID);

    void archive(String msgID);

    void close();

    void open();

    boolean isOpened();
}
