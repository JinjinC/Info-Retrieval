/**
 * Created by Jinjin on 2017-03-09.
 * Based on the CSharp code given by Mark D. Smucker
 */
import java.util.HashMap;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

public class Results {
    public class Result implements Comparable<Result>{
        public Result(String docID, double score, int rank)
        {
            this._docID = docID;
            this._score = score;
            this._rank = rank;
        }

        private String _docID;
        private Double _score;
        private int _rank;

        public String docID()
        {
            return _docID ;
        }
        public Double score()
        {
            return _score ;
        }

        public int rank()
        {
            return _rank ;
        }

        // For IComparable, we'll sort from high to low score,
        // if the scores are the same, then we sort from high docno to low docno
        // This is what TREC eval does.  Checked on trec 9 web to work.
        // as of 10/14/2011, I think sorting from high to low docno may be
        // backwards.
        // OKAy, this is what trec_eval does (as far as I can tell):
        //static int
        //comp_sim_docno (ptr1, ptr2)
        //TEXT_TR *ptr1;
        //TEXT_TR *ptr2;
        //{
        //    if (ptr1->sim > ptr2->sim)
        //        return (-1);
        //    if (ptr1->sim < ptr2->sim)
        //        return (1);
        //    return (strcmp (ptr2->docno, ptr1->docno));
        //}
        //
        // so that is a descending sort on score and docno
        //
        //@Override
        public int compareTo(Result rhs) {
            Result lhs = this;
            int scoreCompare = -1 * lhs.score().compareTo( rhs.score() );
            if ( scoreCompare == 0 )
            {
                return -1 * lhs.docID().compareTo( rhs.docID() ) ;
            }
            else
            {
                return scoreCompare ;
            }
        }
    }

    /// <summary>
    /// holds keys of queryID and docID to make sure no dupes are added
    /// </summary>
    private HashMap<String, Object> tupleKeys ;
    /// <summary>
    /// keyed by queryID to an ArrayList of the queries' results.
    /// </summary>
    private Hashtable<String, ArrayList<Result>> query2results ;
    private Hashtable<String, Boolean> query2isSorted ;

    public  Results()
    {
        this.tupleKeys = new HashMap<String, Object>() ;
        this.query2results = new Hashtable<String, ArrayList<Result>>() ;
        this.query2isSorted = new Hashtable<String, Boolean>() ;
    }

    public void AddResult( String queryID, String docID, double score, int rank ) throws Exception
    {
        // be a bit careful about catching a bad mistake
        String key = this.GenerateTupleKey( queryID, docID ) ;
        if ( this.tupleKeys.containsKey( key ) )
            throw new Exception( "Cannot have duplicate queryID and docID data points" ) ;
        this.tupleKeys.put( key, null ) ;

        // Add to database
        ArrayList<Result> results = null ;
        if ( this.query2results.containsKey( queryID ) )
        {
            results = (ArrayList<Result>)this.query2results.get(queryID) ;
        }
        else
        {
            results = new ArrayList<Result>() ;
            this.query2results.put( queryID, results ) ;
            this.query2isSorted.put( queryID, false ) ;
        }
        Result result = new Result( docID, score, rank ) ;
        results.add( result ) ;
    }

    public String GenerateTupleKey( String queryID, String docID )
    {
        return queryID + "-" + docID ;
    }

    /// <summary>
    /// Returns the results for queryID sorted by score
    /// </summary>
    /// <param name="queryID"></param>
    /// <returns></returns>
    public ArrayList<Result> QueryResults( String queryID ) throws Exception
    {
        if ( ! this.query2results.containsKey( queryID ) )
            throw new Exception( "no such queryID in results" ) ;
        ArrayList<Result> results = (ArrayList)this.query2results.get(queryID) ;
        if ( ! (boolean)this.query2isSorted.get(queryID) )
        {
            Collections.sort(results);
            this.query2isSorted.replace(queryID, true) ;
        }
        return results ;
    }
    /// <summary>
    /// returns the collection of QueryIDs
    /// </summary>
    /// <returns></returns>
    public Enumeration<String> QueryIDs(){
        return this.query2results.keys() ;
    }
    public boolean QueryIDExists( String queryID ){
        return this.query2results.containsKey( queryID ) ;
    }
}
