delete from scheduled_activities where planned_activity_id = -1;
delete from scheduled_study_segments where study_segment_id= -1;
delete from scheduled_calendars where assignment_id= (select id from subject_assignments where study_site_id=-1);
delete from subject_assignments where study_site_id=-1;
delete from subjects where grid_id = '91dd4580-801b-4874-adeb-a174361bacea';
delete from amendment_approvals where id=-1;
delete from study_sites  where id=-1;
delete from planned_activities  where id=-1;
delete from periods  where id=-1;
delete from study_segments where id=-1;
delete from epochs  where id=-1;
delete from planned_calendars  where id=-1;
delete from studies  where id=-1;
delete from amendments  where id=-1;
delete from activities  where id=-1;
delete from user_role_sites  where site_id=-1;
delete from sites  where id=-1;
delete from sources where id=-1;
delete from notifications where grid_id='6115c43c-851e-425c-8312-fd78367aaef3';