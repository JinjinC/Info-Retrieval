/**
 * Created by Jinjin on 2017-03-09.
 * Based on the CSharp code given by Mark D. Smucker
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class QRels {
    /// <summary>
    /// the results of reading in the file
    /// </summary>
    public RelevanceJudgments judgments = new RelevanceJudgments();
    /// <summary>
    /// Yo, this will throw IO exceptions if something IO bad happens
    /// </summary>
    /// <param name="fullpath"></param>
    public QRels(String fullpath) throws Exception{
        File file = new File(fullpath);
        BufferedReader sr = new BufferedReader(new FileReader(file));
        String line;
        while((line = sr.readLine()) != null){
            String[] fields = line.split("\\s+");
            // should be "query-num unknown doc-id rel-judgment"
            if ( fields.length != 4 )
            {
                throw new Exception( "input should have 4 columns" );
            }
            String queryID = fields[0];
            String docID = fields[2];
            int relevant = Integer.parseInt(fields[3]);
            judgments.AddJudgement( queryID, docID, relevant );
        }
        sr.close();
    }
}
