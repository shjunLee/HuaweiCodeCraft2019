package com.huawei.dataStructure;

import java.util.Iterator;
//import java.util.NoSuchElementException;

public class Bag<Item> implements Iterable<Item> {
    private Node first;
    private int N;
    private class Node {
        Item item;
        Node next;

        public Node(Item item, Node next) {
            this.item = item;
            this.next = next;
        }
    }

    public Bag() {
        N = 0;
        first = null;
    }

    public void add(Item item) {
        first = new Node(item, first);
        N++;
    }

    public boolean isEmpty() { return N == 0; }

    public int size() { return N; }

    public Iterator<Item> iterator() {
        return new BagIterator();
    }

    private class BagIterator implements Iterator<Item> {
        private Node current = first;
        public boolean hasNext() { return current != null; }
        public void remove() { }
        public Item next() {
            Item item = current.item;
            current = current.next;
            return item;
        }
    }
}