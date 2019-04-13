package com.huawei;

import com.huawei.dataStructure.Queue;
import com.huawei.dataStructure.TwoTuple;
import com.huawei.dataStructure.Vector;
import com.huawei.basement.Car;
import com.huawei.basement.Cross;
import com.huawei.basement.Road;

import java.io.*;
import java.util.HashMap;
import java.util.TreeMap;

public class FileInterface {
    /*****  按行读取，将每行其中的数字字符串提取出来放入一个字符串数组,
     按行存入优先队列*****/
    private Queue<String[]> lines = new Queue<>();
    private Queue<String[]> readlines(String filePath){
        File file = new File(filePath);
        if(file.exists()){
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader br = new BufferedReader(fileReader);
                String lineContent = null;
                while((lineContent = br.readLine())!=null){
                    lineContent = lineContent.substring(1,lineContent.length()-1);
                    lineContent = lineContent.replaceAll(" ", "");
                    lines.enqueue(lineContent.split(","));
                }
                br.close();
                fileReader.close();
            } catch (FileNotFoundException e) {
                System.out.println("no this file");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("io exception");
                e.printStackTrace();
            }
        }
        return lines;
    }

    public TwoTuple<TreeMap<Integer, Cross>, HashMap<Integer, Integer>> crossReader(String filePath) {
        /***读取cross数据，存入HashST中，路口ID的索引为索引***/
        Queue<String[]> lines = readlines(filePath);
        lines.dequeue();//抛弃首行
        TreeMap<Integer, Cross> crossST_temp = new TreeMap<>();
        TreeMap<Integer, Cross> crossST = new TreeMap<>();
        String[] s;
        int id,roadId_0, roadId_1, roadId_2, roadId_3;
        Cross c;
        while (!lines.isEmpty()) {
            s = lines.dequeue();
            id =  Integer.parseInt(s[0]);
            roadId_0 =  Integer.parseInt(s[1]);
            roadId_1 =  Integer.parseInt(s[2]);
            roadId_2 =  Integer.parseInt(s[3]);
            roadId_3 =  Integer.parseInt(s[4]);
            c = new Cross(id, roadId_0, roadId_1, roadId_2, roadId_3);
            crossST_temp.put(id, c);
        }
        HashMap<Integer, Integer> getIndexFromCrossID = new HashMap<>(crossST_temp.size());
        int i  = 1;
        Cross cross;
        for (int cid :
                crossST_temp.keySet()) {
            cross = crossST_temp.get(cid);
            cross.setId(i);
            crossST.put(i, cross);
            getIndexFromCrossID.put(cid, i);
            i++;
        }
        return new TwoTuple<>(crossST, getIndexFromCrossID);
    }

    public TreeMap<Vector, Road> roadReader(String filePath, HashMap<Integer, Integer> getIndexFromCrossID) {
        /***读取road数据，存入HashST中,用Vector(ID, to)索引***/
        Queue<String[]> lines = readlines(filePath);
        lines.dequeue();//抛弃首行
        TreeMap<Vector, Road> roadST = new TreeMap<>();
        int rodeId, rodeLength, rodeSpeed, rodeChannel, rodeFrom, rodeTo;
        String[] s;
        boolean isDuplex;
        Road r;
        while (!lines.isEmpty()) {
            s = lines.dequeue();
            rodeId = Integer.parseInt(s[0]);
            rodeLength = Integer.parseInt(s[1]);
            rodeSpeed = Integer.parseInt(s[2]);
            rodeChannel = Integer.parseInt(s[3]);
            rodeFrom = getIndexFromCrossID.get(Integer.parseInt(s[4]));
            rodeTo = getIndexFromCrossID.get(Integer.parseInt(s[5]));
            isDuplex = (s[6].equals("1"));
            Vector v1 = new Vector(rodeId, rodeTo);
            r = new Road(rodeId, rodeLength, rodeSpeed, rodeChannel, rodeFrom, rodeTo);
            roadST.put(v1, r);
            if (isDuplex) {
                Vector v2 = new Vector(rodeId, rodeFrom);
                r = new Road(rodeId, rodeLength, rodeSpeed, rodeChannel, rodeTo, rodeFrom);
                roadST.put(v2, r);
            }
        }
        return roadST;
    }

    public TreeMap<Integer, Car> carReader(String filePath, HashMap<Integer, Integer> getIndexFromCrossID) {
        /***读取car数据，存入HashST中，ID为索引***/
        Queue<String[]> lines = readlines(filePath);
        lines.dequeue();//抛弃首行
        TreeMap<Integer, Car> carST = new TreeMap<>();
        int id, from, to, speed, startTime;
        boolean isPriority, isPreset;
        String[] s;
        Car c;
        while (!lines.isEmpty()) {
            s = lines.dequeue();
            id = Integer.parseInt(s[0]);
            from = getIndexFromCrossID.get(Integer.parseInt(s[1]));
            to = getIndexFromCrossID.get(Integer.parseInt(s[2]));
            speed = Integer.parseInt(s[3]);
            startTime = Integer.parseInt(s[4]);
            isPriority = (s[5].equals("1"));
            isPreset = (s[6].equals("1"));
            c = new Car(id, from, to, speed, startTime, isPriority, isPreset);
            carST.put(id, c);
        }
        return  carST;
    }

    public void setPresetCarPath(String filePath, TreeMap<Integer, Car> carST, TreeMap<Vector, Road> roadST) {
        Queue<String[]> lines = readlines(filePath);
        lines.dequeue();//抛弃首行
        String[] s;
        Car car;
        Road road;
        int to;
        Vector v = new Vector(0, 0);
        while (!lines.isEmpty()){
            s = lines.dequeue();
            car = carST.get(Integer.parseInt(s[0]));
            car.setStartTime(Integer.parseInt(s[1]));
            to = car.getDestination();
            for (int i = s.length - 1; i > 1 ; i--) {
                v.set(Integer.parseInt(s[i]), to);
                road = roadST.get(v);
                car.pushPath(road);
                to = road.from();
            }
        }
    }

    public void writeAnswer(Queue<Car> arrivedCar, String filePath) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
            out.write("#(carId, StartTime, RoadId...)");
            for (Car car :
                    arrivedCar) {
                if (!car.isPreset())//预置车辆不写入answer.txt
                    out.write(car.outputPath());
            }
            out.close();
            System.out.println("文件创建成功！");
        } catch (IOException e) {
            System.out.println("文件创建失败！！");
        }
    }

}
