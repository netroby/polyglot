package polyglot.ext.param.types;

import polyglot.ext.jl.types.*;
import polyglot.types.*;
import polyglot.types.Package;
import polyglot.util.*;
import java.util.*;

/**
 * Implementation of a ClassType that performs substitutions using a
 * map.  Subclasses must define how the substititions are performed and
 * how to cache substituted types.
 */
public class SubstClassType_c extends ClassType_c implements SubstType
{
    /** The class type we are substituting into. */
    protected ClassType base;

    /** Map from formal parameters (of type Param) to actuals. */
    protected Subst subst;

    public SubstClassType_c(ParamTypeSystem ts, Position pos,
                            ClassType base, Subst subst)
    {
        super(ts, pos);
        this.base = base;
        this.subst = subst;
    }

    public Iterator entries() {
        return subst.entries();
    }

    /** Get the class on that we are performing substitutions. */
    public Type base() {
        return base;
    }

    public Subst subst() {
        return subst;
    }

    ////////////////////////////////////////////////////////////////
    // Perform substitutions on these operations of the base class

    /** Get the class's super type. */
    public Type superType() {
        return subst.substType(base.superType());
    }

    /** Get the class's interfaces. */
    public List interfaces() {
        return subst.substTypeList(base.interfaces());
    }

    /** Get the class's fields. */
    public List fields() {
        return subst.substFieldList(base.fields());
    }

    /** Get the class's methods. */
    public List methods() {
        return subst.substMethodList(base.methods());
    }

    /** Get the class's constructors. */
    public List constructors() {
        return subst.substConstructorList(base.constructors());
    }

    /** Get the class's member classes. */
    public List memberClasses() {
        return subst.substTypeList(base.memberClasses());
    }

    /** Get the class's outer class, if an inner class. */
    public ClassType outer() {
        return (ClassType) subst.substType(base.outer());
    }

    ////////////////////////////////////////////////////////////////
    // Delegate the rest of the class operations to the base class

    /** Get the class's kind: top-level, member, local, or anonymous. */
    public ClassType.Kind kind() {
        return base.kind();
    }

    /** Get the class's full name, if possible. */
    public String fullName() {
        return base.fullName();
    }

    /** Get the class's short name, if possible. */
    public String name() {
        return base.name();
    }

    /** Get the class's package, if possible. */
    public Package package_() {
        return base.package_();
    }

    public Flags flags() {
        return base.flags();
    }

    public String translate(Resolver c) {
        return base.translate(c);
    }

    ////////////////////////////////////////////////////////////////
    // Equality tests

    /** Type equality test. */
    public boolean isSameImpl(Type t) {
        if (! (t instanceof SubstClassType_c)) return false;

        SubstClassType_c x = (SubstClassType_c) t;
        if (! base.isSame(x.base)) return false;
        if (! subst.equals(x.subst)) return false;

        return true;
    }

    /** Hash code. */
    public int hashCode() {
        return base.hashCode() ^ subst.hashCode();
    }
}
