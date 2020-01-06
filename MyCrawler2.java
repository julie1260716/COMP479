import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.net.MalformedURLException;
import java.net.URL;

public class MyCrawler2 {


	Document doc;
	int crawlDepth;
	int maxPages;
	int pageCounter;
	private String startingUrl;
	private String[] topics;
	private ArrayList<String> frontier;		// holds urls of sites that crawler deemed relevant
	private ArrayList<String> toRemove;		// holds urls of sites we want to REMOVE from frontier
	private ArrayList<String> toAdd;		// holds urls of sites we want to ADD to frontier
	private ArrayList<String> visited;  	// holds urls of sites already visited
	private ArrayList<String> toRead;  	// holds urls that are to be indexed
	private ArrayList<String> toExclude;	// holds names of sites we want to exclude
	private SPIMIIndexer indexer; 			// used when we get to scraping (text extraction)
	
	
// default constructor	
  public MyCrawler2() {	   	  
	
	  maxPages = 50;
	  pageCounter = 0;
	  startingUrl = "https://aitopics.org/search";  
	  topics = new String[] {"artificial", "intelligence"};
	  frontier = new ArrayList<>();
	  toRemove = new ArrayList<>(); 
	  toAdd = new ArrayList<>();
	  visited = new ArrayList<>();
	  toRead = new ArrayList<>();
	  indexer = new SPIMIIndexer(true);	
	  toExclude = new ArrayList<String>(Arrays.asList("facebook", 
			  										  "twitter", 
			  										  "linkedin",
			  										  "instagram",
			  										  "youtube"));	  
  }	
  
// initCrawl() : initially picks a URL from the seed set and adds to frontier 
// subsequent crawls will be more refined 
  public void initCrawl()
  {
	  indexer.processQuery("ai department");
	  
	  try {
		
		doc = Jsoup.connect(startingUrl).get();
		//add url to visited list
		visited.add(startingUrl);		
//printVisited();		
		Elements links = doc.select("a[href]");
		String linkUrl;

		  for(int i = 0; i < topics.length; i++) 
		  {
			  //if the link contains any of the topics we want
			  for(Element link : links)
			  {
				  linkUrl = link.attr("abs:href");
				  
				  //add url to frontier(pages to visit)
				  if(linkUrl.contains(topics[i]) && !frontier.contains(linkUrl))			  
				    frontier.add(linkUrl);  
			  }
		  }
		  //filters the current links in the frontier
		  URLfilter();	  		  
		  
		  //add to toRead
		  toRead.addAll(frontier);
		  
		  //now we move on to crawl(), where we refine our search more
		  crawl();
		  
		  System.out.println("max reached");
		  //when frontier has reached max size, stop crawling
		  URLfilter();
		  toRead.addAll(frontier);
		  
		  //pass to read docs to indexer
		  for(String doc : toRead)
			  indexer.readDoc(doc.toString());
			  //System.out.println(doc);
		  
		  System.out.println("all documents have been read");
		  
	} catch (IOException e) {
		System.out.println("URL not supported");
	} 
  }

//crawl() : performs a more thorough scraping, based on refined topic
// 			adds to frontier more relevant docs and removes less relevant 
  public void crawl()
  {	 
	  //perform crawl while frontier is not empty or while max size is not exceeded
	  while(!frontier.isEmpty() && frontier.size() < maxPages)
	  {
		  //System.out.println("PAGES DOWNLOADED : " + pageCounter);
		  
		  for(String url : frontier)
		  {
			// iterate of all urls in frontier 
			  try {  
					    doc = Jsoup.connect(url).get();
						//add url to visited list
						visited.add(url);							
						
						Elements links = doc.select("a[href]");
						String linkUrl;
					 
					  //if the link contains any of the topics we want
					  for(Element link : links)
					  {
						  linkUrl = link.attr("abs:href");	
						  
						  //add url to frontier, ensuring no duplicates
						  if(!frontier.contains(linkUrl) && linkUrl.contains("artificial") && !toAdd.contains(linkUrl)  )						  
							  toAdd.add(linkUrl);						  						  
					  }
					  			  				  
			  	} catch (IOException e) {
					System.out.println("URL not supported");
				} 
		  }
		  
		  //add/remove urls from frontier
		  frontier.clear();
		  frontier.addAll(toAdd);
		  //filters the current links in the frontier 
		  URLfilter();	  		

		  //testing
		  //printFrontier();
		  
		  //fetch page to extract more links and invert text
		  fetchPage();
	  }	   
  }
	
