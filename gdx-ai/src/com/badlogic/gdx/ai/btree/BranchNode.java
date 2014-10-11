/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.ai.btree;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/** A branch node defines a behavior tree branch, contains logic of starting or running sub-branches and leaves
 * 
 * @param <E> type of the blackboard nodes use to read or modify game state
 * 
 * @author implicit-invocation
 * @author davebaol */
public abstract class BranchNode<E> extends Node<E> {

	/** The node metadata specifying static information used by parsers and tools. */
	public static final Metadata METADATA = new Metadata(1, -1, "deterministic");

	public boolean deterministic = true;

	protected int actualTask;
	protected Node<E> nodeRunning;

	/** Create a branch node with a list of children
	 * 
	 * @param nodes list of this node's children, can be empty */
	public BranchNode (Array<Node<E>> nodes) {
		this.children = nodes;
	}

	@Override
	public void childRunning (Node<E> node, Node<E> reporter) {
		runningNode = node;
		control.childRunning(node, this);
	}

	@Override
	public void run (E object) {
		if (runningNode != null) {
			runningNode.run(object);
		} else {
			this.object = object;
			if (actualTask < children.size) {
				if (!deterministic) {
					int lastTask = children.size - 1;
					if (actualTask < lastTask) children.swap(actualTask, MathUtils.random(actualTask, lastTask));
				}
				runningNode = children.get(actualTask);
				runningNode.setControl(this);
				runningNode.object = object;
				runningNode.start(object);
				run(object);
			} else {
				end(object);
			}
		}
	}

	@Override
	public void start (E object) {
		this.actualTask = 0;
		runningNode = null;
	}

	@Override
	protected Node<E> copyTo (Node<E> node) {
		BranchNode<E> branch = (BranchNode<E>)node;
		branch.deterministic = deterministic;
		if (children != null) {
			for (int i = 0; i < children.size; i++) {
				branch.children.add(children.get(i).cloneNode());
			}
		}

		return node;
	}

}
