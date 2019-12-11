------**** Trigger 1 ****---------
-- function for adding msg recipient
create or replace function msg_rec_func()
    returns trigger as
$$
DECLARE
    userID_fun integer;
begin
    if new.toUserID > 0 and new.toUserID NOTNULL then -- if it is a private message
        insert into messageRecipient
        values (new.msgid, new.toUserID);


    elseif new.toGroupId > 0 and new.toGroupID NOTNULL then -- if it is a group message

    -- if the user belongs to the group he/she sent to.
        if (select x.userid
            from groupmember x
            where x.gid = new.togroupid
              and x.userid = new.fromid) notnull then
            -- insert every group members as a recipient into the message recipient table.
            for userID_fun in
                select g.userID
                from groupmember g
                where g.gid = new.togroupid
--                   and g.userid != new.fromid
                loop
                    insert into messagerecipient
                    values (new.msgid, userID_fun);
                end loop;
        else -- if the user does not belong to the group.
            raise exception 'The user does not belong to the group he/she sent to.';
        end if;
    end if;
    return new;
end;
$$
    LANGUAGE plpgsql;


-- Trigger to add message recipient table after insert into message table
drop trigger if exists msg_rec_trig on messageInfo;
create constraint trigger msg_rec_trig
    after insert
    on messageInfo deferrable
    for each row
execute procedure msg_rec_func();


------**** Trigger 2 ****---------
-- Function to delete group memberships after user drops
create or replace function delete_group_member_after_delete_profile_func()
    returns trigger as
$$
begin
    delete from groupmember where userid = OLD.userid;
    return old;
end;

$$
    language plpgsql;

-- Trigger to delete group memberships after user drops
drop trigger if exists delete_group_member_after_delte_profile_trig on profile;
create trigger delete_group_member_after_delete_profile_trig
    after delete
    on "profile"
    for each row
execute procedure delete_group_member_after_delete_profile_func();

------**** Trigger 3 ****---------

-- Trigger to delete a group from groupInfo after all member of this group were deleted
create or replace function delete_group_after_all_members_were_deleted_func()
    returns trigger as
$$
begin
    if (select count(gid) from groupmember where gid = old.gid) = 0 then
        delete from groupinfo where groupinfo.gid = old.gid;
    end if;
    return old;
end;
$$
    language plpgsql;

drop trigger if exists delete_group_after_all_members_were_deleted_trig on groupmember;
create trigger delete_group_after_all_members_were_deleted_trig
    after delete
    on groupmember
    for each row
execute procedure delete_group_after_all_members_were_deleted_func();

------**** Trigger 4 ****---------
--Function to potentially delete messageRecipient after message was deleted
create or replace function delete_messageRecipient_after_msgInfo_deleted_func()
    returns trigger as
$$
begin
    delete from messagerecipient where messagerecipient.msgid = old.msgid;
    return old;
end;
$$
    language plpgsql;

drop trigger if exists delete_messageRecipient_after_msgInfo_deleted_trig on messageinfo;
create trigger delete_messageRecipient_after_msgInfo_deleted_trig
    after delete
    on messageinfo
    for each row
execute procedure delete_messageRecipient_after_msgInfo_deleted_func();



-- ------**** Trigger 5 ****---------
-- -- Function to potentially delete messages after user drops.
-- create or replace function delete_msg_after_delete_user_func()
--     returns trigger as
-- $$
-- begin
--     delete from messageInfo where fromid = OLD.userid;
--     delete from messagerecipient where messagerecipient.userid = OLD.userid;
--     return old;
-- end;
-- $$
--     language plpgsql;
--
--
-- -- Trigger to potentially delete message from messageInfo after user drops.
-- drop trigger if exists delete_msg_after_delete_user_trig on profile;
-- create trigger delete_msg_after_delete_user_trig
--     after delete
--     on "profile"
--     for each row
-- execute procedure delete_msg_after_delete_user_func();


--delete from profile where

