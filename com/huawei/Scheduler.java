package com.huawei;

import com.huawei.dataStructure.IndexMinPQ;
import com.huawei.dataStructure.Queue;
import com.huawei.dataStructure.Vector;
import com.huawei.basement.Car;
import com.huawei.basement.Cross;
import com.huawei.basement.Map;
import com.huawei.basement.Road;

import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeMap;

public class Scheduler {
    private int runningCarNum = 0;//路上跑的的车数
    private int presetCarNumInTree = 0;//本轮调度待上路的预置车辆数
    private boolean isLocked = false;//为true只上路预置车辆
    private int maxCarNum;//最大上路车数

    public void setMaxCarNum(int maxCarNum){
        this.maxCarNum = maxCarNum;
    }

    public void travelRoads(TreeMap<Vector, Road> roadST) {
        Road road;
        presetCarNumInTree = 0;//归零

        for (Vector v :
                roadST.keySet()) {
            road = roadST.get(v);
            presetCarNumInTree += road.persetCarNumInTree();
            if (road.carNum() == 0)
                continue;
            for (int j = 0; j < road.getChannelNum(); j++) {
                handleChannel(road, j);
            }
        }
        if ( presetCarNumInTree + runningCarNum <= maxCarNum )//车数目少于上限,解锁
            isLocked = false;
    }

    private void handleChannel(Road road, int channel) {
        if (road.carNum() == 0)
            return;
        Car car;
        int position = -1;
        int speed;
        for (int p = 0; p < road.getLength(); p++) {
            car = road.getCar(channel, p);
            if (car != null) {//该位置有车
                if (car.isWaiting()) {//该车是等待状态
                    if (position == -1) {//前面无车
                        speed = Math.min(car.getMaxSpeed(), road.getMaxSpeed());
                        if (speed > p) {//速度大于到路口距离
                            position = p;//即使能到达终点,也要当成等待状态
                        } else {//速度小于等于到路口距离
                            /***车走，路更新***/
                            car.setLocation(road, channel, p - speed);//车走到新位置
                            road.removeCar(channel, p);//原位置空出
                            position = p - speed;
                            road.addCar(car, channel, position);//新位置
                        }
                    } else {//前面有车
                        speed = Math.min(car.getMaxSpeed(), road.getMaxSpeed());
                        if (road.getCar(channel, position).isWaiting()) {//前车是等待状态
                            if (speed >= p-position) {//与前车距离小于等于车速
                                position = p;
                            } else {//与前车距离大于车速
                                /***车走，路更新***/
                                road.removeCar(channel, p);//原位置空出
                                car.setLocation(road, channel, p-speed);//车走到新位置
                                road.addCar(car, channel, p-speed);//新位置
                                position = p - speed;
                            }
                        } else {//前车是终止状态
                            /***车走,路更新***/
                            if(speed >= p-position) {//与前车距离小于等于车速
                                car.setLocation(road, channel, position + 1);//车走到新位置
                                road.removeCar(channel, p);//原位置空出
                                position++;
                                road.addCar(car, channel, position);//新位置
                            }else {//与前车距离大于车速
                                road.removeCar(channel, p);//原位置空出
                                car.setLocation(road, channel, p-speed);//车走到新位置
                                position = p - speed;
                                road.addCar(car, channel, position);//新位置
                            }
                        }
                    }
                } else {//该车是终止状态
                    position = p;
                }
            }
        }
    }

    public void createCarSequeue(TreeMap<Vector, Road> roadST) {
        for (Vector v :
                roadST.keySet()) {
            createCarSequeue(roadST.get(v));
        }
    }

    private void createCarSequeue(Road road){
        if (road.primerCar() != null)
            throw new RuntimeException("Something Wrong");
        Car car;
        for (int i = 0; i < road.getChannelNum(); i++) {
            for (int j = 0; j < road.getMaxSpeed(); j++) {
                car = road.getCar(i, j);
                if (car == null)
                    continue;
                if (!car.isWaiting())
                    break;
                if (Math.min(car.getMaxSpeed(), road.getMaxSpeed()) <= j)
                    break;
                road.insertCarIntoSequeue(car);
                break;
            }
        }
    }

