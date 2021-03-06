/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.types;

import polyglot.types.TypeObject;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

public class ParamKey_c extends Key_c implements ParamKey {
    public ParamKey_c(TypeSystem ts, Position pos, String name) {
        super(ts, pos, name);
    }

    @Override
    public boolean equalsImpl(TypeObject o) {
        if (o instanceof ParamKey) {
            return name.equals(((ParamKey) o).name());
        }
        return false;
    }

}
