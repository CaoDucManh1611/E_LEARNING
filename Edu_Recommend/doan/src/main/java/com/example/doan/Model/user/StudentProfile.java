package com.example.doan.Model.user;
import com.example.doan.Model.user.User;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "student_profile")
public class StudentProfile {

    // user_id đồng thời là PK và FK trỏ tới users(id)
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private int hoursStudied;
    private int attendance;
    private int previousScores;
    private int sleepHours;
    private int tutoringSessions;
    private int extracurricularActivities;
    private int learningDisabilities;
    private int familyIncome;
    private int parentalInvolvement;
    private int internetAccess;
    private String socialMediaUsage;
    private int distanceFromHome;
    private int accessToResources;
    private int parentalEducationLevel;
    private int physicalActivity;
    private int motivationLevel;
    private int peerInfluence;
    private int gender;
    private String groupLabel;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public StudentProfile() {}

    // Getters & Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public int getHoursStudied() { return hoursStudied; }
    public void setHoursStudied(int hoursStudied) { this.hoursStudied = hoursStudied; }

    public int getAttendance() { return attendance; }
    public void setAttendance(int attendance) { this.attendance = attendance; }

    public int getPreviousScores() { return previousScores; }
    public void setPreviousScores(int previousScores) { this.previousScores = previousScores; }

    public int getSleepHours() { return sleepHours; }
    public void setSleepHours(int sleepHours) { this.sleepHours = sleepHours; }

    public int getTutoringSessions() { return tutoringSessions; }
    public void setTutoringSessions(int tutoringSessions) { this.tutoringSessions = tutoringSessions; }

    public int getExtracurricularActivities() { return extracurricularActivities; }
    public void setExtracurricularActivities(int extracurricularActivities) { this.extracurricularActivities = extracurricularActivities; }

    public int getLearningDisabilities() { return learningDisabilities; }
    public void setLearningDisabilities(int learningDisabilities) { this.learningDisabilities = learningDisabilities; }

    public int getFamilyIncome() { return familyIncome; }
    public void setFamilyIncome(int familyIncome) { this.familyIncome = familyIncome; }

    public int getParentalInvolvement() { return parentalInvolvement; }
    public void setParentalInvolvement(int parentalInvolvement) { this.parentalInvolvement = parentalInvolvement; }

    public int getInternetAccess() { return internetAccess; }
    public void setInternetAccess(int internetAccess) { this.internetAccess = internetAccess; }

    public String getSocialMediaUsage() { return socialMediaUsage; }
    public void setSocialMediaUsage(String socialMediaUsage) { this.socialMediaUsage = socialMediaUsage; }

    public int getDistanceFromHome() { return distanceFromHome; }
    public void setDistanceFromHome(int distanceFromHome) { this.distanceFromHome = distanceFromHome; }

    public int getAccessToResources() { return accessToResources; }
    public void setAccessToResources(int accessToResources) { this.accessToResources = accessToResources; }

    public int getParentalEducationLevel() { return parentalEducationLevel; }
    public void setParentalEducationLevel(int parentalEducationLevel) { this.parentalEducationLevel = parentalEducationLevel; }

    public int getPhysicalActivity() { return physicalActivity; }
    public void setPhysicalActivity(int physicalActivity) { this.physicalActivity = physicalActivity; }

    public int getMotivationLevel() { return motivationLevel; }
    public void setMotivationLevel(int motivationLevel) { this.motivationLevel = motivationLevel; }

    public int getPeerInfluence() { return peerInfluence; }
    public void setPeerInfluence(int peerInfluence) { this.peerInfluence = peerInfluence; }

    public int getGender() { return gender; }
    public void setGender(int gender) { this.gender = gender; }

    public String getGroupLabel() { return groupLabel; }
    public void setGroupLabel(String groupLabel) { this.groupLabel = groupLabel; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
