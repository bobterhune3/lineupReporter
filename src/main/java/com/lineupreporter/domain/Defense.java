package com.lineupreporter.domain;

public class Defense {

    private long id;
    private String catcher;
    private String first;
    private String second;
    private String third;
    private String shortStop;
    private String left;
    private String center;
    private String right;
    private String ofArm;

    public Defense() {}

    public Defense(long id, String catcher, String first, String second, String third,
                   String shortStop, String left, String center, String right, String ofArm) {
        this.id = id;
        this.catcher = catcher;
        this.first = first;
        this.second = second;
        this.third = third;
        this.shortStop = shortStop;
        this.left = left;
        this.center = center;
        this.right = right;
        this.ofArm = ofArm;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getCatcher() { return catcher; }
    public void setCatcher(String catcher) { this.catcher = catcher; }
    public String getFirst() { return first; }
    public void setFirst(String first) { this.first = first; }
    public String getSecond() { return second; }
    public void setSecond(String second) { this.second = second; }
    public String getThird() { return third; }
    public void setThird(String third) { this.third = third; }
    public String getShortStop() { return shortStop; }
    public void setShortStop(String shortStop) { this.shortStop = shortStop; }
    public String getLeft() { return left; }
    public void setLeft(String left) { this.left = left; }
    public String getCenter() { return center; }
    public void setCenter(String center) { this.center = center; }
    public String getRight() { return right; }
    public void setRight(String right) { this.right = right; }
    public String getOfArm() { return ofArm; }
    public void setOfArm(String ofArm) { this.ofArm = ofArm; }
}
