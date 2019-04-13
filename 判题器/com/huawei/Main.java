package com.huawei;

import com.huawei.dataStructure.Queue;
import com.huawei.dataStructure.TwoTuple;
import com.huawei.dataStructure.Vector;
import com.huawei.basement.Car;
import com.huawei.basement.Cross;
import com.huawei.basement.Map;
import com.huawei.basement.Road;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

public class Main {
    private static int priorityCarNum = 0;//优先车辆总数
    private static int maxSpeed = 0;//所有车辆最高车速
    private static int minSpeed = Integer.MAX_VALUE;//所有车辆最低车速
    private static int priorityMaxSpeed = 0;//优先车辆最高车速
    private static int priorityMinSpeed = Integer.MAX_VALUE;//优先车辆最低车速
    private static int latestTime = 0;//所有车辆最晚出发时间
    private static int earliestTime = Integer.MAX_VALUE;//所有车辆最早出发时间
    private static int priorityLatestTime = 0;//优先车辆最晚出发时间
    private static int priorityEarliestTime = Integer.MAX_VALUE;//优先车辆最早出发时间
    private static int originAmount;//所有车辆出发地分布
    private static int priorityOriginAmount;//优先车辆出发地分布
    private static int destinationAmount;//所有车辆终止点分布
    private static int priorityDestinationAmount;//优先车辆终止点分布
    private static double a;
    private static double b;

    public static void main(String[] args){
        String crossPath = "/home/shjun/Documents/cross.txt";
        String roadPath = "/home/shjun/Documents/road.txt";
        String carPath = "/home/shjun/Documents/car.txt";
        String presetAnswerPath = "/home/shjun/Documents/presetAnswer.txt";
        String answerPath = "/home/shjun/Documents/answer.txt";
        FileInterface fileInterface = new FileInterface();
        TwoTuple<TreeMap<Integer, Cross>, HashMap<Integer, Integer>> twoTuple;
        twoTuple = fileInterface.crossReader(crossPath);
        TreeMap<Integer, Cross> crossST = twoTuple.getFirst();
        HashMap<Integer, Integer> getIndexFromCrossID = twoTuple.getSecond();
        TreeMap<Vector, Road> roadST = fileInterface.roadReader(roadPath, getIndexFromCrossID);
        TreeMap<Integer, Car> carST = fileInterface.carReader(carPath, getIndexFromCrossID);
        fileInterface.setPresetCarPath(presetAnswerPath, carST, roadST);
        fileInterface.setNormalCarPath(answerPath, carST, roadST);
        Queue<Car> arrivedCar = new Queue<>();
        Map map = mapBuilder(roadST, crossST);
        System.out.println("mapCapacity=" + map.capacity());
        calculate(carST, map, roadST);
        Scheduler scheduler = new Scheduler();
        scheduler.setMaxCarNum(map.capacity()+1);
        int time = 0;
        for (int cid :
                carST.keySet()) {
            Car tempcar = carST.get(cid);
            tempcar.nextRoad().addCarInInitTree(tempcar);
        }
        while (true) {

            /***********************/
            scheduler.travelRoads(roadST);//遍历道路
            scheduler.driveCarInTree(roadST, false);//释放优先车辆
            scheduler.createCarSequeue(roadST);//创建优先队列
            scheduler.travelCross(crossST, roadST);//遍历路口
            /***********************/
            scheduler.driveCarInTree(roadST, true);//释放所有可以上路车辆
            Car car = null;
            Iterator iter = carST.values().iterator();
            while (iter.hasNext()) {
                // 获取key
                car = (Car) iter.next();
                if (car.isArrived()) {
                    arrivedCar.enqueue(car);
                    iter.remove();
                    continue;
                }
                car.addTime();
                car.initialCarState();
            }
            if (carST.size() == 0)//调度完成
                break;
            time++;
        }
        int tSum = 0;
        int tSumPri = 0;
        int latestPrTime = 0;
        for (Car acar :
                arrivedCar) {
            tSum += (acar.getLocaltime() - acar.realPlanTime());
            if (acar.isPriority()){
                if (acar.getLocaltime() > latestPrTime)
                    latestPrTime = acar.getLocaltime();
                tSumPri += acar.getLocaltime() - acar.realPlanTime();
            }
        }
        int tPri = latestPrTime - priorityEarliestTime;
        System.out.println("time=" + Math.round(a*tPri + time));
        System.out.println("allTime=" + Math.round(b*tSumPri + tSum));
        System.out.println("realTime="+time);
        System.out.println("tSumPri="+tSumPri);
    }

