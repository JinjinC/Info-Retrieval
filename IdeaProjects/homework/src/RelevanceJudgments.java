/**
 * Created by Jinjin on 2017-03-09.
 * Based on the CSharp code given by Mark D. Smucker
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class RelevanceJudgments {
    /// <summary>
    /// Internal class for use by RelevanceJudgments to hold the judgements
    /// </summary>
    private static class Tuple {
        public Tuple(String queryID, String docID, int relevant)
        {
            this._queryID = queryID;
            this._docID = docID;
            this._relevant = relevant;
        }
        private String _queryID;
        private String _docID;
        private int _relevant;
        public String queryID(){

            return _queryID;
        }
        public String docID(){

            return _docID;
        }
        public int relevant(){

            return _relevant;
        }
        public static String GenerateKey( String queryID, String docID )
        {
            return queryID + "-" + docID ;
        }
        public String Key(){
            return _queryID + "-" + _docID;
        }
    }

    private Hashtable<String, Tuple> tuples;
    private Hashtable<String, ArrayList<String>> query2reldocnos;

    public RelevanceJudgments()
    {
        this.tuples = new Hashtable<String, Tuple>();
        this.query2reldocnos = new Hashtable<String, ArrayList<String>>();
    }

    public void AddJudgement( String queryID, String docID, int relevant ) throws IOException
    {
        Tuple tuple = new Tuple( queryID, docID, relevant );
        if ( this.tuples.containsKey( tuple.Key() ) )
            throw new IOException( "Cannot have duplicate queryID and docID data points" );
        this.tuples.put( tuple.Key(), tuple ) ;
        if ( tuple.relevant() != 0 )
        {
            // store the reldocnos
            ArrayList<String> tmpRelDocnos = null;
            if ( query2reldocnos.containsKey( queryID ) )
            {
                tmpRelDocnos = (ArrayList<String>)query2reldocnos.get(queryID);
            }
            else
            {
                tmpRelDocnos = new ArrayList<String>();
                query2reldocnos.put( queryID, tmpRelDocnos );
            }
            if ( !tmpRelDocnos.contains( docID ) )
                tmpRelDocnos.add( docID );
        }
    }

    /// <summary>
    /// Is the document relevant to the query?
    /// </summary>
    /// <param name="queryID"></param>
    /// <param name="docID"></param>
    /// <returns></returns>
    public boolean IsRelevant( String queryID, String docID ) throws IOException
    {
        return GetJudgment( queryID, docID, true ) != 0 ;
    }

    public int GetJudgment( String queryID, String docID ) throws IOException
    {
        return GetJudgment( queryID, docID, false ) ;
    }

    public int GetJudgment( String queryID, String docID, boolean assumeNonRelevant ) throws IOException
    {
        if ( ! this.query2reldocnos.containsKey( queryID ) )
            throw new IOException( "no relevance judgments for queryID = " + queryID ) ;

        String key = Tuple.GenerateKey( queryID, docID ) ;
        if ( ! tuples.containsKey( key ) )
        {
            if ( assumeNonRelevant )
                return 0 ;
            else
                throw new IOException( "no relevance judgement for queryID and docID" ) ;
        }
        else
        {
            Tuple tuple = (Tuple)tuples.get(key) ;
            return tuple.relevant() ;
        }
    }

    /// <summary>
    /// Number of relevant documents in collection for query
    /// </summary>
    /// <param name="queryID"></param>
    /// <returns></returns>
    public int NumRelevant( String queryID ) throws IOException
    {
        if ( this.query2reldocnos.containsKey( queryID ) )
            return ((ArrayList)this.query2reldocnos.get(queryID)).size() ;
        else
            throw new IOException( "no relevance judgments for queryID = " + queryID ) ;
    }

    /// <summary>
    /// returns the queryID strings
    /// </summary>
    /// <returns></returns>
    public Enumeration<String> QueryIDs()
    {
        return this.query2reldocnos.keys() ;
    }

    public ArrayList<String> RelDocnos( String queryID ) throws IOException
    {
        if ( this.query2reldocnos.containsKey( queryID ) )
            return (ArrayList<String>)this.query2reldocnos.get(queryID) ;
        else
            throw new IOException( "no relevance judgments for queryID = " + queryID ) ;
    }
}
