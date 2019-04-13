package com.huawei.dataStructure;

public class IndexMinPQ<Key extends Comparable<Key>> {
    private int N;
    private int[] pq; //索引二叉堆，从1开始
    private int[] qp; //逆序，qp[pq[i]] = pq[qp[i]] = i
    private Key[] keys; //有优先级之分元素
    private final int DEFAULT_MAXN = 20;

    public IndexMinPQ() {
        keys = (Key[]) new Comparable[DEFAULT_MAXN + 1];
        pq = new int[DEFAULT_MAXN + 1];
        qp = new int[DEFAULT_MAXN + 1];
        for (int i = 0; i <= DEFAULT_MAXN; i++) {
            qp[i] = -1;
        }
    }
    public IndexMinPQ(int maxN) {
        keys = (Key[]) new Comparable[maxN + 1];
        pq = new int[maxN + 1];
        qp = new int[maxN + 1];
        for (int i = 0; i <= maxN; i++) {
            qp[i] = -1;
        }
    }

    public boolean isEmpty() { return N==0; }
    public boolean contains(int k) { return qp[k] != -1; }

    public void insert(int k, Key key) {
        N++;
        qp[k] = N;
        pq[N] = k;
        keys[k] = key;
        swim(N);
    }

    public Key min() { return keys[pq[1]]; }

    public Key key(int k) {
        return keys[k];
    }

    public int delMin() {
        int indexOfMin = pq[1];
        exch(1, N--);
        sink(1);
        keys[pq[N+1]] = null;
//        keys[indexOfMin] = null;
        qp[pq[N+1]] = -1;
        return indexOfMin;
    }

    public int minIndex() { return pq[1]; }

    public void change(int k, Key key) {
        keys[k] = key;
        swim(qp[k]);
        sink(qp[k]);
    }

    public void delete(int k) {
        if (!contains(k)) return;
        int index = qp[k];
        exch(index, N--);
        keys[k] = null;
        qp[k] = -1;
        sink(index);
        swim(index);
    }

    private void swim(int k) {
        while (k > 1 && less(k, k / 2)) {
            exch(k / 2, k);
            k = k/2;
        }
    }

    private void sink(int k) {
        while (2 * k <= N) {
            int j = 2*k;
            if (j < N && less(j+1, j)) j++;
            if (!less(j, k)) break;
            exch(k, j);
            k = j;
        }
    }

    private boolean less(int i, int j) {
        return keys[pq[i]].compareTo(keys[pq[j]]) < 0;
    }

    private void exch(int i, int j) {
        int temp = pq[i];
        pq[i] = pq[j];
        pq[j] = temp;
        qp[pq[i]] = i;
        qp[pq[j]] = j;
    }
}
