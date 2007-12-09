/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.visit;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.Position;

import java.util.*;

/**
 * The <code>FlattenVisitor</code> flattens the AST,
 */
public class FlattenVisitor extends NodeVisitor
{
    protected TypeSystem ts;
    protected NodeFactory nf;
    protected LinkedList stack;

    public FlattenVisitor(TypeSystem ts, NodeFactory nf) {
	this.ts = ts;
	this.nf = nf;
	stack = new LinkedList();
    }

    public Node override(Node parent, Node n) {
        // Insert Blocks when needed to allow local decls to be inserted.
        if (n instanceof If) {
            If s = (If) n;
            Stmt s1 = s.consequent();
            Stmt s2 = s.alternative();
            if (! (s1 instanceof Block)) {
                s = s.consequent(nf.Block(s1.position(), s1));
            }
            if (s2 != null && ! (s2 instanceof Block)) {
                s = s.alternative(nf.Block(s2.position(), s2));
            }
            return visitEdgeNoOverride(parent, s);
        }

        if (n instanceof Do) {
            Do s = (Do) n;
            Stmt s1 = s.body();
            if (! (s1 instanceof Block)) {
                s = s.body(nf.Block(s1.position(), s1));
            }
            return visitEdgeNoOverride(parent, s);
        }

        if (n instanceof While) {
            While s = (While) n;
            Stmt s1 = s.body();
            if (! (s1 instanceof Block)) {
                s = s.body(nf.Block(s1.position(), s1));
            }
            return visitEdgeNoOverride(parent, s);
        }

        if (n instanceof For) {
            For s = (For) n;
            Stmt s1 = s.body();
            if (! (s1 instanceof Block)) {
                s = s.body(nf.Block(s1.position(), s1));
            }
            return visitEdgeNoOverride(parent, s);
        }

	if (n instanceof FieldDecl || n instanceof ConstructorCall) {
            if (! stack.isEmpty()) {
                List l = (List) stack.getFirst();
                l.add(n);
            }
	    return n;
	}

        // punt on switch statement
        if (n instanceof Switch) {
            return n;
        }
                

        if (neverFlatten.contains(n)) {
            return n;
        }

        if (n instanceof ArrayInit) {
            return n;
        }

	return null;
    }

    protected static int count = 0;

    protected static String newID() {
	return "flat$$$" + count++;
    }

    protected Set noFlatten = new HashSet();
    protected Set neverFlatten = new HashSet();

    /** 
     * When entering a BlockStatement, place a new StatementList
     * onto the stack
     */
    public NodeVisitor enter(Node parent, Node n) {
	if (n instanceof Block) {
	    stack.addFirst(new LinkedList());
	}

	if (n instanceof Eval) {
	    // Don't flatten the expression contained in the statement, but
	    // flatten its subexpressions.
	    Eval s = (Eval) n;
	    noFlatten.add(s.expr());
	}

	if (n instanceof LocalDecl) {
	    // Don't flatten the expression contained in the statement, but
	    // flatten its subexpressions.
	    LocalDecl s = (LocalDecl) n;
	    noFlatten.add(s.init());
	}

        if (n instanceof For) {
	    For s = (For) n;
            noFlatten.addAll(s.inits());
            neverFlatten.addAll(s.iters());
            neverFlatten.add(s.cond());
        }

        if (n instanceof While) {
	    While s = (While) n;
            neverFlatten.add(s.cond());
        }

        if (n instanceof Do) {
	    Do s = (Do) n;
            neverFlatten.add(s.cond());
        }

	if (n instanceof Assign) {
	    Assign s = (Assign) n;
	    noFlatten.add(s.left());
	    noFlatten.add(s.right());
	}
        
        if (n instanceof Unary) {
          Unary u = (Unary) n;
          noFlatten.add(u.expr());
        }

	return this;
    }

    /** 
     * Flatten complex expressions within the AST
     */
    public Node leave(Node parent, Node old, Node n, NodeVisitor v) {
        if (noFlatten.contains(old)) {
	    noFlatten.remove(old);
	    return n;
	}

	if (n instanceof Block) {
	    List l = (List) stack.removeFirst();
            Block block = ((Block) n).statements(l);
            if (parent instanceof Block && !stack.isEmpty()) {
              l = (List) stack.getFirst();
              l.add(block);
            }
	    return block;
	}
	else if (n instanceof Stmt) {
	    List l = (List) stack.getFirst();
	    l.add(n);
	    return n;
	}
	else if (n instanceof Expr && ! (n instanceof Lit) &&
	      ! (n instanceof Special) && ! (n instanceof Local)) {

	    Expr e = (Expr) n;

	    if (e instanceof Assign) {
	        return n;
	    }

            /*
            if (e.isTypeChecked() && e.type().isVoid()) {
                return n;
            }
            */

	    // create a local temp, initialized to the value of the complex
	    // expression

	    String name = newID();
	    LocalDecl def = nf.LocalDecl(e.position(), Flags.FINAL,
					 nf.CanonicalTypeNode(e.position(),
					                      e.type()),
					 nf.Id(Position.compilerGenerated(), name), 
					 e);
	    def = def.localInstance(ts.localInstance(e.position(), Flags.FINAL,
						     e.type(), name));

	    List l = (List) stack.getFirst();
	    l.add(def);

	    // return the local temp instead of the complex expression
	    Local use = nf.Local(e.position(), 
	    		nf.Id(Position.compilerGenerated(), name));
	    use = (Local) use.type(e.type());
	    use = use.localInstance(ts.localInstance(e.position(), Flags.FINAL,
						     e.type(), name));
	    return use;
	}

	return n;
    }
}
