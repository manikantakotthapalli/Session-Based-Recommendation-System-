/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package music;

/**
 *
 * @author student
 */
public class Song {
    
    String Song_id;

    public Song(String Song_id) {
        this.Song_id = Song_id;
    }
    
    public void setSongId(String sid)
    {
        this.Song_id=sid;
    }
    public String getSongId()
    {
        return this.Song_id;
    }
    
}
