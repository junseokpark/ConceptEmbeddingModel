package edu.kaist.corus.nlp.model;

import org.codehaus.jackson.annotate.JsonProperty;
import java.util.List;

/**
 * Created by Junseok Park on 2017-07-13.
 */
public class MoaraGene {
    @JsonProperty("programName")
    private String programName;

    @JsonProperty("processedDate")
    private String processedDate;

    @JsonProperty("programVersion")
    private String programVersion;

    @JsonProperty("result")
    private List<MoaraGeneResult> result;


    @Override
    public String toString() {
        return getResult().toString();
    }


    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getProcessedDate() {
        return processedDate;
    }

    public void setProcessedDate(String processedDate) {
        this.processedDate = processedDate;
    }

    public String getProgramVersion() {
        return programVersion;
    }

    public void setProgramVersion(String programVersion) {
        this.programVersion = programVersion;
    }

    public List<MoaraGeneResult> getResult() {
        return result;
    }

    public void setResult(List<MoaraGeneResult> result) {
        this.result = result;
    }
}


