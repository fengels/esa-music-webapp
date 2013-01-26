/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.esamusicwebapp.core.services.chartlyrics;

import com.chartlyrics.api.GetLyricResult;
import com.chartlyrics.api.SearchLyricDirectDocument;
import de.chartlyricsclient.Apiv1Stub;
import de.esamusicwebapp.core.SearchManager;
import de.esamusicwebapp.core.entity.Track;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.axis2.AxisFault;

/**
 *
 * @author erik
 */
public class ChartLyricsApi {

    private static ChartLyricsApi instance;

    public static ChartLyricsApi getInstance() {
        if (instance == null) {
            instance = new ChartLyricsApi();
        }
        return instance;
    }
    
    private ChartLyricsApi(){};
    
    /**
     * This method adds a lyric to a given track
     * @param track 
     */
    public void addLyric(final Track track) {
        try {
            Apiv1Stub st = new Apiv1Stub();
            SearchLyricDirectDocument b = SearchLyricDirectDocument.Factory.newInstance();
            SearchLyricDirectDocument.SearchLyricDirect doc = SearchLyricDirectDocument.SearchLyricDirect.Factory.newInstance();
            doc.setArtist(track.getArtist());
            doc.setSong(track.getTitle());
            b.setSearchLyricDirect(doc);
            GetLyricResult result = st.SearchLyricDirect(b).getSearchLyricDirectResponse().getSearchLyricDirectResult();
            if(result.getLyricId() != 0) {
                //Lyric wurde gefunden
                track.setLyric(result.getLyric());
                track.setLyricUri(result.getLyricUrl());                
            }
        } catch (Exception ex) {
            Logger.getLogger(ChartLyricsApi.class.getName()).log(Level.WARNING, "No connection to the Lyrics-Api possible.", ex);
        }
        if(track.getLyricUri() == null || track.getLyricUri().isEmpty()) {
            track.setLyricUri("http://www.chartlyrics.com/");
        }
    }
}