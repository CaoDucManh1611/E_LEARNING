package com.example.doan.Model.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentRequest {

    private int hoursStudied;
    private int attendance;
    private int previousScores;
    private int sleepHours;
    private int tutoringSessions;
    private int physicalActivity;
    private int parentalInvolvement;
    private int accessToResources;
    private int extracurricularActivities;
    private int motivationLevel;
    private int internetAccess;
    private int familyIncome;
    private int peerInfluence;
    private int learningDisabilities;
    private int parentalEducationLevel;
    private int distanceFromHome;
    private int gender;

    private List<String> inputSkills;
    private int topN = 5;

    public List<Integer> toEncodedVector() {
        return List.of(
                hoursStudied, attendance, previousScores, sleepHours,
                tutoringSessions, physicalActivity,
                parentalInvolvement, accessToResources, extracurricularActivities,
                motivationLevel, internetAccess, familyIncome,
                peerInfluence, learningDisabilities, parentalEducationLevel,
                distanceFromHome, gender
        );
    }

    public int getHoursStudied() { return hoursStudied; }
    public void setHoursStudied(int v) { this.hoursStudied = v; }
    public int getAttendance() { return attendance; }
    public void setAttendance(int v) { this.attendance = v; }
    public int getPreviousScores() { return previousScores; }
    public void setPreviousScores(int v) { this.previousScores = v; }
    public int getSleepHours() { return sleepHours; }
    public void setSleepHours(int v) { this.sleepHours = v; }
    public int getTutoringSessions() { return tutoringSessions; }
    public void setTutoringSessions(int v) { this.tutoringSessions = v; }
    public int getPhysicalActivity() { return physicalActivity; }
    public void setPhysicalActivity(int v) { this.physicalActivity = v; }
    public int getParentalInvolvement() { return parentalInvolvement; }
    public void setParentalInvolvement(int v) { this.parentalInvolvement = v; }
    public int getAccessToResources() { return accessToResources; }
    public void setAccessToResources(int v) { this.accessToResources = v; }
    public int getExtracurricularActivities() { return extracurricularActivities; }
    public void setExtracurricularActivities(int v) { this.extracurricularActivities = v; }
    public int getMotivationLevel() { return motivationLevel; }
    public void setMotivationLevel(int v) { this.motivationLevel = v; }
    public int getInternetAccess() { return internetAccess; }
    public void setInternetAccess(int v) { this.internetAccess = v; }
    public int getFamilyIncome() { return familyIncome; }
    public void setFamilyIncome(int v) { this.familyIncome = v; }
    public int getPeerInfluence() { return peerInfluence; }
    public void setPeerInfluence(int v) { this.peerInfluence = v; }
    public int getLearningDisabilities() { return learningDisabilities; }
    public void setLearningDisabilities(int v) { this.learningDisabilities = v; }
    public int getParentalEducationLevel() { return parentalEducationLevel; }
    public void setParentalEducationLevel(int v) { this.parentalEducationLevel = v; }
    public int getDistanceFromHome() { return distanceFromHome; }
    public void setDistanceFromHome(int v) { this.distanceFromHome = v; }
    public int getGender() { return gender; }
    public void setGender(int v) { this.gender = v; }
    public List<String> getInputSkills() { return inputSkills; }
    public void setInputSkills(List<String> v) { this.inputSkills = v; }
    public int getTopN() { return topN; }
    public void setTopN(int v) { this.topN = v; }
}
