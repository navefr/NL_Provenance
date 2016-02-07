# Makefile for treedistance

include ../Makefile.default
include ../Makefile.local

# directories

PACKDIR=$(CLASSDIR)/headliner/treedistance

# copy the source files in the package directory

$(PACKDIR)/%$(HTMLEXT) : %$(HTMLEXT)
	@cp $< $@

$(PACKDIR)/%$(JAVAEXT) : %$(JAVAEXT)
	@cp $< $@

$(PACKDIR)/%$(CLASSEXT) : %$(JAVAEXT)
	@echo Compiling $<
	@$(JAVAC) -d $(CLASSDIR) $<

CLASSES = TreeDefinition BasicTree\
          ComparisonZhangShasha OpsZhangShasha \
          Transformation \
          TreeEditOperation \
          BasicDelete BasicInsert BasicRename \
          TestTreeDefinition TestZhangShasha\
          CreateTreeHelper
REMOTE_SRC = ${CLASSES:%=$(PACKDIR)/%$(JAVAEXT)} 
PACKAGE_HTMLPAGE = $(PACKDIR)/package.html
OBJS = ${CLASSES:%=$(PACKDIR)/%$(CLASSEXT)}


# DEFAULT RULE

all: checkdirs package

# make sure the package directory exists

checkdirs:
	@mkdir -p $(PACKDIR)

# copy the .java files to the package directory, then package.html,
# then compiles


package: $(REMOTE_SRC) $(PACKAGE_HTMLPAGE) $(OBJS)

clean:
	@rm -f $(OBJS)
	@rm -f $(REMOTE_SRC)

test1:
	@$(JAVA) -DHEADLINER_HOME=$(PROJ) \
          headliner.treedistance.TestTreeDefinition "a-b;b-c;c-d"



test2:
	@$(JAVA) -DHEADLINER_HOME=$(PROJ) \
          headliner.treedistance.TestZhangShasha "f-d;f-e;d-a;d-c;c-b"  "f:6-c:4;f:6-e:5;c:4-d:3;d:3-a:1;d:3-b:2"


#Duplicate nodes uniquified with suffix ":<num" but node label match 
#string BEFORE the last colon
test3:
	@$(JAVA) -DHEADLINER_HOME=$(PROJ) \
          headliner.treedistance.TestZhangShasha "f:6-c:4;f:6-c:5;c:4-d:3;c:4-e:3;c:5-a:1"  "f:6-c:4;f:6-c:5;c:4-e:3;c:4-d:3;c:5-b:1"  

testDep:
	@$(JAVA) -DHEADLINER_HOME=$(PROJ) \
          headliner.treedistance.TestZhangShasha "were-cutters;cutters-box;were-weapons;weapons-the1;weapons-used;used-by;by-hijackers;hijackers-the2" "were-cutters;cutters-box;were-used;used-as;as-weapons;weapons-the1;used-by;by-hijackers;hijackers-the2" 

# Test with parse tree?
# 2 Strings:
#
# The quick brown fox jumped over the lazy dog.
# (S (NP ( DT the) (ADJP (ADJ quick) (ADJP (ADJ lazy) ) ) (NN dog)  ) (VP (VP_2 (VBN jummped ) (RB over) ) (NP ( DT the) (ADJP (ADJ quick) (ADJP (ADJ lazy) ) ) (NN dog) ) ) )
#
# "S-NP;NP-DT;NP-ADJP1;NP-NN;DT-the;NN-fox;ADJP1-ADJ;ADJP1-ADJP2;ADJ-JJ;ADJP2-ADJ2;ADJ2-JJ2;JJ-quick;JJ2-brown;S-VP;VP-VP_2;VP_2-VBN;VP_2-RB;VBN-jumped;RB-over;VP-NP2;NP2-DT2;NP2-ADJP3;NP2-NN2;DT2-the2;ADJP3-ADJ3;ADJ3-lazy;NN2-dog" 
#
# VERSUS
# The fox brown quick over jumped the dog lazy.

testSPhrase:
	@$(JAVA) -DHEADLINER_HOME=$(PROJ) \
          headliner.treedistance.TestZhangShasha "S-NP;NP-DT;NP-ADJP:1;NP-NN;DT-the;NN-fox;ADJP:1-ADJ;ADJP:1-ADJP:2;ADJ-JJ;ADJP:2-ADJ:2;ADJ:2-JJ:2;JJ-quick;JJ:2-brown;S-VP;VP-VP_:2;VP_:2-VBN;VP_:2-RB;VBN-jumped;RB-over;VP-NP:2;NP:2-DT:2;NP:2-ADJP:3;NP:2-NN:2;DT:2-the:2;ADJP:3-ADJ:3;ADJ:3-lazy;NN:2-dog"  \
\
          headliner.treedistance.TestZhangShasha "S-NP;NP-DT;NP-ADJP:1;NP-NN;DT-the;NN-fox;ADJP:1-ADJ;ADJP:1-ADJP:2;ADJ-JJ;ADJP:2-ADJ:2;ADJ:2-JJ:2;JJ-quick;JJ:2-brown;S-VP;VP-VP_:2;VP_:2-VBN;VP_:2-RB;VBN-jumped;RB-over;VP-NP:2;NP:2-DT:2;NP:2-ADJP:3;NP:2-NN:2;DT:2-the:2;ADJP:3-ADJ:3;ADJ:3-lazy;NN:2-dog"  \


#"the quick brown fox" versus "the brown quick fox"
testNounPhrase:
	@$(JAVA) -DHEADLINER_HOME=$(PROJ) \
          headliner.treedistance.TestZhangShasha \
"NP-DT;NP-ADJP:1;NP-NN;DT-the;NN-fox;ADJP:1-ADJ;ADJP:1-ADJP:2;ADJ-JJ;ADJP:2-ADJ:2;ADJ:2-JJ:2;JJ-quick;JJ:2-brown"  \
\
"NP-DT;NP-ADJP:1;NP-NN;DT-the;NN-fox;ADJP:1-ADJ;ADJP:1-ADJP:2;ADJ-JJ;ADJP:2-ADJ:2;ADJ:2-JJ:2;JJ-brown;JJ:2-quick"






