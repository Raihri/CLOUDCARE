package com.example.doctors.model;

public class Doctor {
    // Using Long as ID type but binding from forms allows editing ID manually
    private Long id;

    // Basic
    private String fullName;
    private String pictureUrl;
    private String gender;
    private String dob; // date of birth as string for simplicity (yyyy-mm-dd)
    private String bloodGroup;

    // Contact
    private String email;
    private String phone;
    private String altPhone;
    private String address;
    private String division;
    private String zilla;
    private String street;
    private String postalCode;
    private String emergencyContact;

    // Professional
    private String specialization;
    private String education;
    private Integer experienceYears;
    private String certifications;
    private String languages;
    private String hospitalName;
    private String hospitalAddress;
    private String consultationFee;

    // Availability & Online
    private String workingDays;
    private String workingHours;
    private String onlineAppointmentLink;
    private String leaveDates;
    private Boolean telemedicineAvailable;

    // Online & Social
    private String website;
    private String linkedin;
    private String facebook;
    private String instagram;
    private String twitter;

    // Additional
    private String awards;
    private String publications;
    private String specialInterests;
    private String notes;
    private String rating;

    public Doctor() {}

    // getters and setters for all fields (generated)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPictureUrl() { return pictureUrl; }
    public void setPictureUrl(String pictureUrl) { this.pictureUrl = pictureUrl; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAltPhone() { return altPhone; }
    public void setAltPhone(String altPhone) { this.altPhone = altPhone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }

    public String getZilla() { return zilla; }
    public void setZilla(String zilla) { this.zilla = zilla; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }

    public String getCertifications() { return certifications; }
    public void setCertifications(String certifications) { this.certifications = certifications; }

    public String getLanguages() { return languages; }
    public void setLanguages(String languages) { this.languages = languages; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getHospitalAddress() { return hospitalAddress; }
    public void setHospitalAddress(String hospitalAddress) { this.hospitalAddress = hospitalAddress; }

    public String getConsultationFee() { return consultationFee; }
    public void setConsultationFee(String consultationFee) { this.consultationFee = consultationFee; }

    public String getWorkingDays() { return workingDays; }
    public void setWorkingDays(String workingDays) { this.workingDays = workingDays; }

    public String getWorkingHours() { return workingHours; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }

    public String getOnlineAppointmentLink() { return onlineAppointmentLink; }
    public void setOnlineAppointmentLink(String onlineAppointmentLink) { this.onlineAppointmentLink = onlineAppointmentLink; }

    public String getLeaveDates() { return leaveDates; }
    public void setLeaveDates(String leaveDates) { this.leaveDates = leaveDates; }

    public Boolean getTelemedicineAvailable() { return telemedicineAvailable; }
    public void setTelemedicineAvailable(Boolean telemedicineAvailable) { this.telemedicineAvailable = telemedicineAvailable; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) { this.linkedin = linkedin; }

    public String getFacebook() { return facebook; }
    public void setFacebook(String facebook) { this.facebook = facebook; }

    public String getInstagram() { return instagram; }
    public void setInstagram(String instagram) { this.instagram = instagram; }

    public String getTwitter() { return twitter; }
    public void setTwitter(String twitter) { this.twitter = twitter; }

    public String getAwards() { return awards; }
    public void setAwards(String awards) { this.awards = awards; }

    public String getPublications() { return publications; }
    public void setPublications(String publications) { this.publications = publications; }

    public String getSpecialInterests() { return specialInterests; }
    public void setSpecialInterests(String specialInterests) { this.specialInterests = specialInterests; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }
}
