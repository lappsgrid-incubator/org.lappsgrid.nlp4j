package org.anc.lapps.nlp4j;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.lappsgrid.discriminator.Discriminators.Uri;

/**
 * @author Alexandru Mahmoud
 */
public class NLP4JCustomTrainTest
{
    private NLP4JCustomTrain nlp4JCustomTrain;

    @Before
    public void setup()
    {
        nlp4JCustomTrain = new NLP4JCustomTrain();
    }

    @After
    public void cleanup()
    {
        nlp4JCustomTrain = null;

    }
    @Test
    public void testMetadata()
    {
        String jsonMetadata = nlp4JCustomTrain.getMetadata();
        assertNotNull("service.getMetadata() returned null", jsonMetadata);

        Data data = Serializer.parse(jsonMetadata, Data.class);
        assertNotNull("Unable to parse metadata json.", data);
        assertNotSame(data.getPayload().toString(), Discriminators.Uri.ERROR, data.getDiscriminator());

        ServiceMetadata metadata = new ServiceMetadata((Map) data.getPayload());

        assertEquals("Vendor is not correct", "http://www.lappsgrid.org", metadata.getVendor());
        assertEquals("Name is not correct", NLP4JCustomTrain.class.getName(), metadata.getName());
        assertEquals("Version is not correct.","1.0.0-SNAPSHOT" , metadata.getVersion());
        assertEquals("License is not correct", Discriminators.Uri.APACHE2, metadata.getLicense());

        IOSpecification produces = metadata.getProduces();
        assertEquals("Produces encoding is not correct", "UTF-8", produces.getEncoding());
        assertEquals("Too many annotation types produced", 0, produces.getAnnotations().size());
        assertEquals("Too many output formats", 1, produces.getFormat().size());
        assertEquals("LIF not produced", Discriminators.Uri.LAPPS, produces.getFormat().get(0));

        IOSpecification requires = metadata.getRequires();
        assertEquals("Requires encoding is not correct", "UTF-8", requires.getEncoding());
        assertEquals("Requires Discriminator is not correct", Discriminators.Uri.GET, requires.getFormat().get(0));
    }

    @Test
    public void testErrorInput()
    {
        System.out.println("NLP4JCustomTrainTest.testErrorInput");
        String message = "This is an error message";
        Data<String> data = new Data<>(Uri.ERROR, message);
        String json = nlp4JCustomTrain.execute(data.asJson());
        assertNotNull("No JSON returned from the service", json);

        data = Serializer.parse(json, Data.class);
        assertEquals("Invalid discriminator returned", Uri.ERROR, data.getDiscriminator());
        assertEquals("The error message has changed.", message, data.getPayload());
    }

    @Test
    public void testInvalidDiscriminator()
    {
        System.out.println("NLP4JCustomTrainTest.testInvalidDiscriminator");
        Data<String> data = new Data<>(Uri.QUERY, "");
        String json = nlp4JCustomTrain.execute(data.asJson());
        assertNotNull("No JSON returned from the service", json);
        data = Serializer.parse(json, Data.class);
        assertEquals("Invalid discriminator returned: " + data.getDiscriminator(), Uri.ERROR, data.getDiscriminator());
        System.out.println(data.getPayload());
    }

    @Test
    public void testExecute()
    {
        System.out.println("nlp4JCustomTrain.testExecute");

        String trainTxt = "";
        String devTxt = "";

        try
        {
            trainTxt = readFile("src/test/resources/test-samples/sample-trn.tsv");
            devTxt = readFile("src/test/resources/test-samples/sample-dev.tsv");
        }
        catch (IOException e)
        {
            throw new RuntimeException("A problem occurred in the handling of the test input files.", e);
        }

        Map<String,String> payload = new HashMap<>();
        payload.put("train", trainTxt);
        payload.put("development", devTxt);
        String jsonPayload = Serializer.toJson(payload);

        Data<String> data = new Data<>(Discriminators.Uri.GET, jsonPayload);

        // Configuration based on config-train-sample-optimized.xml
        data.setParameter("tsv-indices", "1,2,3,4,5,6");
        data.setParameter("tsv-fields","form,lemma,pos,feats,dhead,deprel");
        data.setParameter("algorithm", "adagrad-mini-batch");
        data.setParameter("regularization","0.00001");
        data.setParameter("rate","0.02");
        data.setParameter("cutoff","0");
        data.setParameter("lols-fixed","0");
        data.setParameter("lols-decaying","0.95");
        data.setParameter("batch-size","1");
        data.setParameter("max-epoch", "3");
        data.setParameter("bias","0");

        data.setParameter("1-f0-source","i");
        data.setParameter("1-f0-field", "lemma");

        data.setParameter("2-f0-source","j");
        data.setParameter("2-f0-field", "lemma");

        data.setParameter("3-f0-source","i");
        data.setParameter("3-f0-field", "part_of_speech_tag");

        data.setParameter("4-f0-source","j");
        data.setParameter("4-f0-field", "part_of_speech_tag");

        data.setParameter("5-f0-source","i");
        data.setParameter("5-f0-field", "part_of_speech_tag");
        data.setParameter("5-f1-source","i");
        data.setParameter("5-f1-field", "lemma");

        data.setParameter("6-f0-source","j");
        data.setParameter("6-f0-field", "part_of_speech_tag");
        data.setParameter("6-f1-source","j");
        data.setParameter("6-f1-field", "lemma");

        data.setParameter("7-f0-source","i");
        data.setParameter("7-f0-field", "part_of_speech_tag");
        data.setParameter("7-f1-source","j");
        data.setParameter("7-f1-field", "part_of_speech_tag");

        data.setParameter("8-f0-source","i");
        data.setParameter("8-f0-field", "part_of_speech_tag");
        data.setParameter("8-f1-source","j");
        data.setParameter("8-f1-field", "lemma");

        data.setParameter("9-f0-source","i");
        data.setParameter("9-f0-field", "lemma");
        data.setParameter("9-f1-source","j");
        data.setParameter("9-f1-field", "part_of_speech_tag");


        data.setParameter("10-f0-source","i");
        data.setParameter("10-f0-field", "lemma");
        data.setParameter("10-f1-source","j");
        data.setParameter("10-f1-field", "lemma");

        data.setParameter("mode", "dep");
        data.setParameter("saveModel", "myModel");

        String response = nlp4JCustomTrain.execute(data.asJson());
        System.out.println(response);
    }


    /** This method will read a text file from a path, and output its contents as a String.
     *
     * @param path The path to the text file that should be read
     * @return A String representing the contents of the text file.
     */
    protected String readFile(String path) throws IOException
    {
        StringBuilder output = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line = br.readLine();
        while (line != null) {
            output.append(line).append("\r\n");
            line = br.readLine();
        }
        br.close();
        return output.toString();
    }

}
