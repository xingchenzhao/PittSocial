DROP TABLE IF EXISTS profile CASCADE;
DROP TABLE IF EXISTS friend CASCADE;
DROP TABLE IF EXISTS pendingFriend CASCADE;
DROP TABLE IF EXISTS messageInfo CASCADE;
DROP TABLE IF EXISTS messageRecipient CASCADE;
DROP TABLE IF EXISTS groupInfo CASCADE;
DROP TABLE IF EXISTS groupMember CASCADE;
DROP TABLE IF EXISTS pendingGroupMember CASCADE;

commit;
--profile (userID, name, email, password, date of birth, lastlogin)
--Stores the profile and login information for each user registered in the system.
create table profile
(
    userID integer,
    name varchar(50),
    email varchar(50),
    password varchar(50),
    date_of_birth date,
    lastlogin timestamp,
    Constraint profile_PK primary key (userID) not deferrable,
    Constraint email_unique unique (email) not deferrable
);

--friend (userID1, userID2, JDate, message)
--Stores the friends lists for every user in the system. The JDate is when they became friends,
--and the message is the message of friend request.
create table friend
(
    userID1 integer,
    userID2 integer,
    JDate date,
    message varchar(200),
    Constraint friend_PK primary key (userID1, userID2) not deferrable,
    Constraint friend_FK1 foreign key (userID1) references profile (userID) on delete cascade initially deferred deferrable,
    Constraint friend_FK2 foreign key (userID2) references profile (userID) on delete cascade initially deferred deferrable
);

--pendingFriend (fromID, toID, message)
--Stores pending friends requests that have yet to be confirmed by the recipient of the request.
create table pendingFriend
(
   fromID integer,
   toID integer,
   message varchar(200),
   CONSTRAINT pendingFriend_PK PRIMARY KEY (fromID, toID) NOT DEFERRABLE,
   CONSTRAINT pendingFriend_FK1 FOREIGN KEY (fromID) REFERENCES profile (userID) ON DELETE CASCADE INITIALLY DEFERRED DEFERRABLE,
   CONSTRAINT pendingFriend_FK2 FOREIGN KEY (toID) REFERENCES profile (userID) ON DELETE CASCADE INITIALLY DEFERRED DEFERRABLE
);

--group (gID, name, limit, description)
--Stores information for each group in the system.
create table groupInfo
(
    gID integer,
    name varchar(50),
    size integer,
    description varchar(200),
    Constraint groupInfo_PK primary key (gID) not deferrable
);

--message (msgID, fromID, message, toUserID, toGroupID, timeSent)
--Stores every message sent by users in the system. Note that the default values of ToUserID
--and ToGroupID should be NULL.
create table messageInfo
(
    msgID integer,
    fromID integer,
    message varchar(200),
    toUserID integer default null,
    toGroupID integer default null,
    timeSent timestamp,
    CONSTRAINT messageInfo_PK PRIMARY KEY (msgID) NOT DEFERRABLE,
    CONSTRAINT messageInfo_FK1 FOREIGN KEY (fromID) REFERENCES profile (userID) ON DELETE SET NULL INITIALLY DEFERRED DEFERRABLE,
    CONSTRAINT messageInfo_FK2 FOREIGN KEY (toUserID) REFERENCES profile (userID) ON DELETE SET NULL INITIALLY DEFERRED DEFERRABLE,
    CONSTRAINT messageInfo_FK3 FOREIGN KEY (toGroupID) REFERENCES groupInfo(gID)  ON DELETE SET NULL INITIALLY DEFERRED DEFERRABLE
);

--messageRecipient (msgID, userID)
--Stores the recipients of each message stored in the system.
create table messageRecipient
(
    msgID integer,
    userID integer,
    CONSTRAINT messageRecipient_PK PRIMARY KEY (msgID, userID) NOT DEFERRABLE,
    CONSTRAINT messageRecipient_FK1 FOREIGN KEY (msgID) REFERENCES messageInfo (msgID) INITIALLY DEFERRED DEFERRABLE
--     CONSTRAINT messageRecipient_FK2 FOREIGN KEY (userID) REFERENCES profile (userID) ON DELETE SET DEFAULT INITIALLY DEFERRED DEFERRABLE
-- Not Necessary to make foreign key constraint on userID here, since we made a trigger to insert into messageRecipient table after we insert msg into
-- messageInfo table, and the userID's reference was already checked in the messageInfo.
);

--groupMember (gID, userID, role)
--Stores the users who are members of each group in the system. The ’role’ indicate whether a
--user is a manager of a group (who can accept joining group request) or a member.
create table groupMember
(
    gID integer,
    userID integer,
    role varchar(20),
    CONSTRAINT groupMember_PK PRIMARY KEY (gID, userID) NOT DEFERRABLE,
    CONSTRAINT groupMember_FK1 FOREIGN KEY (gID) REFERENCES groupInfo (gID) ON DELETE CASCADE INITIALLY DEFERRED DEFERRABLE,
    CONSTRAINT groupMember_FK2 FOREIGN KEY (userID) REFERENCES profile (userID) ON DELETE CASCADE INITIALLY DEFERRED DEFERRABLE
);
--pendingGroupMember (gID, userID, message)
--Stores pending joining group requests that have yet to be accept/reject by the manager of the
--group.
create table pendingGroupMember
(
     gID integer,
    userID integer,
    message varchar(200),
    CONSTRAINT pendingGroupMember_PK PRIMARY KEY (gID, userID) NOT DEFERRABLE,
    CONSTRAINT pendingGroupMember_FK1 FOREIGN KEY (gID) REFERENCES groupInfo (gID) ON DELETE CASCADE INITIALLY DEFERRED DEFERRABLE,
    CONSTRAINT pendingGroupMember_FK2 FOREIGN KEY (userID) REFERENCES profile (userID) ON DELETE CASCADE INITIALLY DEFERRED DEFERRABLE
);