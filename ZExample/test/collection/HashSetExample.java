package test.collection;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

public class HashSetExample {

    public static void main(String[] args) {

     //  Set<String> collect = new HashSet<String>(10, (float) 0.8);
    //    Set<String> collect = new HashSet<String>();
//    Stack<String> collect = new Stack<String>();
      //Queue<String> collect = new LinkedList<>();
      Queue<String> collect = new  PriorityQueue<>(); 
        collect.add("One");
        collect.add("Two");
 
        
        
        collect.add("1");
        collect.add("One");
        collect.add("2");
        collect.add("Three");
        collect.add("3");
        collect.add("333");
        collect.add("4444");
        collect.add("55555");
        collect.add("666666");
       /* Iterator<String> it = collect.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }*/
        
        for (String element : collect) {
            System.out.print(element + "-->");
        }
    }

}