    public void travelCross(TreeMap<Integer, Cross> crossST, TreeMap<Vector, Road> roadST){
        /**可能堵死**/
        Cross cross;
        Queue<Cross> crosses = new Queue<>();
        for (int crossId :
                crossST.keySet()) {
            crosses.enqueue(crossST.get(crossId));
        }
        int size = crosses.size();
        int loop = 0;
        while (!crosses.isEmpty()){
            cross = crosses.dequeue();
            if(!handleCross(cross, roadST)) {//没处理干净，暂时保存
                crosses.enqueue(cross);
            }
            if(crosses.isEmpty())
                break;
            if (cross.getId() >= crosses.head().getId()){//一次遍历结束
                if(crosses.size() == size){//该次遍历结束路口不减少
                    loop++;
                    if (loop > 9)//一般循环3次未clear的路口不减少,可认为堵死了
                    /****重新规划,但只能右转***/
                        throw new RuntimeException("Too Many Cars!!!");
                }
                size = crosses.size();//保存该次遍历结束时的size
            }
        }
    }

    private boolean handleCross(Cross cross, TreeMap<Vector, Road> roadST) {
        int[] roads = new int[4];
        for (int i = 0; i < 4; i++) {
            roads[i] = cross.getRoads()[i];
        }
        Arrays.sort(roads);
        Vector v = new Vector(0, 0);
        int i = 0;
        boolean isClear = true;
        while (i < 4) {
            v.set(roads[i], cross.getId());
            isClear &= handleRoad(cross, roadST.get(v), roadST);
            i++;
        }
        return isClear;
    }

    private boolean handleRoad(Cross cross, Road road, TreeMap<Vector, Road> roadST){
        if (road == null)
            return true;
        if (road.carSequeueIsEmpty())
            return true;
        Car acar;
        while(!road.carSequeueIsEmpty()) {
            acar = road.primerCar();//最优先车辆
            int channel = acar.getChannel();
            if(!driveCarInSequeue(cross, acar, roadST)) {
                return false;//runcar失败，下一条路
            }
            handleChannel(road, channel);
            refreshSequeue(channel, road);//更新Sequeue
            /***单道路优先车辆发车***/
            driveCarInTree(road, false);//只上优先车辆
        }
        return true;
    }

    private void refreshSequeue(int channel, Road road) {
        road.deleteCarInSequeue(channel);//从优先队列删除走了的车
        Car acar;
        for (int i = 0; i < road.getMaxSpeed(); i++) {
            acar = road.getCar(channel, i);
            if (acar == null)
                continue;
            if (!acar.isWaiting())
                break;
            road.insertCarIntoSequeue(acar);//新车加入队列
            break;
        }
    }

