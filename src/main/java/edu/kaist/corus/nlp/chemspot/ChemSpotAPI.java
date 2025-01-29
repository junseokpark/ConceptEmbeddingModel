package edu.kaist.corus.nlp.chemspot;

import de.berlin.hu.chemspot.ChemSpot;
import de.berlin.hu.chemspot.ChemSpotFactory;
import de.berlin.hu.chemspot.Mention;

/**
 * Created by Junseok Park
 */


public class ChemSpotAPI {
    public static void Chemspot(String input){
        try
        {ChemSpot tagger = ChemSpotFactory.createChemSpot("conf/chemspot/dict.zip", "conf/chemspot/ids.zip", "conf/chemspot/multiclass.bin");
           // BufferedReader in = new BufferedReader(new FileReader("./"+input));
            //BufferedWriter out = new BufferedWriter(new FileWriter("./"+output, true));

            //out.write("Entity|Startpoint|Endpoint|CHID|MESH|ChEBI|CAS|InChI|DrugBank\n");

            //String s;

            //while ((s=in.readLine())!=null){
            for (Mention mention : tagger.tag(input)) {
                System.out.println(mention.getText()+"|"+mention.getStart()+"|"+mention.getEnd()+"|"+mention.getCHID()+"|"+
                        mention.getMESH()+"|"+mention.getCHEB()+"|"+mention.getCAS()+"|"+mention.getINCH()+"|"+mention.getDRUG()+"\n");
            }
            //}
            //out.close();
            //in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws Exception
    {

        //ChemNLP chemNLP = new ChemNLP();


        //ChemSpot tagger = chemNLP.init("conf/chemspot");

        //String s=chemNLP.Chemspot("Similarly, Sertoli cells from 11-day-old rats treated daily with LHRH agonist for 5 days in culture, showed no inhibition of aromatase activity after a 4-h stimulation with FSH or (Bu)2cAMP. ",tagger);
        //System.out.print(s);

        Chemspot("Imatinib");
    }
}
