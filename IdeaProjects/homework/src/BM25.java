/**
 * Created by Jinjin on 2017-03-14.
 */
import java.io.*;
import java.util.*;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class BM25 {
    private static Hashtable lexicon = new Hashtable();
    private static Hashtable docIDtoNo = new Hashtable();
    private static ArrayList<String> position = new ArrayList<String>();
    private static Hashtable<String, String> docLength = new Hashtable();

    public static void main(String [ ] args) throws IOException
    {
        //check if arguments are sufficient. if not, exit with a help message
        if(args.length != 3){
            System.out.println("The program accepts three command line arguments:\n a path to the directory location of your index\n the queries file,\n and the name of file to store your output");
            return;
        }

        //get arguments
        String path = args[0]; //path to latimes-index folder
        String queries = args[1]; // path to queries.txt
        String output = args[2]; // path of the output

        //todo: Build Hashtable - docid to docNo
        //open metadata.txt
        FileInputStream finD = new FileInputStream(path + "/metadata.txt");
        InputStreamReader xoverD = new InputStreamReader(finD);
        BufferedReader brD = new BufferedReader(xoverD);
        //write postingsList
        String lineD;
        int colLength = 0;
        while((lineD = brD.readLine()) != null){
            String[] docs = lineD.split("\t");
            docIDtoNo.put(docs[0], docs[1]);
            int last = docs.length - 1;
            colLength = colLength + Integer.parseInt(docs[last]);
            docLength.put(docs[0],docs[last]);
        }

        //todo: build lexicon HashTable
        //open lexicon.txt
        FileInputStream finL = new FileInputStream(path + "/lexicon.txt");
        InputStreamReader xoverL = new InputStreamReader(finL);
        BufferedReader brL = new BufferedReader(xoverL);
        //write lexicon into a HashTable
        String lineL;
        while((lineL = brL.readLine()) != null){
            String[] leContent = lineL.split(" ");
            lexicon.put(leContent[0],leContent[1]);
        }

        //todo: Build postingsList ArrayList based on tokenid
        //open postingsList.txt
        FileInputStream finP = new FileInputStream(path + "/postingsList.txt");
        InputStreamReader xoverP = new InputStreamReader(finP);
        BufferedReader brP = new BufferedReader(xoverP);
        //write postingsList
        String lineP;
        while((lineP = brP.readLine()) != null){
            lineP = lineP.trim();
            position.add(lineP);
        }

        //todo: process queries
        //write output
        BufferedWriter out = new BufferedWriter(new FileWriter(path + "/" + output));
        //open queries.txt
        FileInputStream fin = new FileInputStream(path + "/" + queries);
        InputStreamReader xover = new InputStreamReader(fin);
        BufferedReader br = new BufferedReader(xover);
        //read queries.txt
        String line;
        ArrayList<String> queryWords = new ArrayList<String>();

        // todo: used for BM25
        int N = docIDtoNo.size();
        int qf = 1;
        double k1 = 1.2;
        double k2 = 7.0;
        double b = 0.75;
        double avdl = (double) colLength/N;

        while((line = br.readLine()) != null){
            //tokenize the query
            queryWords.clear();
            String lowerLine = line.toLowerCase();
            StringTokenizer st = new StringTokenizer(lowerLine, " ");

            int k = 0; // to mark topicID
            String topicID = "";
            while (st.hasMoreTokens()) {
                String[] tokens = st.nextToken().split("[^a-zA-Z0-9]+");
                for (String token : tokens) {
                    if (!token.equals("")) {
                        token = token.trim();
                        // add query'words to the arrayList queryWords
                        //todo: stem the term in the query - used for hw4 only
//                        Stemmer s = new Stemmer();
//                        char[] arr = token.toCharArray();
//                        for (char ar:arr){
//                            s.add(ar);
//                        }
//                        s.stem();
//                        token = s.toString();
                        //stemmer end here

                        if (k == 0){
                            topicID = token;
                            k++;
                        }else {
                            queryWords.add(token);
                        }
                    }
                }
            } // end tokenization of the query
            // todo: BM25
            if (queryWords.size() > 0){
                //todo: begin BM25 here - there is what hw4 different form hw2
                // to store BM25 scores for each doc <docid, BM25>
                Map<String, Double> BM25Scores = new TreeMap<String, Double>();

                for (int i = 0; i < queryWords.size(); i++){
                    String termID = (String)lexicon.get(queryWords.get(i));
                    String postings = position.get(Integer.parseInt(termID));
                    String[] lists = postings.split(" ");
                    //# of docs with term i
                    int n = lists.length/2;
                    // count for term i in the query
                    int tf = 0;
                    // for each doc contains term i
                    int j = 0;
                    while ( j < lists.length ){
                        String docid = lists[j];
                        String docno = (String) docIDtoNo.get(docid);
                        int dl = Integer.parseInt(docLength.get(docid));
                        // the tf of term i in doc j
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
                    public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
                        return o2.getValue().compareTo(o1.getValue());
                    }
                });

                int rankBM = 1;
                for(Map.Entry<String,Double> mapping:list){
                    if (rankBM < 1001){
                        System.out.println(mapping.getKey()+":"+mapping.getValue());
                        String docid = mapping.getKey();
                        String docno = (String) docIDtoNo.get(docid);
                        out.write(topicID + " " + "Q0" + " " + docno + " " + rankBM + " " + mapping.getValue() + " " + "j433chenBM25-baseline");
                        out.newLine();
                    }
                    rankBM = rankBM + 1;
                }
            }else{
                System.out.println("no query");
                continue;
            }
        }
        br.close();
        brL.close();
        brP.close();
        brD.close();
        out.close();
    }


}
