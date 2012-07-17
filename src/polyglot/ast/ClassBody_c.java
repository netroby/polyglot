/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.ast;

import java.util.*;

import polyglot.main.Report;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

/**
 * A <code>ClassBody</code> represents the body of a class or interface
 * declaration or the body of an anonymous class.
 */
public class ClassBody_c extends Term_c implements ClassBody
{
    protected List<ClassMember> members;

    public ClassBody_c(Position pos, List members) {
        super(pos);
        assert(members != null);
        this.members = ListUtil.copy(members, true);
    }

    public List members() {
        return this.members;
    }

    public ClassBody members(List members) {
        ClassBody_c n = (ClassBody_c) copy();
        n.members = ListUtil.copy(members, true);
        return n;
    }

    public ClassBody addMember(ClassMember member) {
        ClassBody_c n = (ClassBody_c) copy();
        List l = new ArrayList(this.members.size() + 1);
        l.addAll(this.members);
        l.add(member);
        n.members = ListUtil.copy(l, true);
        return n;
    }

    protected ClassBody_c reconstruct(List members) {
        if (! CollectionUtil.equals(members, this.members)) {
            ClassBody_c n = (ClassBody_c) copy();
            n.members = ListUtil.copy(members, true);
            return n;
        }

        return this;
    }

    public Node visitChildren(NodeVisitor v) {
        List members = visitList(this.members, v);
        return reconstruct(members);
    }

    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        return this;
    }

    public String toString() {
        return "{ ... }";
    }

    protected void duplicateFieldCheck(TypeChecker tc) throws SemanticException {
        ClassType type = tc.context().currentClass();

        ArrayList l = new ArrayList(type.fields());

        for (int i = 0; i < l.size(); i++) {
            FieldInstance fi = (FieldInstance) l.get(i);

            for (int j = i+1; j < l.size(); j++) {
                FieldInstance fj = (FieldInstance) l.get(j);

                if (fi.name().equals(fj.name())) {
                    throw new SemanticException("Duplicate field \"" + fj + "\".", fj.position());
                }
            }
        }
    }

    protected void duplicateConstructorCheck(TypeChecker tc) throws SemanticException {
        ClassType type = tc.context().currentClass();

        ArrayList l = new ArrayList(type.constructors());

        for (int i = 0; i < l.size(); i++) {
            ConstructorInstance ci = (ConstructorInstance) l.get(i);

            for (int j = i+1; j < l.size(); j++) {
                ConstructorInstance cj = (ConstructorInstance) l.get(j);

                if (ci.hasFormals(cj.formalTypes())) {
                    throw new SemanticException("Duplicate constructor \"" + cj + "\".", cj.position());
                }
            }
        }
    }

    protected void duplicateMethodCheck(TypeChecker tc) throws SemanticException {
        ClassType type = tc.context().currentClass();
        TypeSystem ts = tc.typeSystem();

        ArrayList l = new ArrayList(type.methods());

        for (int i = 0; i < l.size(); i++) {
            MethodInstance mi = (MethodInstance) l.get(i);

            for (int j = i+1; j < l.size(); j++) {
                MethodInstance mj = (MethodInstance) l.get(j);

                if (isSameMethod(ts, mi, mj)) {
                    throw new SemanticException("Duplicate method \"" + mj + "\".", mj.position());
                }
            }
        }
    }

    protected void duplicateMemberClassCheck(TypeChecker tc) throws SemanticException {
        ClassType type = tc.context().currentClass();

        ArrayList l = new ArrayList(type.memberClasses());

        for (int i = 0; i < l.size(); i++) {
            ClassType mi = (ClassType) l.get(i);

            for (int j = i+1; j < l.size(); j++) {
                ClassType mj = (ClassType) l.get(j);

                if (mi.name().equals(mj.name())) {
                    throw new SemanticException("Duplicate member type \"" + mj + "\".", mj.position());
                }
            }
        }
    }

    protected boolean isSameMethod(TypeSystem ts, MethodInstance mi,
                                   MethodInstance mj) {
        return mi.isSameMethod(mj);
    }

    public Node typeCheck(TypeChecker tc) throws SemanticException {
        duplicateFieldCheck(tc);
        duplicateConstructorCheck(tc);
        duplicateMethodCheck(tc);
        duplicateMemberClassCheck(tc);

        return this;
    }
    
    public NodeVisitor exceptionCheckEnter(ExceptionChecker ec) throws SemanticException {
        return ec.push();
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        if (!members.isEmpty()) {
            w.newline(4);
            w.begin(0);
	    ClassMember prev = null;

            for (Iterator i = members.iterator(); i.hasNext(); ) {
                ClassMember member = (ClassMember) i.next();
		if ((member instanceof polyglot.ast.CodeDecl) ||
		    (prev instanceof polyglot.ast.CodeDecl)) {
			w.newline(0);
		}
		prev = member;
                printBlock(member, w, tr);
                if (i.hasNext()) {
                    w.newline(0);
                }
            }

            w.end();
            w.newline(0);
        }
    }

    /**
     * Return the first (sub)term performed when evaluating this
     * term.
     */
    public Term firstChild() {
        // Do _not_ visit class members.
        return null;
    }

    /**
     * Visit this term in evaluation order.
     */
    public List acceptCFG(CFGBuilder v, List succs) {
        return succs;
    }
    public Node copy(NodeFactory nf) {
        return nf.ClassBody(this.position, this.members);
    }

    private static final Collection TOPICS = 
                CollectionUtil.list(Report.types, Report.context);
     
}
