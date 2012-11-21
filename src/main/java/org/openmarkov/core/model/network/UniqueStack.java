package org.openmarkov.core.model.network;

import java.util.HashSet;
import java.util.Stack;

// This class stack ensure that each element is stored only once.
public class UniqueStack<T> {
	private final Stack<T> stack = new Stack<T>();
	private final HashSet<T> set = new HashSet<T>();

	public boolean push(T t) {
		// Only add element to stack if the set does not contain the specified element.
		if (set.add(t)) {
			stack.add(t);
		}
		return true;
	}

	public T pop() {
		T ret = stack.pop();
		set.remove(ret);
		return ret;
	}
	
	public boolean empty() {
		return set.isEmpty();
	}
}

