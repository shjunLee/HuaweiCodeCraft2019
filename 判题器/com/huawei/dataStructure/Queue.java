package com.huawei.dataStructure;

import java.util.Iterator;

public class Queue<Item> implements Iterable<Item> {
    private Node first;//head
    private Node last;//tail
    private int N;

    private class Node {
        Item item;
        Node next;
    }

    public boolean isEmpty() { return N==0; }
    public int size() { return N; }
    public void enqueue(Item item) {
        Node oldlast = last;
        last = new Node();
        last.item = item;
        last.next = null;
        if(isEmpty()) first = last;
        else oldlast.next = last;
        N++;
    }
    public Item dequeue() {
        Item item = first.item;
        if(isEmpty()) last = null;
        first = first.next;
        N--;
        return item;
    }

    public Item head() {
        return first.item;
    }

    public Item tail() {
        return last.item;
    }

    public void merge(Queue<Item> another) {
        this.last.next = another.first;
        this.last = another.last;
        this.N += another.N;
        another.first = null;
        another.last = null;
        another.N = 0;
    }

    public void clear(){//清空队列
        first = null;
        last = null;
        N = 0;
    }

    public Iterator<Item> iterator() {
        return new QueueIterator();
    }

    private class QueueIterator implements Iterator<Item> {
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
