package com.lineupreporter.domain;

import org.thymeleaf.util.StringUtils;

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
    public void setShortStop(String shortStop) { this.shortStop = shortStop;}


    /**
     * RF to CF, add 1 to the range rating.
     * LF to CF, add 2 to the range rating.
     * LF to RF, add 1 to the range rating.
     */
    public String getLeft() {
        if(StringUtils.isEmpty(left) && !StringUtils.isEmpty(right)) {
            return right+"$";
        }
        else if(StringUtils.isEmpty(left) && !StringUtils.isEmpty(center)) {
            return center+"$";
        }
        return left;
    }
    public void setLeft(String left) { this.left = left; }


    public String getCenter() {
        if( StringUtils.isEmpty(center) && !StringUtils.isEmpty(left)) {
            char c = left.charAt(0);
            return (Character.isDigit(c) ? (c - '0' + 2) : c) + left.substring(1)+"$";
        }
        else if(StringUtils.isEmpty(center) && !StringUtils.isEmpty(right) ) {
            char c = right.charAt(0);
            return (Character.isDigit(c) ? (c - '0' + 1) : c) + right.substring(1)+"$";
        }
        return center;
    }
    public void setCenter(String center) { this.center = center; }

    public String getRight() {
        if( StringUtils.isEmpty(right) && !StringUtils.isEmpty(center)) {
            char c = center.charAt(0);
            return (Character.isDigit(c) ? (c - '0' + 1) : c) + center.substring(1)+"$";
        }
        else if( StringUtils.isEmpty(right) && !StringUtils.isEmpty(left)) {
            char c = left.charAt(0);
            return (Character.isDigit(c) ? (c - '0' + 1) : c) + left.substring(1)+"$";
        }
        return right;
    }
    public void setRight(String right) { this.right = right; }
    public String getOfArm() { return ofArm; }
    public void setOfArm(String ofArm) { this.ofArm = ofArm; }
}
