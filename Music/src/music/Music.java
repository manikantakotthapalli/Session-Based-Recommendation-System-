package music;


public class Music { 

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        try{ 
             System.out.println("Code with random Selection - working with Jan by appling SVD");
             System.out.println("Status : Execution Started..............");
                 Methods obj = new Methods();
                 obj.createRandomStrings();
                 obj.deinitialisation();
                 obj.initialisation();
               //yella.anitha@gmail.com
                      System.out.println("Status : Instance of methods is created.................");
        
                          obj.getUsers();

                    System.out.println("Status : users loaded................");
                         obj.getSongs();

                    System.out.println("Status : songs loaded................");

                       obj.generateUserSongMatrix();

                     System.out.println("Status : userSong Matrix is Generated..........................");

                    obj.applySVD();
               for(double i=0.01;i<=0.01;i+=0.01)
                 {
                  
                     System.out.println("Status : Creating user clusters using svd..............");
                     
                      obj.generateSongClusters_SVD(i);
                  
                            
                    System.out.println("Status : Started recommendations");        
                         obj.recommendations_SVD();       
                      System.out.println("\n\n\n");
        }
                              obj.deinitialisation();  
        }
        catch(Exception e)
        {
             System.out.println("Main : Error encountered : "+e.getMessage());
             System.out.println(e.getStackTrace());
        }
              
    }
}
