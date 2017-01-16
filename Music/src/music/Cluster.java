package music;
import java.sql.*;
import java.util.*;
public class Cluster {
  
   int cno;
   int user_size=0;
    int songs_size;
   User[] users =new User[35];
   Song[] songs ;
   double[] mean;
   int[] usermean;
   int nusers=0;//numberof users
   int nsongs=0;
   Methods md=new Methods();
  
   DB db;
   Connection con;
   Statement st;
   ResultSet rs;
   String tableName="workingset";
   Song[] usersongs ;
   int[] usersongreference ; 
   int[] songreference;
   int songrefcount=0;
   int usersongcount=0;
   int getusersongcount=0;

    public Cluster(int size,int usersize) {
        songs_size=size;
        user_size=usersize;
        songs=new Song[songs_size];
        usermean=new int[songs_size];
        usersongs= new Song[songs_size];
        usersongreference=new int[songs_size];
        songreference= new int[songs_size];
        mean=new double[user_size];
    }
  
   
    public int getCno() {
        return cno;
    }

    public void setCno(int cno) {
        this.cno = cno;
    }

    public double[] getMean() {
        return mean;
    }
    
    public int[] getUserMean() {
        return usermean;
    }
     
    public void setMean(double[] mean) {
        this.mean = mean;
    }
    
    public void setUserMean(int[] usermean) {
        this.usermean = usermean;
    }

    public User[] getUsers() {
        return users;
    }
    
    public Song[] getUserSongs(){
        return usersongs;
    }
    public void setUserSongs(Song[] usersongs){
        this.usersongs=usersongs;
    }
    //------
     public Song[] getSongs() {
        return songs;
    }

    public void setUsers(User[] users) {
        this.users = users;
    }
//------------
     public void setSongss(Song[] songs) {
        this.songs = songs;
    }
     
    public int getNusers() {
        return nusers;
    }

    //------------
    public int getNsongs() {
        return nsongs;
    }
    
    public int getNusersongs()
    {
        return getusersongcount;
    }
    public void setNusersongs(int getusersongcount)
    {
    this.getusersongcount=getusersongcount;
    }
    public void setNusers(int nusers) {
        this.nusers = nusers;
    }
    
    //-----------
     public void setNsongs(int nsongs) {
        this.nsongs = nsongs;
    }
    
     public void calUseMean()
     {
         try{
        usermean=new int[songs_size];//change size to no of songs
        for(int i=0;i<nusers;i++)
        {
            //System.out.println("mean iteration "+i );
            int[] tempusrow=new int[songs_size];
          // tempusrow=md.fetchUserSongs(songs[i].getSongId(),i);
            tempusrow=md.fetchUser(users[i].getUserId(), i);
            for(int j=0;j<songs_size;j++)
            {
                //System.out.println("mean inner loop "+i );
                usermean[j]+=tempusrow[j];
            }
        }
        }catch(Exception e)
        {
            System.out.println("Exception in calculateMean  : "+e.getMessage());
        }
        
        for(int i=0;i<songs_size;i++)
        {
           try{
                usermean[i]/=nusers;
           }
           catch(Exception e)
           {
               System.out.println("Divide by zero Exception"+e.getMessage());
           }
        }
     }
   
    public void calculateMean()
    {
        try{
        mean=new double[user_size];//change size to no of songs
        for(int i=0;i<nsongs;i++)
        {
            //System.out.println("mean iteration "+i );
            double[] tempusrow=new double[user_size];
            tempusrow=md.fetchSong_SVD(i);
            for(int j=0;j<user_size;j++)
            {
                //System.out.println("mean inner loop "+i );
                mean[j]+=tempusrow[j];
            }
        }
        }catch(Exception e)
        {
            System.out.println("Exception in calculateMean  : "+e.getMessage());
        }
        
        for(int i=0;i<user_size;i++)
        {
           try{
                mean[i]/=nsongs;
               // mean[i]/=nusers;
           }
           catch(Exception e)
           {
               System.out.println("Divide by zero Exception"+e.getMessage());
           }
        }
    }
    
    public void addUserToCluster(User uid)
    {
       // System.out.println("adding to cluster : "+uid);
        users[nusers]=new User(uid.getUserId());
        nusers++ ;
    }
    public void addSongToCluster(Song songid)
    {
          try{
            songs[nsongs]=new Song(songid.getSongId());
            songreference[songrefcount++]=nsongs;
            nsongs++;

            
              //System.out.println("nsongs value "+nsongs);
            return;
        }catch(Exception e)
        {
         System.out.println("Exception in Adding song to cluster  :"+e.getMessage());
        }
    }
    public void addsongstousercluster(User id,int row ,double[][] user_songs,Song[] songs)
    {
        try{
                for(int i=0;i<user_songs[row].length;i++)
                {
                   
                    if(user_songs[row][i]>0)
                    {
                       if(usersongs[i]==null)
                        {
                            usersongs[i]=songs[i];
                           // if(usersongcount<songs_size)
                            usersongreference[usersongcount++]=i;
                        }
                    }
                }
           }
            catch(Exception e)
            {
                System.out.println("Exception in adding songs to user cluster : "+e.getMessage());
            }
    }
    public  void userclustersongcount()
    {
    }
    
   public int getusersongcount()
    { 
        int getusersongcount=0;
    for(int a=0;a<songs_size;a++)
        if(usersongreference[a]>0)
            getusersongcount++;
        return getusersongcount;
    }
    }
    
    