    private static Map mapBuilder(TreeMap<Vector, Road> roadSt, TreeMap<Integer, Cross> crossST) {
        /***用邻接表构建地图***/
        Map map = new Map(crossST.size());
        Road r;
        Cross c;
        for (Vector i :
                roadSt.keySet()) {
            r = roadSt.get(i);
            c = crossST.get(r.from());
            c.addRoadOut(r);
            map.addRoad(r);
        }
        return map;
    }

    private static void calculate(TreeMap<Integer, Car> carST, Map map, TreeMap<Vector, Road> roadST){
        Car car;
        HashMap<Integer, Boolean> allCarOrigin = new HashMap<>(499);
        HashMap<Integer, Boolean> priorityCarOrigin = new HashMap<>(499);
        HashMap<Integer, Boolean> allCarDestination = new HashMap<>(499);
        HashMap<Integer, Boolean> priorityCarDestination = new HashMap<>(499);
        for (int carid :
                carST.keySet()) {//规划非预置车辆路线
            car = carST.get(carid);
            allCarOrigin.put(car.getOrigin(), true);
            allCarDestination.put(car.getDestination(), false);
            if (car.getMaxSpeed() > maxSpeed)
                maxSpeed = car.getMaxSpeed();
            if (car.getMaxSpeed() < minSpeed)
                minSpeed = car.getMaxSpeed();
            if (car.realPlanTime() < earliestTime)
                earliestTime = car.realPlanTime();
            if (car.realPlanTime() > latestTime)
                latestTime = car.realPlanTime();
            if (car.isPriority()) {
                priorityCarNum++;
                priorityCarOrigin.put(car.getOrigin(), true);
                priorityCarDestination.put(car.getDestination(), false);
                if (car.getMaxSpeed() > priorityMaxSpeed)
                    priorityMaxSpeed = car.getMaxSpeed();
                if (car.getMaxSpeed() < priorityMinSpeed)
                    priorityMinSpeed = car.getMaxSpeed();
                if (car.realPlanTime() < priorityEarliestTime)
                    priorityEarliestTime = car.realPlanTime();
                if (car.realPlanTime() > priorityLatestTime)
                    priorityLatestTime = car.realPlanTime();
            }
            originAmount = allCarOrigin.size();
            destinationAmount = allCarDestination.size();
            priorityOriginAmount = priorityCarOrigin.size();
            priorityDestinationAmount = priorityCarDestination.size();
        }
        a = 0.05 * divid(carST.size(), priorityCarNum) + 0.2375 * divid(divid(maxSpeed, minSpeed), divid(priorityMaxSpeed, priorityMinSpeed))
                + 0.2375 * divid(divid(latestTime, earliestTime), divid(priorityLatestTime, priorityEarliestTime))
                + 0.2375 * divid(originAmount, priorityOriginAmount) + 0.2375 * divid(destinationAmount, priorityDestinationAmount);
        b = 0.8 * divid(carST.size(), priorityCarNum) + 0.05 * divid(divid(maxSpeed, minSpeed), divid(priorityMaxSpeed, priorityMinSpeed))
                + 0.05 * divid(divid(latestTime, earliestTime), divid(priorityLatestTime, priorityEarliestTime))
                + 0.05 * divid(originAmount, priorityOriginAmount) + 0.05 * divid(destinationAmount, priorityDestinationAmount);
        System.out.println("a="+a);
        System.out.println("b="+b);
    }

    private static double divid(int a, int b){
        return Math.round(((double)a/b)*100000)/100000.0;
    }

    private static double divid(double a, double b){
        return Math.round((a/b)*100000)/100000.0;
    }
}