    private boolean changeRoad(Car car) {//返回false，说明还在等待状态
        if(car.getDestination() == car.currentRoda().to()){//可以到达终点
            /***车到达终点并不是possion=0!!!***/
            Road road = car.currentRoda();
            road.removeCar(car.getChannel(), car.getPosition());//从道路删除该车
            handleChannel(road, car.getChannel());//处理该车道
            car.setArrived();//该车设置成终点状态
            this.runningCarNum--;//running车辆减1
            return true;
        }
        int speed = Math.min(car.getMaxSpeed(), car.nextRoad().getMaxSpeed());
        int p = speed - car.getPosition();//下一道路最大行驶距离
        /***即使不能出路口,也要参与路口优先级排序***/
        if (p <= 0) {//走到路口
            car.currentRoda().removeCar(car.getChannel(), car.getPosition());
            car.setLocation(car.currentRoda(), car.getChannel(), 0);
            car.currentRoda().addCar(car, car.getChannel(), 0);
            car.setBlocked();
        } else {
            Road nextRoad = car.nextRoad();
            Car tailCar;
            for (int i = 0; i < nextRoad.getChannelNum(); i++) {
                tailCar = nextRoad.tailCar(i);
                if (tailCar == null) {//该车道可以进入
                    car.currentRoda().removeCar(car.getChannel(), car.getPosition());
                    car.setLocation(nextRoad, i, nextRoad.getLength()-p);
                    nextRoad.addCar(car, i, nextRoad.getLength()-p);
                    return true;
                }
                if ((!tailCar.isWaiting()) && (tailCar.getPosition() == tailCar.currentRoda().getLength() - 1)) {//该车道没有位置进入
                    continue;
                } else {//该车道可以进入
                    if (p < nextRoad.getLength() - tailCar.getPosition()) {//距离大于p
                        car.currentRoda().removeCar(car.getChannel(), car.getPosition());
                        car.setLocation(nextRoad, i, nextRoad.getLength()-p);
                        nextRoad.addCar(car, i, nextRoad.getLength()-p);
                    } else if (!tailCar.isWaiting()) {//前车终止状态
                        car.currentRoda().removeCar(car.getChannel(), car.getPosition());
                        car.setLocation(nextRoad, i, tailCar.getPosition() + 1);
                        nextRoad.addCar(car, i, tailCar.getPosition() + 1);
                    } else {//等待状态
                        return false;
                    }
                    return true;
                }
            }
            //没有车道可以进入，走到路尽头
            car.currentRoda().removeCar(car.getChannel(), car.getPosition());
            car.setLocation(car.currentRoda(), car.getChannel(), 0);
            car.currentRoda().addCar(car, car.getChannel(), 0);
            car.setBlocked();
        }
        return true;
    }

    private boolean driveCarInSequeue(Cross c, Car car, TreeMap<Vector, Road> roadST) {
        int currentRoadID = car.currentRoda().getId();
        Queue<Road> roadQueue = new Queue<>();
        Vector v = new Vector(0, 0);
        for (int roadId :
                c.roadsIds()) {
            if (roadId == -1)//没有这个路
                continue;
            if (roadId == currentRoadID)
                continue;
            if (car.planPath().size() != 0 && roadId == car.nextRoad().getId())
                continue;
            v.set(roadId, c.getId());
            if(roadST.get(v) == null)//单向路
                continue;
            roadQueue.enqueue(roadST.get(v));
        }
        Car anotherCar;
        while (!roadQueue.isEmpty()) {
            anotherCar = roadQueue.dequeue().primerCar();
            if (anotherCar == null)
                continue;
            if (conflict(c, car, anotherCar))
                return false;
        }
        return changeRoad(car);
    }
    private boolean conflict(Cross c, Car car, Car anotherCar){
        if (car.currentRoda().to() != anotherCar.currentRoda().to())
            throw new RuntimeException("Not to the same cross!!");
        if (car.currentRoda().to() != c.getId())
            throw new RuntimeException("Wrong car and cross!");
        int nextRoadId, anotherNextRoadId;
        if (car.getDestination() == c.getId()) {//要到终点
            nextRoadId = c.direRoadId(car.currentRoda().getId(), 0);//算直行
        }else{
            nextRoadId = car.nextRoad().getId();
        }
        if (anotherCar.getDestination() == c.getId())
            anotherNextRoadId = c.direRoadId(anotherCar.currentRoda().getId(), 0);
        else
            anotherNextRoadId = anotherCar.nextRoad().getId();
        if (nextRoadId != anotherNextRoadId)//不去同一道路
            return false;
        if (car.isPriority() ^ anotherCar.isPriority()){//优先级不同
            return anotherCar.isPriority();
        }else {//优先级相同
            int carDire = c.direction(car.currentRoda().getId(), nextRoadId);
            int anotherCarDire = c.direction(anotherCar.currentRoda().getId(), anotherNextRoadId);
            return (anotherCarDire < carDire);
        }
    }

