/*
 * TypeChecked.java
 * 
 * Author: nystrom
 * Creation date: Dec 19, 2004
 */
package polyglot.frontend.goals;

import java.util.*;

import polyglot.ast.*;
import polyglot.frontend.*;
import polyglot.frontend.passes.DisambiguatorPass;
import polyglot.frontend.passes.TypeCheckPass;
import polyglot.main.Report;
import polyglot.types.ParsedClassType;
import polyglot.types.TypeSystem;
import polyglot.visit.*;
import polyglot.visit.NodeVisitor;
import polyglot.visit.TypeChecker;


public class Disambiguated extends SourceFileGoal {
    public Disambiguated(Job job) {
        super(job);
    }

    public Pass createPass(ExtensionInfo extInfo) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        return new DisambiguatorPass(this, new AmbiguityRemover(job(), ts, nf));
    }

    public boolean hasBeenReached() {
        if (Report.should_report(TOPICS, 3))
            Report.report(3, "checking " + this);

        if (super.hasBeenReached()) {
            if (Report.should_report(TOPICS, 3))
                Report.report(3, "  ok (cached)");
            return true;
        }
        
        if (! hasBeenRun()) {
            if (Report.should_report(TOPICS, 3))
                Report.report(3, "  not run yet");
            return false;
        }
        
        if (job().ast() == null) {
            if (Report.should_report(TOPICS, 3))
                Report.report(3, "  null ast for " + job());
            return false;
        }
        
        // Do a relatively quick test of the types declared in the AST.
        for (Iterator i = job().ast().typesBelow().iterator(); i.hasNext(); ) {
            ParsedClassType ct = (ParsedClassType) i.next();
            if (! ct.signaturesResolved()) {
                if (Report.should_report(TOPICS, 3))
                    Report.report(3, "  signatures for " + ct + " ambiguous");
                Scheduler scheduler = job().extensionInfo().scheduler();
                scheduler.addConcurrentDependency(this, scheduler.SignaturesResolved(ct));
                return false;
            }
        }
        
        // Now look for ambiguities in the AST.
        final boolean[] allOk = new boolean[] { true };
        
        job().ast().visit(new NodeVisitor() {
            public Node override(Node parent, Node n) {
                if (! allOk[0]) {
                    return n;
                }
                
                if (parent instanceof LocalClassDecl && n instanceof ClassDecl) {
                    return n;
                }
                
                if (parent instanceof New && n instanceof ClassBody) {
                    return n;
                }

                if (! n.isDisambiguated()) {
                    if (Report.should_report(TOPICS, 3))
                        Report.report(3, "  not ok at " + n);
                    allOk[0] = false;
                    return n;
                }
                
                return null;
            }
        });
        
        if (allOk[0]) {
            if (Report.should_report(TOPICS, 3))
                Report.report(3, "  ok");
            this.markRun();
        }
        
        return super.hasBeenReached();
    }
    
    private static final Collection TOPICS = Arrays.asList(new String[] { Report.types, Report.frontend });
}
