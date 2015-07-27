import popjava.annotation.POPClass;

@POPClass(isDistributable = false)
public class TestInteger{
    
   public static void main(String[] args){
      Integer i1 = new Integer("localhost");
      Integer i2 = new Integer("localhost");
      //Create an array 
      Integer[] tab = new Integer[10];
      //Initialize the array
      for (int i=0; i<tab.length;i++){
         tab[i] = new Integer("localhost");
      }
      i1.set(11);
      i2.set(14);
      System.out.println("i1="+i1.get());
      System.out.println("i2="+i2.get());
      i1.add(i2);
      System.out.println("i1+i2="+i1.get());
   }
}
