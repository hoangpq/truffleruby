/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.hash;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.core.basicobject.BasicObjectNodes.ReferenceEqualNode;
import org.truffleruby.core.basicobject.BasicObjectNodesFactory.ReferenceEqualNodeFactory;
import org.truffleruby.core.kernel.KernelNodes.SameOrEqlNode;
import org.truffleruby.core.kernel.KernelNodesFactory.SameOrEqlNodeFactory;
import org.truffleruby.language.RubyBaseNode;

public class CompareHashKeysNode extends RubyBaseNode {

    @Child private SameOrEqlNode sameOrEqlNode;
    @Child private ReferenceEqualNode equalNode;

    public boolean equalKeys(VirtualFrame frame, boolean compareByIdentity, Object key, int hashed, Object otherKey, int otherHashed) {
        if (compareByIdentity) {
            return equal(key, otherKey);
        } else {
            return hashed == otherHashed && sameOrEql(frame, key, otherKey);
        }
    }

    /**
     * Checks if the two keys are the same object, which is used by both modes (by identity or not)
     * of lookup. Enables to check if the two keys are the same without a method call.
     */
    public boolean referenceEqualKeys(boolean compareByIdentity, Object key, int hashed, Object otherKey, int otherHashed) {
        if (compareByIdentity) {
            return equal(key, otherKey);
        } else {
            return hashed == otherHashed && equal(key, otherKey);
        }
    }

    private boolean sameOrEql(VirtualFrame frame, Object key1, Object key2) {
        if (sameOrEqlNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            sameOrEqlNode = insert(SameOrEqlNodeFactory.create(null));
        }
        return sameOrEqlNode.executeSameOrEql(frame, key1, key2);
    }

    private boolean equal(Object key1, Object key2) {
        if (equalNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            equalNode = insert(ReferenceEqualNodeFactory.create(null));
        }
        return equalNode.executeReferenceEqual(key1, key2);
    }

}
