package de.esa.musicwebapp.services.lastfm;

import de.esa.musicwebapp.entity.Track;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Michael
 */
public class LastFMApi {

    /**
     * API Root-Url
     */
    private static final String APPLICATION_URL = "http://ws.audioscrobbler.com/2.0/?";
    /**
     * API Key
     */
    private static final String API_KEY = "b3e1a560606cc5ac6b121eca31e54084";
    /**
     * Example:
     * http://ws.audioscrobbler.com/2.0/?method=artist.search&artist=cher&api_key=b3e1a560606cc5ac6b121eca31e54084
     */
    /**
     * Singleton Instance
     */
    private static LastFMApi instance;

    /**
     * Creates a singleton instance of the LastFMApi
     *
     * @return Instance of LastFMApi
     */
    public static LastFMApi getInstance() {
        if (instance == null) {
            instance = new LastFMApi();
        }
        return instance;
    }

    /**
     * Internal Class for grouping album infos
     */
    class AlbumInfo {

        public String mbid;
        public String albumname;
        public String artist;
        public String image;
    }

    /**
     * Internal Class for grouping tack infos
     */
    class TrackInfo {

        public String mbid;
        public String trackname;
        public String artist;
        public String image;
        public String lastfmuri;
    }

    /**
     * Singleton Constructor
     */
    private LastFMApi() {
    }

    /**
     * Searches for track by trackname (and optional by artistname)
     *
     * @param trackName the name of the track
     * @param optionalArtistName the name of the artist (option)
     * @return List of Track Objects
     */
    public List<Track> searchTrack(final String trackName, final String optionalArtistName) {
        List<Track> resultList = _getTracks(trackName, optionalArtistName);
        return resultList;
    }

