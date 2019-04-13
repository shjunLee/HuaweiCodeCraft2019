package com.huawei.basement;

import com.huawei.dataStructure.IndexMinPQ;
import com.huawei.dataStructure.Queue;
import com.huawei.dataStructure.Vector;

import java.util.Stack;
import java.util.TreeMap;

public class Car implements Comparable<Car>{
    private final int id;
    private final int origin;
    private final int destination;
    private final int maxSpeed;
    private final int planeTime;
    private final boolean isPriority;
    private final boolean isPreset;
    private int localtime = 0;//当前时间
    private int startTime = 0; //实际出发时间
    private boolean isRunning = false;//是否在行驶
    private boolean isArrived = false;//是否到达
    private boolean isWaiting = true;//是否是等待状态
    private Road road;//当前车所在的道路
    private int channel = 0; //在道路的哪条车道,从0开始
    private int position = 0;//车在车道的位置,终点为0
    private Queue<Integer> path = new Queue<>();//用队列储存车辆行驶路线经过的道路id
    private Stack<Road> planPath = new Stack<>();//规划的路径

    public Car(int id, int origin, int destination, int maxSpeed, int planeTime, boolean isPriority, boolean isPreset) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.maxSpeed = maxSpeed;
        this.planeTime = planeTime;
        this.isPriority = isPriority;
        this.isPreset = isPreset;
    }

    public boolean isPriority() { return isPriority; }

    public boolean isPreset(){ return isPreset; }

    public void initialCarState() {
        isWaiting = true;
    }

    public boolean isWaiting() {
        return isWaiting;
    }

    public int getId() {
        return this.id;
    }

    public int getDestination() {
        return this.destination;
    }

    public int getOrigin() {
        return this.origin;
    }

    public int getMaxSpeed() {
        return this.maxSpeed;
    }

    public int getplaneTime() {
        if (this.isPreset)//预置车辆返回实际出发时间
            return startTime;
        return planeTime;
    }

    public int realPlanTime(){
        return planeTime;
    }

    public Road currentRoda() {
        return road;
    }

    public int getChannel() {
        return channel;
    }

    public int getPosition() {
        return position;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime){ this.startTime = startTime; }

    public Stack<Road> planPath() {
        return this.planPath;
    }

    public Road nextRoad() {//车计划路线的下一条道路
        return planPath.peek();
    }

    public void pushPath(Road road){
        planPath.push(road);
    }

    public void setLocation(Road road, int channel, int position){
        if (localtime < startTime){
            throw new RuntimeException("Not start time yet!");
        }
        if (position < 0 || position > road.getLength()-1) {
            throw new RuntimeException("The car is not on this rode!");
        }
        if (channel > road.getChannelNum()-1 || channel < 0) {
            throw new RuntimeException("Wrong channel ID!");
        }
        if (this.road == road) {//没换rode行驶
            if (this.channel != channel) {
                throw new RuntimeException("Can not change channel!!!");
            }
            if (this.position < position || this.position - position > Math.min(maxSpeed, this.road.getMaxSpeed())) {
                throw new RuntimeException("Out of speed");
            }
        } else {//换rode行驶
            if (this.road != null) {
                if (this.road.to() != road.from()) {
                    throw new RuntimeException("Wrong Road");
                }
            }
            path.enqueue(road.getId());
            planPath.pop();//计划路线中划去当前所在道路
        }
        if (!this.isWaiting()){
            System.out.println("Get U");
            throw new RuntimeException(this.id+":已经是终止状态,不能走！！");
        }

        this.road = road;
        this.channel = channel;
        this.position = position;
        this.isRunning = true;
        this.isWaiting = false;
        /***这个地方错了！！！***/
        if (startTime == 0) {//记录实际出发时间
            startTime = localtime;
        }
    }

    public void setArrived() {//设置车辆到达终点状态
        isArrived = true;
        isWaiting = false;
        isRunning = false;
//        road = null;
    }

    public String outputPath() {
        //返回出车辆行驶路线
        String s;
        s = "\n(" + id + ",  " + startTime;
        for (int i :
                path) {
            s += (", " + i);
        }
        s += ")";
        return s;
    }

    public void addTime() {
        localtime++;
    }

    public int getLocaltime() {
        return localtime;
    }

    public boolean isRunning() {
        return  isRunning;
    }

    public boolean isArrived() {
        return isArrived;
    }

    private void relax(Map map, Road [] edgeTo, double[] distTo, IndexMinPQ<Double> pq) {
        int v = pq.delMin();
        for (Road e :
                map.adj(v)) {
            int w = e.to();
            if (distTo[w] > distTo[v] + e.getWeight()) {
                distTo[w] = distTo[v] + e.getWeight();
                edgeTo[w] = e;
                if (pq.contains(w))
                    pq.change(w, distTo[w]);
                else {
                    pq.insert(w, distTo[w]);
                }
            }
        }
    }

    public int compareTo(Car another) {
        /***对待上路车辆与非上路车辆优先级计算方法不同***/
        if ((!this.isRunning) && (!another.isRunning)) {//都没上路
            if (this.isPriority ^ another.isPriority) {//优先级不同
                if (this.isPriority)
                    return -1;//优先级高的返回值小，排前面
                else
                    return 1;
            }
            if (this.getStartTime() != another.getStartTime())//优先级相同,实际出发时间小的优先(主要影响预置车辆)
                return this.getStartTime() - another.getStartTime();
            return this.id - another.id;//ID小的排前面
        }
        if (this.isRunning && another.isRunning){//都上路了
            if (this.isPriority ^ another.isPriority) {//优先级不同
                if (this.isPriority)
                    return -1;//优先级高的返回值小，排前面
                else
                    return 1;
            }
            if (this.position != another.position)//优先级一致,距离路口近的优先级高
                return this.position - another.position;
            return this.channel - another.channel;//距路口距离也一致,车道号小的优先级高
        }
        throw new RuntimeException("不应该出现的比较!!!");
    }

    public String toString(){
        return "Car:" + this.id;
    }

    public boolean equals(Object obj){
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Car other = (Car) obj;
        return (this.id == other.id);
    }

    public int hashCode() {
        return this.id;
    }
}
