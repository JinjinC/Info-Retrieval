/**
 * Created by Jinjin on 2017-03-10.
 * Based on the CSharp code given by Mark D. Smucker
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ResultsFile {
    /// <summary>
    /// the results
    /// </summary>
    public Results results = new Results() ;
    public String runID ;

    /// <summary>
    /// Yo, this will throw IO exceptions if something IO bad happens
    /// </summary>
    /// <param name="path"></param>
    public ResultsFile(String fullpath) throws Exception {
        File file = new File(fullpath);
        BufferedReader sr = new BufferedReader(new FileReader(file));
        boolean firstLine = true;
        String line;
        while ((line = sr.readLine()) != null) {
            String[] fields = line.split("\\s+");
            // should be "queryID Q0 doc-id rank score runID"
            if (fields.length != 6) {
                throw new Exception("input should have 6 columns");
            }

            String queryID = fields[0];
            String docID = fields[2];
            int rank = Integer.parseInt(fields[3]);
            double score = Double.parseDouble(fields[4]);
            results.AddResult(queryID, docID, score, rank);
            if (firstLine) {
                this.runID = fields[5];
                firstLine = false;
            } else if (!this.runID.equals(fields[5])) {
                throw new Exception("mismatching runIDs in file");
            }

        }
        sr.close();
    }
}