    public void driveCarInTree(TreeMap<Vector, Road> roadST, boolean tf){//tf为false只上路优先车辆,否则都上
        //优先对剩余位置多的道路上路
        IndexMinPQ<Road> roadList = new IndexMinPQ<>(roadST.size());
        Road road;
        int i = 0;
        for (Vector v :
                roadST.keySet()) {
            road = roadST.get(v);
            roadList.insert(i, road);
            i++;
        }
        while (!roadList.isEmpty()){
            road = roadList.min();
            roadList.delMin();
            driveCarInTree(road, tf);
        }
    }

    private void driveCarInTree(Road road, boolean tf) {
        if (road == null)
            return;
        if (road.getInitTree().size() == 0)
            return;
        Car car = null;
        Iterator iter = road.getInitTree().keySet().iterator();
        while (iter.hasNext()) {
            // 获取key
            car = (Car) iter.next();
            if (isLocked){//为true只上路预置车辆
                if (!car.isPreset())
                    continue;
            }
            if (car.getLocaltime() < car.getplaneTime())//没到上路时间
                continue;
            if (tf || car.isPriority()) {
                if (releaseCar(car)) {//释放成功，失败了也不影响其他车辆上路
                    iter.remove();
                    this.runningCarNum++;//上路车辆自加1
                    if (car.isPreset()) {
                        presetCarNumInTree--;
                    }else{
                        if ( presetCarNumInTree + runningCarNum > maxCarNum )//车辆达到上限,加锁
                            isLocked = true;
                    }

                }
            } else {//tf为true不会执行到这里
                break;//一旦遇到非优先车辆，后面都是非优先车辆，不用遍历了
            }
        }
    }

    private boolean releaseCar(Car car) {
        Road nextRoad = car.nextRoad();
        Car tailCar;
        int speed = Math.min(car.getMaxSpeed(), nextRoad.getMaxSpeed());
        for (int i = 0; i < nextRoad.getChannelNum(); i++) {
            tailCar = nextRoad.getCar(i, nextRoad.getLength()-1);
            if ((tailCar != null) && (!tailCar.isWaiting())) {
                continue;
            }
            tailCar = nextRoad.tailCar(i);
            if (tailCar == null) {
                car.setLocation(nextRoad, i, nextRoad.getLength()-speed);
                nextRoad.addCar(car, i, nextRoad.getLength()-speed);
                return true;
            } else {
                int p = tailCar.getPosition();
                if(speed < nextRoad.getLength() - p){
                    car.setLocation(nextRoad, i, nextRoad.getLength()-speed);
                    nextRoad.addCar(car, i, nextRoad.getLength()-speed);
                    return true;
                }else{
                    if (tailCar.isWaiting()) {
//                        System.out.println("应该不会出现");
                        return false;
                    } else {
                        car.setLocation(nextRoad, i, p+1);
                        nextRoad.addCar(car, i, p+1);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void relaxRoad(TreeMap<Vector, Road> roadST, Map map, double congestionRation){
        /***动态规划拥堵率大于congestionRation的道路的车的路径***/
        Road r;
        Car car;
        for (Vector v :
                roadST.keySet()) {
            r = roadST.get(v);
            if (r.congestionRation() > congestionRation) {
                for (int i = 0; i < r.getChannelNum(); i++) {
                    for (int j = 0; j < r.getMaxSpeed(); j++) {
                        car = r.getCar(i, j);
                        if (car != null){
                            if (car.isPreset())//预置车辆不能规划
                                continue;
                            if (car.isBlocked())//上次路口调度过的的车不规划
                                continue;
                            if (car.currentRoda().to() == car.getDestination())
                                continue;
                            if (car.isPriority() && car.nextRoad().to() == car.getDestination())//其他车辆为非优先车辆让路
                                continue;
                            if (car.getMaxSpeed() > car.getPosition()) {//只规划车速大于到路口距离的车
                                car.roadPlanning(map, r.to(), roadST);
                            }
                        }
                    }
                }
            }
        }
    }
}