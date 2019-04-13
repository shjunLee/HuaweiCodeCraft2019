package com.huawei.basement;

import com.huawei.dataStructure.Bag;

import java.util.TreeMap;

public class Cross implements Comparable<Cross>{
    private int id;
    private final int [] roads = new int[4];
    private TreeMap<Integer, Integer> roadIndex = new TreeMap<>();
    private Bag<Road> roadsOut = new Bag<>();

    public Cross(int id, int roadId_0, int roadId_1, int roadId_2, int roadId_3) {
        this.id = id;
        roads[0] = roadId_0;
        roads[1] = roadId_1;
        roads[2] = roadId_2;
        roads[3] = roadId_3;
        roadIndex.put(roadId_0, 0);
        roadIndex.put(roadId_1, 1);
        roadIndex.put(roadId_2, 2);
        roadIndex.put(roadId_3, 3);
    }

    public int[] roadsIds(){
        return roads;
    }

    public void addRoadOut(Road road){
        roadsOut.add(road);
    }

    public void setId(int id){
        this.id = id;
    }

    public Bag<Road> roadsOut() {
        return roadsOut;
    }

    public int getId() {
        return id;
    }

    public int direRoadId(int currentRoadid, int direction) {
        int index = indexOf(currentRoadid);
        switch (direction) {
            case 0://对立方向道路id
                if (index < 2)
                    return roads[index + 2];
                else
                    return roads[index - 2];
            case 1://左边道路id
                if (index < 3)
                    return roads[index + 1];
                else
                    return roads[0];
            case 2://右边道路ID
                if(index > 0)
                    return roads[index-1];
                else
                    return roads[3];
            default:
                throw new RuntimeException("Wrong direction!!");
        }
    }

    public int[] getRoads() {
        return roads;
    }

    public int direction(int id1, int id2) {
        int det = roadIndex.get(id1) - roadIndex.get(id2);
        if (det == 2 || det == -2) {//直行
            return 0;
        }
        if (det == -1 || det == 3) {//左转
            return 1;
        }
        if (det == 1 || det == -3) {//右转
            return 2;
        }
        System.out.println(det);
        throw new RuntimeException("Can not get Direction");
    }

    public int indexOf(int rodeId) {
        return roadIndex.get(rodeId);
    }

    public int compareTo(Cross c) {
        return this.id - c.getId();
    }
}