 // 1. fetches a page from the frontier 
 // 2. pass text and links to parser 
 // 3. extracted links are tested (URL filtering) to 
 // determine if they should be added to frontier 
 // 4. should delete the fetched page, add to visited
  public void fetchPage()
  {  	  
	  if(!frontier.isEmpty())
	  {
		//fetch first url in frontier
		  String thisUrl = frontier.get(0);
		  try {  
			  doc = Jsoup.connect(thisUrl).get();

			  Elements links = doc.select("a[href]");
			  String linkUrl;
				  			  
			  // extract links to potentially add to frontier
			  for(Element link : links)
			  {
				  linkUrl = link.attr("abs:href");	
				  
				  //add url to frontier, ensuring no duplicates
				  if(!frontier.contains(linkUrl) && !toAdd.contains(linkUrl))						  
				  {
					  //ai specific terms
					  if(linkUrl.contains("machine") || linkUrl.contains("data") || linkUrl.contains("ai"))	
					  	toAdd.add(linkUrl);						  						  
				  }
			  }
			  
			  // pass current page to parser to invert
			  toRead.add(thisUrl);
			  //indexer.readDoc(thisUrl);
			  			  			  				  
	  	} catch (IOException e) {
	  		System.out.println("URL not supported");
		}   

		  //remove this page from frontier, add to visited list
		  visited.add(thisUrl);
		  frontier.remove(thisUrl);
		  
		  //add new relevant urls
		  frontier.addAll(toAdd);
		  
		  //increment pageCounter
		  pageCounter++;
		 
		  //filters the current links in the frontier 
		  URLfilter();	  		
	
		  //testing
		  //printFrontier();
		  
		  //go back to crawling
		  crawl();
		 	  
	  } // end isEmpty
  }
	
//perform URL filtering, fetches URL from frontier and decides if it should be excluded
// **my version of RobotExclusion Protocol**
  public void URLfilter()
 {	  	  URL aUrl;   	
 
		  for(String url : frontier) {
			  
			try {
				//EXCLUSION: exclude urls that are not http or https protocols
				aUrl = new URL(url);
				
				if(aUrl.getProtocol().toString().compareToIgnoreCase("mailto") == 0)
				{	
					//System.out.println("bad URL");
					toRemove.add(url);
					visited.add(url);
				}
				
					
			} catch (MalformedURLException e) {				
				e.printStackTrace();
			} 	
			  
			  //EXCLUSION: we want to exclude urls from facebook, linkedIn, youtube, etc
			  if(isExcluded(url) || url.contains("explore.concordia"))
			  {			  
				  toRemove.add(url);
				  //System.out.println("\t the following URL was removed:\t" + url);
				  //add to visited list, we don't want to revisit
				  visited.add(url);  
			  }	    			    			  			  	  
		  }     
		  
		  frontier.removeAll(toRemove);
		 
 }

  public void updateFrontier()
  {
	  //removes/adds urls to be removed/added
	  frontier.removeAll(toRemove);
	  frontier.addAll(toAdd);
	  
	  //clear toRemove and toAdd ArrayList
	  toAdd.clear();
	  toRemove.clear();
  }

  // part of RobotExclusion, we don't want any of these sites  
  public boolean isExcluded(String url)
  {
	  for(String site : toExclude) 
	  {
		  if(url.contains(site) || url.contains("/fr/"))
			  return true;
	  }
	  
	  return false;
  }
  
// used for debugging purposes	
  public void printFrontier()
  {
	  System.out.println("\nCURRENT FRONTIER:");
	  for(String url : frontier)
		  System.out.println(url);
  }
  
  public void printVisited()
  {
	  System.out.println("\nCURRENT VISITED:");
	  for(String url : visited)
		  System.out.println(url);
  }
 
  public void rank()
  {

	  indexer.printDictionary();
	  
	  indexer.opakiRank(); //using BM25
	  indexer.tfIdfRank(); //using using tf-idf
  }
}
