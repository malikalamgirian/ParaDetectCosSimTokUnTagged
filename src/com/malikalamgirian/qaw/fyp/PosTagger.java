/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.malikalamgirian.qaw.fyp;

import edu.stanford.nlp.tagger.maxent.*;
import java.io.File;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

/**
 *
 * @author wasif
 */
public class PosTagger {

    public PosTagger(Properties properties) throws Exception {
        /*
         * Set state of proper Properties instance field
         */
        this.properties = properties;
        /*
         * This also sets instance variable "stop_Word_Removed_XML_File_URL"
         */
        this.setTagged_Strings_XML_File_URL(properties);

        /*
         * Instantiate Local variables
         */
        /* Initialize the tagger here */
        tagger = new MaxentTagger("../../../stanford_Tagger_Parser/"
                + "stanford-postagger-full-2010-04-30/stanford-postagger-full-2010-04-30"
                + "/models/left3words-wsj-0-18.tagger");

        /*
         * call Process method
         */
        process();
    }
    /*
     * Declarations
     */
    private Properties properties;
    private String tagged_Strings_XML_File_URL;
    /*
     * The MaxentTagger Reference variable
     */
    private MaxentTagger tagger;

    private void process() throws Exception {
        /*
         * Local Declarations
         */
        Document doc;

        Element root;

        NodeList pairs,
                string1,
                string2;

        Node pair;

        String stringStr1,
                stringStr2,
                taggedString1,
                taggedString2;

        try {
            System.out.println("I have entered the PosTagger.process() : " + tagger.tagString("I have entered the PosTagger.process()."));

            System.out.println("properties.getInput_XML_File_URL() is : " + properties.getInput_XML_File_URL());
            System.out.println("properties.getTagged_Strings_XML_File_URL() is : " + properties.getTagged_Strings_XML_File_URL());

            /*
             * 1.Get XML Document, for <code>Properties.input_XML_File_URL</code>
             */
            doc = XMLProcessor.getXMLDocumentForXMLFile(properties.getInput_XML_File_URL());

            /*
             * 2. Get Document Element
             */
            root = doc.getDocumentElement();

            /*
             * 3. Select all "String1", "String2", "Pair" pairs
             */
            string1 = root.getElementsByTagName("String1");
            string2 = root.getElementsByTagName("String2");
            pairs = root.getElementsByTagName("Pair");

            /*
             * 4. Loop through all pairs
             *    Create final tagged tree
             */
            for (int i = 0; i < pairs.getLength(); i++) {

                stringStr1 = string1.item(i).getTextContent();
                stringStr2 = string2.item(i).getTextContent();
                pair = pairs.item(i);

                taggedString1 = removePosFromTaggedString(tagger.tagString(stringStr1), " ", "_");
                taggedString2 = removePosFromTaggedString(tagger.tagString(stringStr2), " ", "_");

                addTaggedPair(doc, root, pair, taggedString1, taggedString2);

                /*
                 * TEST
                 *
                 * Final validation testing
                 */
                if (i == 12) {
                    System.out.println("FINAL TEST : \tTagger" +
                            "\n\tString1 : " + stringStr1 +
                            "\n\tString2 : " + stringStr2 +
                            "\n\ttaggedString1 : " + taggedString1 +
                            "\n\ttaggedString2 : " + taggedString2 );
                }

                System.out.println((i + 1) + " / " + pairs.getLength() + "\n");
            }

            /*
             * 5. Transform the tree, to "tagged_Strings_XML_File_URL"
             */
            XMLProcessor.transformXML(doc, new StreamResult(new File(this.tagged_Strings_XML_File_URL)));

        } catch (Exception e) {
            throw new Exception("PosTagger : Process :"
                    + e + " : " + e.getMessage());
        }
    }

    /*
     * Helper Methods
     */
    private String getTagged_Strings_XML_File_URL() {
        return tagged_Strings_XML_File_URL;
    }

    private void setTagged_Strings_XML_File_URL(Properties properties) throws Exception {
        /*
         * Local Declarations
         */
        String suffixToAdd,
                newURL;
        try {
            /*
             * add Suffix
             */
            suffixToAdd = "_tagged";

            /*
             * call FileSystemManager.addSuffixToFileURL
             */
            newURL = FileSystemManager.addSuffixToFileURL(properties.getInput_XML_File_URL(), suffixToAdd, null);
            /*
             * set instance state
             */
            tagged_Strings_XML_File_URL = newURL;
            /*
             * set "properties" instance field value
             */
            properties.setTagged_Strings_XML_File_URL(newURL);

        } catch (Exception e) {
            throw new Exception("PosTagger : setTagged_Strings_XML_File_URL : "
                    + e + " : " + e.getMessage());
        }

    }

    private boolean addTaggedPair(Document doc, Element root, Node pair, String taggedString1, String taggedString2) throws Exception {
        /*
         * Local Declarations
         */
        Element taggedPair,
                taggedStr1,
                taggedStr2;
        try {
            /*
             * Creates following structure
             * 
             * <TaggedPair>
             *      <TaggedString1></TaggedString1>
             *      <TaggedString2></TaggedString2>
             * </TaggedPair>
             */
            taggedPair = doc.createElement("TaggedPair");

            taggedStr1 = doc.createElement("TaggedString1");
            taggedStr2 = doc.createElement("TaggedString2");

            taggedStr1.setTextContent(taggedString1);
            taggedStr2.setTextContent(taggedString2);

            taggedPair.appendChild(taggedStr1);
            taggedPair.appendChild(taggedStr2);

            pair.appendChild(taggedPair);

            root.normalize();

            return true;

        } catch (Exception e) {
            throw new Exception("PosTagger : addTaggedPair :"
                    + e + " : " + e.getMessage());
        }
    }

    private String removePosFromTaggedString(String tagged_String, String tokenizerChar, String intraLexemeSeparatorCharacter) throws Exception {
        /*
         * Local Declarations
         */
        String pos_Removed_String = "",
                lexemes[] = null,
                token = null;

        try {
            /*
             * Get lexemes
             */
            lexemes = tagged_String.split(tokenizerChar);

            /*
             * loop and make Token based string
             */
            for (int i = 0; i < lexemes.length; i++) {
                /*
                 * Extract token
                 */
                try{
                    token = lexemes[i].substring(0, lexemes[i].lastIndexOf(intraLexemeSeparatorCharacter));
                }
                catch(IndexOutOfBoundsException ex){
                    /*
                     * Case for no intraLexemeSeparatorCharacter found.
                     */
                     token =  lexemes[i].substring(0, lexemes[i].length());
                }

                /*
                 * Make String
                 */
                pos_Removed_String += token + tokenizerChar;

            }

            /*
             * Remove last tokenizerChar
             */
            pos_Removed_String = pos_Removed_String.substring(0, pos_Removed_String.length() - 1);


        } catch (Exception e) {
            throw new Exception("StandardSimilarityAnalysed : removePosFromTaggedString : "
                    + e + " : " + e.getMessage());
        }

        return pos_Removed_String;
    }
}
