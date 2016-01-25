import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
import dataStructure.Query;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import org.junit.Test;
import org.w3c.dom.Document;
import rdbms.RDBMS;
import simplenlg.features.Feature;
import simplenlg.features.Tense;
import simplenlg.framework.*;
import simplenlg.lexicon.Lexicon;
import simplenlg.lexicon.XMLLexicon;
import simplenlg.phrasespec.*;
import simplenlg.realiser.english.Realiser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SimpleNLGTest {

    @Test
    public void test() throws Exception {
        LexicalizedParser lexiParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
        RDBMS db = new RDBMS("mas");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document tokens = builder.parse(new File("NaLIRWeb/src/zfiles/tokens.xml"));

        String query1 = "return me the homepage of SIGMOD. ";
        String query2 = "return me the conferences in database area. ";
        String query3 = "return me the authors who published papers in SIGMOD after 2005. ";
        String query4 = "return me the authors from \"Tel Aviv University\" who published papers in VLDB. ";

        String ans3 = "\"Amr Magdy\" is an author who published \"Exploiting Geo-tagged Tweets to Understand Localized Language Diversity.\" in SIGMOD in 2014";

        String querySentence = ans3;
        Query query = new Query(querySentence, db.schemaGraph);

        components.StanfordNLParser.parse(query, lexiParser);
        components.NodeMapper.phraseProcess(query, db, tokens);
        components.EntityResolution.entityResolute(query);
        components.TreeStructureAdjustor.treeStructureAdjust(query, db);
        components.Explainer.explain(query);
        components.SQLTranslator.translate(query, db);

        ParseTree queryTree = query.originalParseTree;

        Lexicon lexicon = Lexicon.getDefaultLexicon();
        NLGFactory nlgFactory = new NLGFactory(lexicon);
        Realiser realiser = new Realiser(lexicon);

        ArrayList<ParseTreeNode> orderedOriginalTreeNodes = new ArrayList<>();
        for (ParseTreeNode originalParseTreeNode : queryTree.allNodes) {
            orderedOriginalTreeNodes.add(originalParseTreeNode);
        }
        Collections.sort(orderedOriginalTreeNodes, new Comparator<ParseTreeNode>() {
            public int compare(ParseTreeNode o1, ParseTreeNode o2) {
                return o1.wordOrder - o2.wordOrder;
            }
        });

        DocumentElement sentence = nlgFactory.createSentence();
        for (ParseTreeNode parseTreeNode : orderedOriginalTreeNodes) {
            if (parseTreeNode.parent != null) {
                NLGElement phraseElement = null;
                if (parseTreeNode.pos.startsWith("VB")) {
                    phraseElement = nlgFactory.createVerbPhrase(parseTreeNode.label);
                } else if (parseTreeNode.pos.startsWith("JJ")) {
                    phraseElement = nlgFactory.createAdjectivePhrase(parseTreeNode.label);
                } else if (parseTreeNode.pos.startsWith("NN")) {
                    phraseElement = nlgFactory.createNounPhrase(parseTreeNode.label);
                } else if (parseTreeNode.pos.startsWith("RB")) {
                    phraseElement = nlgFactory.createAdverbPhrase(parseTreeNode.label);
                } else {
                    phraseElement = nlgFactory.createStringElement(parseTreeNode.label);
                }
                sentence.addComponent(phraseElement);
            }
        }

        String output = realiser.realiseSentence(sentence);  //Realiser created earlier
        System.out.println();
        System.out.println(sentence.printTree(""));
        System.out.println(output);
    }

    @Test
    public void testSentenceBuilder() throws Exception {
        LexicalizedParser lexiParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
        RDBMS db = new RDBMS("mas");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document tokens = builder.parse(new File("NaLIRWeb/src/zfiles/tokens.xml"));

        String query1 = "return me the homepage of SIGMOD. ";
        String query2 = "return me the conferences in database area. ";
        String query3 = "return me the authors who published papers in SIGMOD after 2005. ";
        String query4 = "return me the authors from \"Tel Aviv University\" who published papers in VLDB. ";

        String ans3 = "\"Amr Magdy\" is an author who published \"Exploiting Geo-tagged Tweets to Understand Localized Language Diversity.\" in SIGMOD in 2014";

        String querySentence = ans3;
        Query query = new Query(querySentence, db.schemaGraph);

        components.StanfordNLParser.parse(query, lexiParser);
        components.NodeMapper.phraseProcess(query, db, tokens);
        components.EntityResolution.entityResolute(query);
        components.TreeStructureAdjustor.treeStructureAdjust(query, db);
        components.Explainer.explain(query);
        components.SQLTranslator.translate(query, db);

        ParseTree queryTree = query.originalParseTree;
        String output = SentenceBuilder.getInstance().buildSentence(queryTree, Collections.<Integer, String>emptyMap());

        System.out.println();
        System.out.println(output);
    }


    @Test
    public void test2() throws Exception {
        System.out.println("Starting...");
        Lexicon lexicon = Lexicon.getDefaultLexicon();
        NLGFactory nlgFactory = new NLGFactory(lexicon);
        Realiser realiser = new Realiser(lexicon);

        SPhraseSpec s1 = nlgFactory.createClause("I",
                "bought", "a new widget engine");
        s1.setFeature(Feature.TENSE, Tense.PAST);

        SPhraseSpec s2 = nlgFactory.createClause("", "created");
        NPPhraseSpec object1 = nlgFactory.createNounPhrase("product A");
        NPPhraseSpec object2 = nlgFactory.createNounPhrase("product B");
        NPPhraseSpec object3 = nlgFactory.createNounPhrase("product C");

        CoordinatedPhraseElement cc = nlgFactory.createCoordinatedPhrase();
        cc.addCoordinate(object1);
        cc.addCoordinate(object2);
        cc.addCoordinate(object3);

        s2.setObject(cc);
        s2.setFeature(Feature.TENSE, Tense.PAST);

        s2.setFeature(Feature.COMPLEMENTISER, ", which"); // non-restrictive?
        s1.addComplement(s2);

        String output = realiser.realiseSentence(s1);
        System.out.println(output);
    }

    @Test
    public void test3() throws Exception {
        // below is a simple complete example of using simplenlg V4
        // afterwards is an example of using simplenlg just for morphology

        // set up
        Lexicon lexicon = new XMLLexicon();                          // default simplenlg lexicon
        NLGFactory nlgFactory = new NLGFactory(lexicon);             // factory based on lexicon

        // create sentences
        //      "John did not go to the bigger park. He played football there."
        NPPhraseSpec thePark = nlgFactory.createNounPhrase("the", "park");   // create an NP
        AdjPhraseSpec bigp = nlgFactory.createAdjectivePhrase("big");        // create AdjP
        bigp.setFeature(Feature.IS_COMPARATIVE, true);                       // use comparative form ("bigger")
        thePark.addModifier(bigp);                                        // add adj as modifier in NP
        // above relies on default placement rules.  You can force placement as a premodifier
        // (before head) by using addPreModifier
        PPPhraseSpec toThePark = nlgFactory.createPrepositionPhrase("to");    // create a PP
        toThePark.setObject(thePark);                                     // set PP object
        // could also just say nlgFactory.createPrepositionPhrase("to", the Park);

        SPhraseSpec johnGoToThePark = nlgFactory.createClause("John",      // create sentence
                "go", toThePark);

        johnGoToThePark.setFeature(Feature.TENSE,Tense.PAST);              // set tense
        johnGoToThePark.setFeature(Feature.NEGATED, true);                 // set negated

        // note that constituents (such as subject and object) are set with setXXX methods
        // while features are set with setFeature

        DocumentElement sentence = nlgFactory                                                   // create a sentence DocumentElement from SPhraseSpec
                .createSentence(johnGoToThePark);

        // below creates a sentence DocumentElement by concatenating strings
        StringElement hePlayed = new StringElement("he played");
        StringElement there = new StringElement("there");
        WordElement football = new WordElement("football");

        DocumentElement sentence2 = nlgFactory.createSentence();
        sentence2.addComponent(hePlayed);
        sentence2.addComponent(football);
        sentence2.addComponent(there);

        // now create a paragraph which contains these sentences
        DocumentElement paragraph = nlgFactory.createParagraph();
        paragraph.addComponent(sentence);
        paragraph.addComponent(sentence2);

        // create a realiser.  Note that a lexicon is specified, this should be
        // the same one used by the NLGFactory
        Realiser realiser = new Realiser(lexicon);
        //realiser.setDebugMode(true);     // uncomment this to print out debug info during realisation
        NLGElement realised = realiser.realise(paragraph);

        System.out.println(realised.getRealisation());

        // end of main example

        // second example - using simplenlg just for morphology
        // below is clumsy as direct access to morphology isn't properly supported in V4.2
        // hopefully will be better supported in later versions

        // get word element for "child"
        WordElement word = (WordElement) nlgFactory.createWord("child", LexicalCategory.NOUN);
        // create InflectedWordElement from word element
        InflectedWordElement inflectedWord = new InflectedWordElement(word);
        // set the inflected word to plural
        inflectedWord.setPlural(true);
        // realise the inflected word
        String result = realiser.realise(inflectedWord).getRealisation();

        System.out.println(result);
    }
}
