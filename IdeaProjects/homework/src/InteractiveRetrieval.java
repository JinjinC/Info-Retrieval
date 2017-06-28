/**
 * Created by Jinjin on 2017-03-27.
 */
import java.io.*;
import java.util.*;

public class InteractiveRetrieval {
    private static Hashtable lexicon = new Hashtable();
    private static Hashtable docIDtoNo = new Hashtable();
    private static ArrayList<String> position = new ArrayList<String>();
    private static Hashtable headline = new Hashtable();
    private static Hashtable<String, String> docLength = new Hashtable();
    private static ArrayList<String> queryWords = new ArrayList<String>();
    private static ArrayList<String> retrievedResult = new ArrayList<String>();
    private static int colLength = 0;

    public static void showResults(String path, String query) throws IOException{

        ArrayList<String> results = BM25Retrival(path, query);
        for (int r = 0; r < results.size(); r++) {
            String summary = SentenceBreaker(results.get(r));

            String docid = results.get(r);
            String docno = (String) docIDtoNo.get(docid);

            //get the doc
            String date = docno.substring(2, 8);
            String yy = date.substring(4, 6);
            String mm = date.substring(0, 2);
            String dd = date.substring(2, 4);
            String Date = mm + "/" + dd + "/" + yy;

            //for print out
            String headl = (String) headline.get(docid);
            if (headl.equals("no headlines")) {
                headl = summary.substring(0, 50) + "...";
            }

            String rank = Integer.toString(r + 1);
            String RHD = rank + ". " + headl + "(" + Date + ")";
            System.out.println(RHD + "\n" + summary + "(" + docno + ")");

        }
    }

