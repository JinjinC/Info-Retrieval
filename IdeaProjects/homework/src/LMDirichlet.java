/**
 * Created by Jinjin on 2017-03-16.
 * based on my BM25.java
 */
import java.io.*;
import java.util.*;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LMDirichlet {
    private static Hashtable lexicon = new Hashtable();
    private static Hashtable docIDtoNo = new Hashtable();
    private static ArrayList<String> position = new ArrayList<String>();
    private static Hashtable<String, String> docLength = new Hashtable();
    private static Hashtable<String, String> tokenTCounts = new Hashtable();

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

        //todo: Build postingsList ArrayList based on tokenid
        //open postingsList.txt
        FileInputStream finP = new FileInputStream(path + "/postingsList.txt");
        InputStreamReader xoverP = new InputStreamReader(finP);
        BufferedReader brP = new BufferedReader(xoverP);
        String lineP;
        while((lineP = brP.readLine()) != null){
            lineP = lineP.trim();
            position.add(lineP);
        }
        brP.close();

        // I write this to output tokenTCounts.txt. It's done, so useless now
//        BufferedWriter out = new BufferedWriter(new FileWriter(path + "/tokenTCounts.txt"));
//        for ( int i = 0; i < position.size(); i++){
//            int termid = i;
//            int tCount = 0;
//            String postings = position.get(i);
//            String[] lists = postings.split(" ");
//            int j = 0;
//            while (j < lists.length){
//                tCount = tCount + Integer.parseInt(lists[j + 1]);
//                j = j + 2;
//            }
//            out.write(termid + " " + tCount);
//            out.newLine();
//
//        }
//        out.close();

        //todo: write out total word counts in the collection first - termid to counts in the

        //todo: Build Hashtable - docid to docNo
        //open metadata.txt
        FileInputStream finD = new FileInputStream(path + "/metadata.txt");
        InputStreamReader xoverD = new InputStreamReader(finD);
        BufferedReader brD = new BufferedReader(xoverD);
        String lineD;
        int colLength = 0;
        while((lineD = brD.readLine()) != null){
            String[] docs = lineD.split("\t");
            docIDtoNo.put(docs[0], docs[1]);
            int last = docs.length - 1;
            colLength = colLength + Integer.parseInt(docs[last]);
            docLength.put(docs[0],docs[last]);
        }
        brD.close();

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
        brL.close();

        //todo: build token total counts HashTable <tokenid, counts>
        FileInputStream finT = new FileInputStream(path + "/tokenTCounts.txt");
        InputStreamReader xoverT = new InputStreamReader(finT);
        BufferedReader brT = new BufferedReader(xoverT);
        String lineT;
        while((lineT = brT.readLine()) != null){
            String[] leContent = lineT.split(" ");
            tokenTCounts.put(leContent[0],leContent[1]);
        }
        brT.close();

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

        int m = 1000;

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
                        if (k == 0){
                            topicID = token;
                            k++;
                        }else {
                            queryWords.add(token);
                        }
                    }
                }
            } // end tokenization of the query

            // for each doc in the collection
            // to store LMD scores for each doc <docid, LMD>
            Map<String, Double> LMDScores = new TreeMap<String, Double>();
            for ( int d = 0; d < docIDtoNo.size(); d++){
                String docid = Integer.toString(d + 1);
                //String docno = (String) docIDtoNo.get(docid);
                double prob = 1.0;
                int dl = Integer.parseInt(docLength.get(docid));

                for (int i = 0; i < queryWords.size(); i++) {
                    String termID = (String) lexicon.get(queryWords.get(i));
                    String postings = position.get(Integer.parseInt(termID));
                    String[] lists = postings.split(" ");
                    int tf = 0;
                    // if the term i is in the doc d
                    int j = 0;
                    while (j < lists.length) {
                        if (docid.equals(lists[j])) {
                            tf = Integer.parseInt(lists[j + 1]);
                            break;
                        } else if (Integer.parseInt(lists[j]) > (d + 1)) {
                            break;
                        }
                        j = j + 2;
                    }
                    int tCounts = Integer.parseInt(tokenTCounts.get(termID));
                    double Pwc = (double) tCounts / colLength;
                    double temp = (tf + m * Pwc) / (dl + m);
                    prob = prob * temp;
                }
                if (prob >= 1.0) {
                    System.out.print("errot");
                }
                LMDScores.put(docid, prob);
            }
            List<Map.Entry<String,Double>> list = new ArrayList<Map.Entry<String,Double>>(LMDScores.entrySet());
            //sort the LMD scores
            Collections.sort(list,new Comparator<Map.Entry<String,Double>>() {
                //descending order
                public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });

            int rankLMD = 1;
            for(Map.Entry<String,Double> mapping:list){
                if (rankLMD < 1001){
                    //System.out.println(mapping.getKey()+":"+mapping.getValue());
                    String docid = mapping.getKey();
                    String docno = (String) docIDtoNo.get(docid);
                    out.write(topicID + " " + "Q0" + " " + docno + " " + rankLMD + " " + mapping.getValue() + " " + "j433chenBM25-LM-Dirichlet");
                    out.newLine();
                }
                rankLMD = rankLMD + 1;
            }
        }
        br.close();
        out.close();
    }


}
