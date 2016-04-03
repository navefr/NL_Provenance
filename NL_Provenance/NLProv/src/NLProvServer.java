import Top1.DerivationTree2;
import TopKBasics.KeyMap2;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dataStructure.Block;
import dataStructure.Query;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.compiler.Parser;
import org.deri.iris.compiler.ParserException;
import org.deri.iris.evaluation.stratifiedbottomup.seminaive.SemiNaiveEvaluator;
import org.deri.iris.facts.Facts;
import org.deri.iris.facts.IFacts;
import org.deri.iris.optimisations.magicsets.MagicSets;
import org.deri.iris.optimisations.rulefilter.RuleFilter;
import org.deri.iris.rules.compiler.CompiledRule;
import org.deri.iris.rules.compiler.ICompiledRule;
import org.deri.iris.rules.compiler.RuleCompiler;
import org.deri.iris.storage.IRelation;
import org.w3c.dom.Document;
import rdbms.RDBMS;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NLProvServer {

    private static Map<IPredicate, IRelation> factMap = null;

    public static void main(String[] args) throws Exception {

        LexicalizedParser lexiParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
        RDBMS db = new RDBMS("mas");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document tokens = builder.parse(new File("NL_Provenance/NaLIRWeb/src/zfiles/tokens.xml"));

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new MainHandler());
        server.createContext("/answer", new AnswerHandler(lexiParser, db, tokens));
        server.createContext("/explanation", new ExplanationHandler(lexiParser, db, tokens));
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MainHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

            String response = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
                    "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                    "<head>\n" +
                    "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                    "<title></title>\n" +
                    "<meta name=\"keywords\" content=\"\" />\n" +
                    "<meta name=\"description\" content=\"\" />\n" +
                    "<link href=\"http://fonts.googleapis.com/css?family=Source+Sans+Pro:200,300,400,600,700,900|Quicksand:400,700|Questrial\" rel=\"stylesheet\" />\n" +
                    "<link href=\"default.css\" rel=\"stylesheet\" type=\"text/css\" media=\"all\" />\n" +
                    "<link href=\"fonts.css\" rel=\"stylesheet\" type=\"text/css\" media=\"all\" />\n" +
                    "\n" +
                    "<style>\n" +
                    "html, body\n" +
                    "\t{\n" +
                    "\t\theight: 100%;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tbody\n" +
                    "\t{\n" +
                    "\t\tmargin: 0px;\n" +
                    "\t\tpadding: 0px;\n" +
                    "\t\tbackground: #2056ac;\n" +
                    "\t\tfont-family: 'Questrial', sans-serif;\n" +
                    "\t\tfont-size: 12pt;\n" +
                    "\t\tcolor: rgba(0,0,0,.6);\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\th1, h2, h3\n" +
                    "\t{\n" +
                    "\t\tmargin: 0;\n" +
                    "\t\tpadding: 0;\n" +
                    "\t\tcolor: #404040;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tp, ol, ul\n" +
                    "\t{\n" +
                    "\t\tmargin-top: 0;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tol, ul\n" +
                    "\t{\n" +
                    "\t\tpadding: 0;\n" +
                    "\t\tlist-style: none;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tp\n" +
                    "\t{\n" +
                    "\t\tline-height: 180%;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tstrong\n" +
                    "\t{\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\ta\n" +
                    "\t{\n" +
                    "\t\tcolor: #2056ac;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\ta:hover\n" +
                    "\t{\n" +
                    "\t\ttext-decoration: none;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\n" +
                    "\t.container\n" +
                    "\t{\n" +
                    "\t\tmargin: 0px auto;\n" +
                    "\t\twidth: 1200px;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Form Style                                                                    */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t\tform\n" +
                    "\t\t{\n" +
                    "\t\t}\n" +
                    "\t\t\n" +
                    "\t\t\tform label\n" +
                    "\t\t\t{\n" +
                    "\t\t\t\tdisplay: block;\n" +
                    "\t\t\t\ttext-align: left;\n" +
                    "\t\t\t\tmargin-bottom: 0.5em;\n" +
                    "\t\t\t}\n" +
                    "\t\t\t\n" +
                    "\t\t\tform .submit\n" +
                    "\t\t\t{\n" +
                    "\t\t\t\tmargin-top: 2em;\n" +
                    "\t\t\t\tline-height: 1.5em;\n" +
                    "\t\t\t\tfont-size: 1.3em;\n" +
                    "\t\t\t}\n" +
                    "\t\t\n" +
                    "\t\t\tform input.text,\n" +
                    "\t\t\tform select,\n" +
                    "\t\t\tform textarea\n" +
                    "\t\t\t{\n" +
                    "\t\t\t\tposition: relative;\n" +
                    "\t\t\t\t-webkit-appearance: none;\n" +
                    "\t\t\t\tdisplay: block;\n" +
                    "\t\t\t\tborder: 0;\n" +
                    "\t\t\t\tbackground: #fff;\n" +
                    "\t\t\t\tbackground: rgba(255,255,255,0.75);\n" +
                    "\t\t\t\twidth: 100%;\n" +
                    "\t\t\t\tborder-radius: 0.50em;\n" +
                    "\t\t\t\tmargin: 1em 0em;\n" +
                    "\t\t\t\tpadding: 1.50em 1em;\n" +
                    "\t\t\t\tbox-shadow: inset 0 0.1em 0.1em 0 rgba(0,0,0,0.05);\n" +
                    "\t\t\t\tborder: solid 1px rgba(0,0,0,0.15);\n" +
                    "\t\t\t\t-moz-transition: all 0.35s ease-in-out;\n" +
                    "\t\t\t\t-webkit-transition: all 0.35s ease-in-out;\n" +
                    "\t\t\t\t-o-transition: all 0.35s ease-in-out;\n" +
                    "\t\t\t\t-ms-transition: all 0.35s ease-in-out;\n" +
                    "\t\t\t\ttransition: all 0.35s ease-in-out;\n" +
                    "\t\t\t\tfont-size: 1em;\n" +
                    "\t\t\t\toutline: none;\n" +
                    "\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform input.text:hover,\n" +
                    "\t\t\t\tform select:hover,\n" +
                    "\t\t\t\tform textarea:hover\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform input.text:focus,\n" +
                    "\t\t\t\tform select:focus,\n" +
                    "\t\t\t\tform textarea:focus\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tbox-shadow: 0 0 2px 1px #E0E0E0;\n" +
                    "\t\t\t\t\tbackground: #fff;\n" +
                    "\t\t\t\t}\n" +
                    "\t\t\t\t\n" +
                    "\t\t\t\tform textarea\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tmin-height: 12em;\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform .formerize-placeholder\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tcolor: #555 !important;\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform ::-webkit-input-placeholder\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tcolor: #555 !important;\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform :-moz-placeholder\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tcolor: #555 !important;\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform ::-moz-placeholder\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tcolor: #555 !important;\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform :-ms-input-placeholder\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tcolor: #555 !important;\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform ::-moz-focus-inner\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tborder: 0;\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Image Style                                                                   */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t.image\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t\tborder: 1px solid rgba(0,0,0,.1);\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.image img\n" +
                    "\t{\n" +
                    "\t\tdisplay: block;\n" +
                    "\t\twidth: 100%;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.image-full\n" +
                    "\t{\n" +
                    "\t\tdisplay: block;\n" +
                    "\t\twidth: 100%;\n" +
                    "\t\tmargin: 0 0 3em 0;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.image-left\n" +
                    "\t{\n" +
                    "\t\tfloat: left;\n" +
                    "\t\tmargin: 0 2em 2em 0;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.image-centered\n" +
                    "\t{\n" +
                    "\t\tdisplay: block;\n" +
                    "\t\tmargin: 0 0 2em 0;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.image-centered img\n" +
                    "\t{\n" +
                    "\t\tmargin: 0 auto;\n" +
                    "\t\twidth: auto;\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* List Styles                                                                   */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\tul.style1\n" +
                    "\t{\n" +
                    "\t}\n" +
                    "\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Social Icon Styles                                                            */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\tul.contact\n" +
                    "\t{\n" +
                    "\t\tmargin: 0;\n" +
                    "\t\tpadding: 2em 0em 0em 0em;\n" +
                    "\t\tlist-style: none;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tul.contact li\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t\tpadding: 0em 0.10em;\n" +
                    "\t\tfont-size: 1em;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tul.contact li span\n" +
                    "\t{\n" +
                    "\t\tdisplay: none;\n" +
                    "\t\tmargin: 0;\n" +
                    "\t\tpadding: 0;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tul.contact li a\n" +
                    "\t{\n" +
                    "\t\tcolor: #FFF;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tul.contact li a:before\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t\tbackground: #4C93B9;\n" +
                    "\t\twidth: 40px;\n" +
                    "\t\theight: 40px;\n" +
                    "\t\tline-height: 40px;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t\tcolor: rgba(255,255,255,1);\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Button Style                                                                  */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t.button\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t\tmargin-top: 2em;\n" +
                    "\t\tpadding: 0.8em 2em;\n" +
                    "\t\tbackground: #64ABD1;\n" +
                    "\t\tline-height: 1.8em;\n" +
                    "\t\tletter-spacing: 1px;\n" +
                    "\t\ttext-decoration: none;\n" +
                    "\t\tfont-size: 1em;\n" +
                    "\t\tcolor: #FFF;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.button:before\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t\tbackground: #8DCB89;\n" +
                    "\t\tmargin-right: 1em;\n" +
                    "\t\twidth: 40px;\n" +
                    "\t\theight: 40px;\n" +
                    "\t\tline-height: 40px;\n" +
                    "\t\tborder-radius: 20px;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t\tcolor: #272925;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.button-small\n" +
                    "\t{\n" +
                    "\t}\n" +
                    "\n" +
                    "\tinput[type=text] {\n" +
                    "\t\tpadding:5px; \n" +
                    "\t\tborder:2px solid #ccc; \n" +
                    "\t\t-webkit-border-radius: 5px;\n" +
                    "\t\tborder-radius: 5px;\n" +
                    "\t}\n" +
                    "\n" +
                    "\tinput[type=text]:focus {\n" +
                    "\t\tborder-color:#333;\n" +
                    "\t}\n" +
                    "\n" +
                    "\tinput[type=submit] {\n" +
                    "\t\tpadding:5px 15px; \n" +
                    "\t\tbackground:#ccc; \n" +
                    "\t\tborder:0 none;\n" +
                    "\t\tcursor:pointer;\n" +
                    "\t\t-webkit-border-radius: 5px;\n" +
                    "\t\tborder-radius: 5px; \n" +
                    "\t}\t\n" +
                    "\t\n" +
                    "/*********************************************************************************/\n" +
                    "/* Heading Titles                                                                */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t.title\n" +
                    "\t{\n" +
                    "\t\tmargin-bottom: 3em;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.title h2\n" +
                    "\t{\n" +
                    "\t\tfont-size: 2.8em;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.title .byline\n" +
                    "\t{\n" +
                    "\t\tfont-size: 1.1em;\n" +
                    "\t\tcolor: #6F6F6F#;\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Header                                                                        */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#header-wrapper\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\tbackground: #2056ac;\n" +
                    "\t}\n" +
                    "\n" +
                    "\t#header\n" +
                    "\t{\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Logo                                                                          */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#logo\n" +
                    "\t{\n" +
                    "\t\tpadding: 8em 0em 4em 0em;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#logo h1\n" +
                    "\t{\n" +
                    "\t\tdisplay: block;\n" +
                    "\t\tmargin-bottom: 0.20em;\n" +
                    "\t\tpadding: 0.20em 0.9em;\n" +
                    "\t\tfont-size: 3.5em;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#logo a\n" +
                    "\t{\n" +
                    "\t\ttext-decoration: none;\n" +
                    "\t\tcolor: #FFF;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#logo span\n" +
                    "\t{\n" +
                    "\t\ttext-transform: uppercase;\n" +
                    "\t\tfont-size: 2.90em;\n" +
                    "\t\tcolor: rgba(255,255,255,1);\n" +
                    "\t}\n" +
                    "\n" +
                    "\t#logo span a\n" +
                    "\t{\n" +
                    "\t\tcolor: rgba(255,255,255,0.8);\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Menu                                                                          */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#menu\n" +
                    "\t{\n" +
                    "\t\theight: 60px;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#menu ul\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t\tpadding: 0em 2em;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#menu li\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#menu li a, #menu li span\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t\tpadding: 0em 1.5em;\n" +
                    "\t\ttext-decoration: none;\n" +
                    "\t\tfont-size: 0.90em;\n" +
                    "\t\tfont-weight: 600;\n" +
                    "\t\ttext-transform: uppercase;\n" +
                    "\t\tline-height: 60px;\n" +
                    "\t\toutline: 0;\n" +
                    "\t\tcolor: #FFF;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#menu li:hover a, #menu li.active a, #menu li.active span\n" +
                    "\t{\n" +
                    "\t\tbackground: #FFF;\n" +
                    "\t\tborder-radius: 7px 7px 0px 0px;\n" +
                    "\t\tcolor: #2056ac;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#menu .current_page_item a\n" +
                    "\t{\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Banner                                                                        */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#banner\n" +
                    "\t{\n" +
                    "\t\tpadding-top: 5em;\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Wrapper                                                                       */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\n" +
                    "\t.wrapper\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\tpadding: 0em 0em 5em 0em;\n" +
                    "\t\tbackground: #FFF;\n" +
                    "\t}\n" +
                    "\n" +
                    "\t#wrapper1\n" +
                    "\t{\n" +
                    "\t\tbackground: #FFF;\n" +
                    "\t}\n" +
                    "\n" +
                    "\t#wrapper2\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\tbackground: #F3F3F3;\n" +
                    "\t\tpadding: 5em 0em;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t/* Double Border */\n" +
                    "\t.ta6 {\n" +
                    "\t\tborder: 3px double #CCCCCC;\n" +
                    "\t\twidth: 700px;\n" +
                    "\t\theight: 80px;\n" +
                    "\t}\n" +
                    "\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Welcome                                                                       */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#welcome\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\twidth: 1000px;\n" +
                    "\t\tpadding: 6em 100px 0em 100px;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#welcome .content\n" +
                    "\t{\n" +
                    "\t\tpadding: 0em 8em;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#welcome .title h2\n" +
                    "\t{\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#welcome a,\n" +
                    "\t#welcome strong\n" +
                    "\t{\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Page                                                                          */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#page-wrapper\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\tbackground: #2F1E28;\n" +
                    "\t\tpadding: 3em 0em 6em 0em;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\n" +
                    "\t#page\n" +
                    "\t{\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Content                                                                       */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#content\n" +
                    "\t{\n" +
                    "\t\tfloat: left;\n" +
                    "\t\twidth: 700px;\n" +
                    "\t\tpadding-right: 100px;\n" +
                    "\t\tborder-right: 1px solid rgba(0,0,0,.1);\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Sidebar                                                                       */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#sidebar\n" +
                    "\t{\n" +
                    "\t\tfloat: right;\n" +
                    "\t\twidth: 350px;\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Footer                                                                        */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#footer\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\tpadding: 5em 0em;\n" +
                    "\t\tbackground: #E3F0F7;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\n" +
                    "\n" +
                    "\t\t\n" +
                    "\t#footer .fbox1,\n" +
                    "\t#footer .fbox2,\n" +
                    "\t#footer .fbox3\n" +
                    "\t{\n" +
                    "\t\tfloat: left;\n" +
                    "\t\twidth: 320px;\n" +
                    "\t\tpadding: 0px 40px 0px 40px;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#footer .icon\n" +
                    "\t{\n" +
                    "\t\tdisplay: block;\n" +
                    "\t\tmargin-bottom: 1em;\n" +
                    "\t\tfont-size: 3em;\n" +
                    "\t}\n" +
                    "\n" +
                    "\t\n" +
                    "\t#footer .title span\n" +
                    "\t{\n" +
                    "\t\tcolor: rgba(255,255,255,0.4);\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Copyright                                                                     */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#copyright\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\tpadding: 5em 0em;\n" +
                    "\t\tborder-top: 20px solid rgba(255,255,255,0.08);\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#copyright p\n" +
                    "\t{\n" +
                    "\t\tletter-spacing: 1px;\n" +
                    "\t\tfont-size: 0.90em;\n" +
                    "\t\tcolor: rgba(255,255,255,0.6);\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#copyright a\n" +
                    "\t{\n" +
                    "\t\ttext-decoration: none;\n" +
                    "\t\tcolor: rgba(255,255,255,0.8);\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Newsletter                                                                    */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#newsletter\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\tpadding: 8em 0em;\n" +
                    "\t\tbackground: #EDEDED;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#newsletter .title h2\n" +
                    "\t{\n" +
                    "\t\tcolor: rgba(0,0,0,0.8);\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#newsletter .content\n" +
                    "\t{\n" +
                    "\t\twidth: 600px;\n" +
                    "\t\tmargin: 0px auto;\n" +
                    "\t}\n" +
                    "\n" +
                    "</style>\n" +
                    "\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<div id=\"header-wrapper\">\n" +
                    "\t<div id=\"header\" class=\"container\">\n" +
                    "\t\t<div id=\"logo\">\n" +
                    "        \t<span class=\"icon icon-cog\"></span>\n" +
                    "\t\t\t<h1><a href=\"/\">NLProv</a></h1>\n" +
                    "\t\t</div>\n" +
                    "\t</div>\n" +
                    "</div>\n" +
                    "<div class=\"wrapper\">\n" +
                    "\t<div id=\"welcome\" class=\"container\">\n" +
                    "    \t\n" +
                    "\t<div class=\"title\">\n" +
                    "\t\t  <h2>Ask anything</h2>\n" +
                    "\t</div>\n" +
                    "\t<div>\n" +
                    "\t\t<textarea id=\"question\" class=\"ta6\"></textarea>\n" +
                    "\t</div>\n" +
                    "\t<input type=\"submit\" value=\"Submit\" id=\"submit\" class=\"button\">\n" +
                    "\t</div>\n" +
                    "</div>\n" +
                    "<div id=\"footer\">\n" +
                    "\t<div class=\"container\">\n" +
                    "\t</div>\n" +
                    "</div>\n" +
                    "<div id=\"copyright\">\n" +
                    "</div>\n" +
                    "\n" +
                    "<script type=\"text/javascript\">\n" +
                    "    document.getElementById(\"submit\").onclick = function () {\n" +
                    "        var query = document.getElementById(\"question\").value;\n" +
                    "        location.href = \"\\answer?\" + query;\n" +
                    "    };\n" +
                    "</script>" +
                    "</body>\n" +
                    "</html>\n";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }


    static class AnswerHandler implements HttpHandler {
        private LexicalizedParser lexiParser;
        private RDBMS db;
        private Document tokens;

        public AnswerHandler(LexicalizedParser lexiParser, RDBMS db, Document tokens) {
            this.lexiParser = lexiParser;
            this.db = db;
            this.tokens = tokens;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();
            List<String> values = null;
            try {
                 values = handleQuery(query);
            } catch (Exception ignored) {}
            String response = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
                    "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                    "<head>\n" +
                    "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                    "<title></title>\n" +
                    "<meta name=\"keywords\" content=\"\" />\n" +
                    "<meta name=\"description\" content=\"\" />\n" +
                    "<link href=\"http://fonts.googleapis.com/css?family=Source+Sans+Pro:200,300,400,600,700,900|Quicksand:400,700|Questrial\" rel=\"stylesheet\" />\n" +
                    "<link href=\"default.css\" rel=\"stylesheet\" type=\"text/css\" media=\"all\" />\n" +
                    "<link href=\"fonts.css\" rel=\"stylesheet\" type=\"text/css\" media=\"all\" />\n" +
                    "\n" +
                    "<style>\n" +
                    "html, body\n" +
                    "\t{\n" +
                    "\t\theight: 100%;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tbody\n" +
                    "\t{\n" +
                    "\t\tmargin: 0px;\n" +
                    "\t\tpadding: 0px;\n" +
                    "\t\tbackground: #2056ac;\n" +
                    "\t\tfont-family: 'Questrial', sans-serif;\n" +
                    "\t\tfont-size: 12pt;\n" +
                    "\t\tcolor: rgba(0,0,0,.6);\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\th1, h2, h3\n" +
                    "\t{\n" +
                    "\t\tmargin: 0;\n" +
                    "\t\tpadding: 0;\n" +
                    "\t\tcolor: #404040;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tp, ol, ul\n" +
                    "\t{\n" +
                    "\t\tmargin-top: 0;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tol, ul\n" +
                    "\t{\n" +
                    "\t\tpadding: 0;\n" +
                    "\t\tlist-style: none;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tp\n" +
                    "\t{\n" +
                    "\t\tline-height: 180%;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tstrong\n" +
                    "\t{\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\ta\n" +
                    "\t{\n" +
                    "\t\tcolor: #2056ac;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\ta:hover\n" +
                    "\t{\n" +
                    "\t\ttext-decoration: none;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\n" +
                    "\t.container\n" +
                    "\t{\n" +
                    "\t\tmargin: 0px auto;\n" +
                    "\t\twidth: 1200px;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Form Style                                                                    */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t\tform\n" +
                    "\t\t{\n" +
                    "\t\t}\n" +
                    "\t\t\n" +
                    "\t\t\tform label\n" +
                    "\t\t\t{\n" +
                    "\t\t\t\tdisplay: block;\n" +
                    "\t\t\t\ttext-align: left;\n" +
                    "\t\t\t\tmargin-bottom: 0.5em;\n" +
                    "\t\t\t}\n" +
                    "\t\t\t\n" +
                    "\t\t\tform .submit\n" +
                    "\t\t\t{\n" +
                    "\t\t\t\tmargin-top: 2em;\n" +
                    "\t\t\t\tline-height: 1.5em;\n" +
                    "\t\t\t\tfont-size: 1.3em;\n" +
                    "\t\t\t}\n" +
                    "\t\t\n" +
                    "\t\t\tform input.text,\n" +
                    "\t\t\tform select,\n" +
                    "\t\t\tform textarea\n" +
                    "\t\t\t{\n" +
                    "\t\t\t\tposition: relative;\n" +
                    "\t\t\t\t-webkit-appearance: none;\n" +
                    "\t\t\t\tdisplay: block;\n" +
                    "\t\t\t\tborder: 0;\n" +
                    "\t\t\t\tbackground: #fff;\n" +
                    "\t\t\t\tbackground: rgba(255,255,255,0.75);\n" +
                    "\t\t\t\twidth: 100%;\n" +
                    "\t\t\t\tborder-radius: 0.50em;\n" +
                    "\t\t\t\tmargin: 1em 0em;\n" +
                    "\t\t\t\tpadding: 1.50em 1em;\n" +
                    "\t\t\t\tbox-shadow: inset 0 0.1em 0.1em 0 rgba(0,0,0,0.05);\n" +
                    "\t\t\t\tborder: solid 1px rgba(0,0,0,0.15);\n" +
                    "\t\t\t\t-moz-transition: all 0.35s ease-in-out;\n" +
                    "\t\t\t\t-webkit-transition: all 0.35s ease-in-out;\n" +
                    "\t\t\t\t-o-transition: all 0.35s ease-in-out;\n" +
                    "\t\t\t\t-ms-transition: all 0.35s ease-in-out;\n" +
                    "\t\t\t\ttransition: all 0.35s ease-in-out;\n" +
                    "\t\t\t\tfont-size: 1em;\n" +
                    "\t\t\t\toutline: none;\n" +
                    "\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform input.text:hover,\n" +
                    "\t\t\t\tform select:hover,\n" +
                    "\t\t\t\tform textarea:hover\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform input.text:focus,\n" +
                    "\t\t\t\tform select:focus,\n" +
                    "\t\t\t\tform textarea:focus\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tbox-shadow: 0 0 2px 1px #E0E0E0;\n" +
                    "\t\t\t\t\tbackground: #fff;\n" +
                    "\t\t\t\t}\n" +
                    "\t\t\t\t\n" +
                    "\t\t\t\tform textarea\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tmin-height: 12em;\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform .formerize-placeholder\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tcolor: #555 !important;\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform ::-webkit-input-placeholder\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tcolor: #555 !important;\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform :-moz-placeholder\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tcolor: #555 !important;\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform ::-moz-placeholder\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tcolor: #555 !important;\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform :-ms-input-placeholder\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tcolor: #555 !important;\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform ::-moz-focus-inner\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tborder: 0;\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Image Style                                                                   */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t.image\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t\tborder: 1px solid rgba(0,0,0,.1);\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.image img\n" +
                    "\t{\n" +
                    "\t\tdisplay: block;\n" +
                    "\t\twidth: 100%;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.image-full\n" +
                    "\t{\n" +
                    "\t\tdisplay: block;\n" +
                    "\t\twidth: 100%;\n" +
                    "\t\tmargin: 0 0 3em 0;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.image-left\n" +
                    "\t{\n" +
                    "\t\tfloat: left;\n" +
                    "\t\tmargin: 0 2em 2em 0;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.image-centered\n" +
                    "\t{\n" +
                    "\t\tdisplay: block;\n" +
                    "\t\tmargin: 0 0 2em 0;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.image-centered img\n" +
                    "\t{\n" +
                    "\t\tmargin: 0 auto;\n" +
                    "\t\twidth: auto;\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* List Styles                                                                   */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\tul.style1\n" +
                    "\t{\n" +
                    "\t}\n" +
                    "\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Social Icon Styles                                                            */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\tul.contact\n" +
                    "\t{\n" +
                    "\t\tmargin: 0;\n" +
                    "\t\tpadding: 2em 0em 0em 0em;\n" +
                    "\t\tlist-style: none;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tul.contact li\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t\tpadding: 0em 0.10em;\n" +
                    "\t\tfont-size: 1em;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tul.contact li span\n" +
                    "\t{\n" +
                    "\t\tdisplay: none;\n" +
                    "\t\tmargin: 0;\n" +
                    "\t\tpadding: 0;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tul.contact li a\n" +
                    "\t{\n" +
                    "\t\tcolor: #FFF;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tul.contact li a:before\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t\tbackground: #4C93B9;\n" +
                    "\t\twidth: 40px;\n" +
                    "\t\theight: 40px;\n" +
                    "\t\tline-height: 40px;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t\tcolor: rgba(255,255,255,1);\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Button Style                                                                  */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t.button\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t\tmargin-top: 2em;\n" +
                    "\t\tpadding: 0.8em 2em;\n" +
                    "\t\tbackground: #64ABD1;\n" +
                    "\t\tline-height: 1.8em;\n" +
                    "\t\tletter-spacing: 1px;\n" +
                    "\t\ttext-decoration: none;\n" +
                    "\t\tfont-size: 1em;\n" +
                    "\t\tcolor: #FFF;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.button:before\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t\tbackground: #8DCB89;\n" +
                    "\t\tmargin-right: 1em;\n" +
                    "\t\twidth: 40px;\n" +
                    "\t\theight: 40px;\n" +
                    "\t\tline-height: 40px;\n" +
                    "\t\tborder-radius: 20px;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t\tcolor: #272925;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.button-small\n" +
                    "\t{\n" +
                    "\t}\n" +
                    "\n" +
                    "\tinput[type=text] {\n" +
                    "\t\tpadding:5px; \n" +
                    "\t\tborder:2px solid #ccc; \n" +
                    "\t\t-webkit-border-radius: 5px;\n" +
                    "\t\tborder-radius: 5px;\n" +
                    "\t}\n" +
                    "\n" +
                    "\tinput[type=text]:focus {\n" +
                    "\t\tborder-color:#333;\n" +
                    "\t}\n" +
                    "\n" +
                    "\tinput[type=submit] {\n" +
                    "\t\tpadding:5px 15px; \n" +
                    "\t\tbackground:#ccc; \n" +
                    "\t\tborder:0 none;\n" +
                    "\t\tcursor:pointer;\n" +
                    "\t\t-webkit-border-radius: 5px;\n" +
                    "\t\tborder-radius: 5px; \n" +
                    "\t}\t\n" +
                    "\t\n" +
                    "/*********************************************************************************/\n" +
                    "/* Heading Titles                                                                */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t.title\n" +
                    "\t{\n" +
                    "\t\tmargin-bottom: 3em;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.title h2\n" +
                    "\t{\n" +
                    "\t\tfont-size: 2.8em;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.title .byline\n" +
                    "\t{\n" +
                    "\t\tfont-size: 1.1em;\n" +
                    "\t\tcolor: #6F6F6F#;\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Header                                                                        */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#header-wrapper\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\tbackground: #2056ac;\n" +
                    "\t}\n" +
                    "\n" +
                    "\t#header\n" +
                    "\t{\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Logo                                                                          */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#logo\n" +
                    "\t{\n" +
                    "\t\tpadding: 8em 0em 4em 0em;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#logo h1\n" +
                    "\t{\n" +
                    "\t\tdisplay: block;\n" +
                    "\t\tmargin-bottom: 0.20em;\n" +
                    "\t\tpadding: 0.20em 0.9em;\n" +
                    "\t\tfont-size: 3.5em;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#logo a\n" +
                    "\t{\n" +
                    "\t\ttext-decoration: none;\n" +
                    "\t\tcolor: #FFF;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#logo span\n" +
                    "\t{\n" +
                    "\t\ttext-transform: uppercase;\n" +
                    "\t\tfont-size: 2.90em;\n" +
                    "\t\tcolor: rgba(255,255,255,1);\n" +
                    "\t}\n" +
                    "\n" +
                    "\t#logo span a\n" +
                    "\t{\n" +
                    "\t\tcolor: rgba(255,255,255,0.8);\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Menu                                                                          */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#menu\n" +
                    "\t{\n" +
                    "\t\theight: 60px;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#menu ul\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t\tpadding: 0em 2em;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#menu li\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#menu li a, #menu li span\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t\tpadding: 0em 1.5em;\n" +
                    "\t\ttext-decoration: none;\n" +
                    "\t\tfont-size: 0.90em;\n" +
                    "\t\tfont-weight: 600;\n" +
                    "\t\ttext-transform: uppercase;\n" +
                    "\t\tline-height: 60px;\n" +
                    "\t\toutline: 0;\n" +
                    "\t\tcolor: #FFF;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#menu li:hover a, #menu li.active a, #menu li.active span\n" +
                    "\t{\n" +
                    "\t\tbackground: #FFF;\n" +
                    "\t\tborder-radius: 7px 7px 0px 0px;\n" +
                    "\t\tcolor: #2056ac;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#menu .current_page_item a\n" +
                    "\t{\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Banner                                                                        */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#banner\n" +
                    "\t{\n" +
                    "\t\tpadding-top: 5em;\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Wrapper                                                                       */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\n" +
                    "\t.wrapper\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\tpadding: 0em 0em 5em 0em;\n" +
                    "\t\tbackground: #FFF;\n" +
                    "\t}\n" +
                    "\n" +
                    "\t#wrapper1\n" +
                    "\t{\n" +
                    "\t\tbackground: #FFF;\n" +
                    "\t}\n" +
                    "\n" +
                    "\t#wrapper2\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\tbackground: #F3F3F3;\n" +
                    "\t\tpadding: 5em 0em;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t/* Double Border */\n" +
                    "\t.ta6 {\n" +
                    "\t\tborder: 3px double #CCCCCC;\n" +
                    "\t\twidth: 700px;\n" +
                    "\t\theight: 80px;\n" +
                    "\t}\n" +
                    "\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Welcome                                                                       */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#welcome\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\twidth: 1000px;\n" +
                    "\t\tpadding: 6em 100px 0em 100px;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#welcome .content\n" +
                    "\t{\n" +
                    "\t\tpadding: 0em 8em;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#welcome .title h2\n" +
                    "\t{\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#welcome a,\n" +
                    "\t#welcome strong\n" +
                    "\t{\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Page                                                                          */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#page-wrapper\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\tbackground: #2F1E28;\n" +
                    "\t\tpadding: 3em 0em 6em 0em;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\n" +
                    "\t#page\n" +
                    "\t{\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Content                                                                       */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#content\n" +
                    "\t{\n" +
                    "\t\tfloat: left;\n" +
                    "\t\twidth: 700px;\n" +
                    "\t\tpadding-right: 100px;\n" +
                    "\t\tborder-right: 1px solid rgba(0,0,0,.1);\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Sidebar                                                                       */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#sidebar\n" +
                    "\t{\n" +
                    "\t\tfloat: right;\n" +
                    "\t\twidth: 350px;\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Footer                                                                        */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#footer\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\tpadding: 5em 0em;\n" +
                    "\t\tbackground: #E3F0F7;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\n" +
                    "\n" +
                    "\t\t\n" +
                    "\t#footer .fbox1,\n" +
                    "\t#footer .fbox2,\n" +
                    "\t#footer .fbox3\n" +
                    "\t{\n" +
                    "\t\tfloat: left;\n" +
                    "\t\twidth: 320px;\n" +
                    "\t\tpadding: 0px 40px 0px 40px;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#footer .icon\n" +
                    "\t{\n" +
                    "\t\tdisplay: block;\n" +
                    "\t\tmargin-bottom: 1em;\n" +
                    "\t\tfont-size: 3em;\n" +
                    "\t}\n" +
                    "\n" +
                    "\t\n" +
                    "\t#footer .title span\n" +
                    "\t{\n" +
                    "\t\tcolor: rgba(255,255,255,0.4);\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Copyright                                                                     */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#copyright\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\tpadding: 5em 0em;\n" +
                    "\t\tborder-top: 20px solid rgba(255,255,255,0.08);\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#copyright p\n" +
                    "\t{\n" +
                    "\t\tletter-spacing: 1px;\n" +
                    "\t\tfont-size: 0.90em;\n" +
                    "\t\tcolor: rgba(255,255,255,0.6);\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#copyright a\n" +
                    "\t{\n" +
                    "\t\ttext-decoration: none;\n" +
                    "\t\tcolor: rgba(255,255,255,0.8);\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Newsletter                                                                    */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#newsletter\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\tpadding: 8em 0em;\n" +
                    "\t\tbackground: #EDEDED;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#newsletter .title h2\n" +
                    "\t{\n" +
                    "\t\tcolor: rgba(0,0,0,0.8);\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#newsletter .content\n" +
                    "\t{\n" +
                    "\t\twidth: 600px;\n" +
                    "\t\tmargin: 0px auto;\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Table                                                                    \t */\n" +
                    "/*********************************************************************************/\n" +
                    "body {\n" +
                    "\tbackground: #fafafa url(http://jackrugile.com/images/misc/noise-diagonal.png);\n" +
                    "\tcolor: #444;\n" +
                    "\tfont: 100%/30px 'Helvetica Neue', helvetica, arial, sans-serif;\n" +
                    "\ttext-shadow: 0 1px 0 #fff;\n" +
                    "}\n" +
                    "\n" +
                    "strong {\n" +
                    "\tfont-weight: bold; \n" +
                    "}\n" +
                    "\n" +
                    "em {\n" +
                    "\tfont-style: italic; \n" +
                    "}\n" +
                    "\n" +
                    "table {\n" +
                    "\tbackground: #f5f5f5;\n" +
                    "\tborder-collapse: separate;\n" +
                    "\tbox-shadow: inset 0 1px 0 #fff;\n" +
                    "\tfont-size: 12px;\n" +
                    "\tline-height: 24px;\n" +
                    "\tmargin: 30px auto;\n" +
                    "\ttext-align: left;\n" +
                    "\twidth: 800px;\n" +
                    "}\t\n" +
                    "\n" +
                    "th {\n" +
                    "\tbackground: url(http://jackrugile.com/images/misc/noise-diagonal.png), linear-gradient(#777, #444);\n" +
                    "\tborder-left: 1px solid #555;\n" +
                    "\tborder-right: 1px solid #777;\n" +
                    "\tborder-top: 1px solid #555;\n" +
                    "\tborder-bottom: 1px solid #333;\n" +
                    "\tbox-shadow: inset 0 1px 0 #999;\n" +
                    "\tcolor: #fff;\n" +
                    "  font-weight: bold;\n" +
                    "\tpadding: 10px 15px;\n" +
                    "\tposition: relative;\n" +
                    "\ttext-shadow: 0 1px 0 #000;\t\n" +
                    "}\n" +
                    "\n" +
                    "th:after {\n" +
                    "\tbackground: linear-gradient(rgba(255,255,255,0), rgba(255,255,255,.08));\n" +
                    "\tcontent: '';\n" +
                    "\tdisplay: block;\n" +
                    "\theight: 25%;\n" +
                    "\tleft: 0;\n" +
                    "\tmargin: 1px 0 0 0;\n" +
                    "\tposition: absolute;\n" +
                    "\ttop: 25%;\n" +
                    "\twidth: 100%;\n" +
                    "}\n" +
                    "\n" +
                    "th:first-child {\n" +
                    "\tborder-left: 1px solid #777;\t\n" +
                    "\tbox-shadow: inset 1px 1px 0 #999;\n" +
                    "}\n" +
                    "\n" +
                    "th:last-child {\n" +
                    "\tbox-shadow: inset -1px 1px 0 #999;\n" +
                    "}\n" +
                    "\n" +
                    "td {\n" +
                    "\tborder-right: 1px solid #fff;\n" +
                    "\tborder-left: 1px solid #e8e8e8;\n" +
                    "\tborder-top: 1px solid #fff;\n" +
                    "\tborder-bottom: 1px solid #e8e8e8;\n" +
                    "\tpadding: 10px 15px;\n" +
                    "\tposition: relative;\n" +
                    "\ttransition: all 300ms;\n" +
                    "}\n" +
                    "\n" +
                    "td:first-child {\n" +
                    "\tbox-shadow: inset 1px 0 0 #fff;\n" +
                    "}\t\n" +
                    "\n" +
                    "td:last-child {\n" +
                    "\tborder-right: 1px solid #e8e8e8;\n" +
                    "\tbox-shadow: inset -1px 0 0 #fff;\n" +
                    "}\t\n" +
                    "\n" +
                    "tr {\n" +
                    "\tbackground: url(http://jackrugile.com/images/misc/noise-diagonal.png);\t\n" +
                    "}\n" +
                    "\n" +
                    "tr:nth-child(odd) td {\n" +
                    "\tbackground: #f1f1f1 url(http://jackrugile.com/images/misc/noise-diagonal.png);\t\n" +
                    "}\n" +
                    "\n" +
                    "tr:last-of-type td {\n" +
                    "\tbox-shadow: inset 0 -1px 0 #fff; \n" +
                    "}\n" +
                    "\n" +
                    "tr:last-of-type td:first-child {\n" +
                    "\tbox-shadow: inset 1px -1px 0 #fff;\n" +
                    "}\t\n" +
                    "\n" +
                    "tr:last-of-type td:last-child {\n" +
                    "\tbox-shadow: inset -1px -1px 0 #fff;\n" +
                    "}\t\n" +
                    "\n" +
                    "tbody:hover td {\n" +
                    "\tcolor: transparent;\n" +
                    "\ttext-shadow: 0 0 3px #aaa;\n" +
                    "}\n" +
                    "\n" +
                    "tbody:hover tr:hover td {\n" +
                    "\tcolor: #444;\n" +
                    "\ttext-shadow: 0 1px 0 #fff;\n" +
                    "}\n" +
                    "\t\n" +
                    "</style>\n" +
                    "\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<div id=\"header-wrapper\">\n" +
                    "\t<div id=\"header\" class=\"container\">\n" +
                    "\t\t<div id=\"logo\">\n" +
                    "        \t<span class=\"icon icon-cog\"></span>\n" +
                    "\t\t\t<h1><a href=\"/\">NLProv</a></h1>\n" +
                    "\t\t</div>\n" +
                    "\t</div>\n" +
                    "</div>\n" +
                    "<div class=\"wrapper\">\n" +
                    "\t<div id=\"welcome\" class=\"container\">\n" +
                    "    \t\n" +
                    "\t<div class=\"title\">\n" +
                    "\t\t  <h3>" + query + "</h3>\n" +
                    "\t</div>\n" +
                    "\t<div>\n";
            if (values != null) {
                response += "\t\t<table>\n" +
                        "\t\t\t<tbody>\n";
                int id = 0;
                for (String value : values) {
                    response += "  <tr>\n" +
                            "    <td>" + value + "</td>\n" +
                            "    <td> <input type=\"submit\" value=\"Single\" id=\"single_" + id + "\"> </td>\n" +
                            "    <td> <input type=\"submit\" value=\"Multiple\" id=\"multiple_" + id + "\"> </td>\n" +
                            "    <td> <input type=\"submit\" value=\"Summarized\" id=\"summarized_" + id + "\"> </td>\n" +
                            "  </tr>\n";
                    id++;
                }
                response += "\t\t\t</tbody>\n" +
                        "\t\t</table>\n";
            }

            response += "\t</div>\n" +
                    "\t</div>\n" +
                    "</div>\n" +
                    "<div id=\"footer\">\n" +
                    "\t<div class=\"container\">\n" +
                    "\t</div>\n" +
                    "</div>\n" +
                    "<div id=\"copyright\">\n" +
                    "</div>\n";

            if (values != null) {
                response += "\n" +
                        "<script type=\"text/javascript\">\n";
                for (int i = 0; i < values.size(); i++) {
                    response += "    document.getElementById(\"single_" + i + "\").onclick = function () {\n" +
                                "        location.href = \"\\explanation?query=" + query.replaceAll(" ", "%20").replaceAll("\"", "%22") + "&answer=" + values.get(i).replaceAll(" ", "%20").replaceAll("\"", "%22") + "&type=single\";\n" +
                                "    };\n";
                    response += "    document.getElementById(\"multiple_" + i + "\").onclick = function () {\n" +
                            "        location.href = \"\\explanation?query=" + query.replaceAll(" ", "%20").replaceAll("\"", "%22") + "&answer=" + values.get(i).replaceAll(" ", "%20").replaceAll("\"", "%22") + "&type=multiple\";\n" +
                            "    };\n";
                    response += "    document.getElementById(\"summarized_" + i + "\").onclick = function () {\n" +
                            "        location.href = \"\\explanation?query=" + query.replaceAll(" ", "%20").replaceAll("\"", "") + "&answer=" + values.get(i).replaceAll(" ", "%20").replaceAll("\"", "%22") + "&type=summarized\";\n" +
                            "    };\n";
                }
                response += "</script>\n";
            }

            response += "</body>\n" +
                        "</html>\n";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private List<String> handleQuery(String querySentence) throws Exception {
            Query query = new Query(querySentence, db.schemaGraph);

            components.StanfordNLParser.parse(query, lexiParser);
            components.NodeMapper.phraseProcess(query, db, tokens);
            components.EntityResolution.entityResolute(query);
            components.TreeStructureAdjustor.treeStructureAdjust(query, db);
            components.Explainer.explain(query);
            System.out.println(query.originalParseTree);
            components.SQLTranslator.translate(query, db);

            if (query.blocks.size() == 1) {
                Block block = query.blocks.get(0);
                Map<ITuple, Collection<DerivationTree2>> tupleProvenanceTrees = measSN(block.DATALOGQuery);

                List<String> ans = new ArrayList<>();
                for (Map.Entry<ITuple, Collection<DerivationTree2>> tupleWithProvenanceTrees : tupleProvenanceTrees.entrySet()) {
                    ans.add(tupleWithProvenanceTrees.getKey().get(0).getValue().toString());
                }

                return ans;
            } else {
                return null;
            }
        }
    }


    static class ExplanationHandler implements HttpHandler {
        private LexicalizedParser lexiParser;
        private RDBMS db;
        private Document tokens;

        public ExplanationHandler(LexicalizedParser lexiParser, RDBMS db, Document tokens) {
            this.lexiParser = lexiParser;
            this.db = db;
            this.tokens = tokens;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();

            Map<String, String> params = new HashMap<>();
            String[] querySplit = query.split("&");
            for (String part : querySplit) {
                String[] partSplit = part.split("=");
                assert partSplit.length == 2;
                params.put(partSplit[0], partSplit[1]);
            }

            String explanation = null;
            try {
                explanation = handleQuery(params.get("query"), params.get("answer"), params.get("type"));
            } catch (Exception ignored) {}


            String response = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
                    "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                    "<head>\n" +
                    "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                    "<title></title>\n" +
                    "<meta name=\"keywords\" content=\"\" />\n" +
                    "<meta name=\"description\" content=\"\" />\n" +
                    "<link href=\"http://fonts.googleapis.com/css?family=Source+Sans+Pro:200,300,400,600,700,900|Quicksand:400,700|Questrial\" rel=\"stylesheet\" />\n" +
                    "<link href=\"default.css\" rel=\"stylesheet\" type=\"text/css\" media=\"all\" />\n" +
                    "<link href=\"fonts.css\" rel=\"stylesheet\" type=\"text/css\" media=\"all\" />\n" +
                    "\n" +
                    "<style>\n" +
                    "html, body\n" +
                    "\t{\n" +
                    "\t\theight: 100%;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tbody\n" +
                    "\t{\n" +
                    "\t\tmargin: 0px;\n" +
                    "\t\tpadding: 0px;\n" +
                    "\t\tbackground: #2056ac;\n" +
                    "\t\tfont-family: 'Questrial', sans-serif;\n" +
                    "\t\tfont-size: 12pt;\n" +
                    "\t\tcolor: rgba(0,0,0,.6);\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\th1, h2, h3\n" +
                    "\t{\n" +
                    "\t\tmargin: 0;\n" +
                    "\t\tpadding: 0;\n" +
                    "\t\tcolor: #404040;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tp, ol, ul\n" +
                    "\t{\n" +
                    "\t\tmargin-top: 0;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tol, ul\n" +
                    "\t{\n" +
                    "\t\tpadding: 0;\n" +
                    "\t\tlist-style: none;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tp\n" +
                    "\t{\n" +
                    "\t\tline-height: 180%;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tstrong\n" +
                    "\t{\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\ta\n" +
                    "\t{\n" +
                    "\t\tcolor: #2056ac;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\ta:hover\n" +
                    "\t{\n" +
                    "\t\ttext-decoration: none;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\n" +
                    "\t.container\n" +
                    "\t{\n" +
                    "\t\tmargin: 0px auto;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Form Style                                                                    */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t\tform\n" +
                    "\t\t{\n" +
                    "\t\t}\n" +
                    "\t\t\n" +
                    "\t\t\tform label\n" +
                    "\t\t\t{\n" +
                    "\t\t\t\tdisplay: block;\n" +
                    "\t\t\t\ttext-align: left;\n" +
                    "\t\t\t\tmargin-bottom: 0.5em;\n" +
                    "\t\t\t}\n" +
                    "\t\t\t\n" +
                    "\t\t\tform .submit\n" +
                    "\t\t\t{\n" +
                    "\t\t\t\tmargin-top: 2em;\n" +
                    "\t\t\t\tline-height: 1.5em;\n" +
                    "\t\t\t\tfont-size: 1.3em;\n" +
                    "\t\t\t}\n" +
                    "\t\t\n" +
                    "\t\t\tform input.text,\n" +
                    "\t\t\tform select,\n" +
                    "\t\t\tform textarea\n" +
                    "\t\t\t{\n" +
                    "\t\t\t\tposition: relative;\n" +
                    "\t\t\t\t-webkit-appearance: none;\n" +
                    "\t\t\t\tdisplay: block;\n" +
                    "\t\t\t\tborder: 0;\n" +
                    "\t\t\t\tbackground: #fff;\n" +
                    "\t\t\t\tbackground: rgba(255,255,255,0.75);\n" +
                    "\t\t\t\twidth: 100%;\n" +
                    "\t\t\t\tborder-radius: 0.50em;\n" +
                    "\t\t\t\tmargin: 1em 0em;\n" +
                    "\t\t\t\tpadding: 1.50em 1em;\n" +
                    "\t\t\t\tbox-shadow: inset 0 0.1em 0.1em 0 rgba(0,0,0,0.05);\n" +
                    "\t\t\t\tborder: solid 1px rgba(0,0,0,0.15);\n" +
                    "\t\t\t\t-moz-transition: all 0.35s ease-in-out;\n" +
                    "\t\t\t\t-webkit-transition: all 0.35s ease-in-out;\n" +
                    "\t\t\t\t-o-transition: all 0.35s ease-in-out;\n" +
                    "\t\t\t\t-ms-transition: all 0.35s ease-in-out;\n" +
                    "\t\t\t\ttransition: all 0.35s ease-in-out;\n" +
                    "\t\t\t\tfont-size: 1em;\n" +
                    "\t\t\t\toutline: none;\n" +
                    "\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform input.text:hover,\n" +
                    "\t\t\t\tform select:hover,\n" +
                    "\t\t\t\tform textarea:hover\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform input.text:focus,\n" +
                    "\t\t\t\tform select:focus,\n" +
                    "\t\t\t\tform textarea:focus\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tbox-shadow: 0 0 2px 1px #E0E0E0;\n" +
                    "\t\t\t\t\tbackground: #fff;\n" +
                    "\t\t\t\t}\n" +
                    "\t\t\t\t\n" +
                    "\t\t\t\tform textarea\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tmin-height: 12em;\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform .formerize-placeholder\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tcolor: #555 !important;\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform ::-webkit-input-placeholder\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tcolor: #555 !important;\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform :-moz-placeholder\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tcolor: #555 !important;\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform ::-moz-placeholder\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tcolor: #555 !important;\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform :-ms-input-placeholder\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tcolor: #555 !important;\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\t\t\t\tform ::-moz-focus-inner\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\tborder: 0;\n" +
                    "\t\t\t\t}\n" +
                    "\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Image Style                                                                   */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t.image\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t\tborder: 1px solid rgba(0,0,0,.1);\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.image img\n" +
                    "\t{\n" +
                    "\t\tdisplay: block;\n" +
                    "\t\twidth: 100%;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.image-full\n" +
                    "\t{\n" +
                    "\t\tdisplay: block;\n" +
                    "\t\twidth: 100%;\n" +
                    "\t\tmargin: 0 0 3em 0;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.image-left\n" +
                    "\t{\n" +
                    "\t\tfloat: left;\n" +
                    "\t\tmargin: 0 2em 2em 0;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.image-centered\n" +
                    "\t{\n" +
                    "\t\tdisplay: block;\n" +
                    "\t\tmargin: 0 0 2em 0;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.image-centered img\n" +
                    "\t{\n" +
                    "\t\tmargin: 0 auto;\n" +
                    "\t\twidth: auto;\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* List Styles                                                                   */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\tul.style1\n" +
                    "\t{\n" +
                    "\t}\n" +
                    "\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Social Icon Styles                                                            */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\tul.contact\n" +
                    "\t{\n" +
                    "\t\tmargin: 0;\n" +
                    "\t\tpadding: 2em 0em 0em 0em;\n" +
                    "\t\tlist-style: none;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tul.contact li\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t\tpadding: 0em 0.10em;\n" +
                    "\t\tfont-size: 1em;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tul.contact li span\n" +
                    "\t{\n" +
                    "\t\tdisplay: none;\n" +
                    "\t\tmargin: 0;\n" +
                    "\t\tpadding: 0;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tul.contact li a\n" +
                    "\t{\n" +
                    "\t\tcolor: #FFF;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\tul.contact li a:before\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t\tbackground: #4C93B9;\n" +
                    "\t\twidth: 40px;\n" +
                    "\t\theight: 40px;\n" +
                    "\t\tline-height: 40px;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t\tcolor: rgba(255,255,255,1);\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Button Style                                                                  */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t.button\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t\tmargin-top: 2em;\n" +
                    "\t\tpadding: 0.8em 2em;\n" +
                    "\t\tbackground: #64ABD1;\n" +
                    "\t\tline-height: 1.8em;\n" +
                    "\t\tletter-spacing: 1px;\n" +
                    "\t\ttext-decoration: none;\n" +
                    "\t\tfont-size: 1em;\n" +
                    "\t\tcolor: #FFF;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.button:before\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t\tbackground: #8DCB89;\n" +
                    "\t\tmargin-right: 1em;\n" +
                    "\t\twidth: 40px;\n" +
                    "\t\theight: 40px;\n" +
                    "\t\tline-height: 40px;\n" +
                    "\t\tborder-radius: 20px;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t\tcolor: #272925;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.button-small\n" +
                    "\t{\n" +
                    "\t}\n" +
                    "\n" +
                    "\tinput[type=text] {\n" +
                    "\t\tpadding:5px; \n" +
                    "\t\tborder:2px solid #ccc; \n" +
                    "\t\t-webkit-border-radius: 5px;\n" +
                    "\t\tborder-radius: 5px;\n" +
                    "\t}\n" +
                    "\n" +
                    "\tinput[type=text]:focus {\n" +
                    "\t\tborder-color:#333;\n" +
                    "\t}\n" +
                    "\n" +
                    "\tinput[type=submit] {\n" +
                    "\t\tpadding:5px 15px; \n" +
                    "\t\tbackground:#ccc; \n" +
                    "\t\tborder:0 none;\n" +
                    "\t\tcursor:pointer;\n" +
                    "\t\t-webkit-border-radius: 5px;\n" +
                    "\t\tborder-radius: 5px; \n" +
                    "\t}\t\n" +
                    "\t\n" +
                    "/*********************************************************************************/\n" +
                    "/* Heading Titles                                                                */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t.title\n" +
                    "\t{\n" +
                    "\t\tmargin-bottom: 3em;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.title h2\n" +
                    "\t{\n" +
                    "\t\tfont-size: 2.8em;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t.title .byline\n" +
                    "\t{\n" +
                    "\t\tfont-size: 1.1em;\n" +
                    "\t\tcolor: #6F6F6F#;\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Header                                                                        */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#header-wrapper\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\tbackground: #2056ac;\n" +
                    "\t}\n" +
                    "\n" +
                    "\t#header\n" +
                    "\t{\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Logo                                                                          */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#logo\n" +
                    "\t{\n" +
                    "\t\tpadding: 8em 0em 4em 0em;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#logo h1\n" +
                    "\t{\n" +
                    "\t\tdisplay: block;\n" +
                    "\t\tmargin-bottom: 0.20em;\n" +
                    "\t\tpadding: 0.20em 0.9em;\n" +
                    "\t\tfont-size: 3.5em;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#logo a\n" +
                    "\t{\n" +
                    "\t\ttext-decoration: none;\n" +
                    "\t\tcolor: #FFF;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#logo span\n" +
                    "\t{\n" +
                    "\t\ttext-transform: uppercase;\n" +
                    "\t\tfont-size: 2.90em;\n" +
                    "\t\tcolor: rgba(255,255,255,1);\n" +
                    "\t}\n" +
                    "\n" +
                    "\t#logo span a\n" +
                    "\t{\n" +
                    "\t\tcolor: rgba(255,255,255,0.8);\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Menu                                                                          */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#menu\n" +
                    "\t{\n" +
                    "\t\theight: 60px;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#menu ul\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t\tpadding: 0em 2em;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#menu li\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#menu li a, #menu li span\n" +
                    "\t{\n" +
                    "\t\tdisplay: inline-block;\n" +
                    "\t\tpadding: 0em 1.5em;\n" +
                    "\t\ttext-decoration: none;\n" +
                    "\t\tfont-size: 0.90em;\n" +
                    "\t\tfont-weight: 600;\n" +
                    "\t\ttext-transform: uppercase;\n" +
                    "\t\tline-height: 60px;\n" +
                    "\t\toutline: 0;\n" +
                    "\t\tcolor: #FFF;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#menu li:hover a, #menu li.active a, #menu li.active span\n" +
                    "\t{\n" +
                    "\t\tbackground: #FFF;\n" +
                    "\t\tborder-radius: 7px 7px 0px 0px;\n" +
                    "\t\tcolor: #2056ac;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#menu .current_page_item a\n" +
                    "\t{\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Banner                                                                        */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#banner\n" +
                    "\t{\n" +
                    "\t\tpadding-top: 5em;\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Wrapper                                                                       */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\n" +
                    "\t.wrapper\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\tpadding: 0em 0em 5em 0em;\n" +
                    "\t\tbackground: #FFF;\n" +
                    "\t}\n" +
                    "\n" +
                    "\t#wrapper1\n" +
                    "\t{\n" +
                    "\t\tbackground: #FFF;\n" +
                    "\t}\n" +
                    "\n" +
                    "\t#wrapper2\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\tbackground: #F3F3F3;\n" +
                    "\t\tpadding: 5em 0em;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t/* Double Border */\n" +
                    "\t.ta6 {\n" +
                    "\t\tborder: 3px double #CCCCCC;\n" +
                    "\t\twidth: 700px;\n" +
                    "\t\theight: 80px;\n" +
                    "\t}\n" +
                    "\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Welcome                                                                       */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#welcome\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\tpadding: 6em 100px 0em 100px;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#welcome .content\n" +
                    "\t{\n" +
                    "\t\tpadding: 0em 8em;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#welcome .title h2\n" +
                    "\t{\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#welcome a,\n" +
                    "\t#welcome strong\n" +
                    "\t{\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Page                                                                          */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#page-wrapper\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\tbackground: #2F1E28;\n" +
                    "\t\tpadding: 3em 0em 6em 0em;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\n" +
                    "\t#page\n" +
                    "\t{\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Content                                                                       */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#content\n" +
                    "\t{\n" +
                    "\t\tfloat: left;\n" +
                    "\t\twidth: 700px;\n" +
                    "\t\tpadding-right: 100px;\n" +
                    "\t\tborder-right: 1px solid rgba(0,0,0,.1);\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Sidebar                                                                       */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#sidebar\n" +
                    "\t{\n" +
                    "\t\tfloat: right;\n" +
                    "\t\twidth: 350px;\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Footer                                                                        */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#footer\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\tpadding: 5em 0em;\n" +
                    "\t\tbackground: #E3F0F7;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\n" +
                    "\n" +
                    "\t\t\n" +
                    "\t#footer .fbox1,\n" +
                    "\t#footer .fbox2,\n" +
                    "\t#footer .fbox3\n" +
                    "\t{\n" +
                    "\t\tfloat: left;\n" +
                    "\t\twidth: 320px;\n" +
                    "\t\tpadding: 0px 40px 0px 40px;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#footer .icon\n" +
                    "\t{\n" +
                    "\t\tdisplay: block;\n" +
                    "\t\tmargin-bottom: 1em;\n" +
                    "\t\tfont-size: 3em;\n" +
                    "\t}\n" +
                    "\n" +
                    "\t\n" +
                    "\t#footer .title span\n" +
                    "\t{\n" +
                    "\t\tcolor: rgba(255,255,255,0.4);\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Copyright                                                                     */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#copyright\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\tpadding: 5em 0em;\n" +
                    "\t\tborder-top: 20px solid rgba(255,255,255,0.08);\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#copyright p\n" +
                    "\t{\n" +
                    "\t\tletter-spacing: 1px;\n" +
                    "\t\tfont-size: 0.90em;\n" +
                    "\t\tcolor: rgba(255,255,255,0.6);\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#copyright a\n" +
                    "\t{\n" +
                    "\t\ttext-decoration: none;\n" +
                    "\t\tcolor: rgba(255,255,255,0.8);\n" +
                    "\t}\n" +
                    "\n" +
                    "/*********************************************************************************/\n" +
                    "/* Newsletter                                                                    */\n" +
                    "/*********************************************************************************/\n" +
                    "\n" +
                    "\t#newsletter\n" +
                    "\t{\n" +
                    "\t\toverflow: hidden;\n" +
                    "\t\tpadding: 8em 0em;\n" +
                    "\t\tbackground: #EDEDED;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#newsletter .title h2\n" +
                    "\t{\n" +
                    "\t\tcolor: rgba(0,0,0,0.8);\n" +
                    "\t}\n" +
                    "\t\n" +
                    "\t#newsletter .content\n" +
                    "\t{\n" +
                    "\t\twidth: 600px;\n" +
                    "\t\tmargin: 0px auto;\n" +
                    "\t}\n" +
                    "\n" +
                    "</style>\n" +
                    "\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<div id=\"header-wrapper\">\n" +
                    "\t<div id=\"header\" class=\"container\">\n" +
                    "\t\t<div id=\"logo\">\n" +
                    "        \t<span class=\"icon icon-cog\"></span>\n" +
                    "\t\t\t<h1><a href=\"/\">NLProv</a></h1>\n" +
                    "\t\t</div>\n" +
                    "\t</div>\n" +
                    "</div>\n" +
                    "<div class=\"wrapper\">\n" +
                    "\t<div id=\"welcome\" class=\"container\">\n" +
                    "    \t\n" +
                    "\t<div class=\"title\">\n" +
                    "\t\t  <h3>" + params.get("query") +"</h3>\n" +
                    "\t\t  <h4>" + params.get("answer") +"</h4>\n" +
                    "\t</div>\n" +
                    "\t<div>\n";
            if (explanation != null) {
                response += "<pre style=\"text-align:initial\">" + explanation + "</pre>\n";
            }
            response += "\t</div>\n" +
                    "\t</div>\n" +
                    "</div>\n" +
                    "<div id=\"footer\">\n" +
                    "\t<div class=\"container\">\n" +
                    "\t</div>\n" +
                    "</div>\n" +
                    "<div id=\"copyright\">\n" +
                    "</div>\n" +
                    "\n" +
                    "<script type=\"text/javascript\">\n" +
                    "    document.getElementById(\"submit\").onclick = function () {\n" +
                    "        var query = document.getElementById(\"question\").value;\n" +
                    "        location.href = \"\\answer?\" + query;\n" +
                    "    };\n" +
                    "</script>" +
                    "</body>\n" +
                    "</html>\n";

            response += "\n" +
                    "</body>\n" +
                    "</html>\n";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private String handleQuery(String querySentence, String answer, String type) throws Exception {
            Query query = new Query(querySentence, db.schemaGraph);

            components.StanfordNLParser.parse(query, lexiParser);
            components.NodeMapper.phraseProcess(query, db, tokens);
            components.EntityResolution.entityResolute(query);
            components.TreeStructureAdjustor.treeStructureAdjust(query, db);
            components.Explainer.explain(query);
            System.out.println(query.originalParseTree);
            components.SQLTranslator.translate(query, db);

            if (query.blocks.size() == 1) {
                Block block = query.blocks.get(0);
                Map<ITuple, Collection<DerivationTree2>> tupleProvenanceTrees = measSN(block.DATALOGQuery);

                Collection<String> ans = new ArrayList<>();
                for (Map.Entry<ITuple, Collection<DerivationTree2>> tupleWithProvenanceTrees : tupleProvenanceTrees.entrySet()) {
                    if (answer.equals(tupleWithProvenanceTrees.getKey().get(0).getValue().toString().replaceAll("\"", "%22"))) {
                        NaturalLanguageProvenanceCreator nlProvenanceCreator = new NaturalLanguageProvenanceCreator(querySentence, block, query.originalParseTree);
                        return nlProvenanceCreator.getNaturalLanguageProvenance(tupleWithProvenanceTrees.getValue(), type);
                    }
                }
            }
            return null;
        }
    }

    private static Map<IPredicate, IRelation> getFactMap() throws IOException, ParserException {
        if (factMap == null) {
            // Create a Reader on the Datalog program file.
            Stream<String> lines = Files.lines(Paths.get("NL_Provenance\\resources\\mas_db_subset.iris"));
            String masDbSubset = lines.map(s -> s).collect(Collectors.joining("\n"));

            // Parse the Datalog program.
            Parser parser = new Parser();
            parser.parse(masDbSubset);

            // Retrieve the facts, rules and queries from the parsed program.
            factMap = parser.getFacts();
        }
        Map<IPredicate, IRelation> ans = new HashMap<IPredicate, IRelation>();
        for (Map.Entry<IPredicate, IRelation> entry : factMap.entrySet()) {
            ans.put(entry.getKey(), entry.getValue());
        }
        return ans;
    }

    private static Map<ITuple, Collection<DerivationTree2>> measSN (String query) throws Exception {
        KeyMap2.getInstance().Reset();

        // Parse the query.
        Parser parser = new Parser();
        parser.parse(query + '.');
        List<IRule> rules = parser.getRules();

        // Create a default configuration.
        Configuration configuration = new Configuration();

        // Enable Magic Sets together with rule filtering.
        configuration.programOptmimisers.add(new RuleFilter());
        configuration.programOptmimisers.add(new MagicSets());

        // Convert the map from predicate to relation to a IFacts object.
        IFacts facts = new Facts(getFactMap(), configuration.relationFactory);

        // Evaluate all queries over the knowledge base.
        List<ICompiledRule> cr = compile(rules, facts, configuration);
        SemiNaiveEvaluator sn = new SemiNaiveEvaluator();
        sn.evaluateRules(cr, facts, configuration);

        Map<ITuple, Collection<DerivationTree2>> provenanceTrees = new HashMap<>();
        for (ICompiledRule compiledRule : cr) {
            for (Map.Entry<ITuple, Collection<DerivationTree2>> tupleWithTrees : ((CompiledRule) compiledRule).evaluatedProvenanceTrees.entrySet()) {
                ITuple tuple = tupleWithTrees.getKey();
                Collection<DerivationTree2> trees = tupleWithTrees.getValue();
                if (!provenanceTrees.containsKey(tuple)) {
                    provenanceTrees.put(tuple, new ArrayList<>());
                }
                provenanceTrees.get(tuple).addAll(trees);
            }

        }
        return provenanceTrees;
    }

    private static List<ICompiledRule> compile( List<IRule> rules, IFacts facts, Configuration mConfiguration ) throws EvaluationException
    {
        assert rules != null;
        assert facts != null;
        assert mConfiguration != null;

        List<ICompiledRule> compiledRules = new ArrayList<ICompiledRule>();

        RuleCompiler rc = new RuleCompiler( facts, mConfiguration.equivalentTermsFactory.createEquivalentTerms(), mConfiguration );

        for (IRule rule : rules) {
            compiledRules.add(rc.compile( rule ));
        }

        return compiledRules;
    }
}