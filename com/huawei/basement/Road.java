package com.huawei.basement;

import com.huawei.dataStructure.IndexMinPQ;
import com.huawei.dataStructure.Vector;

import java.util.Iterator;
import java.util.TreeMap;

public class Road implements Comparable<Road> {
    private final int id;
    private final int length;
    private final int maxSpeed;
    private final int channelNum;
    private final int from;
    private final int to;
    private Car[][] cars;
    private double weight;
    private int N = 0;//道路上车辆数
    private TreeMap<Car, Boolean> initTree = new TreeMap<>();
    private IndexMinPQ<Car> carSequeue;
    private int persetCarNumInTree = 0;

    public Road(int id, int length, int maxSpeed, int channelNum, int begin, int end) {
        this.id = id;
        this.length = length;
        this.maxSpeed = maxSpeed;
        this.channelNum = channelNum;
        this.from = begin;
        this.to = end;
        this.cars = new Car[channelNum][length];
        carSequeue = new IndexMinPQ<>(channelNum);
        weight = Math.log((double) length/channelNum);
//        weight = Math.sqrt((double) 20/channelNum);
    }
    public void addCarInInitTree(Car car){
        if (car.isPreset())
            persetCarNumInTree++;
        initTree.put(car, car.isPriority());
    }

    public void clearInitTree() {
        persetCarNumInTree = 0;
        initTree.clear();
    }

    public int persetCarNumInTree(){
        return persetCarNumInTree;
    }

    public void removeCarFromTree(Car car) {
        initTree.remove(car);
    }

    public int initTreeSize() { return initTree.size(); }

    public TreeMap<Car, Boolean> getInitTree() {
        return initTree;
    }

    public void insertCarIntoSequeue(Car car){
        if (car != null){
            carSequeue.insert(car.getChannel(), car);
        }
    }

    public Car primerCar() {
        if (carSequeue.isEmpty())
            return null;
        return carSequeue.min();
    }

    public void deleteCarInSequeue(int channel){
        carSequeue.delete(channel);
    }

    public boolean carSequeueIsEmpty(){
        return carSequeue.isEmpty();
    }

    public void initializeWeight(){
        if (length * channelNum - N < 5){
            weight = 100;
        }else {
            weight = Math.log((double) (N +1) * 40 / (channelNum));
        }
    }
    public int carNum(){
        return N;
    }

    public double congestionRation(){
        return (double)N/(length*channelNum);
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getId() {
        return id;
    }

    public int getLength() {
        return length;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public int getChannelNum() {
        return channelNum;
    }

    public int from() {
        return from;
    }

    public int to() {
        return to;
    }

    public Road brothRoad(TreeMap<Vector, Road> roadST){
        Vector v = new Vector(id, from);
        return roadST.get(v);
    }

    public void addCar(Car car, int channel, int position) {
        if (position > length-1) {
            throw new RuntimeException("rodeID:"+ id +"channel:"+channel +"没有空余位置放车！！");
        }
        if (cars[channel][position] != null) {
            throw new RuntimeException("rodeID:"+ id +",channel:"+channel +",position:"+position+".位置已被占用！！");
        }
        cars[channel][position] = car;
        N++;
    }

    public void removeCar(int channel, int position){
        if (cars[channel][position] != null) {
            cars[channel][position] = null;
            N--;
        }else{
            System.out.println("车不存在");
        }
    }

    public Car getCar(int channel, int position) {
        return cars[channel][position];
    }

    public int compareTo(Road road){//剩余位置多的优先
        if((this.length*this.channelNum - this.N) > (road.length*road.channelNum - road.N))
            return -1;
        else if ((this.length*this.channelNum - this.N) < (road.length*road.channelNum - road.N))
            return 1;
        else
            return this.id - road.id;
    }

    public Car tailCar(int channel) {
        int i;
        for (i = this.length-1; i >=0 ; i--) {
            if (getCar(channel, i) != null) {
                break;
            }
        }
        if (i == -1)
            return null;
        return getCar(channel, i);
    }
}