package com.huawei.basement;

import com.huawei.dataStructure.Bag;

public class Map {
    private final int V;//顶点总数
    private int E;//边的总数
    private Bag<Road>[] adj;//领接表,从v指出的道
    private int capacity = 0;//地图最大容纳的车辆数

    public Map(int V) {
        this.V = V;
        this.E = 0;
        adj = (Bag<Road>[]) new Bag[V+1];
        for (int v = 0; v < V+1; v++) {
            adj[v] = new Bag<Road>();
        }
    }

    public int V() {
        return V;
    }

    public int E() {
        return E;
    }

    public void addRoad(Road r) {
        adj[r.from()].add(r);
        E++;
        capacity += r.getChannelNum() * r.getLength();
    }

    public int capacity() {
        return capacity;
    }

    public Iterable<Road> adj(int v) {
        return adj[v];
    }

    public Iterable<Road> roads() {
        Bag<Road> bag = new Bag<Road>();
        for (int v = 1; v < V+1; v++) {
            for (Road r :
                    adj[v]) {
                bag.add(r);
            }
        }
        return bag;
    }
}
