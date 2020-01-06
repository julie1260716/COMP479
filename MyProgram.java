/*COMP 479 - Project II | Julie Merlin 40007795*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class MyProgram {
	
	
	
	public static void main(String[] args) {	
		
		System.out.println("creating web crawler...");
		
		MyCrawler spider = new MyCrawler();
		
		
		spider.initCrawl();
		
		spider.rank();
			
		
		
		
	}//end of main()

}//end of MyProgram
