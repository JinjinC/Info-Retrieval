/**
 * Created by Jinjin on 2017-01-13.
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.util.StringTokenizer;
import java.util.*;

public class IndexEngine {
    public static Hashtable lexicon = new Hashtable();
    public static Hashtable invertedIndex = new Hashtable();;
    public static Hashtable<Integer, ArrayList<Integer>> postingsList;
    private static BufferedWriter outLexicon;
    private static BufferedWriter outInvertedIndex;

    public static Hashtable addToPosting(Hashtable wordCounts, int docID){
        Enumeration tokenIDs;
        int tokenid;
        tokenIDs = wordCounts.keys();
        while(tokenIDs.hasMoreElements()) {
            tokenid = (Integer) tokenIDs.nextElement();
            int count = ((Integer)wordCounts.get(tokenid)).intValue();

            ArrayList<Integer> newList = new ArrayList<Integer>();
            if (postingsList.containsKey(tokenid)){

                newList = postingsList.get(tokenid);
                newList.add(docID);
                newList.add(count);
                postingsList.put(tokenid, newList);

            }else{
                newList.add(docID);
                newList.add(count);
                postingsList.put(tokenid, newList);
            }
        }
        return postingsList;
    }

    public static Hashtable countWords(int tokenID, Hashtable wordCounts){
        if (wordCounts.containsKey(tokenID)){
            int count = ((Integer)wordCounts.get(tokenID)).intValue();
            wordCounts.put(tokenID, count + 1);
        }else {
            wordCounts.put(tokenID,1);
        }
        return wordCounts;
    }

    public static String invertIndex(int tokenID, String token, Hashtable invertedIndex) throws IOException{
        if (!invertedIndex.containsKey(tokenID)){
            invertedIndex.put(tokenID, token);

            //write inverted-index.txt
            outInvertedIndex.write(tokenID + "\t" + token);
            outInvertedIndex.newLine();
        }
        return token;
    }

    public static int convertTokensToIDs(String token, Hashtable lexicon) throws IOException{
        if (lexicon.containsKey(token)){
            int id = ((Integer)lexicon.get(token)).intValue();
            return id;
        }else {
            int termID = lexicon.size();
            lexicon.put(token,termID);

            //write lexicon.txt
            outLexicon.write(token + " " + termID);
            outLexicon.newLine();
            return  termID;
        }
    }

    public static String outputFiles(String docContent, String fileName, String folder) {

        //output the docContent into its docno.txt files
        FileOutputStream fop = null;
        File file;

        try {
            //deal with paths
            String date = fileName.substring(2, 8);
            String yy = date.substring(4, 6);
            String mm = date.substring(0, 2);
            String dd = date.substring(2, 4);
            String folderName = yy + "/" + mm + "/" + dd;

            File dir = new File(folder + "/" + folderName);
            dir.mkdirs();
            file = new File(dir, fileName + ".txt");
            fop = new FileOutputStream(file);

            // if file doesn't exists, then create it
            if (!file.exists()) {
                file.createNewFile();
                System.out.println("Done");
            }else {
                //throw new IOException("the directory already exists.");
                //System.out.println("the directory already exists.");
            }
            // get the content in bytes
            byte[] contentInBytes = docContent.getBytes();
            fop.write(contentInBytes);
            fop.flush();
            fop.close();

        } catch (IOException e) {
            e.printStackTrace();
            //System.exit(1);
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return docContent;
    }

    public static void main(String[] args) throws IOException {
        int id = 0;
        int headNo = 0;
        //check if arguments are sufficient
        if(args.length != 2){
            System.out.println("Please provide a path to the latimes.gz file and a path to a directory where the documents and metadata will be stored");
            return;
        }
        //take the first argument to work as the file path
        String FILEPATH = args[0];
        String folder = args[1];

        //read the GZIP file
        FileInputStream fin = new FileInputStream(FILEPATH);
        GZIPInputStream gzis = new GZIPInputStream(fin);
        InputStreamReader xover = new InputStreamReader(gzis);
        BufferedReader is = new BufferedReader(xover);

        //write metadata
        BufferedWriter out = new BufferedWriter(new FileWriter(folder + "/metadata.txt"));
        //write lexicon
        outLexicon = new BufferedWriter(new FileWriter(folder + "/lexicon.txt"));
        //write inverted Index
        outInvertedIndex = new BufferedWriter(new FileWriter(folder + "/inverted-index.txt"));
        //write postings list
        BufferedWriter outPostingsList = new BufferedWriter(new FileWriter(folder + "/postingsList.txt"));
        //write position - start of each tokenid
        BufferedWriter outPosition = new BufferedWriter(new FileWriter(folder + "/position.txt"));

        //store each line when reading GZIP file
        String line;

        //mark <DOC> as true, <\DOC> as false
        boolean start = false;
        boolean headline = false;
        boolean flagHeadline = false;
        boolean startTokenize = false;

        //store read results
        String docContent = "";
        String fileName = "";

        // read GZIP file line by line
        int docLength = 0;
        //get tokenID based on lexicon
        int tokenID;
        //store corresponding tokenIDs for each doc
        ArrayList<Integer> result = new ArrayList<Integer>();
        //create word counts for each doc
        Hashtable wordCounts = new Hashtable();
        //create postings list for each doc
        postingsList = new Hashtable<Integer, ArrayList<Integer>>();

        while ((line = is.readLine()) != null) {
            //start recording when meet <DOC>
            if (line.equals("<DOC>")) {
                docLength = 0;
                start = true;
                id++;
                //write metadata
                out.write(Integer.toString(id));
                out.write("\t");
            }
            if (line.equals("</DOC>")) {
                start = false;
                if(flagHeadline == false){
                    out.write("no headlines");
                    out.write("\t");
                }
                else{
                    flagHeadline = false;
                }
                //write doc length into metadata.txt
                out.write(Integer.toString(docLength));
                out.newLine();
            }
            if (line.startsWith("<DOCNO>")) {
                fileName = line.substring(7,22);
                fileName = fileName.trim();
                //System.out.println(fileName);
                out.write(fileName);
                out.write("\t");
                //mm/dd/yy
                String date = fileName.substring(2, 8);
                //warning: todo: deal with dates
                out.write(date);
                out.write("\t");
            }
            if (start){
                //docContent
                if (docContent == "") {
                    docContent = docContent + line;
                }else {
                    docContent = docContent + "\n" + line;
                }

            }else{
                docContent = docContent + "\n" + "</DOC>";
                //System.out.println(docContent);
                outputFiles(docContent, fileName, folder);
                docContent = "";
            }

            //for headline
            if (line.equals("<HEADLINE>")) {
                headline = true;
                flagHeadline = true;
            }

            if (line.equals("</HEADLINE>")) {
                headline = false;
            }

            if(headline){
                //todo: write headline to metadata
                headNo++;
                if(headNo == 3){
                    out.write(line);
                    out.write("\t");
                    headline = false;
                    headNo = 0;
                }
            }

            //start tokenize a doc
            int docID = id;
            if (start) {
                //start and stop tokenizing the Headline, TEXT and GRAPHIC
                if (line.equals("<HEADLINE>") || line.equals("<TEXT>") || line.equals("<GRAPHIC>")) {
                    startTokenize = true;

                } else if (line.equals("</HEADLINE>") || line.equals("</TEXT>") || line.equals("</GRAPHIC>")) {
                    startTokenize = false;
                }

                //start tokenize
                if (startTokenize) {
                    if (!line.equals("<HEADLINE>") && !line.equals("</HEADLINE>")
                            && !line.equals("<TEXT>") && !line.equals("</TEXT>")
                            && !line.equals("<GRAPHIC>") && !line.equals("</GRAPHIC>")
                            && !line.equals("<P>") && !line.equals("</P>")) {

                        //lowercase all characters
                        String lowerLine = line.toLowerCase();
                        StringTokenizer st = new StringTokenizer(lowerLine, " ");
                        while (st.hasMoreTokens()) {
                            //treat tokens as any contiguous sequence of alphanumerics
                            String[] tokens = st.nextToken().split("[^a-zA-Z0-9]+"); //or \\P{Alpha}+?
                            for (String token : tokens) {
                                //get doc length
                                if (!token.equals("")) {
                                    token = token.trim();
                                    docLength++;
                                    //System.out.println(token);

                                    //todo: stem the token - used for hw4
                                    Stemmer s = new Stemmer();
                                    char[] arr = token.toCharArray();
                                    for (char ar:arr){
                                        s.add(ar);
                                    }
                                    s.stem();
                                    token = s.toString();
                                    //stemmer end here

                                    //convert each token to its tokenID, return its tokenID
                                    tokenID = convertTokensToIDs(token, lexicon);
                                    //result stores tokenIDs based on docID
                                    result.add(tokenID);
                                    //count words
                                    countWords(tokenID, wordCounts);
                                    //create inverted Index, return its token
                                    invertIndex(tokenID, token, invertedIndex);
                                }
                            }
                        }
                    }
                }
            }else{// the end of the doc
                //return converted result of the doc - tokens to tokenIDs
                result.clear();
                //todo addToPosting(wordCounts)
                addToPosting(wordCounts, docID);
                wordCounts.clear();
            }

        }

        //write postingsList.txt - to store all value starting at tokenid 0
        //write position.txt - to store the start position of each token starting at tokenid 0
        int postingStart = 0;
        //k is tokenid here
        for (int k = 0; k < postingsList.size(); k++){
            ArrayList<Integer> postings = new ArrayList<Integer>();
            postings = postingsList.get(k);
            for(int j = 0; j < postings.size(); j++){
                String posting = (postings.get(j)).toString();
                outPostingsList.write(posting + " ");
            }
            outPostingsList.newLine();
            outPosition.write(Integer.toString(postingStart));
            outPosition.newLine();
            postingStart = postingStart + postings.size();
        }

        out.close();
        outInvertedIndex.close();
        outLexicon.close();
        outPostingsList.close();
        outPosition.close();
        System.out.println("done");
    }
}

