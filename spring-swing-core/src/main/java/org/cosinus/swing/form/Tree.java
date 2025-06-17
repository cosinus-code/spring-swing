/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.swing.form;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import java.util.Hashtable;
import java.util.Vector;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Extension of the {@link JTree}
 * which will automatically inject the application context.
 */
public class Tree extends JTree {

    public Tree() {
        injectContext(this);
    }

    public Tree(Object[] value) {
        super(value);
        injectContext(this);
    }

    public Tree(Vector<?> value) {
        super(value);
        injectContext(this);
    }

    public Tree(Hashtable<?, ?> value) {
        super(value);
        injectContext(this);
    }

    public Tree(TreeNode root) {
        super(root);
        injectContext(this);
    }

    public Tree(TreeNode root, boolean asksAllowsChildren) {
        super(root, asksAllowsChildren);
        injectContext(this);
    }

    public Tree(TreeModel newModel) {
        super(newModel);
        injectContext(this);
    }
}
