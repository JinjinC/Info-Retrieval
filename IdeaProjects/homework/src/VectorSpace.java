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

public class VectorSpace {
    private static Hashtable lexicon = new Hashtable();
    private static Hashtable docIDtoNo = new Hashtable();
    private static Hashtable docWeight = new Hashtable();
    private static ArrayList<String> position = new ArrayList<String>();

    public static void main(String [ ] args) throws IOException
    {
        //check if arguments are sufficient. if not, exit with a help message
        if(args.length != 3){
            System.out.println("The program accepts three command line arguments:\n a path to the directory location of your index\n the queries file,\n and the name of file to store your output");
            return;
        }

        //get arguments
        String path = args[0]; //path the folder
        String queries = args[1]; // path to queries.txt
        String output = args[2]; // path of the output


        //todo: get Wd for each doc - Build Hashtable<docid,Wd>
        //open docWeight.txt
        FileInputStream finWd = new FileInputStream(path + "/fullWd.txt");
        InputStreamReader xoverWd = new InputStreamReader(finWd);
        BufferedReader brWd = new BufferedReader(xoverWd);
        String lineWd;
        while((lineWd = brWd.readLine()) != null){
            String[] docs = lineWd.split(" ");
            if (docs.length == 2){
                docWeight.put(docs[0], docs[1]);
            }
        }


        //todo: docid to docNo - Build Hashtable<docid,docNo>
        //open metadata.txt
        FileInputStream finD = new FileInputStream(path + "/metadata.txt");
        InputStreamReader xoverD = new InputStreamReader(finD);
        BufferedReader brD = new BufferedReader(xoverD);
        String lineD;
        while((lineD = brD.readLine()) != null){
            String[] docs = lineD.split("\t");
            docIDtoNo.put(docs[0], docs[1]);
        }

        //todo: tokenid to token - build lexicon Hashtable <tokenid, token>, tokenid starts with 0
        //open lexicon.txt
        FileInputStream finL = new FileInputStream(path + "/lexicon.txt");
        InputStreamReader xoverL = new InputStreamReader(finL);
        BufferedReader brL = new BufferedReader(xoverL);
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
        //BufferedWriter outDW = new BufferedWriter(new FileWriter(path + "/" + "docWeight-more.txt"));
        //open queries.txt
        FileInputStream fin = new FileInputStream(path + "/" + queries);
        InputStreamReader xover = new InputStreamReader(fin);
        BufferedReader br = new BufferedReader(xover);
        //read queries.txt
        String line;
        ArrayList<String> queryWords = new ArrayList<String>();
        //ArrayList<String> docset = new ArrayList<String>();

        // todo: used for Vector Space
        int N = docIDtoNo.size();
        int Wq = 1;

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
            // todo: Vector Space
            if (queryWords.size() > 0){
                //todo: begin Vector Space here - there is what hw4 different form hw2
                Map<String, Double> VSScores = new TreeMap<String, Double>();

                for (int i = 0; i < queryWords.size(); i++){
                    String termID = (String)lexicon.get(queryWords.get(i));
                    String postings = position.get(Integer.parseInt(termID));
                    String[] lists = postings.split(" ");
                    //# of docs with term i
                    int ft = lists.length/2;
                    // count for term i in the query
                    int fdt = 0;
                    // for each doc contains term i
                    int j = 0;
                    while ( j < lists.length ){
                        String docid = lists[j];
                        String docno = (String) docIDtoNo.get(docid);
                        double Wd = 0.0;
                        // the tf of term i in doc j
                        fdt = Integer.parseInt(lists[j+1]) ;
                        if (docWeight.containsKey(docid)){
                             String tem = (String) docWeight.get(docid);
                             Wd = Double.parseDouble(tem);
                             double temp = fdt * Math.pow((Math.log(N / ft)), 2) / (Wd * Wq);
                             if (VSScores.containsKey(docid)) {
                                double VS = VSScores.get(docid)+temp;
                                VSScores.put(docid, VS);
                             }
                             else {
                                VSScores.put(docid, temp);
                             }
                        }else {
                             System.out.println(docid + " no find Wd ");
//                             Wd = getDocWeight(docno, lexicon);
//                             docWeight.put(docid, Double.toString(Wd));
//                             outDW.write(docid + " " + Wd);
//                             outDW.newLine();
                        }
                        j = j + 2;

                    }
                }


                // write out the Vector Space result file
                List<Map.Entry<String,Double>> list = new ArrayList<Map.Entry<String,Double>>(VSScores.entrySet());
                //sort the Vector Space scores
                Collections.sort(list,new Comparator<Map.Entry<String,Double>>() {
                    //descending order
                    public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
                        return o2.getValue().compareTo(o1.getValue());
                    }
                });

                int rankVS = 1;
                for(Map.Entry<String,Double> mapping:list){
                    if (rankVS < 1001) {
                        //System.out.println(mapping.getKey()+":"+mapping.getValue());
                        String docid = mapping.getKey();
                        String docno = (String) docIDtoNo.get(docid);
                        out.write(topicID + " " + "Q0" + " " + docno + " " + rankVS + " " + mapping.getValue() + " " + "j433chenVS");
                        out.newLine();
                    }
                    rankVS = rankVS + 1;
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
        brWd.close();
        out.close();

        System.out.println("done");
        //out.close();
    }


}
