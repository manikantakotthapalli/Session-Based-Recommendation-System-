/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 
07/01/2014*/
package music;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import java.sql.*;
import java.util.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author student
 */
public class Methods  {
    static  int usize ;
    static  int ssize,tempcount=0 ;
    static int  max_clusters ;
    int test_usize;
    int test_ssize;
    int usercount=0;
    static User[] users ;
    static Song[] songs ;
    static User[] testuser ;
    static Song[] testsongs;
    static double[][] user_songs ;
    static double[][] test_user_song;
    static int[] tpt;
    static int[] fpt;
    double tpsum,fpsum,fnsum;
    String tableName = "workingset";
    String testTable="testset";
    double omin = 0.0, omax = 1.0, nmin = 0.0, nmax = 1000.0, threshold;
    DB db = new DB();
    Connection con = db.getConnector();
    Statement st,stmt;
    ResultSet rs,it;
    int global=0;
    int usersongsref=0;
     Cluster[] c;
     Cluster[] cs;
     Cluster[] commonsongs;
    int nc = 1;//number of clusters
    int ns=1;
    double tp,fp,fn,temp,avg=0.0,ravg;
    int newVsr;
    int recmdcount=0;
    int commoncount=0;//variable for no of common songs in common clusters
    private String[] recm;
    private String[] rec_songs;
   
    Matrix User_Song_Matrix,S,V,U,VT;
    Matrix User_Song_Test_Matrix,TS,TV,TU;
    static double Song_SVD[][],TestSong_SVD[][];
    public HashMap<String,Integer> umap=new HashMap<String,Integer>();
    public HashMap<String,Integer> smap=new HashMap<String,Integer>();
    public HashMap<String,Integer> tumap=new HashMap<String, Integer>();
    public HashMap<String,Integer> tsmap=new HashMap<String, Integer>();
     String testString="";
      String trainingString="";
   public void createRandomStrings()
	{

		 String []names={
				"user_000001","user_000002","user_000003","user_000004","user_000005","user_000006","user_000007","user_000008","user_000009","user_000010",
				"user_000011","user_000012","user_000013","user_000014","user_000015","user_000016","user_000017","user_000018","user_000019","user_000020",
				"user_000021","user_000022","user_000023","user_000024","user_000025","user_000026","user_000027","user_000028","user_000029","user_000030",
				"user_000031","user_000032","user_000033","user_00034","user_000035","user_000036","user_000037","user_000038","user_000039","user_000040",
				"user_000041","user_000042","user_000043","user_000044","user_000045","user_000046","user_000047","user_00048","user_000049","user_000050",
				};
		 int test_Index[]=new int[15];
		 int training_Index[]=new int[35];
		 int testcount=0,traningcount=0;
		
		
		 Random robj=new  Random();
		 

			for(int i=0;traningcount<35;i++)
			{
				int temp=robj.nextInt(names.length);
				if(!contain(temp,training_Index,traningcount+1))
					training_Index[traningcount++]=temp;
			}

			for(int i=0;i<50;i++)
				if(!contain(i, training_Index, traningcount))
					test_Index[testcount++]=i;
	
			
			for(int i=0;i<35;i++)
                            if(i==34)
                            trainingString+="'"+names[training_Index[i]]+"'";
                            else
                            trainingString+="'"+names[training_Index[i]]+"'"+",";
			for(int i=0;i<15;i++)
                            if(i==14)
                                testString+="'"+names[test_Index[i]]+"'";
                                else
				testString+="'"+names[test_Index[i]]+"'"+",";
			
			System.out.println("test  :   "+testString+"\n"+"training   :   "+trainingString);
		}
	
		public  boolean contain(int x,int a[],int lenght)
		{
			for(int i=0;i<lenght;i++)
			if(x==a[i])
		      return true;		
			return false;
		}
    public void initialisation() 
    {
        try{
        st=con.createStatement();
          st.executeUpdate("create view temp1 as SELECT * FROM `dataset` WHERE MONTH(timestamp) in (1,2,3)");
     // st.executeUpdate("create view temp1 as SELECT * FROM `dataset` WHERE time_to_sec(timestamp) between  43200 and 64800");
        st.executeUpdate("Create view temp11 as SELECT songid,count(distinct(userid)) as count from temp1 group by songid having count>=3");
        st.executeUpdate("create view dummy1 as SELECT dataset.userid,dataset.songid FROM dataset, temp11 WHERE dataset.songid=temp11.songid and MONTH(timestamp)in(1,2,3)");
        st.executeUpdate("create view ss1 as select userid,songid,count(songid) as count from dummy1 group by userid,songid");
////        st.executeUpdate("create view workingset as select * from ss1 where userid in ( "+trainingString+ ")");
////        st.executeUpdate("create view testset as select * from ss1 where userid  in ("+testString+")");
      st.executeUpdate("create view workingset as select * from ss1 where userid in ('user_000027','user_000037','user_000046','user_000018','user_000030','user_000004','user_000012','user_000045','user_00034','user_000041','user_000011','user_000043','user_000014','user_000025','user_000016','user_000009','user_000021','user_000040','user_000002','user_000039','user_000008','user_000042','user_000019','user_000035','user_000044','user_000015','user_000026','user_000032','user_0\n" +"00033','user_000047','user_000017','user_000010','user_000031','user_000036','user_000023')");
        st.executeUpdate("create view testset as select * from ss1 where userid  in ('user_000001','user_000003','user_000005','user_000006','user_000007','user_000013','user_000020','user_000022','user_000024','user_000028','user_000029','user_000038','user_00048','user_000049','user_000050')");
        }
        catch(Exception e)
      {
           System.out.println("Initialisation error: " + e.getMessage());
      }
    }
    public void deinitialisation()
    {
        try
        {
           st=con.createStatement();
           st.executeUpdate("drop view temp1");
           st.executeUpdate("drop view temp11");
           st.executeUpdate("drop view dummy1");
           st.executeUpdate("drop view ss1");
           st.executeUpdate("drop view workingset");
           st.executeUpdate("drop view testset");
        }
        catch(Exception e)
        {
             System.out.println("deinitialisation error: " + e.getMessage());           
        }
    }
    
