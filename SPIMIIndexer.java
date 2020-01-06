import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeMap;
import org.jsoup.Jsoup;//https://jsoup.org/cookbook/input/load-document-from-file
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.*;
import org.jsoup.select.Elements;

public class SPIMIIndexer {

	private int ARTICLE_MAX;
	private int TERM_MAX;
	private ArrayList<String> stopWords150 = new ArrayList<String>();	
	private int finalTermCounter = 0;
	boolean isCompressed;
	
	private static Map<String,ArrayList<String>> postingList = new TreeMap<String,ArrayList<String>>();
	
	/******************** PROJECT II VARIABLES ********************/
	
	private static Corpus webCorpus = new Corpus(); //create new corpus object
	private ArrayList<String> terms = new ArrayList<>(); //holds terms and their frequencies
	private boolean docContainT;
	private int tf = 0; //frequency of term in doc
	private int ld = 0; //length of document
	private int ldTotal = 0; //document length over entire corpus
	private int N = 0; //total number of docs in corpus
	private int n = 0; //number of docs that contain term
	BM25 opaki = new BM25();
	TfIdf tfidf = new TfIdf();
	
	public SPIMIIndexer (boolean _isCompress) {
		//initialize the max article size
		this.ARTICLE_MAX = 500;
		this.TERM_MAX = 25000;
		this.isCompressed =_isCompress;
		//want to open and load stop words
		File stop150 = new File("stopwords_150.txt");
		Scanner stopWordreader;
		try {
			//150 STOP WORDS
			stopWordreader = new Scanner(stop150);
			while(stopWordreader.hasNextLine())
			{
				//then we save into stop word list
				stopWords150.add(stopWordreader.nextLine());
			}
			//close
			stopWordreader.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String removeNums (String aText) {
		aText = aText.replaceAll("/", "");
		return aText.replaceAll("[0-9]", "");
	}
	
	private String removeStopwords (String aText) {
		//https://www.baeldung.com/java-string-remove-stopwords
		
		String[] allWords = aText.toLowerCase().split(" "); 
		StringBuilder builder = new StringBuilder();
		
		//if the text contains a stop word, we replace it with an empty string
		for(String word : allWords) {
	        if(!stopWords150.contains(word)) {
	            builder.append(word);
	            builder.append(' ');
	        }
	    }
		         
		String result = builder.toString().trim();
	
		/*//iterate over text and remove all stop words
		//for( String sw : stopWords150) {
			//want to replace with an empty string
			//System.out.println(sw);
			aText.replaceAll(" the ", ""); 
		//}		*/
		
		return result;
	}
	
	private String caseFold (String aText) {
		
		return aText.toLowerCase();
	}
	
	public void readDoc(String url) {		
		
		/*System.out.println("\treading document..");
		ArrayList<String> pages = new ArrayList<String>();
		pages.add("https://www.concordia.ca/research/spotlights.html");
		pages.add("https://www.concordia.ca/research/news/RHH.html?c=research&utm_source=slide1&utm_medium=cqc-carousel-RHH&utm_campaign=homepagebanner");
		pages.add("https://www.concordia.ca/research/news.html");
		pages.add("https://www.concordia.ca/next-gen/artificial-intelligence/research.html");
		pages.add("https://www.concordia.ca/next-gen/artificial-intelligence.html");
		pages.add("https://www.concordia.ca/news/stories/2019/11/13/25-concordia-students-join-ericssons-global-artificial-intelligence-accelerator.html?c=/research/news");
		pages.add("https://www.concordia.ca/next-gen/artificial-intelligence/teaching-and-learning.html");
		pages.add("https://www.concordia.ca/next-gen/artificial-intelligence/faculty-members.html");
	*/	StringTokenizer docTokens;
		Document doc;
	
		try {
		
			// load up web document
			doc = Jsoup.connect(url).get();
			
			String text = doc.body().text();		
			//System.out.println(text);
			
			// clean, compress text
			text = cleanText(text);			
			if(isCompressed) {						
				// remove stop words, numbers & case fold									
				text = caseFold(removeNums(removeStopwords(text)));				
			}	
			
			// create tokens..		
			docTokens = new StringTokenizer(text);
			String nextToken;				
			
			while (docTokens.hasMoreTokens()) 	
			{
				//System.out.println("Reading tokens..");
				nextToken = docTokens.nextToken();
				
				// UPDATE RANKING VARIABLES					
				//increment size of given document
				ld ++;				
				//increment total documents size 
				ldTotal += ld;
				//increment total number of documents
				N++;
								
				if(terms.contains(nextToken))
				{									
					//update docToTerm and docLengths
					webCorpus.addDocToTerm(url, nextToken, ld);	
					
					//System.out.println(webCorpus.getMap().toString());
					
					docContainT = true;									
				}
				else
					docContainT = false;
				
				addToPostingList(postingList, nextToken, url);   										
			
				// UPDATE NUM DOCS CONTAINING TERM			
				if(docContainT) 				
					webCorpus.updateNumDocsT(nextToken);
			
			}// end of doc tokens
						
			// set variables in corpus
			webCorpus.setAvDocLength(ldTotal, N);
			webCorpus.setTotalDocs(N);
					
			
			//*****TESTING*****//
			//webCorpus.getInfo(url, "artificial");				
			//printing out posting list
			//System.out.println(postingList.toString());
			
						
		} catch (IOException e) {
			System.out.println("empty text");
		} 
	}	
		

//**************P2 FUNCTIONS**************//
	
	public void opakiRank() {opaki.computeRank(); }
	
	public void tfIdfRank() {tfidf.computeRank(); }
	
	public int calculateSize(String doc) {
	doc = cleanText(doc);
	StringTokenizer docTokens = new StringTokenizer(doc);
	return docTokens.countTokens();
	}

	public void processQuery(String query) {
	
	System.out.println("\tprocessing query..");
	query = cleanText(query).toLowerCase();
	
	String[] queryWords = query.split(" ");
	String stemmedWord;
	
	for(int i = 0; i < queryWords.length; i++)
	{
		//stem
		stemmedWord = stem(queryWords[i]);
		//add to terms arraylist
		terms.add(stemmedWord);
		
	}		
}

	public String stem(String aWord) {
	aWord = aWord.replaceAll("ies ", "y ");
	aWord = aWord.replaceAll("s", "");
	return aWord;
}
//**************QUERY FUNCTIONS**************//
	
	public void AND(String query) {
		
		query = cleanText(query);
		String result = "";
		ArrayList<String> andPostings = new ArrayList<String>();
																				//dog [1, 3, 10]				
		//delimit by whitespace													//cat [2, 4, 20]				
		String[] queryWords = query.split(" ");
		
		for(int i = 0; i < queryWords.length; i++) {
			//System.out.println(queryWords[i]);
			if(postingList.containsKey(queryWords[i]))
			{
				result = "not_empty";
				//put into andPostings
				andPostings.addAll(postingList.get(queryWords[i]));
			}
		}	
		//https://www.geeksforgeeks.org/count-occurrences-elements-list-java/
		// hashmap to store the frequency of element 
        Map<String, Integer> hm = new HashMap<String, Integer>(); 
  
        for (String i : andPostings) { 
            Integer j = hm.get(i); 
            hm.put(i, (j == null) ? 1 : j + 1); 
        } 
        System.out.println("\nRetreiving results...");
        if(result != "") 
        {
	        int nothing = 0;
	        
        	for (Map.Entry<String, Integer> val : hm.entrySet()) { 		
        		
	        	// if a docId appears x times, then we add to result
	        	if(val.getValue().intValue() == queryWords.length)
	        	{	
	        		nothing++;
	        		if(nothing == 1)
	        			System.out.println("\nThe following documents matched your query: ");
	        		
	        		System.out.print(val.getKey().toString()+", "); 	       
	        	}	        	
	        } 
		}
        else
			System.out.println("\nSorry, no documents matched your query..");    	 				
	}
	
	public void OR(String query) {
		
		String result = "";
		query = cleanText(query);
		String[] queryWords = query.split(" ");
		
		//now we need to search the index and output doc Ids
		System.out.println("\nRetreiving results...\n");
		for(int i = 0; i < queryWords.length; i++)
		{
			System.out.println(queryWords[i] + ": ");
			if(postingList.containsKey(queryWords[i]))
			{
				//add to results set 
				result = "not_empty";
		        System.out.print(postingList.get(queryWords[i]));
			}
		}	
		
		if(result == "")
			System.out.println("\nSorry, no documents matched your query..");    	        	    
	}

//**************HELPER FUNCTIONS**************//
	public String cleanText(String text) {
		//removing < >
		String s1 = text.replaceAll("<","");
		String s2 = s1.replaceAll(">","");
		//removing -
		String s3 = s2.replaceAll("-", " ");
		//removing ,
		String s4 = s3.replaceAll(",", "");
		//removing '
		String s5 = s4.replaceAll("'", "");
		//removing ( )
		String s6 = s5.replaceAll("[()]", "");
		//removing "
		String s7 = s6.replaceAll("\"","");
		//removing 'reuter'
		String s8 = s7.replaceAll("(?i)reuter", "");
		//removing . 
		String s9 = s8.replaceAll("\\.", "");
		//removing + 
		String s10 = s9.replaceAll("\\+", "");
		//removing & 
		String s11 = s10.replaceAll("&", "");
		//remove :
		String s12 = s11.replaceAll(":", "");  
		//remove }
		String s13 = s12.replaceAll("}", "");  
		//remove $
		String s14 = s13.replaceAll("\\$", "");  
		//remove ^
		String s15 = s14.replaceAll("\\^", "");  
		//remove []
		String s16 = s15.replaceAll("\\[", "");  
		String s17 = s16.replaceAll("\\]", ""); 
		//remove ;
		String s18 = s17.replaceAll(";", ""); 
		//remove *
		String s19 = s18.replaceAll("\\*", ""); 
		//remove _
		String s20 = s19.replaceAll("_", ""); 
		//remove ?
		String s21 = s20.replace("?", ""); 
		//remove =
		String s22 = s21.replaceAll("=", ""); 
		String s23 = s22.replaceAll(" and ", ""); 
		return s22;
	}
	
	private void writeBlockToDisk(Map<String,ArrayList<String>> map, PrintWriter pw) {	
		for (Map.Entry<String, ArrayList<String>> term : map.entrySet()) {
			//write the term and its postings
			pw.println(term.getKey() + " " + term.getValue().toString() );
		}
	}
	
	private void addToPostingList(Map<String,ArrayList<String>> map, String term, String docId) {
		
		//check if term is NOT already present
		if(!map.containsKey(term))
		{
			//then we can add it to the dictionary/postingList as a NEW TERM
			map.put(term, new ArrayList<String>());
			//add the docId for that term
			map.get(term).add(docId);
			//increment final term counter (if at merging stage)
			if(finalTermCounter != -1)
				finalTermCounter++;
		}
		else //this means that it exists, and we need to update the postings list
		{
			//add the docId if it is not already there
			if(!map.get(term).contains(docId))
				map.get(term).add(docId);
		}				
	}
	
	public void printDictionary() { //used for debugging purposes
		/*//printing postings list (term: docIds)
		for (Map.Entry<String, ArrayList<String>> term : postingList.entrySet()) {
		    System.out.println(term.getKey() + ":" + term.getValue().toString());
		}*/
		
		//System.out.println("ConcordiaAI");
		//webCorpus.getMap();
	}
	
	public void mergeIndexes() {
		
		String diskPath = System.getProperty("user.dir") + "/DISK/";
		File diskFolder = new File(diskPath);
		
		FileReader reader = null;
		File[] diskFiles = diskFolder.listFiles();
		
		String outputPath = System.getProperty("user.dir") + "/FINAL_INDEX";
		File directory = new File(outputPath);
		boolean created = directory.mkdir();
		
		int blockCounter = 0;
		finalTermCounter = 0;

		for(int i = 0; i < diskFiles.length; i++) {
			try {
				//create file reader and array to hold chars
				reader = new FileReader(diskFiles[i]);
				char[] charsIn = new char[(int)diskFiles[i].length()];
				reader.read(charsIn);
				reader.close();
				
				//extract terms & postings
				String content = new String(charsIn);

				//remove []
				String s1 = content.replaceAll("\\[", "");  
				String s2 = s1.replaceAll("\\]", ""); 
				//splitting based on new line
				String[] splits = s2.split("\\n");
				
				//we want to merged into two blocks of 25000 terms
//				if(blockCounter < 2) {
					
					//extract term and respective postings + some processing..
					for(String line : splits)
					{	
						//write to disk if term count = max
						if (finalTermCounter < TERM_MAX)
						{
							//will split line into 2 parts based on first space
							String aPosting[] = line.split(" ", 2);
							
							//first index holds the term
							String aTerm = aPosting[0];
							//second index holds the docIds
							String aDocList = aPosting[1];							
							
							//extract each docId,  add to arrayList
							String[] ids = aDocList.split(",");
							
/*							//ArrayList<String> docIds = new ArrayList<String>();
							for(String id : ids)
							{
								//add to final index 
								addToPostingList(finalIndex, aTerm, id);
								//want to add to final merged index (querying)
								addToPostingList(mergedIndex, aTerm, id);
							}
*/								
						}					
						else {
							//sort & write to disk
							System.out.println("Writing to disk...");
							System.out.println("Term count is: " + finalTermCounter);
							
							String outputFilename = "BLOCK" + (blockCounter+1) + ".txt";			
							File outPath = new File(System.getProperty("user.dir") + "/FINAL_INDEX/" + outputFilename);
							FileWriter fileWriter = new FileWriter(outPath);
							PrintWriter printWriter = new PrintWriter(fileWriter);
/*							writeBlockToDisk(finalIndex, printWriter);
							printWriter.close();
							blockCounter++;

							//reset the term counter
							finalTermCounter = 0;
							//clear final index
							finalIndex.clear();							
	*/					}		

					} //end of for loop (line in splits)
					
//				}
			}catch (IOException e) {
				e.printStackTrace();			
			}//end of try/catch
		}//end of forloop through disk files	
		
//		System.out.println("CURRENT FINAL INDEX");
//		System.out.print(finalIndex.toString());
		
	}


}//end of SPIMIIndexer
