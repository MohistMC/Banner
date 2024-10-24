package com.destroystokyo.paper.util.maplist;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Spottedleaf
 */
public final class ReferenceList<E> implements Iterable<E> {

    protected final Reference2IntOpenHashMap<E> referenceToIndex = new Reference2IntOpenHashMap<>(2, 0.8f);
    {
        this.referenceToIndex.defaultReturnValue(Integer.MIN_VALUE);
    }

    protected static final Object[] EMPTY_LIST = new Object[0];

    protected Object[] references = EMPTY_LIST;
    protected int count;

    public int size() {
        return this.count;
    }

    public boolean contains(final E obj) {
        return this.referenceToIndex.containsKey(obj);
    }

    public boolean remove(final E obj) {
        final int index = this.referenceToIndex.removeInt(obj);
        if (index == Integer.MIN_VALUE) {
            return false;
        }

        // move the object at the end to this index
        final int endIndex = --this.count;
        final E end = (E)this.references[endIndex];
        if (index != endIndex) {
            // not empty after this call
            this.referenceToIndex.put(end, index); // update index
        }
        this.references[index] = end;
        this.references[endIndex] = null;

        return true;
    }

    public boolean add(final E obj) {
        final int count = this.count;
        final int currIndex = this.referenceToIndex.putIfAbsent(obj, count);

        if (currIndex != Integer.MIN_VALUE) {
            return false; // already in this list
        }

        Object[] list = this.references;

        if (list.length == count) {
            // resize required
            list = this.references = Arrays.copyOf(list, (int)Math.max(4L, count * 2L)); // overflow results in negative
        }

        list[count] = obj;
        this.count = count + 1;

        return true;
    }

    public E getChecked(final int index) {
        if (index < 0 || index >= this.count) {
            throw new IndexOutOfBoundsException("Index: " + index + " is out of bounds, size: " + this.count);
        }
        return (E)this.references[index];
    }

    public E getUnchecked(final int index) {
        return (E)this.references[index];
    }

    public Object[] getRawData() {
        return this.references;
    }

    public void clear() {
        this.referenceToIndex.clear();
        Arrays.fill(this.references, 0, this.count, null);
        this.count = 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
            private E lastRet;
            private int current;

            @Override
            public boolean hasNext() {
                return this.current < ReferenceList.this.count;
            }

            @Override
            public E next() {
                if (this.current >= ReferenceList.this.count) {
                    throw new NoSuchElementException();
                }
                return this.lastRet = (E)ReferenceList.this.references[this.current];
            }

            @Override
            public void remove() {
                final E lastRet = this.lastRet;

                if (lastRet == null) {
                    throw new IllegalStateException();
                }
                this.lastRet = null;

                ReferenceList.this.remove(lastRet);
                --this.current;
            }
        };
    }
}