   /*public void initialisation()
    {
        try{
        st=con.createStatement();
     
        st.executeUpdate("Create view temp11 as SELECT songid,count(distinct(userid)) as count from dataset group by songid having count>3");
        st.executeUpdate("create view dummy1 as SELECT dataset.userid,dataset.songid FROM dataset, temp11 WHERE dataset.songid=temp11.songid");
        st.executeUpdate("create view ss1 as select userid,songid,count(songid) as count from dummy1 group by userid,songid");
        st.executeUpdate("create view workingset as select * from ss1 where userid not in ('user_000036','user_000037','user_000038','user_000039','user_000040','user_000041','user_000042',"
                + "'user_000043','user_000044','user_000045','user_000046','user_000047','user_000048','user_000049','user_000050')");
        st.executeUpdate("create view testset as select * from ss1 where userid  in ('user_000036','user_000037','user_000038','user_000039','user_000040','user_000041','user_000042',"
                + "'user_000043','user_000044','user_000045','user_000046','user_000047','user_000048','user_000049','user_000050')");
      }
        catch(Exception e)
        {
             System.out.println("Initialisation error: " + e.getMessage());
        }
    }
    public void deinitialisation()
    {
        try
        {
           st=con.createStatement();
           st.executeUpdate("drop view temp11");
           st.executeUpdate("drop view dummy1");
           st.executeUpdate("drop view ss1");
           st.executeUpdate("drop view workingset");
           st.executeUpdate("drop view testset");
        }
        catch(Exception e)
        {
             System.out.println("deinitialisation error: " + e.getMessage());           
        }
    }
    
    */
    public User[] getUsers() {

        try {
               st = con.createStatement();
               rs=st.executeQuery("select count(distinct(userid)) as val from "+tableName);
              if(rs.next())  
              {
                usize=rs.getInt("val");
                users = new User[usize];
              }
               rs=st.executeQuery("select count(distinct(songid)) as val from "+tableName);
               System.out.println("user size"+usize);
               if(rs.next())
               {
                   ssize=rs.getInt("val");
                   songs = new Song[ssize];
                   c=new Cluster[ssize];
                   cs=new Cluster[ssize];
               }
                rs=st.executeQuery("select count(distinct(userid)) as val from "+testTable);
              if(rs.next())  
              {
                test_usize=rs.getInt("val");
                tpt=new int[test_usize];
                fpt=new int[test_ssize];   
              }
               rs=st.executeQuery("select count(distinct(songid)) as val from "+testTable);
               
               if(rs.next())
               {
                   test_ssize=rs.getInt("val");
               }
               
               max_clusters=ssize;
               testuser=new User[test_usize];
               testsongs=new Song[test_ssize];
               test_user_song=new double[test_usize][test_ssize];
               user_songs = new double[usize][ssize];
               c = new Cluster[max_clusters];
                       
            st = con.createStatement();
            String query = "SELECT distinct userid as uid FROM "+tableName;
            rs = st.executeQuery(query);
            int i = 0;
             //System.out.println("Entering Loop"+query);
            while (rs.next() ) {
                users[i] = new User(rs.getString("uid"));
                umap.put(rs.getString("uid"), i);
            // System.out.println("iteration num "+i +"  "+ users[i].getUserId());
                i++;
            }

        } catch (Exception e) {
            System.out.println("user error: " + e.getMessage());

        }
        System.out.println("No of songs in consideration :  "+ssize);
        return users;
    }

    public Song[] getSongs() {
        try {
            st = con.createStatement();
            String query = "SELECT distinct songid FROM "+tableName;
            rs = st.executeQuery(query);
            int i = 0;
            while (rs.next()) {
                songs[i] = new Song(rs.getString("songid"));
                smap.put(rs.getString("songid"), i);
               // System.out.println("iteration num"+i + songs[i].getSongId());
                //songs[i]=new StringBuffer(rs.getString("song_id"));
                i++;
            }

        } catch (Exception e) {
            System.out.println("songs errror : "+e.getMessage());
        }
        return songs;
    } 
    
    public void generateUserSongMatrix() {
    long  g=0;
      System.out.println("Status : started generating usersong Matrix................");
                try {
                    st = con.createStatement();
                    String query = "SELECT * FROM "+tableName;
                    rs = st.executeQuery(query);
                    System.out.println("Status :  generating usersong Matrix in progress................");
                   
                    while (rs.next()) {
                        int uindex=0,sindex=0;
                        uindex=umap.get(rs.getString("userid"));
                        sindex=smap.get(rs.getString("songid"));
                        user_songs[uindex][sindex]= normalize(rs.getInt("count")); 
                  
                    }
                } catch (Exception e) {
                    System.out.print("user song matrix error :"+e.getMessage());
                }
			System.out.println("Status : completed generating usersong Matrix................");
}
  
    
    public void displayUserSongMatrix() {
        System.out.println("User Song Matrix.........");
        System.out.println();
        System.out.print("\t");
        for (int k = 0; k < songs.length; k++) 
		{
            System.out.print(songs[k].getSongId());
        }

        for (int i = 0; i < users.length; i++) {
            System.out.print(users[i] + "\t");
            for (int j = 0; j < songs.length; j++) {

                System.out.print(user_songs[i][j] + "\t");
            }
            System.out.println();
        }

    }

