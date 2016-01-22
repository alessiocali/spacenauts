package com.gff.spacenauts.desktop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTestHarness {

    public static void main(String[] args){
    	try{
        while (true) {
        	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        	System.out.format("%nEnter your regex: ");
            Pattern pattern = 
            Pattern.compile(reader.readLine());

            System.out.format("Enter input string to search: ");
            Matcher matcher = 
            pattern.matcher(reader.readLine());

            boolean found = false;
            while (matcher.find()) {
                System.out.format("I found the text" +
                    " \"%s\" starting at " +
                    "index %d and ending at index %d.%n",
                    matcher.group(),
                    matcher.start(),
                    matcher.end());
                found = true;
            }
            if(!found){
                System.out.format("No match found.%n");
            }
        }
    	}catch (IOException e){
    		e.printStackTrace();
    	}
    }
}