    /**
     * Searches for track by trackname (and optional by artistname), processes
     * the result XML and creates the Tracklist
     *
     * @param trackName
     * @param optionalArtistName
     * @return List of Tracks
     */
    private List<Track> _getTracks(final String trackName, final String optionalArtistName) {
        List<Track> trackList = new ArrayList<Track>();

        try {
            String searchTerm = Parameter.track + "=" + trackName + "&";

            if (optionalArtistName != null && optionalArtistName.isEmpty() == false) {
                searchTerm += Parameter.artist + "=" + optionalArtistName + "&";
            }

            final String uri = APPLICATION_URL + Parameter.method + "=" + Method.track_search + "&"
                    + searchTerm
                    + Parameter.api_key + "=" + API_KEY;

            //Logger.getLogger(LastFMApi.class.getName()).log(Level.INFO, "SearchTerm: " + uri, new Exception());

            final Document xmlResult = _fetchResourceAtURI(uri);

            if (xmlResult != null) {

                final NodeList nodeList = xmlResult.getElementsByTagName(Parameter.track.toString());

                for (int idx = 0; idx < nodeList.getLength(); idx++) {
                    final Node node = nodeList.item(idx);

                    if (node.hasChildNodes() == false) {
                        continue;
                    }

                    final NodeList parameter = node.getChildNodes();

                    TrackInfo info = new TrackInfo();

                    for (int idxSub = 0; idxSub < parameter.getLength(); idxSub++) {
                        final Node parameterNode = parameter.item(idxSub);

                        if (parameterNode.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }

                        if (parameterNode.getNodeName().equals(Parameter.mbid.toString())) {
                            info.mbid = parameterNode.getTextContent();
                        }
                        if (parameterNode.getNodeName().equals(Parameter.name.toString())) {
                            info.trackname = parameterNode.getTextContent();
                        }
                        if (parameterNode.getNodeName().equals(Parameter.artist.toString())) {
                            info.artist = parameterNode.getTextContent();
                        }
                        if (parameterNode.getNodeName().equals(Parameter.image.toString())) {
                            info.image = parameterNode.getTextContent();
                        }
                        if (parameterNode.getNodeName().equals(Parameter.url.toString())) {
                            info.lastfmuri = parameterNode.getTextContent();
                        }

                        if (info.mbid != null && info.trackname != null && info.artist != null && info.image != null && info.lastfmuri != null) {
                            trackList.add(new Track(info.artist, info.trackname, "", info.image, "", "", "", "", info.lastfmuri));
                            break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(LastFMApi.class.getName()).log(Level.SEVERE, null, ex);
        }

        return trackList;
    }

    /**
     * Searches for albums their tracks by artistname
     *
     * @param artistName the name of the artist (Must be valid! Example: "Linkin
     * Park", NOT "Linkin" )
     * @return List of Tracks
     */
    public List<Track> searchArtist(final String artistName) {
        List<Track> resultList = new ArrayList<Track>();

        List<AlbumInfo> albums = _getAlbumInfos(artistName);
        for (AlbumInfo album : albums) {
            resultList.add(new Track(album.artist, "", album.albumname, album.image));
            //Logger.getLogger(LastFM.class.getName()).log(Level.INFO, "Found Album " + album.artist + ", " + album.albumname, new Exception());
            _addTracksToList(resultList, album);
        }

        return resultList;
    }

    /**
     * Retrieves a list of AlbumInfo by looking for the artist name
     *
     * @param artistName the name of the artist (Must be valid! Example: "Linkin
     * Park", NOT "Linkin" )
     * @return List of AlbumInfos
     */
    private List<AlbumInfo> _getAlbumInfos(final String artistName) {
        List<AlbumInfo> albumList = new ArrayList<AlbumInfo>();

        try {
            final String uri = APPLICATION_URL + Parameter.method + "=" + Method.artist_gettopalbums + "&"
                    + Parameter.artist + "=" + artistName + "&"
                    + Parameter.api_key + "=" + API_KEY;

            //Logger.getLogger(LastFMApi.class.getName()).log(Level.INFO, "SearchTerm: " + uri, new Exception());

            final Document xmlResult = _fetchResourceAtURI(uri);

            if (xmlResult != null) {

                final NodeList nodeList = xmlResult.getElementsByTagName(Parameter.album.toString());

                for (int idx = 0; idx < nodeList.getLength(); idx++) {
                    final Node node = nodeList.item(idx);

                    if (node.hasChildNodes() == false) {
                        continue;
                    }

                    final NodeList parameter = node.getChildNodes();

                    AlbumInfo info = new AlbumInfo();

                    for (int idxSub = 0; idxSub < parameter.getLength(); idxSub++) {
                        final Node parameterNode = parameter.item(idxSub);

                        if (parameterNode.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }

                        if (parameterNode.getNodeName().equals(Parameter.mbid.toString())) {
                            info.mbid = parameterNode.getTextContent();
                        }
                        if (parameterNode.getNodeName().equals(Parameter.name.toString())) {
                            info.albumname = parameterNode.getTextContent();
                        }
                        if (parameterNode.getNodeName().equals(Parameter.image.toString())) {
                            info.image = parameterNode.getTextContent();
                        }

                        if (info.mbid != null && info.albumname != null && info.image != null) {
                            info.artist = artistName;
                            albumList.add(info);
                            break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(LastFMApi.class.getName()).log(Level.SEVERE, null, ex);
        }

        return albumList;
    }

    /**
     * Adds tracks from the album to the trackList
     *
     * @param tracksList the tracklist where the tracks will be added to
     * @param albumInfo the albuminfo for looking after the tracks of the album
     */
    private void _addTracksToList(final List<Track> tracksList, final AlbumInfo albumInfo) {
        try {
            String searchTerm;
            if (albumInfo.mbid.isEmpty() == false) {
                searchTerm = Parameter.mbid + "=" + albumInfo.mbid;
            } else {
                searchTerm = Parameter.album + "=" + albumInfo.albumname + "&"
                        + Parameter.artist + "=" + albumInfo.artist;
            }

            final String uri = APPLICATION_URL + Parameter.method + "=" + Method.album_getInfo + "&"
                    + searchTerm + "&"
                    + Parameter.api_key + "=" + API_KEY;

            //Logger.getLogger(LastFM.class.getName()).log(Level.INFO, "SEARCHING AlbumInfo, SearchTerm: " + uri, new Exception());

            final Document xmlResult = _fetchResourceAtURI(uri);

            if (xmlResult != null) {

                final NodeList nodeList = xmlResult.getElementsByTagName(Parameter.track.toString());

                for (int idx = 0; idx < nodeList.getLength(); idx++) {
                    final Node node = nodeList.item(idx);

                    if (node.hasChildNodes() == false) {
                        continue;
                    }

                    final NodeList parameter = node.getChildNodes();

                    for (int idxSub = 0; idxSub < parameter.getLength(); idxSub++) {
                        final Node parameterNode = parameter.item(idxSub);

                        if (parameterNode.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }

                        if (parameterNode.getNodeName().equals(Parameter.name.toString())) {
                            tracksList.add(new Track(albumInfo.artist, parameterNode.getTextContent(), albumInfo.albumname, albumInfo.image));
                            break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(LastFMApi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Parses an XML Document object from the given url
     *
     * @param url the url to the xml content
     * @return XML Document
     */
    private Document _fetchResourceAtURI(final String url) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder;

        Document result = null;

        try {
            builder = factory.newDocumentBuilder();
            result = builder.parse(url);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(LastFMApi.class.getName()).log(Level.SEVERE, "XMLParser --> ParserConfigurationException", ex);
        } catch (SAXException ex) {
            Logger.getLogger(LastFMApi.class.getName()).log(Level.SEVERE, "XMLParser --> SAXException", ex);
        } catch (IOException ex) {
            Logger.getLogger(LastFMApi.class.getName()).log(Level.SEVERE, "XMLParser --> IOException", ex);
        }

        return result;
    }
}
