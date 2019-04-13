package com.huawei.dataStructure;

public class Vector implements Comparable<Vector>{
    private int x;
    private int y;

    public Vector(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }
    public int getY() {
        return this.y;
    }
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
    public boolean equals(Object obj){
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Vector other = (Vector) obj;
        if(this.x != other.x)
            return false;
        if (this.y != other.y)
            return false;
        return true;
    }

    public int hashCode() {
        int result = 31;
        result = result*43 + x*17;
        result = result*43 + y;
        return result;
    }

    public int compareTo(Vector v){
        if(this.x - v.getX() == 0)
            return this.y - v.y;
        return this.x - v.getX();
    }
}