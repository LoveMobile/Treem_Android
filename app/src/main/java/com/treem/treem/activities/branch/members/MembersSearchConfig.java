package com.treem.treem.activities.branch.members;

/**
 * Members search config
 */
public class MembersSearchConfig {
    public MembersSearchConfig(){
        matching = new Matching();
        relationship = new Relationship();
        miscellaneous = new Miscellaneous();
    }

    public Miscellaneous getMiscellaneous() {
        return miscellaneous;
    }

    public class Matching{
        private boolean firstName = true;
        private boolean lastName = true;
        private boolean email = true;
        private boolean phone = true;
        private boolean userName = true;

        public boolean isFirstNameSet() {
            return firstName;
        }

        public void setFirstName(boolean firstName) {
            this.firstName = firstName;
        }

        public boolean isLastNameSet() {
            return lastName;
        }

        public void setLastName(boolean lastName) {
            this.lastName = lastName;
        }

        public boolean isEmailSet() {
            return email;
        }

        public void setEmail(boolean email) {
            this.email = email;
        }

        public boolean isPhoneSet() {
            return phone;
        }

        public void setPhone(boolean phone) {
            this.phone = phone;
        }

        public boolean isUserNameSet() {
            return userName;
        }

        public void setUserName(boolean userName) {
            this.userName = userName;
        }
        public boolean isUpdated(){
            return !firstName||!lastName||!email||!phone||!userName;
        }

        public void resetToDefaults() {
            firstName = true;
            lastName = true;
            email = true;
            phone = true;
            userName = true;
        }
    }
    private Matching matching;
    public Matching getMatching(){
        return matching;
    }
    public class Relationship{
        private boolean friends = true;
        private boolean pending = true;
        private boolean invited = true;
        private boolean notFriends = true;

        public boolean isFriendsSet() {
            return friends;
        }

        public void setFriends(boolean friends) {
            this.friends = friends;
        }

        public boolean isPendingSet() {
            return pending;
        }

        public void setPending(boolean pending) {
            this.pending = pending;
        }

        public boolean isInvitedSet() {
            return invited;
        }

        public void setInvited(boolean invited) {
            this.invited = invited;
        }

        public boolean isNotFriendsSet() {
            return notFriends;
        }

        public void setNotFriends(boolean notFriends) {
            this.notFriends = notFriends;
        }
        public boolean isUpdated(){
            return !friends||!pending||!invited||!notFriends;
        }

        public void resetToDefaults() {
            friends = true;
            pending = true;
            invited = true;
            notFriends = true;
        }
    }
    private Relationship relationship;
    public Relationship getRelationship(){
        return relationship;
    }
    public boolean isUpdated(){
        return relationship.isUpdated()||matching.isUpdated();
    }
    public class Miscellaneous{
        private boolean showContacts = true;
        private boolean allowUseContacts = false;

        public boolean isShowContactsSet() {
            return showContacts;
        }

        public void setShowContacts(boolean showContacts) {
            this.showContacts = allowUseContacts&&showContacts;
        }
        @SuppressWarnings("unused")
        public boolean isUpdated(){
            return allowUseContacts&&!showContacts;
        }

        public void resetToDefaults() {
            showContacts = allowUseContacts;
        }

        public void setAllowUseContacts(boolean allowUseContacts) {
            this.allowUseContacts = allowUseContacts;
        }
    }
    private Miscellaneous miscellaneous;

    public void resetToDefaults(){
        matching.resetToDefaults();
        relationship.resetToDefaults();
        miscellaneous.resetToDefaults();
    }
}
