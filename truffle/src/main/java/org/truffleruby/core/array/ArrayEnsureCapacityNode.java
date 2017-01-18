/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.core.array;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.jruby.truffle.language.RubyNode;

@NodeChildren({
        @NodeChild(value = "array", type = RubyNode.class),
        @NodeChild(value = "requiredCapacity", type = RubyNode.class)
})
@ImportStatic(ArrayGuards.class)
public abstract class ArrayEnsureCapacityNode extends RubyNode {

    public static ArrayEnsureCapacityNode create() {
        return ArrayEnsureCapacityNodeGen.create(null, null);
    }

    public abstract Object executeEnsureCapacity(DynamicObject array, int requiredCapacity);

    @Specialization(guards = "strategy.matches(array)", limit = "ARRAY_STRATEGIES")
    public boolean ensureCapacity(DynamicObject array, int requiredCapacity,
            @Cached("of(array)") ArrayStrategy strategy,
            @Cached("createCountingProfile()") ConditionProfile extendProfile) {
        final ArrayMirror mirror = strategy.newMirror(array);

        if (extendProfile.profile(mirror.getLength() < requiredCapacity)) {
            final int capacity = ArrayUtils.capacity(getContext(), mirror.getLength(), requiredCapacity);
            strategy.setStore(array, mirror.copyArrayAndMirror(capacity).getArray());
            return true;
        } else {
            return false;
        }
    }

}