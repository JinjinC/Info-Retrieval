/**
 * Created by Jinjin on 2017-01-13.
 */
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;

public class GetDoc {

    public static String translateDate(String date){
        //translate date
        String dateToDate = "";
        String mm = date.substring(0, 2);
        String dd = date.substring(2, 4);
        String yy = date.substring(4, 6);
        if(mm.equals("01"))
            dateToDate += "January";
        else if(mm.equals("02"))
            dateToDate += "Feburary";
        else if(mm.equals("03"))
            dateToDate += "March";
        else if(mm.equals("04"))
            dateToDate += "April";
        else if(mm.equals("05"))
            dateToDate += "May";
        else if(mm.equals("06"))
            dateToDate += "June";
        else if(mm.equals("07"))
            dateToDate += "July";
        else if(mm.equals("08"))
            dateToDate += "August";
        else if(mm.equals("09"))
            dateToDate += "September";
        else if(mm.equals("10"))
            dateToDate += "October";
        else if(mm.equals("11"))
            dateToDate += "November";
        else if(mm.equals("12"))
            dateToDate += "December";

        dateToDate += " ";
        dateToDate += dd.charAt(1);
        dateToDate += ", ";
        dateToDate += "19" + yy;
        return dateToDate;
    }

    public static void main(String [ ] args) throws IOException
    {
        //check if arguments are sufficient. if not, exit with a help message
        if(args.length != 3){
            System.out.println("The program accepts three command line arguments:\n a path to the location of the documents and metadata store created by the first program,\n either the string \"id\" or the string \"docno\", \n and either the internal integer id of a document or a DOCNO.");
            return;
        }

        //get arguments
        String path = args[0];
        String iden = args[1];
        String cont = args[2];

        //identify using id or docno
        boolean isId = false;
        boolean isDocno = false;

        if(iden.equals("id") || iden.equals("ID")){
            isId = true;
        }
        else if(iden.equals("docno") || iden.equals("DOCNO")){
            isDocno = true;
        }
        else{
            System.out.println("the second argument is id or docno");
        }

        //record the date(used to find docs)
        String date = "123";
        //headline
        String headline = "";

        //init id and docno
        String id = "-1";
        String docno = "-1";

        //open metadata.txt
        FileInputStream fin = new FileInputStream(path + "/metadata.txt");
        InputStreamReader xover = new InputStreamReader(fin);
        BufferedReader br = new BufferedReader(xover);

        String line = br.readLine();

        //read metadata.txt
        while(line != null){
            //System.out.println(line);
            String[] text = line.split("\t");
            //look for the given id
            if(isId && text[0].equals(cont)){
                date = text[2];
                headline = text[3];
                id = cont;
                docno = text[1];
                break; //stop loop
            }
            // look for the given docno
            else if(isDocno && text[1].equals(cont)){
                date = text[2];
                headline = text[3];
                id = text[0];
                docno = cont;
                break; //stop loop
            }
            line = br.readLine();
        }

        br.close();

        //translate date
        String dateToDate = translateDate(date);

        //print out info of metadata
        System.out.println("docno: " + docno);
        System.out.println("internal id: " + id);
        System.out.println("date: " + dateToDate);
        System.out.println("headline: " + headline);
        System.out.println("raw document: ");

        //search doc
        String mm = date.substring(0, 2);
        String dd = date.substring(2, 4);
        String yy = date.substring(4, 6);

        //get the doc
        String filePath = path + "/" + yy + "/" + mm + "/" + dd + "/" + docno + ".txt";
        //print the exact doc
        FileInputStream fin1 = new FileInputStream(filePath);
        InputStreamReader xover1 = new InputStreamReader(fin1);
        BufferedReader br1 = new BufferedReader(xover1);
        String line1 = br1.readLine();
        while(line1 != null){
            System.out.println(line1);
            line1 = br1.readLine();
        }
        br1.close();
    }
}
