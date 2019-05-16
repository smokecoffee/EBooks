package com.atson.commons.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.atson.commons.lang.FP;

public class TestFP {

    public static void main(String[] args) {
        
        ArrayList<String> fp1 = FP.arrayList();
        ArrayList<Sample> fp1_1 = FP.arrayList();
        
        ArrayList<String> fp2 = FP.arrayList(args);
        ArrayList<String> fp3 = FP.arrayList("0","1","2");
        
        Sample[] samples = {new Sample(), new Sample()};
        
        List<String> collection = Arrays.asList(args);
        ArrayList<String> fp4 = FP.arrayList(collection);
        
        List<Sample> colSamples = Arrays.asList(samples);
        ArrayList<Sample> fp_5= FP.arrayList(colSamples);
        
        FP.arrayList(new Sample(), 100);
        
        // Khai bao cac Data Structure
        FP.array(100L);
        FP.list(0L);
        FP.linkedList();

        FP.hashMap();
        FP.hashSet();
        
        FP.treeSet();
        
        FP.linkedHashSet();
        
        FP.readBoolean();
        
        
        
        
    }

}
