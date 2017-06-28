/**
 * Created by Jinjin on 2017-03-10.
 */
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;

public class Evaluate {
    private static Hashtable<String, String> docLength = new Hashtable();
    //private static ArrayList<String> Report = new ArrayList<String>();

    public static void main(String[] args) throws IOException {
        String path = args[0]; //path to folder
        String pathResult = args[1];

        //todo: get document length
        //open metadata.txt
        FileInputStream fin = new FileInputStream( path + "/metadata.txt");
        //FileInputStream fin = new FileInputStream( "outall/metadata.txt");
        InputStreamReader xover = new InputStreamReader(fin);
        BufferedReader br = new BufferedReader(xover);
        String lineL;
        while((lineL = br.readLine()) != null){
            String[] leContent = lineL.split("\t");
            int last = leContent.length - 1;
            docLength.put(leContent[1],leContent[last]);
        }
        br.close();

        //todo: report the per topic evaluation score for each student
         //int studentNum = 14;
        // for Problem 7
        int studentNum = 1;
        for (int stuID = 1; stuID <= studentNum; stuID++){
            // for the mean
            Double sumOfAP = 0.00;
            Double sumOfPat10 = 0.00;
            Double sumOfNDCGat10 = 0.00;
            Double sumOfNDCGat1000 = 0.00;
            Double sumOfTBG = 0.00;

            //todo: write output for each student
            //BufferedWriter out = new BufferedWriter(new FileWriter("student" + stuID + "output.txt"));
            BufferedWriter out = new BufferedWriter(new FileWriter(path + "/evaluate-result.txt"));
            //BufferedWriter out = new BufferedWriter(new FileWriter("J433chen-output.txt"));
            out.write("std" + "\t" + "topic" + "\t" + "AP" + "\t" + "Prec@10"
                    + "\t" + "NDCG@10." + "\t" + "NDCG@1000" + "\t" + "TBG");
            out.newLine();

            // to get the result file of the student
            ArrayList<Results.Result> resultOfQrel = new ArrayList<Results.Result>();
            String qrelsPath = "hw3/qrels/LA-only.trec8-401.450.minus416-423-437-444-447.txt";
            //String resultsPath = "hw3/results-files/student" + stuID + ".results";
            // Problem 7
            //String resultsPath = "hw3/j433chen-hw2‐results.txt";
            String resultsPath = path + "/" + pathResult;
            ResultsFile resultsFile;
            QRels queryResult;
            try {
                // to get the query result
                // to get the result file of the student
                queryResult = new QRels(qrelsPath);
                resultsFile = new ResultsFile(resultsPath);
            }
            catch (Exception e) {
                //Results.add(stuID + "bad format");
                out.write("bad format");
                out.close();
                continue;
            }

            // for each topic
            for (int topic = 401; topic <= 450; topic++){
                if ( topic  != 416 && topic != 423 && topic != 437 && topic != 444 && topic != 447){
                    String queryID = Integer.toString(topic);
                    try{
                        resultOfQrel = resultsFile.results.QueryResults(queryID);
                    }
                    catch (Exception e) {
                        System.out.println(stuID + " no topic" + queryID);
                        out.write(stuID + "\t" + queryID + "\t  " +
                               "0" + "\t" + "0" + "\t" + "0" + "\t" + "0" + "\t" + "0");
                        out.newLine();
                        continue;
                    }

                    //define the effectiveness measures
                    Double avgPrec = 0.00;
                    Double PrecAt10 = 0.00;
                    Double NDCGat10 = 0.00;
                    Double NDCGat1000 = 0.00;
                    Double DCG = 0.00;
                    Double numOfRel = 0.00;
                    Double temp = 0.00;

                    //for TBG
                    Double Pc1r1 = 0.64;
                    Double Pc1r0 = 0.39;
                    Double Ps1r1 = 0.77;
                    Double Ts = 4.4;
                    Double TBG = 0.00;

                    for (int i = 0; i < resultOfQrel.size(); i++) {
                        String docID = resultOfQrel.get(i).docID();
                        int rank = i + 1;
                        if (queryResult.judgments.IsRelevant(queryID, docID) && rank <= 1000 ) {
                            numOfRel++;
                            // for average precision
                            temp = (numOfRel / rank) + temp;
                            // for DCG
                            DCG = DCG + 1 / ((Math.log(rank+1)/Math.log(2)));
                            // for TBG, based on SIGIR 2012 paper
                            Double Tk = 0.0;
                            Double Tdl = 0.0;
                            for ( int j = 0; j < rank - 2; j++){
                                // get the doc length
                                int l = Integer.parseInt(docLength.get(docID));
                                String docjID = resultOfQrel.get(j).docID();
                                if (queryResult.judgments.IsRelevant(queryID, docjID)){
                                    Tdl = (0.018 * l + 7.8) * Pc1r1;
                                }else {
                                    Tdl = (0.018 * l + 7.8) * Pc1r0;
                                }
                                Double tempp = Ts + Tdl;
                                Tk = Tk + tempp;
                            }
                            Double Dt = Math.exp(-Tk * Math.log(2)/224);
                            //becasue N = 1
                            TBG = TBG +  Dt * Pc1r1 * Ps1r1;
                        }
                        if (rank == 10) {
                            //return Precision@10
                            PrecAt10 = numOfRel / rank;
                            //return NDCG@10
                            Double IDCGat10 = 0.00;
                            if (numOfRel > 0){
                                int relNum = queryResult.judgments.NumRelevant(queryID);
                                if ( relNum > 10){
                                    for (int j = 0; j < 10; j++){ //我觉得这里有问题，不应该是10， 应该是numofRel
                                        IDCGat10 = IDCGat10 + 1/((Math.log(j+1+1)/Math.log(2)));
                                    }
                                }else {
                                    for (int j = 0; j < relNum; j++){ //我觉得这里有问题，不应该是10， 应该是numofRel
                                        IDCGat10 = IDCGat10 + 1/((Math.log(j+1+1)/Math.log(2)));
                                    }
                                }
                                NDCGat10 = DCG/IDCGat10;

                            }
                        }
                        if (rank == 1000) {
                            //return NDCG@1000
                            Double IDCGat1000 = 0.00;
                            if (numOfRel > 0) {
                                int relNum = queryResult.judgments.NumRelevant(queryID);
                                if (relNum > 1000) {
                                    for (int j = 0; j < 1000; j++) {
                                        IDCGat1000 = IDCGat1000 + 1/((Math.log(j + 1 + 1) / Math.log(2)));
                                    }
                                }else {
                                    for (int j = 0; j < relNum; j++) {
                                        IDCGat1000 = IDCGat1000 + 1/((Math.log(j + 1 + 1) / Math.log(2)));
                                    }
                                }

                                NDCGat1000 = DCG/IDCGat1000;
                            }
                        }else if(i == resultOfQrel.size() - 1){
                            Double IDCGat1000 = 0.00;
                            if (numOfRel > 0) {
                                for (int j = 0; j < numOfRel; j++) {
                                    IDCGat1000 = IDCGat1000 + 1/((Math.log(j + 1 + 1) / Math.log(2)));
                                }
                                NDCGat1000 = DCG/IDCGat1000;
                            }
                        }
                    }
                    ///int test = queryResult.judgments.NumRelevant(queryID);
                    avgPrec = temp / queryResult.judgments.NumRelevant(queryID);
                    out.write(stuID + "\t" + queryID + "\t  " +
                            String.format("%.4f", avgPrec)+ "\t" + String.format("%.4f", PrecAt10) + "\t" +
                            String.format("%.4f", NDCGat10) + "\t" + String.format("%.4f", NDCGat1000) + "\t" +
                            String.format("%.4f", TBG));
                    //out.write(stuID + "\t" + queryID + "\t" + avgPrec+ "\t" + PrecAt10 + "\t" + NDCGat10 + "\t" + NDCGat1000 + "\t" + TBG);
                    out.newLine();

                    //todo: calculate the mean for each student
                    sumOfAP = sumOfAP + avgPrec;
                    sumOfPat10 = sumOfPat10 + PrecAt10;
                    sumOfNDCGat10 = sumOfNDCGat10 + NDCGat10;
                    sumOfNDCGat1000 = sumOfNDCGat1000 + NDCGat1000;
                    sumOfTBG = sumOfTBG + TBG;
                }
            }

            out.close();
            int tCount = 45;
            System.out.println("student" + stuID + " "
                    + String.format("%.3f", sumOfAP/tCount) + " "
                    + String.format("%.3f", sumOfPat10/tCount) + " "
                    + String.format("%.3f", sumOfNDCGat10/tCount) + " "
                    + String.format("%.3f", sumOfNDCGat1000/tCount) + " "
                    + String.format("%.3f", sumOfTBG/tCount));


        }

    }

}
