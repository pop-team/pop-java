import popjava.annotation.POPClass;
import popjava.base.POPObject;

@POPClass(isDistributable = false)
public class TestInteger{
    
   public static void main(String[] args){
      Integer i1 = new Integer("localhost");
      
      if(!(((Object) i1) instanceof POPObject)){
          System.out.println("i1 is not a POPObject, verify start parameters");
          return;
      }
      
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
      
      
      int [] testArray = new int[]{1,2,3};
      int temp = i1.arrayChanger(testArray);
      System.out.println("19 == "+temp);
      System.out.println("First, should change! "+testArray[0]+", "+testArray[1]+", "+testArray[2]);
      temp = i1.arrayChanger2(testArray);
      System.out.println("19 == "+temp);
      System.out.println("Second, should not change! "+testArray[0]+", "+testArray[1]+", "+testArray[2]);
      
      System.out.println("Should be true "+i1.arrayChanger3(testArray));
   }
}