    public static String SentenceBreaker(String result) throws IOException{
        //for each result
        Hashtable<String,Integer> scoredSentences = new Hashtable();

        String docid = result;
        String docno = (String) docIDtoNo.get(docid);

        //get the doc
        String date = docno.substring(2, 8);
        String yy = date.substring(4, 6);
        String mm = date.substring(0, 2);
        String dd = date.substring(2, 4);
        String docPath = yy + "/" + mm + "/" + dd;

        //for print out
        FileInputStream fin = new FileInputStream("out" + "/" + docPath + "/" + docno + ".txt");
        InputStreamReader xover = new InputStreamReader(fin);
        BufferedReader br = new BufferedReader(xover);
        boolean startHeadline = false;
        boolean startText = false;
        String docH = "";
        String docT = "";
        String line;
        while(( line = br.readLine()) != null){

            //start and stop record the Headline, TEXT and GRAPHIC
            if (line.equals("<HEADLINE>")) {
                startHeadline = true;

            } else if (line.equals("</HEADLINE>")) {
                startHeadline = false;
            }
            //start Record Headline
            if (startHeadline) {
                if (!line.equals("<HEADLINE>") && !line.equals("</HEADLINE>")
                        && !line.equals("<P>") && !line.equals("</P>")) {
                    docH = docH + line;
                }
            }
            if (line.equals("<TEXT>") || line.equals("<GRAPHIC>")) {
                startText = true;

            } else if (line.equals("</TEXT>") || line.equals("</GRAPHIC>")) {
                startText = false;
            }
            if (startText) {
                if (!line.equals("<TEXT>") && !line.equals("</TEXT>")
                        && !line.equals("<GRAPHIC>") && !line.equals("</GRAPHIC>")
                        && !line.equals("<P>") && !line.equals("</P>")) {
                    docT = docT + line;
                }
            }
        }
        br.close();

        //score the sentence in the Headline
        String sensH[] = docH.split("\\.|\\?|!");
        for(String senH:sensH ){
            int h = 1;
            int c = 0;

            ArrayList<String> discTokens = new ArrayList<String>();
            String lowerLine = senH.toLowerCase();
            StringTokenizer st = new StringTokenizer(lowerLine, " ");
            while (st.hasMoreTokens()) {
                //treat tokens as any contiguous sequence of alphanumerics
                String[] tokens = st.nextToken().split("[^a-zA-Z0-9]+"); //or \\P{Alpha}+?
                for (String token : tokens) {
                    //get doc length
                    if (!token.equals("")) {
                        token = token.trim();
                        if (queryWords.contains(token)){
                            if (!discTokens.contains(token)){
                                discTokens.add(token);
                            }
                            c++;
                        }
                    }
                }
            }
            if (!(senH.length() < 5)){
                senH = senH.trim() + ".";
                scoredSentences.put(senH, h + c + discTokens.size());
            }

        }

        String sensT[] = docT.split("\\.|\\?|!");
        for(String senT:sensT ){
            int c = 0;
            int l = 0;
            int k = 0;
            if (senT.equals(sensT[0])){
                l = 2;
            }
            ArrayList<String> discTokens = new ArrayList<String>();
            String lowerLine = senT.toLowerCase();
            StringTokenizer st = new StringTokenizer(lowerLine, " ");
            while (st.hasMoreTokens()) {
                //treat tokens as any contiguous sequence of alphanumerics
                String[] tokens = st.nextToken().split("[^a-zA-Z0-9]+"); //or \\P{Alpha}+?
                for (String token : tokens) {
                    //get doc length
                    if (!token.equals("")) {
                        token = token.trim();
                        if (queryWords.contains(token)){
                            if (!discTokens.contains(token)){
                                discTokens.add(token);
                            }
                            c++;
                        }
                    }
                }
            }
            if (!(senT.length() < 5)){
                senT = senT.trim() + ".";
                if (scoredSentences.containsKey(senT)){
                    scoredSentences.put(senT, scoredSentences.get(senT) + l + c);
                }else{
                    scoredSentences.put(senT,l + c + discTokens.size());
                }
            }
        }
        List<Map.Entry<String,Integer>> list = new ArrayList<Map.Entry<String,Integer>>(scoredSentences.entrySet());
        Collections.sort(list,new Comparator<Map.Entry<String,Integer>>() {
            //descending order
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        int sentenceCount = 0;
        String summary = "";
        for(Map.Entry<String,Integer> mapping:list){
            if (sentenceCount < 4){
                summary = summary + mapping.getKey() + " " ;
            }else {
                break;
            }
            sentenceCount = sentenceCount + 1;
        }

        return summary;

    }
    public static ArrayList BM25Retrival(String path, String query) throws IOException{
        //todo: for return
        //ArrayList<String> retrievedResult = new ArrayList<String>();

        //define the queryWords as public first
        //ArrayList<String> queryWords = new ArrayList<String>();
        retrievedResult.clear();
        queryWords.clear();
        int N = docIDtoNo.size();
        int qf = 1;
        double k1 = 1.2;
        double k2 = 7.0;
        double b = 0.75;
        double avdl = (double) colLength/N;

        if (!query.isEmpty()){
            //tokenize the query
            String lowerLine = query.toLowerCase();
            StringTokenizer st = new StringTokenizer(lowerLine, " ");
            while (st.hasMoreTokens()) {
                String[] tokens = st.nextToken().split("[^a-zA-Z0-9]+");
                for (String token : tokens) {
                    if (!token.equals("")) {
                        token = token.trim();
                        queryWords.add(token);
                    }
                }
            } // end tokenization of the query
            if (queryWords.size() > 0){
                // to store BM25 scores for each doc <docid, BM25>
                Map<String, Double> BM25Scores = new TreeMap<String, Double>();
                for (int i = 0; i < queryWords.size(); i++){
                    String termID = (String)lexicon.get(queryWords.get(i));
                    String postings = position.get(Integer.parseInt(termID));
                    String[] lists = postings.split(" ");
                    int n = lists.length/2;
                    int tf = 0;
                    // for each doc contains term i
                    int j = 0;
                    while ( j < lists.length ){
                        String docid = lists[j];
                        int dl = Integer.parseInt(docLength.get(docid));
                        tf = Integer.parseInt(lists[j+1]) ;
                        double K = k1 * ((1 - b) + b * dl / avdl);
                        double temp = ((k1 + 1)*tf/(K + tf)) * ((k2 + 1)*qf/(k2 + qf)) * (Math.log((N-n+0.5)/(n+0.5))) ;
                        if (BM25Scores.containsKey(docid))
                        {
                            double BM25 = BM25Scores.get(docid)+temp;
                            BM25Scores.put(docid,BM25);
                        }
                        else
                        {
                            BM25Scores.put(docid,temp);
                        }
                        j = j + 2;
                    }

                }
                List<Map.Entry<String,Double>> list = new ArrayList<Map.Entry<String,Double>>(BM25Scores.entrySet());
                //sort the BM25 scores
                Collections.sort(list,new Comparator<Map.Entry<String,Double>>() {
                    //descending order
                    public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                        return o2.getValue().compareTo(o1.getValue());
                    }
                });

                int rankBM = 1;
                for(Map.Entry<String,Double> mapping:list){
                    if (rankBM < 11){
                        String docid = mapping.getKey();
                        //String docno = (String) docIDtoNo.get(docid);
                        retrievedResult.add(docid);
                    }
                    rankBM = rankBM + 1;
                }
            }else{
                System.out.println("no query");
            }
        }


        return retrievedResult;
    }

    public static void main(String[] args) throws IOException{
        //get arguments
        String path = args[0]; //path to the folder

        FileInputStream finD = new FileInputStream(path + "/metadata.txt");
        InputStreamReader xoverD = new InputStreamReader(finD);
        BufferedReader brD = new BufferedReader(xoverD);
        String lineD;
        while(( lineD = brD.readLine()) != null){
            String[] docs = lineD.split("\t");
            docIDtoNo.put(docs[0], docs[1]);
            int last = docs.length - 1;
            colLength = colLength + Integer.parseInt(docs[last]);
            docLength.put(docs[0],docs[last]);
            headline.put(docs[0], docs[3]);
        }
        brD.close();

        //todo: build lexicon HashTable
        FileInputStream finL = new FileInputStream(path + "/lexicon.txt");
        InputStreamReader xoverL = new InputStreamReader(finL);
        BufferedReader brL = new BufferedReader(xoverL);
        String lineL;
        while((lineL = brL.readLine()) != null){
            String[] leContent = lineL.split(" ");
            lexicon.put(leContent[0],leContent[1]);
        }
        brL.close();

        //todo: Build postingsList ArrayList based on tokenid
        FileInputStream finP = new FileInputStream(path + "/postingsList.txt");
        InputStreamReader xoverP = new InputStreamReader(finP);
        BufferedReader brP = new BufferedReader(xoverP);
        String lineP;
        while((lineP = brP.readLine()) != null){
            lineP = lineP.trim();
            position.add(lineP);
        }
        brP.close();

        // todo: get input from user
        Scanner scan = new Scanner(System.in);
        System.out.println("Please input the query：");
        // check if there is any more input
        String query = "";
        while(scan.hasNextLine()){
            query = scan.nextLine();
            if (query.equals("Q")){
                break;
            }else if (query.equals("N")){
                System.out.println("Please input the query：");
            //}else if (query.startsWith("LA") && query.length() == 13) {
            }else if(query.equals("1") || query.equals("2") || query.equals("3") ||query.equals("4") ||query.equals("5")
                    ||query.equals("6") ||query.equals("7") ||query.equals("8") ||query.equals("9") ||query.equals("10") ){

                String rank = retrievedResult.get(Integer.parseInt(query) - 1);
                String docno = (String)docIDtoNo.get(rank);

                String date = docno.substring(2, 8);
                String yy = date.substring(4, 6);
                String mm = date.substring(0, 2);
                String dd = date.substring(2, 4);
                String docPath = yy + "/" + mm + "/" + dd;

                FileInputStream finN = new FileInputStream("out" + "/" + docPath + "/" + docno + ".txt");
                InputStreamReader xoverN = new InputStreamReader(finN);
                BufferedReader brN = new BufferedReader(xoverN);
                String line;
                while(( line = brN.readLine()) != null){
                    System.out.println(line);
                }
                System.out.println("Please type \"N\" for \"new query\", " + "or type \"Q\" for \"quit\"");
                brN.close();
            } else {
                long startTime = System.currentTimeMillis();
                showResults (path, query);
                long endTime   = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Retrieval took " + (double)totalTime/1000 + " seconds.");
                System.out.println("Please type in the number of a document to view, or type \"N\" for \"new query\", "
                        + "or type \"Q\" for \"quit\"");

            }

        }

    }
}
