package edu.kaist.corus.nlp.moara;

/**
 * Created by kmkim on 4/5/17.
 */

import edu.kaist.corus.nlp.model.NERResult;
import moara.mention.MentionConstant;
import moara.mention.functions.GeneRecognition;
import moara.mention.entities.GeneMention;
import moara.normalization.functions.ExactMatchingNormalization;
import moara.normalization.entities.GenePrediction;
import moara.util.text.StringUtil;
import moara.util.Constant;
import moara.bio.entities.Organism;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.InitializingBean;

/**a
 * Created by kmkim on 2017-03-30
 */
public class MoaraAPI {
    public JSONObject execute(String input) throws Exception {

        String outline = "";
        String returnline = "";
        //moara.util.EnvironmentVariable.setMoaraHome("/home/ldapusers/kmkim/IdeaProjects/corus/corus-nlp/");
        //moara.util.EnvironmentVariable.setMoaraHome("conf/moara/");

        moara.util.EnvironmentVariable.setMoaraHome(getClass().getClassLoader().getResource("conf/moara/").toString().replace("%20"," ").replace("file:",""));

        long start_time = System.currentTimeMillis();

        JSONObject outputJson = new JSONObject();
        JSONArray resultArray = new JSONArray();
        JSONObject resultJson = new JSONObject();

        try {

            // Extracting...
            GeneRecognition gr = new GeneRecognition();
            ArrayList<GeneMention> gms = gr.extract(MentionConstant.MODEL_BC2, input);
            // Listing mentions...
            for (int i = 0; i < gms.size(); i++) {
                GeneMention gm = gms.get(i);
            }
            // Normalizing mentions...
            Organism human = new Organism(Constant.ORGANISM_HUMAN);
            ExactMatchingNormalization gn = new ExactMatchingNormalization(human);
            gms = gn.normalize(input, gms);
            // Listing normalized identifiers...
            StringUtil su = new StringUtil();
            for (int i = 0; i < gms.size(); i++) {
                GeneMention gm = gms.get(i);

                if (gm.GeneIds().size() > 0) {
                    for (int j = 0; j < gm.GeneIds().size(); j++) {
                        GenePrediction gp = gm.GeneIds().get(j);
                        try {
                            if (gm.GeneId().GeneId().equals(gp.GeneId())) {
                                outline = gm.Start() + "|" + gm.End() + "|" + gm.Text() + "|" + gp.GeneId() + "|" + gp.OriginalSynonym();
                                //System.out.println(outline);
                                returnline += outline+"\n";

                                resultJson = new JSONObject();
                                resultJson.put("originalWord",gm.Text());
                                resultJson.put("startPoint",gm.Start());
                                resultJson.put("endPoint",gm.End());
                                resultJson.put("geneID",gp.GeneId());
                                resultJson.put("normalizedName",gp.OriginalSynonym());

                                resultArray.add(resultJson);
                                /////********//////
                            }
                        } catch (Exception e) {
                            System.out.println("outputerror");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("err");
            e.printStackTrace();
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        outputJson.put("programName","Moara");
        outputJson.put("programVersion","1.0");
        outputJson.put("result",resultArray);
        outputJson.put("processedDate",dateFormat.format(date));


        return outputJson;
    }


    public static void main( String[] args ) throws Exception {
        long start_time = System.currentTimeMillis();

        try {


            //moara.util.EnvironmentVariable.setMoaraHome("conf/moara/");
            MoaraAPI GN = new MoaraAPI();

            //String testInput = "Clinical trial of polyphenol extract, Seapolynol, isolated from Ecklonia Cava to verify its safety and positive effects on blood sugar level after the meal";


            String testInput =
                    "The purpose of this study is to learn how the immune system works in response to vaccines. " +
                            "We will give the vaccines to subjects who have cancer but have not had treatment, " +
                            "and to patients who have had chemotherapy or stem cell transplant.  " +
                            "Some patients will get vaccines while they are on treatments which boost the immune system (like the immune stimulating drug interleukin-2 or IL-2).  " +
                            "Although we have safely treated many patients with immune boosting drugs, " +
                            "we do not yet know if they improve the body's immune system to respond better to a vaccine.  " +
                            "Some healthy volunteers will also be given the vaccines in order to serve as control subjects to get a good measure of the normal immune response.  " +
                            "We will compare the patients and the healthy volunteers to study how their immune systems respond to the vaccines.  " +
                            "There are several different types of white cells in the blood.  We are interested in immune cells in the blood called T-cells.  " +
                            "These T-cells detect foreign substances in the body (like viruses and cancer cells).  " +
                            "We are trying to learn more about how the body fights these foreign substances.  " +
                            "Our goal is to develop cancer vaccines which would teach T-cells to detect and kill cancer cells better.  " +
                            "We know that in healthy people the immune system effectively protects against recurrent virus infection.  " +
                            "For example, that is why people only get \"mono\" (mononucleosis) once under normal circumstances.  " +
                            "When the body is infected with the \"mono\" virus, the immune system remembers and prevents further infection.  " +
                            "We are trying to use the immune system to prevent cancer relapse.  " +
                            "To test this, we will give two vaccines which have been used to measure these immune responses.  " +
                            "Blood samples will be studied from cancer patients and will be compared to similar samples from normal subjects." +
                            "!@#$%^&*():;?<><}{|+_)`~ " +
                            "SELECT a from b;";


            JSONObject result = GN.execute(testInput);

            System.out.println(result);


        } catch (Exception e) {
            System.out.println("err");
            e.printStackTrace();
        }
    }

}



