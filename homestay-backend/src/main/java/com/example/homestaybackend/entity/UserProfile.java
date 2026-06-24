package com.example.homestaybackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor  // <--- 必须加这个！
@AllArgsConstructor
@Table(name = "user_profile")
public class UserProfile {
    @Id
    private Long userId;

    private String bio;
    private String profession;
    private String targetLocation;
    private String funFact;
    private String pets;
    private String bornYear;
    private String school;
    private String uselessSkill;
    private String wasteTime;
    private String favSong;
    private String languages;

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(String targetLocation) {
        this.targetLocation = targetLocation;
    }

    public String getFunFact() {
        return funFact;
    }

    public void setFunFact(String funFact) {
        this.funFact = funFact;
    }

    public String getPets() {
        return pets;
    }

    public void setPets(String pets) {
        this.pets = pets;
    }

    public String getBornYear() {
        return bornYear;
    }

    public void setBornYear(String bornYear) {
        this.bornYear = bornYear;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getUselessSkill() {
        return uselessSkill;
    }

    public void setUselessSkill(String uselessSkill) {
        this.uselessSkill = uselessSkill;
    }

    public String getWasteTime() {
        return wasteTime;
    }

    public void setWasteTime(String wasteTime) {
        this.wasteTime = wasteTime;
    }

    public String getFavSong() {
        return favSong;
    }

    public void setFavSong(String favSong) {
        this.favSong = favSong;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public String getSelectedPlaces() {
        return selectedPlaces;
    }

    public void setSelectedPlaces(String selectedPlaces) {
        this.selectedPlaces = selectedPlaces;
    }

    public Boolean getShowPlaces() {
        return showPlaces;
    }

    public void setShowPlaces(Boolean showPlaces) {
        this.showPlaces = showPlaces;
    }

    @Column(columnDefinition = "TEXT")
    private String selectedPlaces;

    private Boolean showPlaces = true;

    // --- 手动添加这个方法，消除 Controller 的报错 ---
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // 为了保险，顺便把 getId 也写了（如果后面用到）
    public Long getUserId() {
        return userId;
    }
}