    public void storeUserSongMatrix() {
        try
        {
        Statement st1;
        try {
            st1 = con.createStatement();
            st1.executeUpdate("delete from user_songs");

        } catch (SQLException ex) {
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }

      System.out.println("Status : storing USERSong Matrix is in progress.............");
     int val=0,i,j;
      for ( i = 0; i < users.length; i++) {
            for ( j = 0; j < songs.length; j++) {
                try {
                    st = con.createStatement();
                     val = st.executeUpdate("INSERT into user_songs VALUES('" + users[i].getUserId() + "','" + songs[j].getSongId() + "'," + user_songs[i][j] + ")");
                } 
                catch (Exception ex) {
                    System.out.println("the value of val is : "+val+"\n"+users[i].getUserId()+"\n"+songs[j].getSongId()+ "\n"+ user_songs[i][j]);
                    System.out.println("Error encountered : "+ex.getMessage());
                    Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        }
        catch(Exception e)
        {
             System.out.println("Error in Storing US matrix : "+e.getMessage());
             System.out.println(e.getStackTrace());
        }
    }   
     
   /*                  // code using data base  
    public int[] fetchUserSongs(String song) {
              int[] userid_songs = new int[usize];
             System.out.println("Check point 1");
                for (int j = 0; j < users.length; j++) {
                    try {
                         st = con.createStatement();
                         String query = "SELECT count as value FROM "+tableName+" WHERE songid='" + song + "'and userid= '" + users[j].getUserId() + "'";
                         rs = st.executeQuery(query);
                         if (rs.next()) {   
                         userid_songs[j] = rs.getInt("value");
                        } else {
                                 userid_songs[j] = 0;
                                }
                     } catch (SQLException ex) {
                System.out.print("userid song matrix");
            }
        }
                System.out.println("Song_Vector : "+song);
                
                
                for(int j=0;j<users.length;j++)
                {
                    System.out.print(userid_songs[j]+"  ");
                }
                for(int j=0;j<users.length;j++)
                {
                    System.out.print(user_songs[j][z]);
                }
                z++;   
        return userid_songs;
    }*/
    
    public void printMatrix(Matrix A)
    {
        double temp[][]=A.getArray();
        for(int i=0;i<temp.length;i++)
        {
            for(int j=0;j<temp[i].length;j++)
            {
                System.out.print("  "+temp[i][j]);
            }
            System.out.println("");
        }
    }
    public void applySVD()
    {
        try{
       User_Song_Matrix=new Matrix(user_songs);
       SingularValueDecomposition SvdObj=new SingularValueDecomposition(User_Song_Matrix);
       
      //  System.out.println("\n Row  :"+User_Song_Matrix.getRowDimension()+" \n colm :  "+User_Song_Matrix.getColumnDimension());
        //         System.out.println("rank of matrix A is  :  "+User_Song_Matrix.rank()+"\nsvd obj rank  :  "+SvdObj.rank()); 

       int M=User_Song_Matrix.getRowDimension();
       int N=User_Song_Matrix.getColumnDimension();
      //  printMatrix(User_Song_Matrix);
        //System.out.println("Matrix U  : ");
       U=SvdObj.getU().getMatrix(0, M-1, 0, 2-1);
       // System.out.println("\nColms  :  "+U.getColumnDimension()+"\nRows   :  "+U.getRowDimension());
       // printMatrix(U);
        //System.out.println("Matrix S  :");
//        S=SvdObj.getS();
//        System.out.println("\nCols of S  : "+S.getColumnDimension()+"\nRows  :  "+S.getRowDimension());
//        printMatrix(S);
      
        V=SvdObj.getV().getMatrix(0, N-1, 0, 2-1);
//   VT= V.transpose();
        Song_SVD=V.getArray();
   for(int i=0;i<N-1;i++)
   {
       for(int j=0;j<2-1;j++)
       {
          // System.out.println("Before Normalization"+Song_SVD[i][j]);
            Song_SVD[i][j] =normalize(Song_SVD[i][j]);
           // System.out.println("After Normalization"+Song_SVD[i][j]);
       }
               
   }
       // System.out.println("Matrix V  :");
        //System.out.println("\nColms  :  "+V.getColumnDimension()+"\nRows  :  "+V.getRowDimension());
        //printMatrix(V);
        }
        catch(Exception e)
        {
            System.out.println("Exception in Applying SVD  :  "+e.getMessage());
        }
    }
    public void generateSongClusters_SVD(double tempthreshold)
    {
       try{
           threshold=tempthreshold;
          cs[0]=null;
          cs[0] = new Cluster(ssize,2);
          cs[0].addSongToCluster(songs[0]);
          cs[0].setMean(fetchSong_SVD(0));

        for (int j = 1; j < songs.length; j++)
        {
            double[] temprow = fetchSong_SVD(j);
            int flag = 0;
            double s=0,tmpclstno=0,tmpthr=0;
            for (int k = 0; k < ns; k++)
            {
                for(int x=0;x<ns;x++)
                 {
                     
                   //s = coSimlarity(temprow, cs[x].getMean());
                   double d =euclidean(temprow, cs[k].getMean())+1;
                   s=(1/d);
                     //System.out.println("Similarity : "+s+"  ");
                    if(tmpthr<=s)
                    {
                     tmpthr=s;
                     tmpclstno=x;
                    }
               }
                tempcount++;
              //  System.out.println("Similarity : "+s+"  ");
                
                     if (tmpthr >= threshold)
                     {
                        cs[(int)tmpclstno].addSongToCluster(songs[j]);
                        cs[k].calculateMean();
                        flag = 1;
                        break;
                    }
            }
//System.out.println("count : "+tempcount+"  ");
            if (flag == 0) {
                cs[ns]=null;
                cs[ns] = new Cluster(ssize,2);
             //   c[ns].setsize();
                cs[ns].addSongToCluster(songs[j]);
               // System.out.println("\n song added to new cluster");
                cs[ns].setMean(fetchSong_SVD(j));
                ns++;
                //System.out.println(ns);
            }
        }
     }
      catch(Exception e)
      {
          System.out.println("Error in featching :"+e.getMessage());
          System.out.println(e.getStackTrace());
      }
    }
    /* Calculating Euclidean Distance*/
    public double euclidean(double a[],double b[])
    {
        double res=0;
        try
        {
           a[0]=Math.abs(a[0]);
           b[0]=Math.abs(b[0]);
           a[1]=Math.abs(a[1]);
           b[1]=Math.abs(b[1]);
           
           res=Math.sqrt(Math.pow((b[0]-a[0]), 2)+Math.pow((b[1]-a[1]), 2));
          // System.out.println("similarity is"+res);
        }
        catch(Exception e)
        {
            System.out.println("Exception in Euclidean Distance  :  "+e.getMessage());
        }
        return res;
    }
    
   public int[] fetchTestUserSongs(String song,int index)
   {
     int[] userid_songs = new int[usize];
        try{
               // System.out.println("Song_Vector : "+song);
                  //System.out.println();
                  
                for(int j=0;j<users.length;j++)
                {
                    userid_songs[j]=(int)test_user_song[j][index];
                   // System.out.print(userid_songs[j]+"  ");
                }
        }
        catch(Exception e)
        {
            System.out.println("Exception in fetchTestUserSongs method  : "+e.getMessage());
        }
        return userid_songs;  
   }
   
   public int[] fetchTestUser(String user,int index)
   {
     int[] userid_songs = new int[test_ssize];
        try{
                for(int j=0;j<testsongs.length;j++)
                {
                    userid_songs[j]=(int)test_user_song[index][j];
                }
        }
        catch(Exception e)
        {
            System.out.println("Exception in fetchingTestUser method  : "+e.getMessage());
        }
        return userid_songs;  
   }
   
   public double[] fetchSong_SVD(int index)
   { 
      double[] temp={Song_SVD[index][0],Song_SVD[index][1]};
               
       return temp;
   }
    // fetching song vectors from the user_songs  matrix
    public int[] fetchUserSongs(String song,int index) {
            int[] userid_songs = new int[usize];
        try{
             for(int j=0;j<users.length;j++)
                {
                    userid_songs[j]=(int)user_songs[j][index];
                    //System.out.println( user_songs[j][index]);
                }
        }
        catch(Exception e)
        {
            System.out.println("Exception in fetching method  : "+e.getMessage());
        }
        return userid_songs;
    }
  
    public int[] fetchUser(String user,int index)
    {
        int[] tempuser=new int[ssize];
        try{
            for(int i=0;i<ssize;i++)
                tempuser[i]=(int)user_songs[index][i];
        }
        catch(Exception e)
        {
            System.out.println("Exception in FetchUser  :  "+e.getMessage());
        }
         return tempuser;
    }
    /*.................................................................................................................
         public int[] fetchUserSongs(String song) {
              int[] userid_songs = new int[usize];
             try{
                    System.out.println("Check point 1");
              
                    st = con.createStatement();                      
                    String query = "select * from "+tableName;
                    rs = st.executeQuery(query);
                        int j=0;
                        while(rs.next()&&j<usize)
                       {      
                            if(rs.getString("userid").equals(users[j].getUserId())&&rs.getString("songid").equals(song))
                              {
                                System.out.println(rs.getString("userid")+"   :"+users[j].getUserId());
                                System.out.println(rs.getString("songid")+"  :"+song);
                                userid_songs[j++]=rs.getInt("count");
                                System.out.println(rs.getString("userid")+"  :  "+users[j].getUserId()+"\t"+rs.getString("songid")+"   :  "+song);
                              }
                            else
                            {
                                userid_songs[j++]=0;
                            }
                        
                    }
                for(int k=0;k<users.length;k++)
                {
                    System.out.print(userid_songs[k]+"  ");
                }
              
             }
             catch(Exception e)
             {
                     System.out.println("Error in FetchUserSongs   :"+e.getMessage());
             }
                 return userid_songs;
         }
         /*
         */
   /*  ---------------------------------------------------------------------------------------------*/
 
    public double coSimlarity(int[] a, int[] b) {
        double numerator = 0, csimilarity=0;
        double denominator, denominator_x = 0, denominator_y = 0;
try{
        for (int i = 0; i < a.length; i++) {
            numerator += a[i] * b[i];
            denominator_x += Math.pow(a[i], 2);
            denominator_y += Math.pow(b[i], 2);
        }
        denominator = Math.sqrt(denominator_x) * Math.sqrt(denominator_y);

        if (denominator == 0) {
            csimilarity = 0;
        } else {
            csimilarity = numerator / denominator;
        }
     //   System.out.println("co similarrity "+csimilarity);

    }
    catch(Exception e)
    {
       System.out.println("error in coisine simin"+e.getMessage());
    }
        return csimilarity;
    }

    public double normalize(double data) {
        double nvalue=0;
        try{
        nvalue = (((data - omin) / (omax - omin)) * (nmax - nmin)) + nmin;
        }
        catch(Exception e)
        {
         System.out.println("error in coisine normalise"+e.getMessage());   
        }
        return nvalue;
    }

    public void createSongClusters() {
      try{
          cs[0]=null;
          cs[0] = new Cluster(ssize,usize);
          cs[0].addSongToCluster(songs[0]);
         // cs[0].setMean(fetchUserSongs(songs[0].getSongId(),0));

        for (int j = 1; j < songs.length; j++)
        {
            int[] temprow = fetchUserSongs(songs[j].getSongId(),j);
            int flag = 0;
            double s=0,tmpclstno=0,tmpthr=0;
            for (int k = 0; k < ns; k++)
            {
                for(int x=0;x<ns;x++)
        {
                //   s = coSimlarity(temprow, cs[x].getMean());
                    if(tmpthr<=s)
                    {
                     tmpthr=s;
                     tmpclstno=x;
                    }
                }
                     if (tmpthr >= threshold)
                     {
                        cs[(int)tmpclstno].addSongToCluster(songs[j]);
                        cs[k].calculateMean();
                        flag = 1;
                        break;
                    }
            }

            if (flag == 0) {
                cs[ns]=null;
                cs[ns] = new Cluster(ssize,usize);
             //   c[ns].setsize();
                cs[ns].addSongToCluster(songs[j]);
               // System.out.println("\n song added to new cluster");
               // cs[ns].setMean(fetchUserSongs(songs[j].getSongId(),j));
                ns++;
                //System.out.println(ns);
            }
        }
     }
      catch(Exception e)
      {
          System.out.println("Error in featching :"+e.getMessage());
          System.out.println(e.getStackTrace());
      }
        dispalyCluster(cs, ns);
    }

    public void createUserClusters(double thr)
    {
        try{
        threshold=thr;
        c[0]=new Cluster(ssize,usize);
        c[0].addUserToCluster(users[0]);
        c[0].addsongstousercluster(users[0], 0, user_songs, songs);
        c[0].setUserMean(fetchUser(users[0].getUserId(), 0));
        for(int i=1;i<usize;i++)
        {
            int[] tempuser=fetchUser(users[i].getUserId(), i);
            int flag=0;
            double s=0,tmpclstno=0,tmpthr=0;
            for(int j=0;j<nc;j++)
            {
                for(int x=0;x<nc;x++)
                {
                 s = coSimlarity(tempuser, c[x].getUserMean());
                 if(tmpthr<=s)
                 {
                  tmpthr=s;
                  tmpclstno=x;
                 }
                }
              //  System.out.println("The value of  s :  "+s);
                if(tmpthr>=threshold)
                {
                    c[(int)tmpclstno].addUserToCluster(users[i]);
                    c[(int)tmpclstno].addsongstousercluster(users[i], i, user_songs, songs);
                    c[(int)tmpclstno].calUseMean();
                    flag=1;
                    break;
                }
            }
            if (flag == 0) {
                c[nc]=null;
                c[nc] = new Cluster(ssize,usize);
                c[nc].addUserToCluster(users[i]);
                c[nc].addsongstousercluster(users[i], i, user_songs, songs);
                c[nc].setUserMean(fetchUser(users[i].getUserId(), i));
                nc++;
                //System.out.println(nc);
            }
        }
        showClusters(c, nc);
        }
        catch(Exception e)
        {
            System.out.println("Exception in Creating usercluster  :  "+e.getMessage());
        }
    }
    
    public void dispalyCluster(Cluster[] cluster,int numcls)
    {
        System.out.println("\n Cluster data...................");   
        for(int i=0;i<numcls;i++)
        {
            //System.out.println("Cluster no : "+ i);
            System.out.println("No of songe in cluster   "+i+"  is "+cluster[i].getNsongs());
                
        }
    }
    public void showClusters(Cluster[] c,int nc) {
            for(int i=0;i<nc;i++)
            {
                  System.out.println("Number of Songs in user Cluster  : "+i+"  are "+c[i].getusersongcount());
            }
       
        }

    public Song[] getcommonsongs(int ucindex,int scindex)
    {
        Song[] comnSongs=new Song[ssize];
        Song[] user =c[ucindex].getUserSongs();
        Song[] song =cs[scindex].getSongs();
        int kvalue=c[ucindex].getusersongcount();
        int jvalue=cs[scindex].songrefcount;
     //  commoncount=0;
      //  System.out.println("usercls.Lenght  : "+user.length+"  song.Length  : "+song.length+"  Kval : "+kvalue+"  jValue   : "+jvalue);;
        try
        {
           for(int k=0;k<kvalue;k++)
           {
               int i=c[ucindex].usersongreference[k];
               for(int j=0;j<jvalue;j++)
               {
                   if(!song[j].equals(null)&&!user[i].equals(null))
                   {
                   if(user[i].getSongId().equals(song[j].getSongId()))
                   {
                       comnSongs[commoncount++]=user[i]; 
                    //   System.out.println("Song added to common song list : "+comnSongs[count]+""+user[i]+" j value "+j);
                       break;
                   }
                  }
//                   else
//                   {
//                       System.out.println("Error occured at if loop : "+user[i].getSongId()+" "+song[j].getSongId()+" i and j values are  "+i+ " "+j);
//                   }
               }
           }
        }
        catch(Exception e)
        {
            System.out.println("Exception in commonsong :"+e.getMessage());
        }
        return comnSongs;
    }
    
public void getTestUsers()
{
    try{
         st = con.createStatement();
            // loading users into testuser array
            
            String query = "select distinct userid from "+testTable;
            rs = st.executeQuery(query);
            int i = 0;
            while (rs.next()) {
                testuser[i] = new User(rs.getString("userid"));
                tumap.put(rs.getString("userid"), i);
              //  System.out.println(testuser[i].getUserId());
                i++;
            }
    }
    catch(Exception e)
    {
        System.out.println("Error in Loading Test Users : "+e.getMessage());
    }
}

public void getTestSongs()
{
    try{
            int p=0;
            String   query = "select distinct songid from "+testTable;
            st=con.createStatement();
            rs = st.executeQuery(query);
            while (rs.next()) {
                testsongs[p] = new Song(rs.getString("songid"));
                tsmap.put(rs.getString("songid"), p);
              //  System.out.println(testsongs[p].getSongId());
                p++;
            }

    }
    catch(Exception e)
    {
        System.out.println("Error in Loading Test Songs : "+e.getMessage());
    }
}
public void generatingTestUserSongMatrix()
{
    try{
            st=con.createStatement();
            rs=st.executeQuery("select * from  "+testTable);
            
            int uindex=0,sindex=0;
            while(rs.next())
            {
                uindex=tumap.get(rs.getString("userid"));
                sindex=tsmap.get(rs.getString("songid"));
                test_user_song[uindex][sindex]=rs.getInt("count");
            }
            
     }
    catch(Exception e)
            {
                System.out.println("Error in Generating TestUSerSOng MAtrix : "+e.getMessage());
            }
}
 //----------------
public void recommendations_SVD()
{
    try {     
            usercount=0;
           getTestUsers();
           getTestSongs();
           generatingTestUserSongMatrix();
           avg=0;
           tpsum=0;
           fpsum=0;
           ravg=0;
           fnsum=0;           
            for(int u=0;u<test_usize;u++)
            {
                 int flag=0;
                for(int s=0;s<test_ssize;s++)
                {
                    if(test_user_song[u][s]!=0)
                    {
                        String song1=testsongs[s].getSongId();
                        int clusterNo=  mapToCluster(testsongs[s].getSongId());
                        //System.out.println("\nfirst Song listen by User : "+testuser[u].getUserId()+"  is  "+testsongs[s].getSongId()+" and he is mapped to the cluster : "+clusterNo+"\n");
                        tp=0;
                        fp=0;
                        fn=0;
                        if(clusterNo==-1)
                        {
                            continue;
                            //System.out.println("\n No Remommendations for  "+testuser[u].getUserId()+"\n");
                        }
                        else
                        {  
                         flag=1;
                         calcPN_SVD(u,s,clusterNo);
                         //System.out.println("\n For  "+testuser[u].getUserId()+"   The TruePositives : "+tp+"    The FalsePositives : "+fp);

                         System.out.println("\n For  "+testuser[u].getUserId()+"   The TruePositives : "+tp+"    The FalsePositives : "+fp+"  The FalseNegative : "+fn+"  "+" Precision : "+(tp/(tp+fp)));
                          usercount++;
                          temp=tp/(tp+fp);
                        // System.out.println("\n\n"+temp);
                         tpsum+=tp;
                          fpsum+=fp;
                          fnsum+=fn;
                          avg+=(double)(tp/(fp+tp));
                          ravg+=(double)(tp/(fn+tp));
                        }
                         break;
                    }
                }
                 if(flag==0)
                   System.out.println("No Recommendations of the user : "+testuser[u].getUserId());
            } 
            
          double p1=(tpsum/(tpsum+fpsum));
          double r1=(tpsum/(tpsum+fnsum));
          double f1=2*((p1*r1)/(p1+r1));
          double p2=avg/usercount;
          double r2=ravg/usercount;
          double f2=2*((p2*r2)/(p2+r2));
          System.out.println("The precision for tpsum fpsum is  "+p1);
          System.out.println("The Recall for tpsum fnsum is     " +r1);
          System.out.println("The fmeasure for tpsum fnsum is   " +f1);
          
          
          System.out.println("The Over all Presision for thrshold  "+threshold+"  is   : `avg : "+avg+",  : "+usercount+"   "+p2);
          System.out.println("The Over all Recall for thrshold  "+threshold+"  is      : `avg : "+ravg+", : "+usercount+"   "+r2);
          System.out.println("The Over all Fmeasure for thrshold  "+threshold+"  is    : "+usercount+"   "+f2);
        
        } catch (Exception e) {
            System.out.println("Error in Recommendatins method : " + e.getMessage());

        }
}
  public void recommendSongs() {      
        try {     
            usercount=0;
           getTestUsers();
           getTestSongs();
           generatingTestUserSongMatrix();
           avg=0;
           tpsum=0;
           fpsum=0;
            for(int u=0;u<test_usize;u++)
            {
                 int flag=0;
                for(int s=0;s<test_ssize;s++)
                {
                    if(test_user_song[u][s]!=0)
                    {
                        String song1=testsongs[s].getSongId();
                        int clusterNo=  mapToCluster(testsongs[s].getSongId());
                        //System.out.println("\nfirst Song listen by User : "+testuser[u].getUserId()+"  is  "+testsongs[s].getSongId()+" and he is mapped to the cluster : "+clusterNo+"\n");
                        tp=0;
                        fp=0;
                        fn=0;
                        if(clusterNo==-1)
                        {
                            continue;
                            //System.out.println("\n No Remommendations for  "+testuser[u].getUserId()+"\n");
                        }
                        else
                        {  
                         flag=1;
                         calcPN(u,s,clusterNo);
                         //System.out.println("\n For  "+testuser[u].getUserId()+"   The TruePositives : "+tp+"    The FalsePositives : "+fp);

                         System.out.println("\n For  "+testuser[u].getUserId()+"   The TruePositives : "+tp+"    The FalsePositives : "+fp+"  The FalseNegative : "+fn+"  "+" Precision : "+(tp/(tp+fp)));
                          usercount++;
                          temp=tp/(tp+fp);
                        // System.out.println("\n\n"+temp);
                         tpsum+=tp;
                          fpsum+=fp;
                          fnsum+=fn;
                          avg+=temp;
                        }
                         break;
                    }
                }
                 if(flag==0)
                   System.out.println("No Recommendations of the user : "+testuser[u].getUserId());
            }
             double res1=(tpsum+fpsum);
             double precision=tpsum /res1;
             double res2=(tpsum+fnsum);
             double recall=tpsum/res2;
             System.out.println("\n The Precision is  : ("+tpsum+")/("+tpsum+"+"+fpsum+")"+"   =   "+precision);
             System.out.println("\n Avg User Precision   :"+avg/usercount);
             System.out.println("\n The Recall is     : ("+tpsum+")/("+tpsum+"+"+fnsum+")"+"   =   "+recall);
             System.out.println("\n The Thresholg value is   :  "+threshold);
           
        } catch (Exception e) {
            System.out.println("Error in Recommendatins method : " + e.getMessage());

        }
    }

  public void Recomendations()
  {
      int i=0;
      Song[] commnSongs=null;
      try{
           getTestUsers();
           getTestSongs();
           generatingTestUserSongMatrix();
           avg=0;
           recmdcount=0;
          for(i=0;i<test_usize;i++)
          {
             int ucno=maptoUserCluster(testuser[i],i);
             int scno=maptoSongCluster(testuser[i],i);
             // System.out.println("User is "+testuser[i].getUserId()+"mapped to "+ucno+"user clsuter and "+scno+"song cluster");
              commnSongs=getcommonsongs(ucno,scno);
              if(commoncount!=0)
              calculatePN(i,commnSongs);
              commoncount=0;
          }
          commnSongs=null;
          System.out.println("The Over all Presision for thrshold  "+threshold+"  is   : `avg : "+avg+",  : "+recmdcount+"   "+avg/recmdcount);
      }
      catch(Exception e)
      {
         
          System.out.println("Exception in Recommendation : "+e.getMessage());
      }
  }
  
  public int maptoUserCluster(User user,int index)
  {
     // Song[] SFUC=new Song[ssize];
       int[] temp=fetchTestUser(user.getUserId(), index);
       int clsno=-1;
       double thrval=0;
      for (int i=0;i<nc;i++)
      {
         int[] clsmean=c[i].getUserMean();
         double s=coSimlarity(temp, clsmean);
         if(s>thrval)
         {
             clsno=i;
             thrval=s;
         }
      }
      //  SFUC=c[clsno].getUserSongs();
           return clsno;
  }
  public int maptoSongCluster(User user,int index)
  {
      int clsno=-1;
      for(int i=0;i<test_ssize;i++)
      {
          if(test_user_song[index][i]>0)
          {
              clsno=mapToCluster(testsongs[i].getSongId());
              if(clsno>0)
              usersongsref=cs[clsno].usersongreference.length;
          }
                      
      }
      return clsno;
  }
  public int mapToCluster(String song)
   {
       for(int i=0;i<ns;i++)
       {
         Song[] s= cs[i].getSongs();
            for (int j = 0; j < cs[i].getNsongs(); j++) {
                if(song.equals(s[j].getSongId()))
                {
                   return i; 
                }
            }  
       }
       return -1;
   }
   
  public void calculatePN(int u,Song[] s)
  {
     try
     {
        int totalcount=0;
         tp=0; fn=0;fp=0;
         recmdcount++;         
         for(int j=0;j<test_ssize;j++)
         {
            
            if(test_user_song[u][j]>0.0)
               {
                   totalcount++;
                   for(int k=0;k<commoncount;k++)
                   {
                       if(testsongs[j].getSongId().equals(s[k].getSongId()))
                       {
                           tp++;
                           break;
                       }
                   }
               }
         }
         System.out.print("Totalcount  :  "+totalcount+" commoncount  :   "+commoncount);
                 fn=totalcount-tp;
                 fp=commoncount-tp;
                 double temp=(tp/(fp+tp));
          System.out.println("User : "+testuser[u].getUserId()+"  TP : "+tp+"  FP : "+fp +" Precision   :  "+temp);
          avg+=(double)(tp/(fp+tp));
     }
     catch(Exception e)
     {
         System.out.println("Exception in CalculatePN  :  "+e.getMessage());
     }
  }
  
   public void calcPN(int u,int s, int clstno)
   {
       try{
           Song[] sng= c[clstno].getSongs();
           int clusize=c[clstno].getNsongs();
           int flag=0;
           int totalcount=0;
           for(int j=s+1;j<test_ssize;j++)
           {
               flag=0;
               if(test_user_song[u][j]>0.0)
               {
                   totalcount++;
                   for (int i = 0; i < clusize; i++) 
                   {
                     if(sng[i].getSongId().equals(testsongs[j].getSongId()))
                     {
                       tp+=1;
                       break;
                    //   flag=1;
                     }
                   }
                  // if(flag==0)
                    //   fp+=1;    
                  
               }
               
           }
           fn=totalcount-tp;
           fp=clusize-tp;
           //tpt[u]=tp;
           //fpt[u]=fp;
       }
       catch(Exception e)
       {
           System.out.println("Exception in calPN method : "+e.getMessage());
       }
   }
   
   public void calcPN_SVD(int u,int s, int clstno)
   {
       try{
           Song[] sng= cs[clstno].getSongs();
           int clusize=cs[clstno].getNsongs();
           int flag=0;
           int totalcount=0;
           for(int j=s+1;j<test_ssize;j++)
           {
               flag=0;
               if(test_user_song[u][j]>0.0)
               {
                   totalcount++;
                   for (int i = 0; i < clusize; i++) 
                   {
                     if(sng[i].getSongId().equals(testsongs[j].getSongId()))
                     {
                       tp+=1;
                       break;
                    //   flag=1;
                     }
                   }
                  // if(flag==0)
                    //   fp+=1;    
                  
               }
               
           }
           fn=totalcount-tp;
           fp=clusize-tp;
           //tpt[u]=tp;
           //fpt[u]=fp;
       }
       catch(Exception e)
       {
           System.out.println("Exception in calPN method : "+e.getMessage());
       }
   }
    
   
   /* original code before working for recall as on 7 dec
    *  public void calcPN(int u,int s, int clstno)
   {
       try{
           Song[] sng= c[clstno].getSongs();
           int clusize=c[clstno].getNsongs();
           int flag=0;
           for(int j=s+1;j<test_ssize;j++)
           {
               if(test_user_song[u][j]>0.0)
               {
                   for (int i = 0; i < clusize; i++) 
                   {
                     if(sng[i].getSongId().equals(testsongs[j].getSongId()))
                     {
                       tp+=1;
                       flag=1;
                     }
                   }
                   if(flag==0)
                       fp+=1;    
                   
               }
           }
           //tpt[u]=tp;
           //fpt[u]=fp;
       }
       catch(Exception e)
       {
           System.out.println("Exception in calPN method : "+e.getMessage());
       }
   }
  
   */ 
   
   public void showPrecision()
   {
       int n=0,m=0;
       try{
           System.out.println("Checkpoint 1");
       for(int i=0;i<test_usize;i++)
       {
           System.out.println("Checkpoint 2    "+test_usize);
           System.out.println(testuser[i].getUserId()+"   :  "+"TruePositives : "+tpt[i]+"  FalsePositives  : "+fpt[i]);
           n+=tpt[i];
           m+=fpt[i];
       }
       
       System.out.println("\n\nThe Precision Value  :  "+(n/(n+m)));
       }
       catch(Exception e)
       {
           System.out.println("Exception in ShowPrecision method :"+e.getMessage());
       }
   }
//--------------------
    
   /* 
    public void recommendSongs() {
        User[] testuser = new User[test_size];
        int flag;
        try {
            st = con.createStatement();
            st.executeUpdate("delete from recm_songs");
            String query = "select distinct userid from testdataset";
            rs = st.executeQuery(query);
            int i = 0;
            //System.out.println("Entering Loop");
            while (rs.next()) {
                testuser[i] = new User(rs.getString("userid"));
                //System.out.println("User Id : "+ users[i].getUserId());
                i++;
            }

            for (int j = 0; j < i; j++) {
                
                int[] temprow = fetchUserSongs(testuser[j].getUserId());
                flag = 0;

                for (int k = 0; k < nc; k++) {
                    double s = coSimlarity(temprow, c[k].getMean());

                    double s1 = normalize(s);

                    //System.out.println("checking cluster num " + k + " for test user num " + j +s1);
                    if (s1 >= threshold) {
                        flag = 1;
                        break;
                    }

                }

                if (flag == 0) {
                    System.out.println("Recommended songs for user id" + testuser[j].getUserId());
                    System.out.println("NO RECOMMENDED SONGS");

                } else {
                    
                    HashMap<String, Integer> recsongs = new HashMap<String, Integer>();

                    User[] clusterusers = c[flag].getUsers();
                    int nousers = c[flag].getNusers();

                    System.out.println("Recommended songs for user id" + testuser[j].getUserId());
                    for (int l = 0; l < nousers; l++) {
                        //System.out.println("Recommended songs for user id" + clusterusers[i].getUserId() + i);
                        Song[] rsongs = new Song[ssize];
                        rsongs = getTopSongs(clusterusers[l].getUserId());

                        for (int k = 0; k < getNumberOfSongs(clusterusers[l].getUserId()); k++)
                        {
                            if (rsongs[k].getSongId() != null) 
                            {
                                
                            //    System.out.println(rsongs[k].getSongId());
                               if (recsongs.containsKey(rsongs[k].getSongId())) 
                                {
                                    if (recsongs.get(rsongs[k].getSongId()) < getScount(clusterusers[l].getUserId(), rsongs[k].getSongId()))
                                    {
                                        recsongs.remove(rsongs[k].getSongId());
                                        recsongs.put(rsongs[k].getSongId(), getScount(clusterusers[l].getUserId(), rsongs[k].getSongId()));
                                        //System.out.println("in rec if ");
                                    }
                                    
                                } else {
                                    recsongs.put(rsongs[k].getSongId(), getScount(clusterusers[l].getUserId(), rsongs[k].getSongId()));
                                    System.out.println(rsongs[k].getSongId());
                                } 
                                
                                //RecSong song=new RecSong(rsongs[k].getSongId(),getScount(rsongs[k].getSongId(),clusterusers[l].getUserId()));
                                //testuser[j].addRecmSong(song);
                               
                                
                                
                                
                            }
                        }
                    }
                    
                    Iterator it=recsongs.entrySet().iterator();
                    
                    while(it.hasNext())
                    {
                        Map.Entry entry = (Map.Entry) it.next();

                        String key = (String)entry.getKey();

                        Integer val = (Integer)entry.getValue();
                        // System.out.println("key,val: " + key + "," + val);
                        String query1 = "insert into recm_songs values ('"+testuser[j].getUserId()+"','"+key+"',"+val+")";
                        st.executeUpdate(query1);
                        
                       // recsongs.remove(key);
                        it.remove();
                        
                        
                    }

                    //testuser[j].setRec_songs(recsongs);
                   

                }

            }

        } catch (Exception e) {
            System.out.println("test user error: " + e.getMessage());

        }
    }

    */
    public void showStats() {

        User[] testuser = new User[test_usize];
        //HashMap<String, Integer> rec_songs = new HashMap<String, Integer>();
        int max_rec=4;
        double flag, m, n = 0, sum = 0;
        double[][] p;
        double[] ap;

        try 
        {
            st = con.createStatement();
            String query = "select distinct userid from songcount";
            rs = st.executeQuery(query);
            int i = 0;
            //System.out.println("Entering Loop");
            while (rs.next()) 
            {
                
                testuser[i] = new User(rs.getString("userid"));
                // System.out.println("User Id : "+ users[i].getUserId());
                i++;
                
            }

            p = new double[i][max_rec];
            ap = new double[i];
            for(int row=0;row<i;row++)
            {
                ap[row]=0;
                for(int col=0;col<max_rec;col++)
                {
                    p[row][col]=0;
                }
            }
            
            
            for (int j = 0; j < i; j++) 
            {

                System.out.println("user :" +  (j+1) );

               String[] rec_songs=getRecommendedSongs(testuser[j].getUserId());
                
                //System.out.println("length is"+rec_songs.length+"user is "+testuser[j].getUserId());

                if ( rec_songs==null) 
                {
                    System.out.println(" no songs " );
                    continue;
                } 
                else 
                {

                    //ValueComparator bvc = new ValueComparator(rec_songs);
                    //TreeMap<String, Integer> sorted_songs = new TreeMap<String, Integer>(bvc);
                    //sorted_songs.putAll(rec_songs);

                    //String[] s = sorted_songs.keySet().toArray(new String[sorted_songs.size()]);

                    n = 0;

                    for (int k = 1; k <= max_rec; k++) 
                    {
                        if(k>rec_songs.length)
                        {
                              break;
                        }
                        m = 0;
                        
                        //System.out.println("k"+k);
                        for (int l = 0; l < k; l++) 
                        {
                            //System.out.println("l"+l);
                            if(l>=rec_songs.length)
                            {
                                break;
                            }
                            if (getScount(testuser[j].getUserId(), rec_songs[l]) > 0) 
                            {
                                m++;
                            }

                        }
                       // System.out.println("j="+j+"  k="+k+" m="+m);
                        p[j][k-1] = m / k;
                        n += p[j][k-1] * getScount(testuser[j].getUserId(), rec_songs[k - 1]);
                        
                        

                    }

                    ap[j] = n / max_rec;
                    
                    
                    sum += ap[j];

               

                for (int x = 0; x < i; x++) {
                    for (int y = 1; y <= max_rec; y++) {
                        System.out.print(p[x][y-1]+" ");
                    }
                    System.out.println();
                }

                    System.out.println("Average precision is "+ap[j]);
                }
                 
                 
            }
            double map = sum / i;

                System.out.println("mean average precision is" + map);
        } catch (Exception e) {
            System.out.println("stats error");
             e.printStackTrace();
        }

    }
    
    public Song[] getTopSongs(String user_id)
    {
        Song[] songs = new Song[ssize];
        try {
            st = con.createStatement();
            String query = "select songid from "+tableName+" where scount>0 and userid = '" + user_id + "'";
            rs = st.executeQuery(query);
            int i = 0;
            //System.out.println("Entering Loop");
            while (rs.next()) {
                songs[i] = new Song(rs.getString("songid"));
                //System.out.println("iteration num"+i + users[i].getUserId());
                i++;

            }

        } catch (Exception e) {
            System.out.println("user top songs error: " + e.getMessage());

        }
        return songs;
    }

    public int getNumberOfSongs(String user_id) 
    {
        //Song[] songs=new Song[max_songs] ;
        int count = 0;
        try {
            st = con.createStatement();
            String query = "select count(songid) as count  from "+tableName+" where scount>0 and userid = '" + user_id + "'";
            rs = st.executeQuery(query);
            int i = 0;
            //System.out.println("Entering Loop");
            if (rs.next()) {
                count = rs.getInt("count");
                //System.out.println("iteration num"+i + users[i].getUserId());
                //i++;
            }

        } catch (Exception e) {
            System.out.println("user noof songs error: " + e.getMessage());

        }
        return count;
    }
    
    public int getScount(String user_id, String songid)
    {
        int count = 0;
        try {
            st = con.createStatement();
            String query = "select scount as count  from dataset10000  where userid = '" + user_id + "' and songid='"+songid+"'";
            
           //System.out.println(query);
            rs = st.executeQuery(query);
            int i = 0;
            //System.out.println("Entering Loop");
            if (rs.next()) {
                count = rs.getInt("count");
                //System.out.println("iteration num"+i + users[i].getUserId());
                //i++;
            }

        } catch (Exception e) {
            System.out.println("user getscount error: " + e.getMessage());

        }
        return count;
    }
    
    public String[] getRecommendedSongs(String user_id)
    {
        
        
        try {
            
            st = con.createStatement();
            String cquery = "select count(distinct song_id) as count   from recm_songs  where userid = '" + user_id + "' order by scount ";
            
            //System.out.println("in recommended songs"+cquery);
            rs = st.executeQuery(cquery);
            rs.next();
            int c=rs.getInt("count");
            
            if(c>0)
            {
                String query = "select distinct songid   from recm_songs  where userid = '" + user_id + "' order by scount";
                recm = new String[c];
                rs=st.executeQuery(query);
                int i = 0;
                //System.out.println("Entering Loop in songs");
                while(rs.next()) 
                {
                    recm[i]=rs.getString("song_id");
                   // System.out.println("rec iteration num"+i );
                    i++;
                }
            }
            
          return recm;
            
        } catch (Exception e) {
            System.out.println("user recm songs error: " + e.getMessage());

        }
        return null;
       
        
    }

    
}
class ValueComparator implements Comparator<String> {

    Map<String, Integer> base;

    public ValueComparator(Map<String, Integer> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.    
    @Override
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}
